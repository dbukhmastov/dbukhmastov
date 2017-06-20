package apstek.util.sleep;

public class Sleep {
	public static void mySleep(int time) {
		try {
			Thread.sleep(time);
		} catch (Throwable e) {
		}
	}
}
