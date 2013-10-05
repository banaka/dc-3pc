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
    ParticipantImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput){
        super(netController, procNo, stateToDie, voteInput);        
    }

    public void processVoteRequest(String command, int sendTo) {
        txCommand = command;
        logMsg("SENT VOTE - "+vote);
        currentState = ProcessState.LoggedVote;

        if (vote) {
            sendMsg(MsgContent.VoteYes, command, sendTo);
        } else {
            sendMsg(MsgContent.VoteNo, command, sendTo);
        }
        currentState = ProcessState.Uncertain;
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
                    processVoteRequest(msgFields[MessageGenerator.msgData], fromProcId);
                } catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Please Send your transaction command with Vote Req!!");
                }
                break;
            case TIMEOUT:
                System.out.println("Timed out ...relistening...");
            default:
                logMsg("Not expected ::"+msgContent.content);
        }
        return true;
    }

}
