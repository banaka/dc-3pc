/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * @author bansal
 */
abstract public class Process {

    public final static String TX_MSG_SEPARATOR = "\\$";

    Set<Integer> up = new HashSet<Integer>();
    Set<Integer> upReply = new HashSet<Integer>();
    Set<Integer> recoverUP = new HashSet<Integer>();

    ProcessState currentState;
    int procNo;
    int timeout;
    NetController netController;
    Hashtable<String, String> playlist;
    String txCommand;
    public int txNo;
    ProcessBackground processBackground;
    boolean vote;
    int totalMessageReceived;
    int totalMessageToReceive;
    String logFileName;
    String playListInstructions;
    public Logger logger;
    public int aliveTimeout;
    private final Set<MsgContent> backgroundSet = new HashSet<MsgContent>(Arrays.asList(MsgContent.IAMALIVE, MsgContent.CHECKALIVE));
    boolean recovered;
    public boolean interimCoodrinator = false;

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

    Process(NetController netController, int procNo, Boolean voteInput, int msgCount, Config config) {
        this.netController = netController;
        this.vote = voteInput;
        this.procNo = procNo;
        timeout = config.delay;
        playlist = new Hashtable<String, String>();
        processBackground = new ProcessBackground(this);
        processBackground.start();
        totalMessageToReceive = msgCount;
        logFileName = "Log" + procNo + ".log";
        playListInstructions = "PlayListInstruction" + procNo + ".txt";
        setLogger();

        aliveTimeout = config.aliveTimeout;

        //When starting the process initiate its playlist based of the values present in the playlist instructions
        recoverPlayList();
        recoverIfNeeded();
    }

    public void sendMsgToAll(MsgContent msgContent) {
        synchronized (up) {
            Iterator<Integer> it = up.iterator();
            while (it.hasNext())
                sendMsg(msgContent, "", it.next());
        }
    }

    private void setLogger() {
        logger = Logger.getLogger("MyLog");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINER);
        Handler consoleHandler = null;
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                consoleHandler = handler;
                break;
            }
        }
        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            logger.addHandler(consoleHandler);
        }
        consoleHandler.setLevel(Level.CONFIG);


        try {
            FileHandler fileHandler = new FileHandler(logFileName, true);
            fileHandler.setLevel(Level.INFO);
            logger.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

        } catch (SecurityException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
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
        String outputMsg = MsgGen.genMsg(msgCont, data, procNo);
        logger.log(Level.CONFIG, "Sent msg " + outputMsg + " to " + sendTo);
        this.netController.sendMsg(sendTo, outputMsg);
    }

    public void sendStateRequestRes(int procId) {
        sendMsg(Enum.valueOf(MsgContent.class, currentState.msgState), "", procId);
    }

    public void updateUpSet(int procId) {
        synchronized (upReply) {
            upReply.add(procId);
        }
    }

    /**
     * This Function Makes sure that the processes reset their state once every transaction is completed.
     */
    public void refreshState() {
        while (true) {
            recovered = false;
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
                        txNo++;
                        break;
                    case EDIT:
                        processEditPlaylist(cmd[1], cmd[2], cmd[3], cmd[4]);
                        txNo++;
                        break;
                    case DELETE:
                        processDelFromPlaylist(cmd[1], cmd[2]);
                        txNo++;
                        break;
                }
            }
        } catch (FileNotFoundException e) {
            //logger.log(Level.WARNING, "Playlist doesn't seem to have been created yet!!");
            try {
                new FileWriter(playListInstructions);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "PlaylistInstructions File seems to have some issue Please Check!!" + e);
        }
    }

    public void recoverIfNeeded() {
        try {
            FileReader file = new FileReader(logFileName);
            BufferedReader reader = new BufferedReader(file);
            String line = null;
            List<String> logFile = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                logFile.add(line);
            }
            recovered = true;
            boolean notDecided = false;
            boolean deriveredUpset = false;
            for (int i = logFile.size() - 1; i >= 0; i--) {
                if (logFile.get(i).contains("INFO:")) {
                    String matcher = logFile.get(i);
                    if (!deriveredUpset && matcher.contains((LogMsgType.UPSET.txt))) {
                        getUpset(matcher);
                        deriveredUpset = true;

                        if (notDecided) {
                            System.out.println("GOT up and need to decide ");
                            break;
                        }
                    }
                    if (matcher.contains(LogMsgType.NEWTX.txt)) {
                        //The process is sure that COMMIT has not taken place so the process can again start waiting
                        return;
                    } else if (matcher.contains(LogMsgType.ABORT.txt) || matcher.contains(LogMsgType.REC_VOTE_REQ.txt)) {
                        abort();
                        return;
                    } else if (matcher.contains(LogMsgType.PRECOMMIT.txt)) {
                        currentState = ProcessState.Commitable;
                        notDecided = true;
                        if (deriveredUpset) {
                            System.out.println("Know need to decide but yet to get UP");
                            break;
                        } else continue;
                    } else if (matcher.contains(LogMsgType.COMMIT.txt)) {
                        String txcmd = logFile.get(i + 2);
                        this.txCommand = txcmd.substring(txCommand.lastIndexOf(":"));
                        commit();
                        return;
                    } else if (matcher.contains(LogMsgType.VOTEYES.txt)) {
                        currentState = ProcessState.Uncertain;
                        notDecided = true;
                        if (deriveredUpset)
                            break;
                        else continue;
                    }

                }
            }
            if (notDecided) {
                //a. Cannot recover untill we have up a subet of recoversubset
                //b. Cannot recover untill we have recoverUp non zero, non null
                //c. IF recover is equal to itself then just ACT on the state you have

                //
                if (recoverUP == null || recoverUP.size() == 0) {
                    System.out.println("Need to decide but do not have anything in UP");
                    //cannot have recovery
                    //TODO :ERROR as we need the upset to go further the process cannot recover
                } else if (recoverUP.size() == 1 && recoverUP.iterator().next() == procNo) {
                    switch (currentState) {
                        case Commitable:
                            commit();
                            break;
                        case Uncertain:
                            abort();
                            break;
                    }
                } else {
                    //Check that all in up have come recover up have come up
                    while (!recoverUP.containsAll(up)) {
                        sleeping_for(10);
                    }
                    synchronized (up) {
                        Iterator<Integer> it = up.iterator();
                        while (it.hasNext()) {
                            Integer i = it.next();
                            if (i != procNo)
                                sendMsg(MsgContent.STATUS_REQ, "", i);
                        }
                    }
                }

                //do not work until we get response form someone
                //sendMsg(MsgContent.STATUS_REQ, "", up.iterator().next());
                //Wait for response otherwise ?????
                startListening(0);
            }

        } catch (IOException e) {
            //logger.log(Level.FINE, "Unable to read the Log File. Recovery is not needed " + e);
            return;
        }

    }

    public void getUpset(String str) {
        if (str.contains((LogMsgType.UPSET.txt))) {
            String[] set = str.substring(str.lastIndexOf("[") + 1, str.lastIndexOf("]")).split(",");
            for (String j : set) {
                recoverUP.add(Integer.parseInt(j.trim()));
            }
        }
    }

    public void initTransaction() {
        logger.info(LogMsgType.NEWTX.txt);
        interimCoodrinator = false;
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
            sleeping_for(10);
            counter += 10;

            if (counter >= timeout) {
                msg = procNo + ";TIMEOUT";
                globalCounter.value = 0;
                counter = 0;
                break;
            }
        }
        if (globalTimeout != 0 || isBackground(msg)) {
            globalCounter.value += counter;
        }
        return msg;
    }

    private boolean isBackground(String msg) {
        String[] msgFields = msg.split(MsgGen.MSG_FIELD_SEPARATOR);
        MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFields[MsgGen.msgContent]);
        return backgroundSet.contains(msgContent);
    }

    public void startListening(int globalTimeout) {
        GlobalCounter globalCounter = new GlobalCounter(0);
//        String lastMsg = "";
        while (globalTimeout == 0 || globalCounter.value < globalTimeout) {
            String msg = waitTillTimeoutForMessage(globalCounter, globalTimeout);
//            lastMsg = msg;
            String[] msgFields = msg.split(MsgGen.MSG_FIELD_SEPARATOR);
            int fromProcId = Integer.parseInt(msgFields[MsgGen.processNo].trim());
            if (fromProcId != procNo) {
                updateMessagesReceived();
            }
            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFields[MsgGen.msgContent]);
            boolean shouldContinue = true;
            String margin = "";
            if(isBackground(msgContent.content))
                margin = "...............................................................";
            logger.log(Level.CONFIG, margin+msgContent.content+";From :"+fromProcId+";current state: "+currentState);

            switch (msgContent) {
                case COMMIT:
                    commit();
                    shouldContinue = false;
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
        if (currentState == ProcessState.WaitForVotReq || currentState == ProcessState.Commited) {
            logger.log(Level.SEVERE, "Error: Previous state " + currentState.msgState + " doesn't allow to abort!");
            return;
        }
        if (currentState != ProcessState.Aborted) {
            logger.log(Level.INFO, LogMsgType.ABORT.txt);
            currentState = ProcessState.Aborted;

            Helper.clearLogs(logFileName);
        }
    }

    public void precommit() {
        if (currentState != ProcessState.Uncertain) {
            logger.log(Level.SEVERE, "Error: Previous state to committable is not uncertain!");
            return;
        }
        logger.log(Level.INFO, LogMsgType.PRECOMMIT.txt);

        currentState = ProcessState.Commitable;
    }

    abstract public boolean handleSpecificCommands(MsgContent command, String[] msgFields);

    public void commit() {
        if (currentState != ProcessState.Commitable) {
            logger.log(Level.SEVERE, "Error: Previous state to commit is not commitable");
            return;
        }
        logger.log(Level.INFO, LogMsgType.COMMIT.txt);
        logger.log(Level.INFO, txCommand);
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
        Helper.clearLogs(logFileName);

    }
}

class GlobalCounter {

    GlobalCounter(int value) {
        this.value = value;
    }

    public int value;
}