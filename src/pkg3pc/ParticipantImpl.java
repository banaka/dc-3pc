/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.NetController;

/**
 *
 * @author bansal
 */
public class ParticipantImpl extends Process implements Participant {
    ParticipantImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput, int msgCount){
        super(netController, procNo, stateToDie, voteInput, msgCount);
    }

    public void processStateRequest(){
        
    }
    
    @Override
    public void precommit(){
        logMsg("PRECOMMIT");
    }

    @Override
    public boolean handleSpecificCommands(MsgContent msgContent, String[] msgFields) {
        int fromProcId = Integer.parseInt(msgFields[MessageGenerator.processNo].trim());
        switch (msgContent) {
            case VOTE_REQ:
                logMsg("RECIEVED VOTE REQ");
                try{
                    return processVoteRequest(msgFields[MessageGenerator.msgData], fromProcId);
                } catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Please Send your transaction command with Vote Req!!");
                }
                break;
            case TIMEOUT:
                switch(currentState) {
                    case Uncertain:
                        //Run Coordinator election
                        break;
                    //Forever wait if Wait VoteReq state
                    case WaitForVotReq:
                        break;
                    default:
                        System.out.println("Timed out ...relistening...");
                }
                break;
            default:
                logMsg("Not expected ::"+msgContent.content);
        }
        return true;
    }

    public boolean processVoteRequest(String command, int sendTo) {
        txCommand = command;
        if (vote) {
            logMsg("SENT VOTE - "+vote);
            currentState = ProcessState.LoggedVote;
            sendMsg(MsgContent.VoteYes, command, sendTo);
            currentState = ProcessState.Uncertain;
        } else {
//            sendMsg(MsgContent.VoteNo, command, sendTo);
            abort();
            return false;
        }
        return true;
    }
}
