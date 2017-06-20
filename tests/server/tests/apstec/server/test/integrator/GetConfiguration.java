package apstec.server.test.integrator;

import java.nio.file.FileSystems;
import java.nio.file.Files;

public interface GetConfiguration{
	static String cfg = "/home/mmwoperator/jenkins/test/configurations/dump1/settings/config.xml";

	default byte[] getConfiguration() throws Throwable {
		return getConfiguration(cfg);
	}
	
	default byte[] getConfiguration(String cfgPath) throws Throwable {
		byte[] config = Files.readAllBytes(FileSystems.getDefault().getPath(cfgPath));
		byte[] cfgPackage = new byte[10 + config.length];
		int length = config.length;
		cfgPackage[0] = 6; cfgPackage[1] = 7;
		cfgPackage[6] = (byte) (length & 0xff);
		cfgPackage[7] = (byte) ((length >> 8) & 0xff);
		cfgPackage[8] = (byte) ((length >> 16) & 0xff);
		cfgPackage[9] = (byte) ((length >> 24) & 0xff);
		System.arraycopy(config, 0, cfgPackage, 10, config.length);
		return cfgPackage;
	}
}
