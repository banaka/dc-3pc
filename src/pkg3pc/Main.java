package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bansal
 */
public class Main {

    static void initiateProcess(NetController n, Config config, String[] args) {
        Process p;
        Boolean vote = true;
        int pid = Integer.parseInt(args[0]);

        if(args.length > 1)
            vote = Boolean.parseBoolean(args[1]);

        if (pid == 0) {
            p = new CoordinatorImpl(n, pid, null, vote, config.command, config.numProcesses);
        } else {
            p = new ParticipantImpl(n, pid, null, vote);
        }
//        p.start();
        p.refreshState();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        int pid;
        if(args.length > 0)
                pid = Integer.parseInt(args[0]);
        else
            throw new IOException("Enter PID as the first parameter!");
        String filename = "config.txt";

//        List<NetController> netControllerList = new ArrayList<NetController>();

//        Properties prop = new Properties();
//        prop.load(new FileInputStream(filename));

        List<String> msgsMainList = new ArrayList<String>(
                Arrays.asList(MsgContent.ABORT.content,
                        MsgContent.COMMIT.content,
                        MsgContent.VOTE_REQ.content,
                        MsgContent.PRECOMMIT.content,
                        MsgContent.VoteYes.content,
                        MsgContent.VoteNo.content));

//        int N = Helper.loadInt(prop, "NumProcesses");

//        for (int i = 0; i < N; i++) {
//            try {
                Config config = new Config(filename, pid);
                NetController netController = new NetController(config, msgsMainList);
                initiateProcess(netController, config, args);
//                netControllerList.add(netController);
//             } catch (Exception e) {
//                System.out.println("Trying to run config " + e);
//            }
//        }

        //Shutdown of all the processes after the Main execution Closes...
//        for (int i = 0; i < N; i++) {
//            NetController netController = netControllerList.get(i);
//            netController.shutdown();
//        }
    }
}
