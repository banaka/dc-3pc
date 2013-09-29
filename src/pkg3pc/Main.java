package pkg3pc;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

/**
 *
 * @author bansal
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        MessageGenerator test = new MessageGenerator();
        System.out.println(test.genMsg(MsgContent.VoteYes, 1));
        
        String filename = "/Users/bansal/Desktop/dc/config.txt";
        String logFile="/Users/bansal/Desktop/dc/logs";
        String trnFile ="";
        
        List<NetController> netControllerList = new ArrayList<NetController>();
        
        Properties prop = new Properties();
	prop.load(new FileInputStream(filename));
        
        int N = Helper.loadInt(prop, "NumProcesses");
        
        for (int i=0; i<N; i++){
            try {        
            Config config = new Config(filename , prop,i);
            NetController netController = new NetController(config);        
            netControllerList.add(netController);
            }
            catch (Exception e){
                System.out.println("Trying to run config "+e);
            }
        }
        
        for (int i=0; i<N; i++){
            NetController tmp = netControllerList.get(i);
            for (int j=0; j<N; j++){
            tmp.sendMsg(j, "Hello this is "+i);
            }
        }
        
        for (int j=0; j<N; j++){
            {
            NetController tmp = netControllerList.get(j);
            
            List<String> output = tmp.getReceivedMsgs();
            for (int k=0; k<output.size();k++){
                System.out.print(output.get(k));
            }
            System.out.println("");
        }

            //Shutdown of all the processes after the Main execution Closes...
        for(int i=0; i<N; i++){
            NetController netController = netControllerList.get(i);
            netController.shutdown();
        }
    }
    }
        
    
}
