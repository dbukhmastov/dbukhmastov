package apstec.server.test.generator.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import apstec.server.test.generator.GeneratorBaseTest;

/**
 * @test
 * @id SimpleTest
 * @executeClass apstec.server.test.generator.system.SimpleTest
 * @source SimpleTest.java
 * @title Test SimpleTest command
 */
public class SimpleTest extends GeneratorBaseTest{
	
	public static void main(String[] args) {
        main(new SimpleTest(), args);
    }

	protected boolean runTest() throws Throwable {
		Thread.sleep(60000);
		try {//jdbc:oracle:thin
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/integrator", "apstec", "apstec");
			Statement stmt = con.createStatement();
			//integrator_persons
			//integrator_logs
			ResultSet rs = stmt.executeQuery("select * from integrator_alarms");
			log("---- ResultSet");
			while (rs.next()) {
                log("Next row: ", rs);
            }
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return true;
	}

}
