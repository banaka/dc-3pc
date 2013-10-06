 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.NetController;

import java.util.Collections;
import java.util.logging.Level;

 /**
 *
 * @author bansal
 */
public class ParticipantImpl extends Process implements Participant {
    ParticipantImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput, int msgCount){
        super(netController, procNo, stateToDie, voteInput, msgCount);
    }
    public int coordinator;
    public void processStateRequest(){
        
    }
    
    @Override
    public void precommit(){
        logger.info(LogMsgType.PRECOMMIT.txt);
    }

    @Override
    public boolean handleSpecificCommands(MsgContent msgContent, String[] msgFields) {
        int fromProcId = Integer.parseInt(msgFields[MessageGenerator.processNo].trim());
        switch (msgContent) {
            case STATE_REQ:
                sendStateRequestRes(fromProcId);
                break;
            case VOTE_REQ:
                logger.info(LogMsgType.REC_VOTE_REQ.txt);
                try{
                    coordinator = fromProcId;
                    return processVoteRequest(msgFields[MessageGenerator.msgData], fromProcId);
                } catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("Please Send your transaction command with Vote Req!!");
                }
                break;
            case TIMEOUT:
                switch(currentState) {
                    case Uncertain:
                    case Commitable:
                        //Run Coordinator election
                        up.remove(coordinator);
                        coordinator = Collections.min(up);
                        sendMsg(MsgContent.U_R_COORDINATOR,"",coordinator);
                        break;
                    //Forever wait if Wait VoteReq state
                    case WaitForVotReq:
                        break;
                    default:
                        System.out.println("Timed out ...relistening...");
                }
                break;
            default:
                logger.log(Level.WARNING,"Not expected ::"+msgContent.content);
        }
        return true;
    }

    public boolean processVoteRequest(String command, int sendTo) {
        txCommand = command;
        if (vote) {
            logger.info(LogMsgType.VOTEYES.txt);
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
