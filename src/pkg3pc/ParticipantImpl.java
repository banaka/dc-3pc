/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.util.*;
import java.util.logging.Level;

/**
 * @author bansal
 */
public class ParticipantImpl extends Process implements Participant {
    ParticipantImpl(NetController netController, int procNo, Boolean voteInput, int msgCount, Config config) {
        super(netController, procNo, voteInput, msgCount, config);
    }

    public int coordinator;
    public boolean isWaitingForAck = false;
    Map<Integer, MsgContent> interimStates;
    Set<Integer> interimAcks;

    public void processStateRequest() {
    }

    @Override
    public boolean handleSpecificCommands(MsgContent msgContent, String[] msgFields) {
        int fromProcId = Integer.parseInt(msgFields[MsgGen.processNo].trim());
        switch (msgContent) {
            case COMMITED:
            case ABORTED:
                interimStates.put(fromProcId, msgContent);
                takeDecision();
                break;
            case COMMITABLE:
            case UNCERTAIN:
                interimStates.put(fromProcId, msgContent);
                if (interimStates.size() == up.size())
                    takeDecision();
                break;
            case STATE_REQ:
                updateCoordinator(fromProcId);
                sendStateRequestRes(fromProcId);
                break;
            case VOTE_REQ:
                logger.info(LogMsgType.REC_VOTE_REQ.txt + MsgGen.MSG_FIELD_SEPARATOR + txCommand);
                try {
                    coordinator = fromProcId;
                    String[] ups = msgFields[MsgGen.ups].split(",");
                    for (String s : ups) {
                        synchronized (up) {
                            up.add(Integer.parseInt(s));
                        }
//                        synchronized (upReply) {
//                            upReply.add(Integer.parseInt(s));
//                        }
                    }
                    return processVoteRequest(msgFields[MsgGen.msgData], fromProcId);
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.log(Level.WARNING, "Please Send your transaction command with Vote Req!!");
                }
                break;
            case PRECOMMIT:
                //Change the state to precommit and Also send ACK to the Coordinator
                precommit();
                sendMsg(MsgContent.ACK, "", fromProcId);
                break;
            case STATUS_REQ:
                sendMsg(Enum.valueOf(MsgContent.class, currentState.msgState), txCommand, fromProcId);
                //sendStatusRequestRes(fromProcId);
                break;
            case U_R_COORDINATOR:
                //update my uplist
                if (!interimCoodrinator) {
                    updateCoordinator(fromProcId);
                    interimCoodrinator = true;
                    interimStates = new HashMap<Integer, MsgContent>();
                    //ask for state req
                    sendMsgToAll(MsgContent.STATE_REQ);
                }
                break;
            case ACK:
                if (interimCoodrinator && currentState == ProcessState.Commitable) {
                    interimAcks.add(fromProcId);
                }
                if (interimAcks.size() == up.size()) {
                    commit();
                    sendMsgToAll(MsgContent.COMMIT);
                    isWaitingForAck = false;
                }
                break;
            case TIMEOUT:
                if (interimCoodrinator) {
                    if (isWaitingForAck) {
                        commit();
                        sendMsgToAll(MsgContent.COMMIT);
                        isWaitingForAck = false;
                    }
                    takeDecision();
                    break;
                }
                switch (currentState) {
                    case Uncertain:
                    case Commitable:
                        //Run Coordinator election
                        synchronized (up) {
                            up.remove(coordinator);
                            coordinator = Collections.min(up);
                        }
                        sendMsg(MsgContent.U_R_COORDINATOR, "", coordinator);
                        break;
                    //Forever wait if Wait VoteReq state
                    case WaitForVotReq:
                        break;
                    default:
                        logger.log(Level.WARNING, "Timed out ...relistening...");
                }
                break;
            default:
                logger.log(Level.WARNING, "Not expected ::" + msgContent.content);
        }
        return true;
    }

    private void takeDecision() {
        if (interimStates.containsValue(MsgContent.COMMITED)) {
            commit();
//            if (!recovered)
            sendMsgToAll(MsgContent.COMMIT);
        } else if (interimStates.containsValue(MsgContent.ABORTED)) {
            abort();
//            if (!recovered)
            sendMsgToAll(MsgContent.ABORT);
        } else if (interimStates.containsValue(MsgContent.COMMITABLE)) {
            //IF anyone process is uncertain them Send precommit to all (Can be modified to be sedning it only to who are uncertain
            if (interimStates.containsValue(MsgContent.UNCERTAIN)) {
                precommit();
                sendMsgToAll(MsgContent.PRECOMMIT);
                isWaitingForAck = true;
            } else {
                //Otherwise it implies everyone else is in COMMITTABLE state and we need to COMMIT
                commit();
                sendMsgToAll(MsgContent.COMMIT);
            }
        } else {
            abort();
//            if (!recovered)
            sendMsgToAll(MsgContent.ABORT);
        }
    }

    private void updateCoordinator(int fromProcId) {
        synchronized (up) {
            for (int i = coordinator; i < fromProcId; i++)
                up.remove(i);
        }
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
