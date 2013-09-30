/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import ut.distcomp.framework.NetController;

/**
 *
 * @author bansal
 */
public class Process extends Thread{

    public final static String TX_MSG_SEPARATOR = "$";
    public final static String MSG_FIELD_SEPARATOR = ";";
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
        playlist = new Hashtable<String, String>();
        viewNumber = 0;
        processBackground = new ProcessBackground(this);
        processBackground.start();
    }

    public void run() {
        refershState();
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

    public void sendMsg(String msg) {
    }

    public void logMsg(String logMsg) {
        System.out.println(logMsg);

    }

    public void refershState() {
        /*ToDo: Clear my queue both back and main */
        currentState = ProcessState.WaitForVotReq;
        initTransaction();

    }

    public void initTransaction() {
        logMsg("NEW TX - WAITING FOR VOTE REQ");
    }

    public void processVoteRequest(String command) {
        txCommand = command.split(MSG_FIELD_SEPARATOR)[2];
    }

    public void abort() {
        logMsg("ABORT");

    }

    public void commit() {
        logMsg("COMMIT");

    }
}