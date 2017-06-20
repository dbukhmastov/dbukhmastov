package apstec.server.test.integrator;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

import apstec.server.test.BaseTest;
import apstec.server.test.Util;

public class AlarmPart implements Util {
	private static final Charset ch =  Charset.forName("UTF-8");
	
	public SpaceBox[] sBoxes;
	public String time;
	public byte[] photo;
	private volatile int hashcode = -1;
	
	AlarmPart(){
	}
	
	public boolean content(ByteBuffer bytes, int index, BaseTest logger) {
		/*
		unsigned int alarmLevel; 4 bytes
	    unsigned int alarmType; 4 bytes
	    unsigned int points; 96 bytes (8 point -> 3 axes -> float = 4bytes)
	    unsigned int textLength; 4 bytes 
	    */
		
		if (bytes.position() < 108 + index) {
			logger.log("Absent first SpaceBox", toString(bytes, index, 108 + index));
			return false;
		}
		SpaceBox firstSpaceBox = parseSpaceBox(bytes, index, logger);
		if (firstSpaceBox == null){
			logger.log("Can't parse first SpaceBox", toString(bytes, index, 108 + index));
			return false;
		}
		StringBuilder hashString = new StringBuilder(String.valueOf(firstSpaceBox.hashCode()));
		index += firstSpaceBox.length();
		if (bytes.position() <= index) {
			logger.log("Wrong package, after first box", toString(bytes, 0, index+4));
			return false;
		}
		int iSBCount = (int) toLong(bytes, index);
		index += 4;
		sBoxes = new SpaceBox[iSBCount + 1];
		sBoxes[0] = firstSpaceBox;
		for (int i = 0; i < iSBCount; i++) {
			if (bytes.position() < 108 + index) {
				logger.log("position=", bytes.position(), ", index=", index);
				logger.log("Wrong package, next box", toString(bytes, 0, index));
				return false;
			}
			sBoxes[i+1] = parseSpaceBox(bytes, index, logger);
			if (sBoxes[i+1] == null){
				logger.log("Can't parse first SpaceBox", toString(bytes, index, 108 + index));
				return false;
			}
			index += sBoxes[i+1].length();
			hashString.append(String.valueOf(sBoxes[i+1].hashCode()));
		}
		if (bytes.position() < 4 + index) {
			logger.log("position=", bytes.position(), ", index=", index);
			logger.log("Wrong package, time length", toString(bytes, 0, index));
			return false;
		}
		int iTimeLength = (int) toLong(bytes, index);
		index += 4;
		if (iTimeLength > 0 && bytes.position() >= iTimeLength + index) {
			byte[] tmp = getByteArray(bytes, index,  iTimeLength);
			index += tmp.length;
			time = new String(tmp, ch);
			// time should not present in the hashcode
			//hashString.append(time);
		} else {
			logger.log("position=", bytes.position(), ", index=", index, ", iTimeLength=", iTimeLength);
			logger.log("Wrong package, time", toString(bytes, 0, index));
			return false;
		}
		//photo
		int iPhotoIndex =  index;
		if (bytes.position() >= index + 4) {
			int iPhotoLength = (int) toLong(bytes, index);
			index += 4;
			if (iPhotoLength > 0) {
				if (bytes.position() < index + iPhotoLength) {
					logger.log("position=", bytes.position(), ", index=", index, ", iPhotoLength=", iPhotoLength);
					logger.log("Photo till (length + 4 bytes) ", toString(bytes, iPhotoIndex, index + 4));
					return false;
				}
				byte[] tmp = getByteArray(bytes, index, iPhotoLength);
				index += tmp.length;
				photo = tmp;
				CRC32 crc = new CRC32();
				crc.update(photo);
				hashString.append(crc.getValue());
			}
		} else {
			logger.log("absent photo length. position=", bytes.position(), ", index=", index);
			logger.log("Package (skip first spacebox) ", toString(bytes, 108, index + 4));
			return false;
		}
		hashcode = hashString.hashCode();
		return true;
	}
	
	private SpaceBox parseSpaceBox(ByteBuffer bytes, int index, BaseTest logger) {
		int alarmLevel = (int) toLong(bytes, index);
		index += 4;
		int alarmType = (int) toLong(bytes, index);
		index += 4;
		float[][] points = new float[8][3];
		for (float[] next : points) {
			next[0] = toFloat(bytes, index);
			index += 4;
			next[1] = toFloat(bytes, index);
			index += 4;
			next[2] = toFloat(bytes, index);
			index += 4;
		}
		byte[] text = null;
		int length = (int) toLong(bytes, index);
		index += 4;
		if (length > 0) {
			if (bytes.position() < index + length) {
				logger.log("position=", bytes.position(), ", index=", index, ", length=", length);
				logger.log("Wrong length of textInfo", toString(bytes, 0, index));
				return null;
			}
			text = getByteArray(bytes, index, length);
		}
		return new SpaceBox(alarmLevel, alarmType, points, text);
	}
	
	private byte[] getByteArray(ByteBuffer bytes, int index, int length) {
		byte[] arr = new byte[length];
		for (int i = 0; i < length; i++) {
			arr[i] = bytes.get(index+i);
		}
		return arr;
	}
	
	private long toLong(ByteBuffer bytes, int index) {
		return Byte.toUnsignedLong(bytes.get(index)) + (Byte.toUnsignedLong(bytes.get(index+1)) << 8) 
				+ (Byte.toUnsignedLong(bytes.get(index+2)) << 16) + (Byte.toUnsignedLong(bytes.get(index+3)) << 24); 
	}
	
	private float toFloat(ByteBuffer bytes, int index) {
		return Float.intBitsToFloat((int) toLong(bytes, index)); 
	}
	
	public int hashCode() {
		return hashcode;
	}
	
	public int length() {
		int length = 12;
		for (SpaceBox next : sBoxes) {
			length += next.length();
		}
		return length + (time == null ? 0 : time.length()) + (photo == null ? 0 : photo.length);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("AlarmPart time=");
		sb.append(time);
		for (SpaceBox next : sBoxes) {
			sb.append("\n");
			sb.append(next.toString());
		}
		if (photo != null) {
			sb.append(", photo=");
			sb.append(photo.length);
		}
		return sb.toString();
	}
	
	public String toFullString() {
		StringBuilder sb = new StringBuilder("AlarmPart time=");
		sb.append(time);
		for (SpaceBox next : sBoxes) {
			sb.append("\n");
			sb.append(next.toFullString());			
		}
		sb.append(", photo=");
		if (photo == null) {
			sb.append("null");
		} else {
			sb.append(photo.length);
		}
		return sb.toString();
	}
}
