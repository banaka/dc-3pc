/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Throughout this class we have to make use of the noOfProcesses and not the up set because we cannot iterate over
 * the up set when it is getting updated.. and it makes sense to ake use of the noOfProcesses because the processes are
 * anyways not when Decision has been made..
 *
 * @author bansal
 */
public class CoordinatorImpl extends Process implements Coordinator {

    Map<Integer, String> replySet = new HashMap<Integer, String>();
    int noOfProcesses;

    CoordinatorImpl(NetController netController, int procNo, Boolean voteInput, int msgCount, Config config) {
        super(netController, procNo, voteInput, msgCount, config);
        this.txCommand = config.command;
        this.noOfProcesses = config.numProcesses;
    }

    @Override
    public void initTransaction() {
        currentState = ProcessState.VoteReq;
        logger.info(LogMsgType.START3PC.txt);
        for (int i = 0; i < noOfProcesses; i++) {
            synchronized (up) {
                up.add(i);
            }
//            synchronized (upReply) {
//                upReply.add(i);
//            }
        }
        sendVoteRequests();
        getVotes();
        processVotes();
    }

    public void precommitPhase() {
        replySet.clear();
        startListening(timeout);
        processAcks();
    }

    @Override
    public boolean handleSpecificCommands(MsgContent msgContent, String[] msgFields) {
        boolean shouldContinue = true;
        int fromProcId = Integer.parseInt(msgFields[MsgGen.processNo].trim());

        switch (msgContent) {
            case VoteYes:
            case VoteNo:
                replySet.put(fromProcId, msgContent.content);
                if (replySet.size() >= (noOfProcesses - 1))
                    shouldContinue = false;
                break;
            case ACK:
                replySet.put(fromProcId, msgContent.content);
                if (replySet.size() >= (noOfProcesses - 1))
                    shouldContinue = false;
                break;
            case TIMEOUT:
                if (currentState == ProcessState.VoteReq)
                    send_abort();
                if (currentState == ProcessState.Uncertain)
                    processAcks();
                shouldContinue = false;
                break;
            default:
                logger.log(Level.WARNING, "Not expected ::" + msgContent.content);
        }
        return shouldContinue;
    }

    public void send_abort() {
        for (int i = 0; i < noOfProcesses; i++)
            if (("VoteYes").equals(this.replySet.get(i)))
                sendMsg(MsgContent.ABORT, "", i);
        abort();
    }

    public void send_commit() {
        logger.info(LogMsgType.COMMIT.txt);
        for (int i = 0; i < noOfProcesses; i++) {
            if (i != procNo)
                sendMsg(MsgContent.COMMIT, "", i);
        }
        commit();
    }

    public void send_precommit() {
        logger.info(LogMsgType.PRECOMMIT.txt);
        for (int i = 0; i < noOfProcesses; i++) {
            if (i != procNo)
                sendMsg(MsgContent.PRECOMMIT, "", i);
        }
        currentState = ProcessState.Commitable;
        precommitPhase();
    }

    public void sendVoteRequests() {
        this.currentState = ProcessState.VoteReq;
        String txAppendNodes = appendParticipants(txCommand);
        //Need to do this because the up set gets over written even before these messages are sent out
        for (int i = 0; i < noOfProcesses; i++) {
            if (i != procNo)
                sendMsg(MsgContent.VOTE_REQ, txAppendNodes, i);
        }
    }

    private String appendParticipants(String txCommand) {
        String appendedString = txCommand + MsgGen.MSG_FIELD_SEPARATOR;
        for (int i = 0; i < noOfProcesses; i++) {
            appendedString += (i + ",");
        }
        return appendedString;
    }

    public void getVotes() {
        startListening(timeout);
    }

    /**
     * Ths Coordinator doesnt need to check if any of the process has actually sent ACK. IF all of them send ACK
     * before timeout well and good else it will wait for timeout and snd COMMIT
     */
    public void processAcks() {
        logger.log(Level.CONFIG, "Acks Reply Set " + replySet);
        send_commit();
        replySet.clear();
    }


    /**
     * Control Comes here only when we have all the votes or Timeout has taken place
     * We then Check if any of the vote if NO send abort
     * else we send PRE-COMMIT
     */
    public void processVotes() {
        logger.log(Level.CONFIG, "Votes Reply Set " + replySet);
        if (replySet.size() < (noOfProcesses - 1)) {
            send_abort();

        } else {
            boolean allSaidYes = true;
            if (this.vote == false)
                allSaidYes = false;
            for (String vote : replySet.values()) {
                if ("VoteNo".equals(vote))
                    allSaidYes = false;
            }

            if (allSaidYes) {
                send_precommit();
            } else {
                send_abort();
            }
        }
        replySet.clear();
    }

}
