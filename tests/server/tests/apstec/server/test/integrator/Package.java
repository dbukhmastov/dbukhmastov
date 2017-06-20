package apstec.server.test.integrator;

import java.nio.ByteBuffer;

import apstec.server.test.BaseTest;
import apstec.server.test.Util;

public class Package implements Util {
	/*
	unsigned char m_data_type; 1 byte
    unsigned char m_code_message; 1 byte
    unsigned int  m_counter; 4 bytes
    unsigned int  m_size; 4 bytes 
    */
	
	public int m_data_type;
	public int m_code_message;
	public long m_counter;
	public long m_size;
	public AlarmPart[] parts;
	private ByteBuffer content;
	private BaseTest logger;
	private volatile int hashcode = -1;
	
	public Package(ByteBuffer bytes, BaseTest logger){
		this.logger = logger;
		m_data_type = Byte.toUnsignedInt(bytes.get(0));
		m_code_message = Byte.toUnsignedInt(bytes.get(1));
		m_counter = toLong(bytes, 2);
		m_size = toLong(bytes, 6);
		hashcode = String.valueOf(m_data_type + m_code_message).hashCode();
	}
	
	public boolean content(ByteBuffer bytes) {
		StringBuilder hashString = new StringBuilder(String.valueOf(m_data_type));
		hashString.append(m_code_message);
		if (m_data_type == 4) {
			int index = 0;
			parts = new AlarmPart[15];
			int iCounter = 0;
			while (bytes.position() > index) {
				AlarmPart next = new AlarmPart();
				if (next.content(bytes, index, logger)) {
					index += next.length();
					if (parts.length <= iCounter) {
						AlarmPart[] tmp = new AlarmPart[parts.length + 15];
						System.arraycopy(parts, 0, tmp, 0, parts.length);
						parts = tmp;
					}
					parts[iCounter] = next;
					hashString.append(parts[iCounter++].hashCode());
				} else {
					logger.log("Can't parse AlarmPart = ", iCounter);
					return false;
				}
			}
			if (bytes.position() != index) {
				logger.log("Alarm has been parsed incorrectly, position=", bytes.position(), ", index=", index);
				return false;
			}
			if (parts.length != iCounter) {
				AlarmPart[] tmp = new AlarmPart[iCounter];
				System.arraycopy(parts, 0, tmp, 0, iCounter);
				parts = tmp;
			}
		} else {
			this.content = bytes;
			hashString.append(toRealString(bytes));
		}
		hashcode = hashString.hashCode();
		return true;
	}

	public ByteBuffer content() {
		return content;
	}
	
	private long toLong(ByteBuffer bytes, int index) {
		return Byte.toUnsignedLong(bytes.get(index)) + (Byte.toUnsignedLong(bytes.get(index+1)) << 8) 
				+ (Byte.toUnsignedLong(bytes.get(index+2)) << 16) + (Byte.toUnsignedLong(bytes.get(index+3)) << 24); 
	}
	
	public int hashCode() {
		return hashcode;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Package type=");
		sb.append(m_data_type);
		sb.append(", code=");
		sb.append(m_code_message);
		sb.append(", counter=");
		sb.append(m_counter);
		sb.append(", size=");
		sb.append(m_size);
		if (m_data_type == 4 && parts != null) {
			sb.append(", AlarmParts {");
			for (AlarmPart next : parts) {
				sb.append("\n");
				sb.append(next.toString());
			}
			sb.append("\n}");
		}
		return sb.toString();
	}
	
	public String toFullString() {
		StringBuilder sb = new StringBuilder("Package type=");
		sb.append(m_data_type);
		sb.append(", code=");
		sb.append(m_code_message);
		sb.append(", counter=");
		sb.append(m_counter);
		sb.append(", size=");
		sb.append(m_size);
		if (m_size > 0) {
			if (m_data_type == 4 && parts != null) {
				sb.append(", AlarmParts { \n");
				for (AlarmPart next : parts) {
					sb.append(next.toFullString());
					sb.append("\n");
				}
			} else {
				sb.append(" }");
				sb.append(", content{ ");
				sb.append(content==null ? "null" : toRealString(content));
				sb.append(" }");
			}
		}
		return sb.toString();
	}
}
