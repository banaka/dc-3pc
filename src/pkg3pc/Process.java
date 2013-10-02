/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static pkg3pc.MsgContent.ABORT;
import static pkg3pc.MsgContent.CHECKALIVE;
import static pkg3pc.MsgContent.IAMALIVE;
import static pkg3pc.MsgContent.STATE_REQ;
import ut.distcomp.framework.NetController;

/**
 *
 * @author bansal
 */
public class Process extends Thread {

    public final static String TX_MSG_SEPARATOR = "$";
    //FORMAT FOR Transaction
    //ADD$Song$URL
    //EDIT$SONGOLD$URLOLD$NEWSONG$NEWURL
    //DELETE$SONG$URL
    int viewNumber;
    //Set to maintain the up set 
    Set<Integer> up = new HashSet<Integer>();
    ProcessState currentState;
    ProcessState endState;
    int procNo;
    int timeout;
    NetController netController;
    Hashtable<String, String> playlist;
    String txCommand;
    ProcessBackground processBackground;
    boolean vote;

    Process(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput) {
        this.netController = netController;
        this.vote = voteInput;
        this.procNo = procNo;
        this.endState = stateToDie;
        timeout = 10;
        playlist = new Hashtable<String, String>();
        viewNumber = 0;
        processBackground = new ProcessBackground(this);
        processBackground.start();
    }

    public void run() {
        try {
            // Comment is only for debugging purposes...  
            //this.sendMsg(MsgContent.CHECKALIVE, "", 1);
            refershState();
        } catch (InterruptedException ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void processAddToPlayist(String name, String url) {
    }

    void processDelFromPlaylist(String name, String url) {
    }

    void processEditPlaylist(String name, String url, String newName, String newUrl) {
    }

    public void waitUntillTimeout() {
    }

    public void waitForInitialVoteReq() {
    }

    public void sendMsg(MsgContent msgCont, String data, int sendTo) {
        String outputMsg = MessageGenerator.genMsg(msgCont, data, procNo);
        this.netController.sendMsg(sendTo, outputMsg);
        logMsg("Sent msg " + outputMsg + " to " + sendTo);
    }

    public void logMsg(String logMsg) {
        System.out.println("Log of proc " + procNo + " " + logMsg);
    }

    public void sendStateRequestRes(int procId) {
    }

    public void CheckUpstatus() {
    }

    public void updateUpSet(int procId) {
        //up.contains(procId);
    }

    public void refershState() throws InterruptedException {
        /*ToDo: Clear my queue both back and main */
        currentState = ProcessState.WaitForVotReq;
        initTransaction();

    }

    public void initTransaction() throws InterruptedException {
        logMsg("NEW TX - WAITING FOR VOTE REQ");
       // synchronized (this.netController.objectToWait) {
            //this.netController.objectToWait.wait(timeout);
            String msg;
            while ((msg = this.netController.getReceivedMsgMain()) != null) {
                String[] msgFeilds = msg.split(MessageGenerator.MSG_FIELD_SEPARATOR);
                int fromProcId = Integer.parseInt(msgFeilds[MessageGenerator.processNo].trim());
                String txCmd = msgFeilds[MessageGenerator.msgData];
                MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFeilds[MessageGenerator.msgContent]);
                switch (msgContent) {
                    case VOTE_REQ:
                        logMsg("RECIEVED VOTE REQ");
                        processVoteRequest(txCmd, fromProcId);
                        break;
                }
            }
        //}
    }

    public void processVoteRequest(String command, int sendTo) {
        txCommand = command.split(MessageGenerator.MSG_FIELD_SEPARATOR)[MessageGenerator.msgData];
        if (vote) {
            sendMsg(MsgContent.VoteYes, command, sendTo);
        } else {
            sendMsg(MsgContent.VoteNo, command, sendTo);
        }
        logMsg("SENT VOTE ");

    }

    public void abort() {
        logMsg("ABORT");
    }

    public void precommit() {
        logMsg("PRECOMMIT");
    }

    public void commit() {
        logMsg("COMMIT");
        String[] cmd = txCommand.split(TX_MSG_SEPARATOR);
        switch (cmd[0]) {
            case "ADD":
                processAddToPlayist(cmd[1], cmd[2]);
                break;
            case "EDIT":
                processEditPlaylist(cmd[1], cmd[2], cmd[3], cmd[4]);
                break;
            case "DELETE":
                processDelFromPlaylist(cmd[1], cmd[2]);
                break;

        }
    }
}