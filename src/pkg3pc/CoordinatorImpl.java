/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.NetController;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author bansal
 */
public class CoordinatorImpl extends Process implements Coordinator {

    Map<Integer, String> votes = new HashMap<Integer, String>();

    @Override
    public void initTransaction() {
        logMsg(" START 3PC");
        sendVoteRequests();
        processVotes();
    }

    @Override
    public void precommit() {
        logMsg("PRECOMMIT");
    }

    @Override
    public void handleSpecificCommands(MsgContent command, String[] msgFields) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    CoordinatorImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput, String txData, int totalProcNo) {
        super(netController, procNo, stateToDie, voteInput);
        this.txCommand = txData;
        for (int i = 0; i < totalProcNo; i++) {
            this.up.add(i);
        }

    }

    public void sendVoteRequests() {
        for (int i : up) {
            if(i != procNo)
                sendMsg(MsgContent.VOTE_REQ, txCommand, i);
        }
    }

    public void processVotes() {
        /*Ideally the wait should be just untill we get messages from all the proceses or till timeout */
        while (votes.size() < (up.size()-1)) {
//            synchronized (this.netController.objectToWait) {
//                try {
//                    //thread to sleep so that we can get some messages 
//                    this.netController.objectToWait.wait(20);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(ProcessBackground.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            while(true){
            String msg;
            while ((msg = this.netController.getReceivedMsgMain()) == null)
                sleeping_for(10);
            logMsg("Received a message!!! - "+msg);
            String[] msgFeilds = msg.split(MessageGenerator.MSG_FIELD_SEPARATOR);
            int fromProcId = Integer.parseInt(msgFeilds[MessageGenerator.processNo].trim());
            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFeilds[MessageGenerator.msgContent]);
            if (msgContent == MsgContent.VoteYes || msgContent == MsgContent.VoteNo) {
                votes.put(fromProcId, msgContent.content);
            }
        }

        //Votes Check...
        System.out.println(votes);
        boolean allSaidYes = true;
        if(this.vote == false)
            allSaidYes = false;
        for(String vote : votes.values()){
            if("VoteNo".equals(vote))
                allSaidYes = false;
        }

        if(allSaidYes){
            logMsg("COMMIT");
            for(int i : up)
                sendMsg(MsgContent.COMMIT, "", i);
        }else{
            logMsg("ABORT");
            for(int i : up)
                sendMsg(MsgContent.ABORT, "", i);
        }
        startListening();
    }
}
