/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.NetController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author bansal
 */
public class CoordinatorImpl extends Process implements Coordinator {

    Map<Integer, String> replySet = new HashMap<Integer, String>();
    int noOfProcesses;

    @Override
    public void initTransaction() {
        logger.info(LogMsgType.START3PC.txt);
        for (int i = 0; i < noOfProcesses; i++) {
            this.up.add(i);
        }
        sendVoteRequests();
        getVotes();
        processVotes();
    }

    @Override
    public void precommit() {
        logger.info(LogMsgType.PRECOMMIT.txt);
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
                logger.log(Level.CONFIG, "TIMEOUT");
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
        logger.info(LogMsgType.ABORT.txt);
        for (int i : up)
            if (("VoteYes").equals(this.replySet.get(i)))
                sendMsg(MsgContent.ABORT, "", i);
        abort();
    }

    public void send_commit() {
        logger.info(LogMsgType.COMMIT.txt);
        for (int i : up)
            sendMsg(MsgContent.COMMIT, "", i);
        commit();
    }

    public void send_precommit() {
        logger.info(LogMsgType.PRECOMMIT.txt);
        for (int i : up)
            sendMsg(MsgContent.PRECOMMIT, "", i);
        precommit();
    }

    CoordinatorImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput,
                    String txData, int totalProcNo, int msgCount) {
        super(netController, procNo, stateToDie, voteInput, msgCount);
        this.txCommand = txData;
        this.noOfProcesses = totalProcNo;
    }

    public void sendVoteRequests() {
        this.currentState = ProcessState.VoteReq;
        String txAppendNodes = appendParticipants(txCommand);
        for (int i : up) {
            if (i != procNo)
                sendMsg(MsgContent.VOTE_REQ, txAppendNodes, i);
        }
    }

    private String appendParticipants(String txCommand) {
        String appendedString = txCommand + MsgGen.MSG_FIELD_SEPARATOR;
        Iterator it = up.iterator();
        while(it.hasNext()){
            appendedString += (it.next() + ",");
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
     * Control Comes here only when we have all the votes or Timeone has taken place
     * We then Check if any of the vote if NO send abort
     * else we send PRECOMMIT
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
