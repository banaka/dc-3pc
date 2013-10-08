/**
 * This code may be modified and used for non-commercial 
 * purposes as long as attribution is maintained.
 *
 * @author: Isaac Levy
 */

package ut.distcomp.framework;

import pkg3pc.Helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    public int msgCountFrom;
    public int partialCommitTo;
    public int txNo;
    public int aliveTimeout;
    public Properties prop;

    /**
     * Loads config from a file.  Optionally puts in 'procNum' if in file.
     * See sample file for syntax
     *
     * @param filename
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Config(String filename, int procNum) throws IOException {
        prop = new Properties();
        prop.load(new FileInputStream(filename));
        this.filename = filename;
        numProcesses = Helper.loadInt(prop, "NumProcesses");
        timeout = 5000;
        aliveTimeout = 2000;
        if (prop.getProperty("delay") != null) {
            delay = Helper.loadInt(prop, "delay");
        } else {
            delay = 10;
        }
        timeout = 3 * delay;
        if (prop.getProperty("timeout") != null)
            timeout = Helper.loadInt(prop, "timeout");
        if (prop.getProperty("timeout") != null)
            aliveTimeout = Helper.loadInt(prop, "timeout") / 2;
        clean = prop.getProperty("clean");

        txNo = Helper.loadInt(prop, "txNo");
        command = getCommand();

        logger = Logger.getLogger("NetFramework");
        logger.setLevel(Level.FINER);
        logger.setUseParentHandlers(false);
        Handler consoleHandler = null;
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = handler;
                break;
            }
        }
        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            logger.addHandler(consoleHandler);
        }
        consoleHandler.setLevel(Level.CONFIG);

//        Handler handler = new FileHandler();
//        handler.setFormatter(new CustomFormatter());
//        logger.addHandler(handler);

        addresses = new InetAddress[numProcesses];
        ports = new int[numProcesses];
        for (int i = 0; i < numProcesses; i++) {
            ports[i] = 8080 + i;
            addresses[i] = InetAddress.getByName("localhost");
        }
        this.procNum = procNum;
    }

    public String getCommand() {
        command = (prop.getProperty("command" + txNo));
        if (command == null)
            command = prop.getProperty("command");
        command = command.trim();
        return command;
    }

    /**
     * Default constructor for those who want to populate config file manually
     */
    public Config() {
    }

    public int timeout;
    public String clean;
    public int delay;
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
    public String filename;
    /**
     * Logger.  Mainly used for console printing, though be diverted to a file.
     * Verbosity can be restricted by raising level to WARN
     */
    public Logger logger;

    public void updateTx() {
        try {
            FileInputStream in = new FileInputStream(filename);
            Properties prop = new Properties();
            prop.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream(filename);
            prop.setProperty("txNo", Integer.toString(txNo + 1));
            prop.store(out, null);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
