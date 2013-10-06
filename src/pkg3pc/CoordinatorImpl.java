/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.NetController;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author bansal
 */
public class CoordinatorImpl extends Process implements Coordinator {

    Map<Integer, String> votes = new HashMap<Integer, String>();

    @Override
    public void initTransaction() {
        logger.info(LogMsgType.START3PC.txt);
        sendVoteRequests();
        getVotes();
        processVotes();
    }

    @Override
    public void precommit() {
        logger.info(LogMsgType.PRECOMMIT.txt);
    }

    @Override
    public boolean handleSpecificCommands(MsgContent msgContent, String[] msgFields) {
        boolean shouldContinue = true;
        int fromProcId = Integer.parseInt(msgFields[MessageGenerator.processNo].trim());

        switch (msgContent) {
            case VoteYes:
            case VoteNo:
                votes.put(fromProcId, msgContent.content);
                if (votes.size() >= (up.size() - 1))
                    shouldContinue = false;
                break;
            case TIMEOUT:
                if (currentState == ProcessState.VoteReq)
                    send_abort();
                shouldContinue = false;
                break;
            default:
                logger.log(Level.WARNING, "Not expected ::" + msgContent.content);
        }
        return shouldContinue;
    }

    private void send_abort() {
        logger.info(LogMsgType.ABORT.txt);
        for (int i : up)
            if (("VoteYes").equals(this.votes.get(i)))
                sendMsg(MsgContent.ABORT, "", i);
        abort();
    }

    private void send_commit() {
        logger.info(LogMsgType.COMMIT.txt);
        for (int i : up)
            sendMsg(MsgContent.COMMIT, "", i);
        commit();
    }

    CoordinatorImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput,
                    String txData, int totalProcNo, int msgCount) {
        super(netController, procNo, stateToDie, voteInput, msgCount);
        this.txCommand = txData;
        for (int i = 0; i < totalProcNo; i++) {
            this.up.add(i);
        }

    }

    public void sendVoteRequests() {
        this.currentState = ProcessState.VoteReq;
        for (int i : up) {
            if (i != procNo)
                sendMsg(MsgContent.VOTE_REQ, txCommand, i);
        }
    }

    public void getVotes() {
        startListening(timeout);
    }

    public void processVotes() {
        //Votes Check...
        if (votes.size() < (up.size() - 1))
            return;
        System.out.println(votes);
        boolean allSaidYes = true;
        if (this.vote == false)
            allSaidYes = false;
        for (String vote : votes.values()) {
            if ("VoteNo".equals(vote))
                allSaidYes = false;
        }

        if (allSaidYes) {
            send_commit();
        } else {
            send_abort();
        }
        votes.clear();
    }

}
