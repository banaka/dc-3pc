package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.io.IOException;

/**
 * @author bansal
 */
public class Main {

    /**
     * args :
     * 0 : pid of the process
     * 1 : Vote for that process
     * 2 : No of messages to be recieved from a process
     * 3 : Proess from which we are to recieve these message
     * 4 : Die after sending the messages to a process n
     * @param n
     * @param config
     * @param args
     */
    static void initiateProcess(NetController n, Config config, String[] args) {
        Process p;
        Boolean vote = true;
        int msgCount = 0;
        int pid = Integer.parseInt(args[0]);
        if (config.clean != null && Boolean.parseBoolean(config.clean.trim())) {
            Helper.clearLogs("Log" + pid + ".log");
            Helper.clearLogs("PlayListInstruction" + pid + ".txt");
        }
        if (args.length > 1)
            if("true".equals(args[1]) || "false".equals(args[1]))
                vote = Boolean.parseBoolean(args[1]);

        if( args.length > 2)
            if(!("-1".equals(args[2])))
                msgCount = Integer.parseInt(args[2]);

        if( args.length > 3)
                config.msgCountFrom = Integer.parseInt(args[3]);

        if( args.length > 4)
                config.partialCommitTo = Integer.parseInt(args[4]);

        if (pid == 0) {
//            config.updateTx();
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
