package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author bansal
 */
public class Main {

    static void initiateProcess(NetController n, Config config, String[] args) {
        Process p;
        Boolean vote = true;
        int pid = Integer.parseInt(args[0]);
        if (config.clean != null)
            Helper.clearLogs("Log" + pid + ".log");
        if (args.length > 1)
            vote = Boolean.parseBoolean(args[1]);

        if (pid == 0) {
            p = new CoordinatorImpl(n, pid, vote, 0, config);
        } else {
            p = new ParticipantImpl(n, pid, vote, 0, config);
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
