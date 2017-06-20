package apstec.integrator.test;

import java.nio.ByteBuffer;

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
	public ByteBuffer content = null;
	
	Package(ByteBuffer bytes){
		m_data_type = Byte.toUnsignedInt(bytes.get(0));
		m_code_message = Byte.toUnsignedInt(bytes.get(1));
		m_counter = toLong(bytes, 2);
		m_size = toLong(bytes, 6);
	}
	
	private long toLong(ByteBuffer bytes, int index) {
		return Byte.toUnsignedLong(bytes.get(index)) + (Byte.toUnsignedLong(bytes.get(index+1)) << 8) 
				+ (Byte.toUnsignedLong(bytes.get(index+2)) << 16) + (Byte.toUnsignedLong(bytes.get(index+3)) << 24); 
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
			sb.append(", content{ ");
			sb.append(content==null ? "null" : toRealString(content));
			sb.append(" }");
		}
		return sb.toString();
	}
}
