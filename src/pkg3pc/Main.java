package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.io.IOException;

/**
 * @author bansal
 */
public class Main {

    static void initiateProcess(NetController n, Config config, String[] args) {
        Process p;
        Boolean vote = true;
        int msgCount = 0;
        int pid = Integer.parseInt(args[0]);
        if (config.clean != null)
            Helper.clearLogs("Log" + pid + ".log");
        if (args.length > 1)
            vote = Boolean.parseBoolean(args[1]);
        if( args.length > 2)
            msgCount = Integer.parseInt(args[2]);
        if (pid == 0) {
            p = new CoordinatorImpl(n, pid, vote, msgCount, config);
        } else {
            p = new ParticipantImpl(n, pid, vote, msgCount, config);
        }
        p.refreshState();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        int pid;
        if (args.length > 0)
            pid = Integer.parseInt(args[0]);
        else
            throw new IOException("Enter PID as the first parameter!");
        String filename = "config.properties";

        Config config = new Config(filename, pid);
        NetController netController = new NetController(config);
        initiateProcess(netController, config, args);
    }
}
