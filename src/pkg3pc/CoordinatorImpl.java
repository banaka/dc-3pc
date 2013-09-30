/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.HashMap;
import java.util.Map;
import ut.distcomp.framework.NetController;

/**
 *
 * @author bansal
 */
public class CoordinatorImpl extends Process implements Coordinator {

    public void initTransaction() {
        logMsg("START 3PC");
        sendVoteRequets();
        processVotes();
    }

    Map<Integer, String> votes = new HashMap<Integer, String>();
    
    CoordinatorImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput, String txData, int totalProcNo) {
        super(netController, procNo, stateToDie, voteInput);
        this.txCommand = txData;
        for (int i = 0; i < totalProcNo; i++) {
            this.up.add(i);
        }

    }

    public void sendVoteRequets() {
        for (int i : up) {
            sendMsg(MsgContent.VOTE_REQ, txCommand, i);
        }
    }

    public void processVotes() {
        //use wait(timeout) 
        //get for all the messages to come 
        
        
    }
}
