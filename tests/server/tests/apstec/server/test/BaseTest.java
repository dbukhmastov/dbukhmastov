package apstec.server.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.sun.javatest.Status;
import com.sun.javatest.Test;

import apstec.server.test.Util;

public abstract class BaseTest implements Util, Test {
	// Properties
	protected static final String BUNDLE_NUMBER = "bundle_number";
	protected static final String CMD_PORT = "cmd_port";
	protected static final String CONFIGURATION_DIR = "configuration_dir";
	protected static final String DATA_PORT = "data_port";
	protected static final String INTEGRATOR_PORT = "integrator_port";
	protected static final String RECEIVE_BUFFER = "receive_buffer";
	protected static final String SEND_BUFFER = "send_buffer";
	protected static final String SOCKET_TIMEOUT = "socket_timeout";
	protected static final String STRESS_TEST_TIME = "stress_test_time";
	protected static final String TEST_TIME = "test_time";
	protected static final String TIMEOUT = "timeout";

    private static final String OK = "OK";
    private static final String FAIL = "FAIL";
    private static final String RESOURCE = "/apstec/testinfo";
    
    private PrintWriter out, err;
    
    protected String configurationDir, serverBin;
    protected long timeout, stressTestTime, socketTimeout, testTime;
    protected int bundleNumber = -1;
    protected volatile boolean bStopFlag = true, isFailed = false;

    public static void main(Test test, String[] args) {
        PrintWriter err = new PrintWriter(System.err, true);
        PrintWriter out = new PrintWriter(System.out, true);
        Status s = test.run(args, out, err);
        s.exit();
    }

    public void log(Object... strs) {
        print(out, strs);
    }

    public void error(Object... strs) {
        print(err, strs);
    }

    private final void print(PrintWriter ps, Object... strs) {
        for (Object next : strs) {
        	if (next instanceof String[]) {
        		for (String next2 : (String[])next) {
        			ps.print(next2);
        			ps.print(" ");
        		}
        	} else {
        		ps.print(next.toString());
        	}
        }
        ps.println();
    }

    public final Status run(String[] args, PrintWriter out, PrintWriter err) {
        try {
            if (init(RESOURCE, out, err) && runTest()) {
                return Status.passed(OK);
            }
        } catch (Throwable th) {
            error("Unexpected Throwable: " + th.getClass().getName() + ":" + th.getMessage());
            th.printStackTrace();
        } finally {
            cleanup();
        }
        return Status.failed(FAIL);
    }

    protected void cleanup() {
    	bStopFlag = false;
    	try {
			Thread.sleep(10);
		} catch (Throwable e) {}
    }
    
    protected boolean startProcess(Map<String,String> env, boolean cleanLog, String... command) {
    	if (command.length > 0) {
    		log("Process: ", command);
    		ProcessBuilder pb = new ProcessBuilder(command);
    		pb.directory(new File("/home/mmwoperator/jenkins/build/server/bin"));
    		if (env != null) {
    			pb.environment().putAll(env);
    		}
    		try {
    			final Process ps = pb.start();
    			if (ps != null && ps.isAlive()) {
    				if (cleanLog) {
    					new Thread() {
        					public void run() {
        						try (InputStream err = ps.getErrorStream(); InputStream out = ps.getInputStream()) {
        							while (bStopFlag && !isFailed) {
        								try {
        									if (err.available() > 0) {
            									err.read(new byte[err.available()]);
            								}
            								if (out.available() > 0) {
            									out.read(new byte[out.available()]);
            								}
        								} catch (Throwable ee) {
                						}
        								try {
        									Thread.sleep(5);
        								} catch(Throwable e) {}
        							}
        						} catch (Throwable e) {
        							e.printStackTrace();
        						}
        						
        					}
        				}.start();
    				}
    				return true;
    			}
    			return false;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return false;
    }

    private final boolean init(String resource, PrintWriter out, PrintWriter err) throws IOException {
        this.out = out;
        this.err = err;
        Properties props = new Properties();
        try (InputStream is = resource.getClass().getResourceAsStream(resource)) {
            props.load(is);
            for (Entry<Object, Object> next : props.entrySet()) {
                log(next.getKey(), "=", next.getValue());
            }
        } catch (Throwable e) {
        }
        String tmp = "";
        if ((tmp = props.getProperty(BUNDLE_NUMBER)) == null) {
        	log("\"BUNDLE_NUMBER\" have to be defined.");
        	return false;
        }
        bundleNumber = Integer.parseInt(tmp.trim());
        serverBin = "server_"+bundleNumber;
        timeout = Integer.parseInt((tmp = props.getProperty(TIMEOUT)) == null ? "120000" : tmp.trim());
        stressTestTime = Integer.parseInt((tmp = props.getProperty(STRESS_TEST_TIME)) == null ? "120000" : tmp.trim());
        socketTimeout = Integer.parseInt((tmp = props.getProperty(SOCKET_TIMEOUT)) == null ? "60000" : tmp.trim());
        testTime = Integer.parseInt((tmp = props.getProperty(TEST_TIME)) == null ? "150000" : tmp.trim());
        configurationDir = props.getProperty(CONFIGURATION_DIR);
        if (configurationDir != null) {
        	configurationDir = configurationDir.trim();
        }
        return init(props);
    }

    protected abstract boolean init(Properties props) throws IOException;
    
    protected abstract boolean runTest() throws Throwable;
}
