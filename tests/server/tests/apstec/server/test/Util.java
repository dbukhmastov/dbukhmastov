package apstec.server.test;

import java.nio.ByteBuffer;

public interface Util {
	 default StringBuilder toString(ByteBuffer bb) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bb.position(); i++) {
            sb.append(((int) bb.get(i)) & 0xff);
            sb.append(", ");
        }
        return sb;
    }
	 
	 default StringBuilder toString(ByteBuffer bb, int offset, int length) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = offset; i < length; i++) {
	        	if (i >= bb.position()) {
	        		break;
	        	}
	            sb.append(((int) bb.get(i)) & 0xff);
	            sb.append(", ");
	        }
	        return sb;
	    }
	 
	default String toRealString(ByteBuffer bb) {
		byte[] result = new byte[bb.position()];
		for (int i = 0; i < bb.position(); i++) {
			result[i] = bb.get(i);
		}
		return new String(result);
	}
}