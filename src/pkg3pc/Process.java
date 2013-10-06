/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import ut.distcomp.framework.NetController;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
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
    String logFileName;
    String playListInstructions;
    public Logger logger;

    public enum playlistCommand {

        ADD(0),
        EDIT(1),
        DELETE(2);
        private final int value;

        playlistCommand(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
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

        logFileName = "Log" + procNo + ".log";
        playListInstructions = "PlayListInstruction" + procNo + ".txt";
        logger = Logger.getLogger("MyLog");
        logger.setLevel(Level.CONFIG);

        try {
            FileHandler fh = new FileHandler(logFileName, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //When starting the process initiate its playlist based of the values present in the playlist instructions
        recoverPlayList();

    }

    void changeState(ProcessState ps) {
        if (ps == endState) {
            System.exit(0);
        } else {
            currentState = ps;
        }
    }

    void updateMessagesReceived() {
        totalMessageReceived++;
        if (totalMessageToReceive != 0 && totalMessageReceived >= totalMessageToReceive) {
            System.exit(0);
        }
    }

    void processAddToPlaylist(String name, String url) {
        logger.log(Level.CONFIG, "Adding item to playlist - " + name + " " + url + " :D ");
        playlist.put(name, url);
    }

    void processDelFromPlaylist(String name, String url) {
        logger.log(Level.CONFIG, "Deleting item of the playlist - " + name + " " + playlist.get(name));
        playlist.remove(playlist.get(name));
    }

    void processEditPlaylist(String name, String url, String newName, String newUrl) {
        logger.log(Level.CONFIG, "Editing item of the playlist - " + name + " " + url + " too " + newUrl + " " + newName);
        playlist.remove(playlist.get(name));
        playlist.put(newName, newUrl);
    }

    public void sendMsg(MsgContent msgCont, String data, int sendTo) {
        String outputMsg = MessageGenerator.genMsg(msgCont, data, procNo);
        this.netController.sendMsg(sendTo, outputMsg);
        logger.log(Level.CONFIG, "Sent msg " + outputMsg + " to " + sendTo);
    }

    public void sendStateRequestRes(int procId) {
        sendMsg(Enum.valueOf(MsgContent.class, currentState.msgState),"",procId);
    }

    public void updateUpSet(int procId) {
        up.add(procId);
    }

    public void refreshState() {
        while (true) {
            //ToDo: Read my DtLog and check whether init transaction or recover
            isRecoveryNeeded();
            /*ToDo: Clear my queue both back and main */
            // If init transaction //
            initTransaction();
        }
    }

    public void recoverPlayList() {
        try {
            FileReader file = new FileReader(playListInstructions);
            BufferedReader reader = new BufferedReader(file);
            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] cmd = line.split(TX_MSG_SEPARATOR);
                switch (playlistCommand.valueOf(cmd[0])) {
                    case ADD:
                        processAddToPlaylist(cmd[1], cmd[2]);
                        break;
                    case EDIT:
                        processEditPlaylist(cmd[1], cmd[2], cmd[3], cmd[4]);
                        break;
                    case DELETE:
                        processDelFromPlaylist(cmd[1], cmd[2]);
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Playlist Doesnt seem to have been created yet!!" + e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "PlaylistInstructions File seems to have some issue Please Check!!" + e);
        }
    }

    public void isRecoveryNeeded() {
        try {
            FileReader file = new FileReader(logFileName);
            BufferedReader reader = new BufferedReader(file);
            String line = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("INFO")) {
                    String msg = line.substring(line.lastIndexOf(":"));
                    LogMsgType logMsg = Enum.valueOf(LogMsgType.class, msg);
                    switch (logMsg) {
                        case COMMIT:
                            currentState = ProcessState.Commited;
                            //TODO need to set the command
                            commit();
                            break;
                        case PRECOMMIT:
                            currentState = ProcessState.Commitable;
                            break;
                        case ABORT:
                            break;
                        case NEWTX:
                            break;
                        case REC_VOTE_REQ:
                            break;
                        case VOTEYES:
                            break;
                        case START3PC:
                            break;

                    }

                }

            }
        } catch (IOException e) {
            logger.log(Level.FINE, "Unable to read the Log File. Please Check!!" + e);
        }

    }

    public void initTransaction() {
        logger.info(LogMsgType.NEWTX.txt);
        currentState = ProcessState.WaitForVotReq;
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
            if (counter >= timeout) {
                msg = procNo + ";TIMEOUT";
                break;
            }
            sleeping_for(10);
            counter += 10;
        }
        if (globalTimeout != 0) {
            globalCounter.value += counter;
        }
        return msg;
    }

    public void startListening(int globalTimeout) {
        GlobalCounter globalCounter = new GlobalCounter(0);
        while (globalTimeout == 0 || globalCounter.value < globalTimeout) {
            String msg = waitTillTimeoutForMessage(globalCounter, globalTimeout);
            String[] msgFields = msg.split(MessageGenerator.MSG_FIELD_SEPARATOR);
            logger.log(Level.CONFIG, "Received a message!!! - " + msg);
            int fromProcId = Integer.parseInt(msgFields[MessageGenerator.processNo].trim());
            if (fromProcId != procNo) {
                updateMessagesReceived();
            }
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
                case CHECKALIVE:
                    sendMsg(MsgContent.IAMALIVE, "", fromProcId);
                    break;
                case IAMALIVE:
                    updateUpSet(fromProcId);
                    break;
                default:
                    shouldContinue = handleSpecificCommands(msgContent, msgFields);
            }
            if (!shouldContinue) {
                break;
            }
        }
    }

    public void abort() {
        logger.log(Level.INFO, LogMsgType.ABORT.txt);
        currentState = ProcessState.Aborted;

        clearLogs();
    }

    abstract public void precommit();

    abstract public boolean handleSpecificCommands(MsgContent command, String[] msgFields);

    public void commit() {
        logger.log(Level.INFO, LogMsgType.COMMIT.txt);

        currentState = ProcessState.Commited;
        String[] cmd = txCommand.split(TX_MSG_SEPARATOR);
        switch (playlistCommand.valueOf(cmd[0])) {
            case ADD:
                processAddToPlaylist(cmd[1], cmd[2]);
                break;
            case EDIT:
                processEditPlaylist(cmd[1], cmd[2], cmd[3], cmd[4]);
                break;
            case DELETE:
                processDelFromPlaylist(cmd[1], cmd[2]);
                break;

        }

        //Add the transaction message into the playlist instruction
        try {
            FileWriter fileWriter = new FileWriter(playListInstructions, true);
            BufferedWriter bufferFileWriter = new BufferedWriter(fileWriter);
            fileWriter.append(txCommand + "\n");
            bufferFileWriter.close();

        } catch (IOException e) {
            logger.log(Level.FINE, "Unable to write into the PlaylistInstructions File. Please Check!!" + e);
        }
        clearLogs();

    }

    public void clearLogs() {
        try {
            FileWriter fileWriter = new FileWriter(logFileName);
            fileWriter.write("");
            fileWriter.close();
        } catch (IOException x) {
            logger.log(Level.SEVERE, "Error while deleting the File " + logFileName + " " + x.getStackTrace());
        }
    }
}

class GlobalCounter {

    GlobalCounter(int value) {
        this.value = value;
    }

    public int value;
}