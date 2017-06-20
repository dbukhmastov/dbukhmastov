package apstec.integrator.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import com.sun.javatest.Status;
import com.sun.javatest.Test;

public abstract class BaseTest implements Test {
	// Properties
	public static final String IP = "ip";
	public static final String PORT = "port";
	public static final String TIMEOUT = "timeout";
	public static final String SOCKET_TIMEOUT = "socket_timeout";
	public static final String STRESS_TEST_TIME = "stress_test_time";
	
	private HashMap<String, Object> props = new HashMap<String, Object>();
	private PrintWriter out, err;

	public static void main(String[] args, Test test) {
		try {
			Status s = test.run(args, new PrintWriter(System.out, true), new PrintWriter(System.err, true));
			s.exit();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public final Status run(String[] args, PrintWriter out, PrintWriter err) {
		this.out = out == null ? new PrintWriter(System.out, true) : out;
		this.err = err == null ? new PrintWriter(System.err, true) : err;
		props.clear();
		Status result = new Status(Status.NOT_RUN, "Not run");
		try {
			InputStream is = BaseTest.class.getClassLoader().getResourceAsStream("properties");
			if (is == null) {
				String currentDir = System.getProperty("currentDir", ".");
				File prop = new File(currentDir, "properties");
				if (!prop.isFile()) {
					prop = new File(new File(currentDir, "resource"), "properties");
				}
				if (prop.isFile()) {
					is = new FileInputStream(prop);
				}
			}
			if (is != null) {
				//Server starts listening on 56789, 56790,56791 ports for clients
				Properties props = new Properties();
				props.load(is);
				String tmp;
				this.props.put(IP, (tmp = props.getProperty(IP)) == null ? "127.0.0.1" : tmp);
				this.props.put(PORT, Integer.parseInt((tmp = props.getProperty(PORT)) == null ? "2800" : tmp));
				this.props.put(TIMEOUT, Integer.parseInt((tmp = props.getProperty(TIMEOUT)) == null ? "120000" : tmp));
				this.props.put(SOCKET_TIMEOUT,
						Integer.parseInt((tmp = props.getProperty(SOCKET_TIMEOUT)) == null ? "60000" : tmp));
				this.props.put(STRESS_TEST_TIME,
						Long.parseLong((tmp = props.getProperty(STRESS_TEST_TIME)) == null ? "300000" : tmp));
			} else {
				props.put(IP, "127.0.0.1");
				props.put(PORT, 45678);
				props.put(TIMEOUT, 120000);
				props.put(SOCKET_TIMEOUT, 60000);
				props.put(STRESS_TEST_TIME, (long) 300000);
			}
			for (Entry<String, Object> next : props.entrySet()){
				log(next.getKey(), "=", next.getValue().toString());
			}
			if (!init() || !test()) {
				result = Status.failed("FAILED");
			} else {
				result = Status.passed("PASSED");
			}
		} catch (Throwable e) {
			error(e);
			result = Status.failed("Can't initialize the test");
		} finally {
			if (!finish() && result.isPassed()) {
				result = Status.failed("Can't finalize the test");
			}
		}
		return result;
	}

	protected abstract boolean init() throws IOException;

	protected abstract boolean test() throws IOException;

	protected abstract boolean finish();

	protected Object getProp(String key) {
		return props.get(key);
	}

	protected String toString(byte[] buf) {
		return toStringBuilder(buf, ", ").toString();
	}

	protected StringBuilder toStringBuilder(byte[] buf, String separator) {
		StringBuilder result = new StringBuilder();
		for (byte next : buf) {
			if (result.length() > 0) {
				result.append(separator);
			}
			result.append(Byte.toUnsignedInt(next));
		}
		return result;
	}

	protected void log(String... msg) {
		print(null, out, msg);
	}
	
	protected void error(Throwable e) {
		print(e, err, "Unexpected Throwable");
	}

	protected void error(Throwable e, String... err) {
		print(e, this.err, err);
	}
	
	protected void error(String... err) {
		print(null, this.err, err);
	}
	
	protected void mySleep(long l) {
		try {
			Thread.sleep(l);
		} catch (Throwable e) {}
	}

	private void print(Throwable e, PrintWriter ps, String... strs) {
		if (e != null) {
			e.printStackTrace(ps);
			ps.println();
		}
		for (String next : strs) {
			ps.print(next);
			ps.print(" ");
		}
		ps.println();
	}
}
