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
import ut.distcomp.framework.NetController;

/**
 *
 * @author bansal
 */
public class Process extends Thread {

    public final static String TX_MSG_SEPARATOR = "$";
//    public final static String MSG_FIELD_SEPARATOR = ";";
    int viewNumber;
    //Set to maintain the up set 
    Set<Integer> up = new HashSet<Integer>();
    ProcessState currentState;
    int procNo;
    int timeout;
    NetController netController;
    Hashtable<String, String> playlist;
    String txCommand;
    ProcessBackground processBackground;

    Process(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput) {
        this.netController = netController;
        timeout = 500;
        playlist = new Hashtable<String, String>();
        viewNumber = 0;
        processBackground = new ProcessBackground(this);
        processBackground.start();
    }

    public void run() {
        try {
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
        logMsg("Sending msg "+ outputMsg + " to "+ sendTo);
        this.netController.sendMsg(sendTo, outputMsg);
    }

    public void logMsg(String logMsg) {
        System.out.println(logMsg);

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
        synchronized (this.netController.objectToWait) {
            this.netController.objectToWait.wait(timeout);
        }
    }

    public void processVoteRequest(String command) {
        txCommand = command.split(MessageGenerator.MSG_FIELD_SEPARATOR)[2];
    }

    public void abort() {
        logMsg("ABORT");

    }

    public void commit() {
        logMsg("COMMIT");
        String[] cmd = txCommand.split(TX_MSG_SEPARATOR);
        switch (cmd[0]) {
            case "ADD":
                //processAddToPlayist();
                break;
            case "EDIT":
                break;
            case "DELETE":
                break;

        }
    }
}