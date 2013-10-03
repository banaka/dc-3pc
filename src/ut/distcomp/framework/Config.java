/**
 * This code may be modified and used for non-commercial 
 * purposes as long as attribution is maintained.
 * 
 * @author: Isaac Levy
 */

package ut.distcomp.framework;

import pkg3pc.Helper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Logger;

public class Config {

	/**
	 * Loads config from a file.  Optionally puts in 'procNum' if in file.
	 * See sample file for syntax
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Config(String filename, Properties prop, int procNum) throws FileNotFoundException, IOException {
		logger = Logger.getLogger("NetFramework");

		numProcesses = Helper.loadInt(prop, "NumProcesses");
		command = prop.getProperty("command").trim();
        addresses = new InetAddress[numProcesses];
		ports = new int[numProcesses];
		for (int i=0; i < numProcesses; i++) {
			ports[i] = 8080+i;
			addresses[i] = InetAddress.getByName("localhost");
		}
			this.procNum = procNum;
		}
	
	/**
	 * Default constructor for those who want to populate config file manually
	 */
	public Config() {
	}
    public String command;
	/**
	 * Array of addresses of other hosts.  All hosts should have identical info here.
	 */
	public InetAddress[] addresses;
	

	/**
	 * Array of listening port of other hosts.  All hosts should have identical info here.
	 */
	public int[] ports;
	
	/**
	 * Total number of hosts
	 */
	public int numProcesses;
	
	/**
	 * This hosts number (should correspond to array above).  Each host should have a different number.
	 */
	public int procNum;
	
	/**
	 * Logger.  Mainly used for console printing, though be diverted to a file.
	 * Verbosity can be restricted by raising level to WARN
	 */
	public Logger logger;
}
