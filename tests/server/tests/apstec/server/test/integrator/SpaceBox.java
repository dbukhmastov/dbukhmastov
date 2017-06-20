package apstec.server.test.integrator;

import java.nio.charset.Charset;

public class SpaceBox{
	private static final Charset ch =  Charset.forName("UTF-8");
	/*
	unsigned int alarmLevel; 4 bytes
    unsigned int alarmType; 4 bytes
    unsigned int points; 96 bytes (8 point -> 3 axes -> float = 4bytes)
    unsigned int textLength; 4 bytes 
    */
	
	public int alarmLevel;
	public int alarmType;
	public float[][] points = new float[8][3];
	//public byte[] text;
	private String str;
	private volatile int hashcode = -1;
	
	public SpaceBox(int alarmLevel, int alarmType, float[][] points, byte[] text){
		this.alarmLevel = alarmLevel;
		this.alarmType = alarmType;
		this.points = points;
		//this.text = text;
		if (text != null) {
			str = new String(text, ch);
		}
		hashcode = toFullString().hashCode();
	}
	
	public int length() {
		return 108 + (str == null ? 0 : str.length());
	}
	
	public int hashCode() {
		return hashcode;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("SpaceBox alarmLevel=");
		sb.append(alarmLevel);
		sb.append(", alarmType=");
		sb.append(alarmType);
		if (str != null) {
			sb.append(", text=");
			sb.append(str);
		}
		return sb.toString();	}
	
	public String toFullString() {
		StringBuilder sb = new StringBuilder("SpaceBox alarmLevel=");
		sb.append(alarmLevel);
		sb.append(", alarmType=");
		sb.append(alarmType);
		for (float[] next : points) {
			sb.append(", x=");
			sb.append(next[0]);
			sb.append(", y=");
			sb.append(next[1]);
			sb.append(", z=");
			sb.append(next[2]);
		}
		if (str != null) {
			sb.append(", text=");
			sb.append(str);
		}
		return sb.toString();
	}
}
