package apstec.server.test.integrator;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import apstec.server.test.BaseTest;

public abstract class IntegratorBaseTest extends BaseTest implements GetConfiguration {
    private static final String DEFAULT_INTEGRATOR_PORT = "45678";//45678 OR 2800
    
    protected IntegratorEmulator integrator;
    private String cfg;
    
    protected boolean init(Properties props) throws IOException {
    	cfg = configurationDir + "/settings/config.xml";
    	String tmpStr = "";
    	integrator = new IntegratorEmulator(Integer.parseInt((tmpStr = props.getProperty(INTEGRATOR_PORT)) == null ? DEFAULT_INTEGRATOR_PORT : tmpStr.trim()), timeout, this);
        integrator.start();
        return true;
    }
    
    protected boolean configureServer() throws Throwable {
    	log("configureServer --------- ", new Date());
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
    }
    
    protected boolean startServer(AlarmListener listener) throws Throwable {
    	log("startServer --------- ", new Date());
    	if (startProcess(null, false, "killall", serverBin)) {
        	try {
				Thread.sleep(10);
			} catch(Throwable e) {}
        	if (!startProcess(null, false, "rm", "-rf", configurationDir+"/log/*")){
        		log("Can clear log");
        	}
        	if (listener != null) {
        		integrator.addAlarmListener(listener);
        	}
        	//startProcess(null, "./"+serverBin, "-s", "-b", configurationDir)
        	if (startProcess(null, true, "./"+serverBin, "-b", configurationDir)) {
        		if (integrator.reconnect()) {
        			if (!integrator.waitForNotSystemState(-1, timeout)) {
        				log("Can't connect to server ", integrator.systemState());
        				return false;
        			}
        			return true;
        		} else {
        			log("Server can't connect to server.");
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
}