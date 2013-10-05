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
        getVotes();
        processVotes();
    }

    @Override
    public void precommit() {
        logMsg("PRECOMMIT");
    }

    @Override
    public boolean handleSpecificCommands(MsgContent msgContent, String[] msgFields) {
        boolean shouldContinue = true;
        int fromProcId = Integer.parseInt(msgFields[MessageGenerator.processNo].trim());

        switch (msgContent) {
            case VoteYes:
            case VoteNo:
                votes.put(fromProcId, msgContent.content);
                if(votes.size() >= (up.size()-1))
                    shouldContinue = false;
                break;
            case TIMEOUT:
                if(currentState == ProcessState.VoteReq)
                    send_abort();
                shouldContinue = false;
                break;
            default:
                logMsg("Not expected ::"+msgContent.content);
        }
        return shouldContinue;
    }

    private void send_abort() {
        logMsg("ABORT");
        currentState = ProcessState.LoggedAbort;
        for(int i : up)
            if(("VoteYes").equals(this.votes.get(i)))
                sendMsg(MsgContent.ABORT, "", i);
    }
    private void send_commit() {
        logMsg("COMMIT");
        currentState = ProcessState.LoggedCommit;
        for(int i : up)
            sendMsg(MsgContent.COMMIT, "", i);
    }

    CoordinatorImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput, String txData, int totalProcNo) {
        super(netController, procNo, stateToDie, voteInput);
        this.txCommand = txData;
        for (int i = 0; i < totalProcNo; i++) {
            this.up.add(i);
        }

    }

    public void sendVoteRequests() {
        this.currentState = ProcessState.VoteReq;
        for (int i : up) {
            if(i != procNo)
                sendMsg(MsgContent.VOTE_REQ, txCommand, i);
        }
    }

    public void getVotes() {
        startListening(timeout);
    }

    public void processVotes() {
        /*Ideally the wait should be just untill we get messages from all the proceses or till timeout */
//        while (votes.size() < (up.size()-1)) {
//            synchronized (this.netController.objectToWait) {
//                try {
//                    //thread to sleep so that we can get some messages 
//                    this.netController.objectToWait.wait(20);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(ProcessBackground.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            while(true){
//            String msg;
//            while ((msg = this.netController.getReceivedMsgMain()) == null)
//                sleeping_for(10);
//            logMsg("Received a message!!! - "+msg);
//            String[] msgFeilds = msg.split(MessageGenerator.MSG_FIELD_SEPARATOR);
//            int fromProcId = Integer.parseInt(msgFeilds[MessageGenerator.processNo].trim());
//            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFeilds[MessageGenerator.msgContent]);
//            if (msgContent == MsgContent.VoteYes || msgContent == MsgContent.VoteNo) {
//                votes.put(fromProcId, msgContent.content);
//            }
//        }

        //Votes Check...
        if(votes.size() < (up.size()-1))
            return;
        currentState = ProcessState.VoteCounting;
        System.out.println(votes);
        boolean allSaidYes = true;
        if(this.vote == false)
            allSaidYes = false;
        for(String vote : votes.values()){
            if("VoteNo".equals(vote))
                allSaidYes = false;
        }

        if(allSaidYes){
            send_commit();
        }else{
            send_abort();
         }
    }

}
