package apstec.server.test.integrator.functional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import apstec.server.test.BaseTest;
import apstec.server.test.integrator.AlarmListener;
import apstec.server.test.integrator.GetConfiguration;
import apstec.server.test.integrator.IntegratorEmulator;
import apstec.server.test.integrator.Package;

/**
 * @test
 * @id RunKillTest
 * @executeClass apstec.server.test.integrator.functional.RunKillTest
 * @source RunKillTest.java
 * @title Test many stop-start server  
 */
public class RunKillTest extends BaseTest implements GetConfiguration, AlarmListener {
	private static final String DEFAULT_INTEGRATOR_PORT = "45678";//45678 OR 2800
	private static final String ALARM_HASHCODE_FILE = "/apstec/server/test/integrator/functional/RunKillTest.data";
    
    private IntegratorEmulator integrator;
    private String cfg;
    private int[] hashCodes = new int[1800];
	private int hashCounter = 0;
	private volatile boolean result = true;
    
    public static void main(String[] args) {
        main(new RunKillTest(), args);
    }
    
    protected boolean init(Properties props) throws IOException {
    	if (configurationDir == null) {
    		log ("\"CONFIGURATION_DIR\" have to be defined.");
    		return false;
    	}
    	InputStream is = ALARM_HASHCODE_FILE.getClass().getResourceAsStream(ALARM_HASHCODE_FILE);
		byte[] buf = new byte[10];//10 max count of digits
		int index = 0;
		int tmp = -1;
		while ((tmp = is.read()) != -1) {
			if (tmp == 0X0A) {
				if (hashCodes.length <= hashCounter) {
					int[] tmpArr = new int[hashCodes.length + 1000];
					System.arraycopy(hashCodes, 0, tmpArr, 0, hashCodes.length);
					hashCodes = tmpArr;
				}
				hashCodes[hashCounter++] = Integer.parseInt(new String(buf, 0, index));
				index = 0;
			} else {
				try {
				buf[index++] = (byte) (tmp & 0xff);
				} catch (Throwable e) {
					error("ERR index=", (index-1), ", hashCounter=", (hashCounter-1));
					
				}
			}
		}
		if (hashCodes.length != hashCounter) {
			int[] tmpArr = new int[hashCounter];
			System.arraycopy(hashCodes, 0, tmpArr, 0, hashCounter);
			hashCodes = tmpArr;
		}
		hashCounter = 0;
    	cfg = configurationDir + "/settings/config.xml";
        String tmpStr = "";
        integrator = new IntegratorEmulator(Integer.parseInt((tmpStr = props.getProperty(INTEGRATOR_PORT)) == null ? DEFAULT_INTEGRATOR_PORT : tmpStr.trim()), timeout, this);
        integrator.start();
        return true;
    }
    
    public boolean reconnect() throws Throwable {
    	log("reconnect --------- ", new Date());
    	if (startProcess(null, false, "killall", serverBin)) {
        	try {
				Thread.sleep(10);
			} catch(Throwable e) {}
        	if (!startProcess(null, false, "rm", "-rf", configurationDir+"/log/*")){
        		log("Can clear log");
        	}
        	integrator.addAlarmListener(this);
        	//startProcess(null, "./"+serverBin, "-s", "-b", configurationDir)
        	if (startProcess(null, true, "./"+serverBin, "-b", configurationDir)) {
        		if (integrator.reconnect()) {
        			if (!integrator.waitForNotSystemState(-1, timeout)) {
        				log("Can't connect to server ", integrator.systemState());
        				return false;
        			}
        			if (integrator.systemState() == 1) {
        				byte[] cfgPackage = getConfiguration(cfg);
        				log("set config");
        				if (!integrator.send(cfgPackage, 0, cfgPackage.length)) { // set config
        					log("Can't set config ");
            				return false;
        				}
        				if (!integrator.waitForSystemState(2, timeout)) {
        					log("wrong system state after set config ", integrator.systemState());
            				return false;
            			}
        			}
        			if (integrator.systemState() == 2) {
        				if (!integrator.send(new byte[]{0, 8, 0,0,0,0, 0,0,0,0}, 0, 10)) { // start server
        					log("Can't start server ");
            				return false;
        				}
        				if (!integrator.waitForSystemState(3, timeout)) {
        					log("wrong system state after run ", integrator.systemState());
            				return false;
            			}
        			}
        			return true;
        		} else {
        			log("Server can't reconnect to integrator.");
        		}
        	} else {
        		log("Can't start server");
        	}
        } else {
        	log("Can't call killall");
        }
        return false;
    }

    protected void cleanup() {
    	super.cleanup();
    	if (integrator != null) {
    		integrator.cleanup();
    	}
    	if (serverBin != null) {
    		startProcess(null, false, "killall", serverBin);
    	}
    }
	
    public void alarm(Package alarm) {
		if (hashCodes.length <= hashCounter) {
			log(" ----- wrong Alarm count : ", alarm.hashCode(), ", hashCounter=", (hashCounter-1));
			result = false;
		} else if (hashCodes[hashCounter++] != alarm.hashCode()) {
			log(" ----- wrong Alarm : ", alarm.hashCode(), ", hashCounter=", (hashCounter-1), ", should be ", hashCodes[hashCounter-1]);
			result = false;
		} else {
			log(" ----- Alarm OK : hashCounter=", (hashCounter-1));
		}
    	/*if (hashCodes.length >= hashCounter || hashCodes[hashCounter++] != alarm.hashCode()) {
			log(" ----- wrong Alarm : ", alarm.hashCode(), ", hashCounter=", (hashCounter-1), ", should be ", hashCodes[hashCounter-1]);
			result = false;
		} else {
			log(" ----- Alarm OK : hashCounter=", (hashCounter-1));
		}*/
	}

	protected boolean runTest() throws Throwable {
		long timestamp = System.currentTimeMillis();
		while (!integrator.isFailed && (result &= reconnect()) && System.currentTimeMillis() - timestamp < stressTestTime) {
			GregorianCalendar gc = new GregorianCalendar();
			StringBuilder st = new StringBuilder();
			st.append(gc.get(GregorianCalendar.YEAR)).append("-").append(gc.get(GregorianCalendar.MONTH)).append("-").append(gc.get(GregorianCalendar.DATE)).append(" ").append(gc.get(GregorianCalendar.HOUR)).append(":").append(gc.get(GregorianCalendar.MINUTE)).append(":").append(gc.get(GregorianCalendar.SECOND)).append(".000");
			byte[] tmp = st.toString().getBytes();
			byte[] synchPackage = new byte[10 + tmp.length];
			int length = tmp.length;
			synchPackage[0] = 5;
			synchPackage[6] = (byte) (length & 0xff);
			synchPackage[7] = (byte) ((length >> 8) & 0xff);
			synchPackage[8] = (byte) ((length >> 16) & 0xff);
			synchPackage[9] = (byte) ((length >> 24) & 0xff);
			System.arraycopy(tmp, 0, synchPackage, 10, tmp.length);
			log("send synchronization", " --------- ", new Date());
			integrator.send(synchPackage, 0, synchPackage.length); // synchronization
			try {
				Thread.sleep(30000);
			} catch(Throwable e) {}
			if (integrator.systemState() == 3) {
				log("stop server", " --------- ", new Date());
				integrator.send(new byte[]{0, 5, 0,0,0,0, 0,0,0,0}, 0, 10); // stop server
				if (!integrator.waitForSystemState(2, timeout)) {
					log("wrong system state after stop ", integrator.systemState());
					result = false;
    			}
			} else {
				log("Unexpected: server is stop");
				result = false;
			}
		}
		return result;
	}
}
