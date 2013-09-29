/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.Properties;

/**
 *
 * @author bansal
 */
 public class Helper {
    	
     static public int loadInt(Properties prop, String s) {
		return Integer.parseInt(prop.getProperty(s.trim()));
	}

    
}
