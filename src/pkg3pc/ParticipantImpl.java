 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

 import ut.distcomp.framework.NetController;

import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
import java.util.Map;
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
    public boolean interimCoodrinator = false;
    Map<Integer,MsgContent> interimStates;
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
            case UNCERTAIN:
            case COMMITABLE:
            case COMMITED:
            case ABORTED:
                    interimStates.put(fromProcId, msgContent);
                    if(interimStates.size() == up.size())
                        takeDecision();
                break;
            case STATE_REQ:
                updateCoordinator(fromProcId);
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
            case U_R_COORDINATOR:
                //update my uplist
                updateCoordinator(fromProcId);
                interimCoodrinator = true;
                interimStates = new HashMap<Integer, MsgContent>();
                //ask for state req
                sendMsgToAll(MsgContent.STATE_REQ);
                break;
            case TIMEOUT:
                if(interimCoodrinator){
                    takeDecision();
                    break;
                }
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
                logger.log(Level.WARNING, "Not expected ::" + msgContent.content);
        }
        return true;
    }

     private void sendMsgToAll(MsgContent msgContent) {
         Iterator<Integer> it = up.iterator();
         while (it.hasNext())
             sendMsg(msgContent,"",it.next());
     }

     private void takeDecision() {
         if(interimStates.containsValue(MsgContent.COMMITED)) {
             commit();
             sendMsgToAll(MsgContent.COMMIT);
         }
         else if(interimStates.containsValue(MsgContent.ABORTED)) {
             abort();
             sendMsgToAll(MsgContent.ABORT);
         }
         else if(interimStates.containsValue(MsgContent.COMMITABLE)){


         } else {
             abort();
             sendMsgToAll(MsgContent.ABORT);
         }
     }

     private void updateCoordinator(int fromProcId) {
         for(int i = coordinator; i < fromProcId; i++)
             up.remove(i);
         coordinator = fromProcId;
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
