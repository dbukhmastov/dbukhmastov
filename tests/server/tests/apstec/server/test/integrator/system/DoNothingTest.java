package apstec.server.test.integrator.system;

import apstec.server.test.integrator.AlarmListener;
import apstec.server.test.integrator.IntegratorBaseTest;
import apstec.server.test.integrator.Package;

/**
 * @test
 * @id DoNothingTest
 * @executeClass apstec.server.test.integrator.system.DoNothingTest
 * @source DoNothingTest.java
 * @title Test just read info and alerts from server
 */
public class DoNothingTest extends IntegratorBaseTest implements AlarmListener {
	private volatile Package alarm;
	public static void main(String[] args) {
        main(new DoNothingTest(), args);
    }
	
	public void alarm(Package alarm) {
		this.alarm = alarm;
	}

	protected boolean runTest() throws Throwable {
		if (startServer(this)) {
			long timestamp = System.currentTimeMillis();
			while (System.currentTimeMillis() - timestamp < testTime) {
				try {
					Thread.sleep(stressTestTime);
				} catch(Throwable e) {}
			}
			return alarm == null;
		} 
		return false;
	}

}
