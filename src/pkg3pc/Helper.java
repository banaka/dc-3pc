/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author bansal
 */
 public class Helper {
    	
     static public int loadInt(Properties prop, String s) {
		return Integer.parseInt(prop.getProperty(s.trim()));
	}


    static public void clearLogs(String logFileName) {
        try {
            FileWriter fileWriter = new FileWriter(logFileName);
            fileWriter.write("");
            fileWriter.close();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }
}
