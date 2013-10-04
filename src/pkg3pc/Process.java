/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.NetController;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 *
 * @author bansal
 */
abstract public class Process {

    public final static String TX_MSG_SEPARATOR = "\\$";
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

    public enum playlistCommand {
        ADD(0),
        EDIT(1),
        DELETE(2);

        private final int value;

        playlistCommand(int value) { this.value = value; }
        public int value() { return value; }
    }

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

//    public void run() {
//        try {
//            // Comment is only for debugging purposes...
//            //this.sendMsg(MsgContent.CHECKALIVE, "", 1);
//            refreshState();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    void processAddToPlayist(String name, String url) {
        logMsg("ADDDING item to playlist - "+name+" "+url+" :D ");
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

    public void refreshState() {
        //ToDo: Read my DtLog and check whether init transaction or recover
        /*ToDo: Clear my queue both back and main */
        // If init transaction //
        currentState = ProcessState.WaitForVotReq;
        initTransaction();
    }

    public void initTransaction() {
        logMsg("NEW TX - WAITING FOR VOTE REQ");
       // synchronized (this.netController.objectToWait) {
            //this.netController.objectToWait.wait(timeout);
        //}
        startListening();
    }

    public void sleeping_for(int milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startListening() {
        while(true){
            String msg;
            while ((msg = this.netController.getReceivedMsgMain()) == null)
                sleeping_for(10);
            String[] msgFeilds = msg.split(MessageGenerator.MSG_FIELD_SEPARATOR);
            logMsg("Received a message!!! - "+msg);
            int fromProcId = Integer.parseInt(msgFeilds[MessageGenerator.processNo].trim());
            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFeilds[MessageGenerator.msgContent]);
            switch (msgContent) {
                case COMMIT:
                    commit();
                    break;
                case ABORT:
                    abort();
                    break;
                default:
                    handleSpecificCommands(msgContent, msgFeilds);
            }
        }
    }
    public void processVoteRequest(String command, int sendTo) {
        txCommand = command;
        if (vote) {
            sendMsg(MsgContent.VoteYes, command, sendTo);
        } else {
            sendMsg(MsgContent.VoteNo, command, sendTo);
        }
        logMsg("SENT VOTE - "+vote);

    }

    public void abort() {
        logMsg("ABORT :(");
    }

    abstract public void precommit();

    abstract public void handleSpecificCommands(MsgContent command, String[] msgFields);

    public void commit() {
        logMsg("COMMIT");
        String[] cmd = txCommand.split(TX_MSG_SEPARATOR);
        switch (playlistCommand.valueOf(cmd[0])) {
            case ADD:
                processAddToPlayist(cmd[1], cmd[2]);
                break;
            case EDIT:
                processEditPlaylist(cmd[1], cmd[2], cmd[3], cmd[4]);
                break;
            case DELETE:
                processDelFromPlaylist(cmd[1], cmd[2]);
                break;

        }
    }
}