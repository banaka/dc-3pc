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

    int totalMessageReceived;
    int totalMessageToReceive;

    public enum playlistCommand {
        ADD(0),
        EDIT(1),
        DELETE(2);

        private final int value;

        playlistCommand(int value) { this.value = value; }
        public int value() { return value; }
    }

    Process(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput, int msgCount) {
        this.netController = netController;
        this.vote = voteInput;
        this.procNo = procNo;
        this.endState = stateToDie;
        timeout = 5000;
        playlist = new Hashtable<String, String>();
        viewNumber = 0;
        processBackground = new ProcessBackground(this);
        processBackground.start();
        totalMessageToReceive = msgCount;
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
    void changeState(ProcessState ps){
        if(ps == endState)
            System.exit(0);
        else
            currentState = ps;
    }

    void updateMessagesReceived(){
        totalMessageReceived++;
        if(totalMessageToReceive != 0 && totalMessageReceived >= totalMessageToReceive)
            System.exit(0);
    }
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
        while(true) {
        //ToDo: Read my DtLog and check whether init transaction or recover
        /*ToDo: Clear my queue both back and main */
        // If init transaction //
        initTransaction();
        }
    }

    public void initTransaction() {
        logMsg("NEW TX - WAITING FOR VOTE REQ");
        currentState = ProcessState.WaitForVotReq;
       // synchronized (this.netController.objectToWait) {
            //this.netController.objectToWait.wait(timeout);
        //}
        startListening(0);
    }

    public void sleeping_for(int milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String waitTillTimeoutForMessage(GlobalCounter globalCounter, int globalTimeout) {
        String msg;
        int counter = globalCounter.value;
        while ((msg = this.netController.getReceivedMsgMain()) == null) {
            if(counter >= timeout){
                msg = procNo + ";TIMEOUT";
                break;
            }
            sleeping_for(10);
            counter += 10;
        }
        if(globalTimeout != 0)
            globalCounter.value += counter;
        return msg;
    }

    public void startListening(int globalTimeout) {
        GlobalCounter globalCounter = new GlobalCounter(0);
        while(globalTimeout == 0 || globalCounter.value < globalTimeout){
            String msg = waitTillTimeoutForMessage(globalCounter, globalTimeout);
            String[] msgFields = msg.split(MessageGenerator.MSG_FIELD_SEPARATOR);
            logMsg("Received a message!!! - "+msg);
            int fromProcId = Integer.parseInt(msgFields[MessageGenerator.processNo].trim());
            if(fromProcId != procNo)
                updateMessagesReceived();
            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFields[MessageGenerator.msgContent]);
            boolean shouldContinue = true;
            switch (msgContent) {
                case COMMIT:
                    commit();
                    break;
                case ABORT:
                    abort();
                    shouldContinue = false;
                    break;
                default:
                    shouldContinue = handleSpecificCommands(msgContent, msgFields);
            }
            if(!shouldContinue)
                break;
        }
    }

    public void abort() {
        currentState = ProcessState.LoggedAbort;
        logMsg("ABORT :(");
        currentState = ProcessState.Abort;
        //ToDo: Prepare for new transaction
    }

    abstract public void precommit();

    abstract public boolean handleSpecificCommands(MsgContent command, String[] msgFields);

    public void commit() {
        currentState = ProcessState.LoggedCommit;
        logMsg("COMMIT");
        currentState = ProcessState.Commit;
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

class GlobalCounter {
    GlobalCounter(int value){
        this.value = value;
    }
    public int value;
}