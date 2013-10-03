package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author bansal
 */
public class Main {

    static void initiateProcess(NetController n, int pid, int totalProcessesCount) {
        Process p;
        if (pid == 0) {
            p = new CoordinatorImpl(n, pid, null, true, "",totalProcessesCount);
        } else {
            p = new ParticipantImpl(n, pid, null, true);
        }
        p.start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String filename = "config.txt";

        List<NetController> netControllerList = new ArrayList<NetController>();

        Properties prop = new Properties();
        prop.load(new FileInputStream(filename));

        List<String> msgsMainList = new ArrayList<String>(
                Arrays.asList(MsgContent.ABORT.content, 
                MsgContent.COMMIT.content, 
                MsgContent.VOTE_REQ.content,
                MsgContent.PRECOMMIT.content));

        int N = Helper.loadInt(prop, "NumProcesses");

        for (int i = 0; i < N; i++) {
            try {
                Config config = new Config(filename, prop, i);
                NetController netController = new NetController(config, msgsMainList);
                initiateProcess(netController, i, N);
                netControllerList.add(netController);
            } catch (Exception e) {
                System.out.println("Trying to run config " + e);
            }
        }


//
//        for (int i = 0; i < N; i++) {
//            NetController tmp = netControllerList.get(i);
//            for (int j = 0; j < N; j++) {
//                tmp.sendMsg(j, "Hello this is " + i + ";COMMIT");
//                tmp.sendMsg(j, "Hello this is " + i + ";NO---COOMIT");
//            }
//        }
//        
//        Thread.sleep(2000);
//        for (int j = 0; j < N; j++) {
//            {
//                for (int i = 0; i < 4; i++) {
//                    NetController tmp = netControllerList.get(j);
//                    
//                    String output = tmp.getReceivedMsgMain();
//                    System.out.println("Main : " + j + " " + output);
//                    output = tmp.getReceivedMsgBack();
//                    System.out.println("Back : " + j + " " + output);
//                }
//            }

        //Shutdown of all the processes after the Main execution Closes...
        for (int i = 0; i < N; i++) {
            NetController netController = netControllerList.get(i);
            netController.shutdown();
        }
    }
}
