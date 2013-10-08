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
    public final static String TX_MSG_SEPARATOR_ADD = "$";
    public Config config;

    Set<Integer> up = new HashSet<Integer>();
    Set<Integer> allWhoRUp = new HashSet<Integer>();
    Set<Integer> recoverUP = new HashSet<Integer>();

    ProcessState currentState;
    int procNo;
    int timeout;
    NetController netController;
    Hashtable<String, String> playlist;
    String txCommand;

    public int txNo;
    Set<Integer> txStates = new HashSet<Integer>();

    ProcessBackground processBackground;
    boolean vote;

    int totalMessageReceived;
    int totalMessageToReceive;
    int totalMessageToReceiveFrom;

    int partialCommitTo;

    String logFileName;
    String playListInstructions;
    public Logger logger;
    public int aliveTimeout;
    private final Set<MsgContent> backgroundSet = new HashSet<MsgContent>(Arrays.asList(MsgContent.IAMALIVE, MsgContent.CHECKALIVE));
    boolean recovered;
    public boolean interimCoodrinator = false;
    boolean isAnyOneOperational = false;
    boolean wasCoordinator = false;

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
        totalMessageToReceiveFrom = config.msgCountFrom;
        totalMessageReceived = 0;
        totalMessageToReceive = msgCount;
        partialCommitTo = config.partialCommitTo;

        this.netController = netController;
        this.vote = voteInput;
        this.procNo = procNo;
        timeout = config.timeout;
        playlist = new Hashtable<String, String>();
        processBackground = new ProcessBackground(this);
        coordinator = -1;
        logFileName = "Log" + procNo + ".log";
        playListInstructions = "PlayListInstruction" + procNo + ".txt";
        setLogger();
        this.config = config;
        txNo = config.txNo;
        aliveTimeout = config.aliveTimeout;

        //When starting the process initiate its playlist based of the values present in the playlist instructions
        recoverPlayList();
        recoverFromLogs();
    }

    public void sendMsgToN(MsgContent msgContent) {
        if (partialCommitTo > -1 && msgContent == MsgContent.COMMIT) {
            sendMsg(msgContent, "", partialCommitTo);
            logger.log(Level.SEVERE, "CRASHING!...Sent a Commit to process " + partialCommitTo);
            System.exit(0);
        }
        for (int i = 0; i < config.numProcesses; i++) {
            if (i != procNo)
                sendMsg(msgContent, "", i);
        }
    }

    public void sendMsgToAll(MsgContent msgContent) {
        if (partialCommitTo > -1 && msgContent == MsgContent.COMMIT) {
            sendMsg(msgContent, "", partialCommitTo);
            logger.log(Level.SEVERE, "CRASHING!...Sent a Commit to process " + partialCommitTo);
            System.exit(0);
        }

        synchronized (up) {
            Iterator<Integer> it = up.iterator();
            while (it.hasNext()) {
                int n = it.next();
                if (n != this.procNo)
                    sendMsg(msgContent, "", n);
            }
        }
    }

    /**
     * Send a data feild too in the msges being sent
     *
     * @param msgContent
     * @param data
     */
    public void sendMsgToAll(MsgContent msgContent, String data) {
        if (partialCommitTo > -1 && msgContent == MsgContent.COMMIT) {
            sendMsg(msgContent, data, partialCommitTo);
            logger.log(Level.SEVERE, "CRASHING!...Sent a Commit to process " + partialCommitTo);
            System.exit(0);
        }

        synchronized (up) {
            Iterator<Integer> it = up.iterator();
            while (it.hasNext()) {
                int n = it.next();
                if (n != this.procNo)
                    sendMsg(msgContent, data, n);
            }
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
        if (totalMessageReceived >= totalMessageToReceive) {
            logger.log(Level.SEVERE, "CRASHING!...Expected Number of messages received");
            System.exit(0);
        }
    }

    void processAddToPlaylist(String name, String url) {
        logger.log(Level.CONFIG, "Adding item to playlist - " + name + " " + url + " :D ");
        playlist.put(name, url);
    }

    void processDelFromPlaylist(String name, String url) {
        logger.log(Level.CONFIG, "Deleting item of the playlist - " + name + " " + playlist.get(name));
        if (playlist.get(name) != null)
            playlist.remove(playlist.get(name));
    }

    void processEditPlaylist(String name, String url, String newName, String newUrl) {
        logger.log(Level.CONFIG, "Editing item of the playlist - " + name + " " + url + " too " + newUrl + " " + newName);
        if (playlist.get(name) != null)
            playlist.remove(playlist.get(name));
        playlist.put(newName, newUrl);
    }

    public void sendMsg(MsgContent msgCont, String data, int sendTo) {
        String outputMsg = MsgGen.genMsg(msgCont, data, procNo);
        String margin = "";
        if (isBackground(outputMsg))
            margin = "...............................................................";
        logger.log(Level.CONFIG, margin + "Sent: " + outputMsg + " to " + sendTo);
        int delay = config.delay;
        if (isBackground(outputMsg))
            delay = 0;
        this.netController.sendMsg(sendTo, outputMsg, delay);
    }

    public void sendStateRequestRes(int procId, int fromTxNo) {
        if (txNo == fromTxNo)
            sendMsg(Enum.valueOf(MsgContent.class, currentState.msgState), "", procId);
        else if (txStates.contains(fromTxNo)) {
            sendMsg(Enum.valueOf(MsgContent.class, ProcessState.Commited.msgState), "", procId);
        } else {
            sendMsg(Enum.valueOf(MsgContent.class, ProcessState.Aborted.msgState), "", procId);
        }
    }


    /**
     * This Function Makes sure that the processes reset their state once every transaction is completed.
     */
    public void refreshState() {
        while (true) {
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
                String[] txDetail = line.split(TX_MSG_SEPARATOR, 2);
                txStates.add(Integer.parseInt(txDetail[0]));

                String[] cmd = txDetail[1].split(TX_MSG_SEPARATOR);
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

    /**
     * ************ RECOVERY STUFF STARTS **************************************
     */
    private void recoverFromLogs() {
        if (isRecoveryNeeded()) {
            recovered = true;
            recoverProcessStatus();
            processBackground.start();
        } else {
            processBackground.start();
            recovered = false;
        }
    }

    public boolean isRecoveryNeeded() {
        try {
            FileReader file = new FileReader(logFileName);
            BufferedReader reader = new BufferedReader(file);
            String line = null;
            List<String> logFile = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                logFile.add(line);
            }
            if (logFile.isEmpty())
                return false;
//           recovered = true;              //WHYYY ALWAYS TRUE?
            return extractFromLogFile(logFile);
        } catch (IOException e) {
            logger.log(Level.FINE, "Unable to read the Log File. Recovery is not needed " + e);
        }
        return false;
    }

    private boolean extractFromLogFile(List<String> logFile) {
        //Get the UP SET
        for (int i = logFile.size() - 1; i >= 0; i--) {
            if (logFile.get(i).contains("INFO:")) {
                String matcher = logFile.get(i);
                if (matcher.contains((LogMsgType.UPSET.txt))) {
                    getRecoverUpset(matcher);
                    logger.log(Level.CONFIG, "RECOVER UP SET " + recoverUP.toString());
                    break;
                }
            }
        }
        for (int i = logFile.size() - 1; i >= 0; i--)
            if (logFile.get(i).contains("INFO:"))
                if (logFile.get(i).contains((LogMsgType.START3PC.txt)))
                    wasCoordinator = true;

        //ToDo: Do something with the wasCordinator
        //ToDo: GET the TxNo From DTlog
        //Get the State in which the process died
        // a. Current state
        for (int i = logFile.size() - 1; i >= 0; i--) {
            if (logFile.get(i).contains("INFO:")) {
                String matcher = logFile.get(i);
                if (matcher.contains(LogMsgType.NEWTX.txt)) {
                    //The process is sure that COMMIT has not taken place so the process can again start waiting
                    return false;
                } else if (matcher.contains(LogMsgType.ABORT.txt) || matcher.contains(LogMsgType.REC_VOTE_REQ.txt)) {
                    currentState = ProcessState.Uncertain;
                    abort();
                    return false;
                } else if (matcher.contains(LogMsgType.PRECOMMIT.txt)) {
                    currentState = ProcessState.Commitable;
                    this.txCommand = matcher.split(MsgGen.MSG_FIELD_SEPARATOR)[1];
                    txNo = Integer.parseInt(this.txCommand.split(TX_MSG_SEPARATOR, 2)[0].trim());
                    return true;
                } else if (matcher.contains(LogMsgType.COMMIT.txt)) {
                    this.txCommand = matcher.split(MsgGen.MSG_FIELD_SEPARATOR)[1];
                    txNo = Integer.parseInt(this.txCommand.split(TX_MSG_SEPARATOR, 2)[0].trim());
                    currentState = ProcessState.Commitable;
                    commit();
                    return false;
                } else if (matcher.contains(LogMsgType.VOTEYES.txt)) {
                    currentState = ProcessState.Uncertain;
                    if (!wasCoordinator) {
                        this.txCommand = matcher.split(MsgGen.MSG_FIELD_SEPARATOR)[1];
                        txNo = Integer.parseInt(this.txCommand.split(TX_MSG_SEPARATOR, 2)[0].trim());
                        return true;
                    } else {
                        currentState = ProcessState.Uncertain;
                        abort();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public void recoverProcessStatus() {
//            //TODO :ERROR as we need the upset to go further the process cannot recover
        if (recoverUP.size() == 1) {
            //TELL ALL WHAT THE DECISION IS
            switch (currentState) {
                case Commitable:
                    commit();
                    sendMsgToN(MsgContent.COMMIT);
                    break;
                case Uncertain:
                    abort();
                    sendMsgToN(MsgContent.ABORT);
                    break;
                default:
                    logger.log(Level.WARNING, "!!!!!!!!!!!!!State not Handled!!!!!!!!!!!" + currentState);
            }
        } else {
            //Asking State Req to all present in recover
            Iterator<Integer> it = recoverUP.iterator();
            while (it.hasNext()) {
                Integer i = it.next();
                if (i != procNo)
                    sendMsg(MsgContent.STATUS_REQ, txNo + "", i);
            }
            up = recoverUP;
            startRecoverWaiting();
        }

    }

    public void startRecoverWaiting() {
        Set<Integer> receivedStatuses = new HashSet<Integer>();
//        Map<Integer, MsgContent> inputStates = new HashMap<Integer, MsgContent>();
        Map<Integer, Boolean> recoverStates = new HashMap<Integer, Boolean>();
        recoverStates.put(procNo, true);
//        boolean isAnyOneOperational = false; //!isRecoveredProcess
        while (true) {
            String msg = waitForRecoveryStatusMsg(recoverStates);
            String[] msgFields = msg.split(MsgGen.MSG_FIELD_SEPARATOR);
            int fromProcId = Integer.parseInt(msgFields[MsgGen.processNo].trim());
//            boolean isRecoveredProcess = Boolean.parseBoolean(msgFields[MsgGen.msgData]);

            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFields[MsgGen.msgContent]);

            logger.log(Level.CONFIG, msgContent.content + ";Recovery Message From :" + fromProcId + ";current state: " + currentState);
            //I will come out of this loop only if
            //I will commit or abort as per other processes say on the txNo i gave
            //If they say uncertain, that means they are also on the same Tx right now
            //So no need to check for TxNo with the received message.
            switch (msgContent) {
                case STATUS_REQ:
                    sendMsg(Enum.valueOf(MsgContent.class, currentState.msgState), Boolean.toString(recovered), fromProcId);
                    break;
                case COMMITED:
                    recoverStates.put(fromProcId, Boolean.parseBoolean(msgFields[MsgGen.msgData]));
                    commit();
                    return;
                case ABORTED:
                    recoverStates.put(fromProcId, Boolean.parseBoolean(msgFields[MsgGen.msgData]));
                    abort();
                    return;
                case UNCERTAIN:
                case COMMITABLE:
                    recoverStates.put(fromProcId, Boolean.parseBoolean(msgFields[MsgGen.msgData]));
                    if (!Boolean.parseBoolean(msgFields[MsgGen.msgData])) {
                        isAnyOneOperational = true;
                        return;
                    } else {
                        if (recoverStates.size() == recoverUP.size())
                            return;
                    }
                    break;
                case READY:
                    if (wasCoordinator){
                        abort();
                        break;
                    }
                    currentState = ProcessState.WaitForVotReq;
                    if (!Boolean.parseBoolean(msgFields[MsgGen.msgData])) {
                        isAnyOneOperational = true;
                        return;
                    }
                    break;
                case STARTED:         //IT will surely be participant and will start receiving Precommit/Abort
                    if (wasCoordinator){
                        abort();
                        break;
                    }
                    currentState = ProcessState.ReceivedVoteReq;
                    if (!Boolean.parseBoolean(msgFields[MsgGen.msgData])) {
                        isAnyOneOperational = true;
                        return;
                    }
                    break;

            }
        }
    }

    public void getRecoverUpset(String str) {
        if (str.contains((LogMsgType.UPSET.txt))) {
            String[] set = str.substring(str.lastIndexOf("[") + 1, str.lastIndexOf("]")).split(",");
            for (String j : set) {
                recoverUP.add(Integer.parseInt(j.trim()));
            }
        }
        recoverUP.add(procNo);
    }

    private String waitForRecoveryStatusMsg(Map<Integer, Boolean> recoverStates) {
        String msg;
        int counter = 0;
        while ((msg = this.netController.getReceivedMsgMain()) == null) {
            sleeping_for(10);
            counter += 10;

            if (counter >= timeout) {
                Iterator<Integer> it = recoverUP.iterator();
                while (it.hasNext()) {
                    Integer i = it.next();
                    if (i != procNo && !recoverStates.containsKey(i))
                        sendMsg(MsgContent.STATUS_REQ, txNo + "", i);
                }
                counter = 0;
            }
        }
        return msg;
    }

    /**
     * **************** RECOVERY STUFF ENDS *******************************
     */

    public void initTransaction() {
        interimCoodrinator = false;
        wasCoordinator = false;
        if (!recovered) {
            logger.info(LogMsgType.NEWTX.txt);
            currentState = ProcessState.WaitForVotReq;
        }
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
        while (globalTimeout == 0 || globalCounter.value < globalTimeout) {
            String msg = waitTillTimeoutForMessage(globalCounter, globalTimeout);
            String[] msgFields = msg.split(MsgGen.MSG_FIELD_SEPARATOR);
            int fromProcId = Integer.parseInt(msgFields[MsgGen.processNo].trim());
            if (totalMessageToReceiveFrom != -1 && fromProcId == totalMessageToReceiveFrom) {
                updateMessagesReceived();
            }
            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFields[MsgGen.msgContent]);
            boolean shouldContinue = true;
            String margin = "";
            if (isBackground(msg))
                margin = "...............................................................";
            logger.log(Level.CONFIG, margin + msgContent.content + ";From :" + fromProcId + ";current state: " + currentState);

            switch (msgContent) {
                case COMMIT:
                    commit();
                    shouldContinue = false;
                    break;
                case ABORT:
                    abort();
                    shouldContinue = false;
                    break;
                case STATUS_REQ:
                    //Only sent by recovering nodes...
                    ProcessState p = currentState;
                    int askedForTx = Integer.parseInt(msgFields[MsgGen.msgData].trim());
                    if (askedForTx != txNo) {
                        if (txStates.contains(askedForTx))
                            p = ProcessState.Commited;
                        else
                            p = ProcessState.Aborted;
                    }
                    sendMsg(Enum.valueOf(MsgContent.class, p.msgState), Boolean.toString(recovered), fromProcId);
                    //sendStatusRequestRes(fromProcId);
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
            logger.log(Level.SEVERE, "Error: Previous state " + currentState + " doesn't allow to abort!");
            return;
        }
        if (currentState != ProcessState.Aborted) {
            logger.log(Level.INFO, LogMsgType.ABORT.txt);
            currentState = ProcessState.Aborted;

            Helper.clearLogs(logFileName);
        }
        recovered = false;
    }

    public void precommit() {
        if (currentState != ProcessState.Uncertain) {
            logger.log(Level.SEVERE, "Error: Previous state to committable is not uncertain!");
            return;
        }
        logger.log(Level.INFO, LogMsgType.PRECOMMIT.txt + MsgGen.MSG_FIELD_SEPARATOR + txCommand);

        currentState = ProcessState.Commitable;
    }

    /**
     * *************************************TERMINATION PROTOCOL STUFF ************************
     */
    public int coordinator;
    public boolean isWaitingForAck = false;
    Map<Integer, MsgContent> interimStates;
    Set<Integer> interimAcks = new HashSet<Integer>();

    public boolean handleSpecificCommands(MsgContent msgContent, String[] msgFields) {
        int fromProcId = Integer.parseInt(msgFields[MsgGen.processNo].trim());
        int fromTxNo = -1;
        switch (msgContent) {
            case READY:
            case STARTED:
                if (interimCoodrinator) {
                    abort();
                    return false;
                }
                break;
            case COMMITED:
            case ABORTED:
                if (interimCoodrinator) {
                    interimStates.put(fromProcId, msgContent);
                    logger.log(Level.CONFIG, "Interim States set " + interimStates + " " + up);
                    return takeDecision();
                }
                break;
            case COMMITABLE:
            case UNCERTAIN:
                if (interimCoodrinator) {
                    interimStates.put(fromProcId, msgContent);
                    logger.log(Level.CONFIG, "Interim States set " + interimStates + " " + up);
                    if (interimStates.size() == up.size())
                        return takeDecision();
                }
                break;
            case STATE_REQ:
                fromTxNo = Integer.parseInt(msgFields[MsgGen.msgData].trim());
                updateCoordinator(fromProcId);   //Not using for NOW!
                sendStateRequestRes(fromProcId, fromTxNo);
                break;
            case VOTE_REQ:
                if (isAnyOneOperational && recovered)
                    break;      //Wait for getting an abort or commit
                currentState = ProcessState.ReceivedVoteReq;
                txCommand = new String(msgFields[MsgGen.msgData]);
                logger.info(LogMsgType.REC_VOTE_REQ.txt + MsgGen.MSG_FIELD_SEPARATOR + txCommand);
                try {
                    coordinator = fromProcId;
                    String[] ups = msgFields[MsgGen.ups].split(",");
                    for (String s : ups) {
                        synchronized (up) {
                            up.add(Integer.parseInt(s));
                        }
                    }
                    return processVoteRequest(msgFields[MsgGen.msgData], fromProcId);
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.log(Level.WARNING, "Please Send your transaction command with Vote Req!!");
                }
                break;
            case PRECOMMIT:
                //Change the state to precommit and Also send ACK to the Coordinator
                precommit();
                sendMsg(MsgContent.ACK, "", fromProcId);
                break;
            case U_R_COORDINATOR:
                //logger.log(Level.CONFIG, " " + isAnyOneOperational + " " + recovered + " " + interimCoodrinator);
                if (isAnyOneOperational && recovered)
                    break;      //Wait for getting an abort or commit
                if (currentState == ProcessState.WaitForVotReq) {
                    fromTxNo = Integer.parseInt(msgFields[MsgGen.msgData].trim());
                    if (txStates.contains(fromTxNo)) {
                        sendMsgToAll(MsgContent.COMMIT, "" + txNo);
                    } else {
                        sendMsgToAll(MsgContent.ABORT, "" + txNo);
                    }
                    break;
                }

                //update my uplist
                if (!interimCoodrinator) {
                    updateCoordinator(fromProcId);
                    coordinator = fromProcId;
                    interimCoodrinator = true;
                    interimStates = new HashMap<Integer, MsgContent>();
                    interimStates.put(procNo, Enum.valueOf(MsgContent.class, currentState.msgState));
                    //logger.log(Level.CONFIG, " " + coordinator + " " + interimStates + " " + interimCoodrinator);
                    //ask for state req
                    sendMsgToAll(MsgContent.STATE_REQ, "" + txNo);
                    //This TX no is used to find for which state the request is coming
                }
                break;
            case ACK:
                if (interimCoodrinator && currentState == ProcessState.Commitable) {
                    interimAcks.add(fromProcId);
                }
                if (interimAcks.size() == up.size()) {
                    commit();
                    sendMsgToAll(MsgContent.COMMIT);
                    isWaitingForAck = false;
                    return false;
                }
                break;
            case TIMEOUT:
                if (isAnyOneOperational && recovered)
                    break;      //Wait for getting an abort or commit
                if (interimCoodrinator) {
                    if (isWaitingForAck) {
                        commit();
                        sendMsgToN(MsgContent.COMMIT);
                        isWaitingForAck = false;
                        return false;
                    }
                    return takeDecision();
                }
                switch (currentState) {
                    case Uncertain:
                    case Commitable:
                        //Run Coordinator election
                        synchronized (up) {
                            if (coordinator != procNo)
                                up.remove(coordinator);
                            coordinator = Collections.min(up);
                        }
                        sendMsg(MsgContent.U_R_COORDINATOR, "" + txNo, coordinator);
                        break;
                    //Forever wait if Wait VoteReq state
                    case WaitForVotReq:
                        break;
                    default:
                        logger.log(Level.WARNING, "Timed out ...relistening...");
                }
                break;
            default:
                logger.log(Level.WARNING, "Not expected ::" + msgContent.content);
        }
        return true;
    }

    private void updateCoordinator(int fromProcId) {
        synchronized (up) {
            for (int i = coordinator; i < fromProcId; i++)
                up.remove(i);
        }
    }

    private boolean takeDecision() {
        if (interimStates.containsValue(MsgContent.COMMITED)) {
            commit();
            sendMsgToN(MsgContent.COMMIT);
            return false;
        } else if (interimStates.containsValue(MsgContent.ABORTED)) {
            abort();
            sendMsgToN(MsgContent.ABORT);
            return false;
        } else if (interimStates.containsValue(MsgContent.COMMITABLE)) {
            //IF anyone process is uncertain them Send precommit to all (Can be modified to be sedning it only to who are uncertain
            if (interimStates.containsValue(MsgContent.UNCERTAIN)) {
                precommit();
                sendMsgToAll(MsgContent.PRECOMMIT);
                isWaitingForAck = true;
                interimAcks.add(procNo);
                return true;
            } else {
                //Otherwise it implies everyone else is in COMMITTABLE state and we need to COMMIT
                commit();
                sendMsgToN(MsgContent.COMMIT);
                return false;
            }
        } else {
            abort();
            sendMsgToN(MsgContent.ABORT);
            return false;
        }
    }

    public boolean processVoteRequest(String command, int sendTo) {
        if (vote) {
            logger.info(LogMsgType.VOTEYES.txt + MsgGen.MSG_FIELD_SEPARATOR + txCommand);
            sendMsg(MsgContent.VoteYes, command, sendTo);
            currentState = ProcessState.Uncertain;
        } else {
            abort();
            return false;
        }
        return true;
    }

    /**
     * *************************************TERMINATION PROTOCOL STUFF ENDS************************
     */

    public void commit() {
        if (currentState != ProcessState.Commitable) {
            logger.log(Level.SEVERE, "Error: Previous state to commit is not commitable");
            return;
        }
        logger.log(Level.INFO, LogMsgType.COMMIT.txt + MsgGen.MSG_FIELD_SEPARATOR + txCommand);
        //logger.log(Level.INFO, txCommand);
        currentState = ProcessState.Commited;
        String[] txDetail = txCommand.split(TX_MSG_SEPARATOR, 2);
        String[] cmd = txDetail[1].split(TX_MSG_SEPARATOR);
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
        recovered = false;
        isAnyOneOperational = false;
        //Add the transaction message into the playlist instruction
        try {
            FileWriter fileWriter = new FileWriter(playListInstructions, true);
            BufferedWriter bufferFileWriter = new BufferedWriter(fileWriter);
            fileWriter.append(txCommand + System.getProperty("line.separator"));
            bufferFileWriter.close();

        } catch (IOException e) {
            logger.log(Level.FINE, "Unable to write into the PlaylistInstructions File. Please Check!!" + e);
        }
        Helper.clearLogs(logFileName);
        //Used to check for which transaction have we commited so that by mistake we dont send the nest state
        txStates.add(txNo);
        txNo++;
        printPlaylist();
    }

    public void printPlaylist() {
        logger.log(Level.CONFIG, "Printing Playlist for the process " + procNo);
        Iterator<Map.Entry<String, String>> it = playlist.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            logger.log(Level.CONFIG, " Song " + entry.getKey() + " URL " + entry.getValue());
        }
    }
}

class GlobalCounter {

    GlobalCounter(int value) {
        this.value = value;
    }

    public int value;
}