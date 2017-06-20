package apstec.server.test.integrator.system;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.Properties;

import apstec.server.test.integrator.AlarmListener;
import apstec.server.test.integrator.GetConfiguration;
import apstec.server.test.integrator.IntegratorBaseTest;
import apstec.server.test.integrator.Package;

/**
 * @test
 * @id LongRunTest
 * @executeClass apstec.server.test.integrator.system.LongRunTest
 * @source LongRunTest.java
 * @title Test long run
 */
public class LongRunTest extends IntegratorBaseTest implements GetConfiguration, AlarmListener{
	private static final String ALARM_HASHCODE_FILE = "/apstec/server/test/integrator/system/LongRunTest.data";
	private int[] hashCodes = new int[1800];
	private int hashCounter = 0;
	private volatile boolean result = true;
	
	public static void main(String[] args) {
        main(new LongRunTest(), args);
    }
	
	protected boolean init(Properties props) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(ALARM_HASHCODE_FILE);
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
				buf[index++] = (byte) (tmp & 0xff);
			}
		}
		if (hashCodes.length != hashCounter) {
			int[] tmpArr = new int[hashCounter];
			System.arraycopy(hashCodes, 0, tmpArr, 0, hashCounter);
			hashCodes = tmpArr;
		}
		hashCounter = 0;
		return super.init(props);
	}
	
	public void alarm(Package alarm) {
		if (hashCodes.length <= hashCounter) {
			//log(" ----- wrong Alarm count : ", alarm.hashCode(), ", hashCounter=", (hashCounter-1));
			log(" ----- wrong  Alarm : ", alarm.toString());
			result = false;
		} else if (hashCodes[hashCounter++] != alarm.hashCode()) {
			//log(" ----- wrong Alarm : ", alarm.hashCode(), ", hashCounter=", (hashCounter-1), ", should be ", hashCodes[hashCounter-1]);
			log(" ----- wrong  Alarm : ", alarm.toString());
			result = false;
		}
		/*if (hashCodes.length >= hashCounter || hashCodes[hashCounter++] != alarm.hashCode()) {
			log(" ----- wrong Alarm : ", alarm.hashCode(), ", hashCounter=", (hashCounter-1), ", should be ", hashCodes[hashCounter-1]);
			result = false;
		}*/
	}

	protected boolean runTest() throws Throwable {
		if (startServer(this) && configureServer()) {
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
			log("send synchronization");
			integrator.send(synchPackage, 0, synchPackage.length); // synchronization
			long timestamp = System.currentTimeMillis();
			while (result && System.currentTimeMillis() - timestamp < stressTestTime) {
				try {
					Thread.sleep(5000);
				} catch(Throwable e) {}
			}
			if (integrator.systemState() == 3) {
				log("stop server");
				integrator.send(new byte[]{0, 5, 0,0,0,0, 0,0,0,0}, 0, 10); // stop server
				Thread.sleep(15000);
				result &= integrator.systemState() == 2;
				if (!result) {
					log("wrong system state after stop ", integrator.systemState());
				}
			} else {
				log("stopped already");
			}
			//TODO: add read mySQL
			try {
				Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/integrator", "apstec", "apstec");
				Statement stmt = con.createStatement();
				//integrator_persons
				//integrator_logs
				ResultSet rs = stmt.executeQuery("select * from integrator_alarms");
				log("---- ResultSet of integrator_alarms ", rs.getType());
				while (rs.next()) {
	                log("Next row: ", rs);
	            }
			} catch (Throwable e) {
				log("---- SQL ERROR");
				e.printStackTrace();
			}
		} 
		return false;
	}
}
