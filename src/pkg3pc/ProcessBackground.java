/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author bansal
 */
public class ProcessBackground extends Thread {

    Process p;

    ProcessBackground(Process p) {
        this.p = p;
    }

//    public String waitTillTimeoutForMessage(GlobalCounter globalCounter, int globalTimeout) {
//        String msg;
//        int counter = globalCounter.value;
//        while ((msg = p.netController.getReceivedMsgBack()) == null) {
//            if (counter >= p.timeout) {
//                msg = p.procNo + ";TIMEOUT";
//                break;
//            }
//            p.sleeping_for(10);
//            counter += 10;
//        }
//        if (globalTimeout != 0) {
//            globalCounter.value += counter;
//        }
//        return msg;
//    }

//    public void startListening(int globalTimeout) {
//        GlobalCounter globalCounter = new GlobalCounter(0);
//        while (globalTimeout == 0 || globalCounter.value < globalTimeout) {
//            String msg = waitTillTimeoutForMessage(globalCounter, globalTimeout);
//            String[] msgFields = msg.split(MsgGen.MSG_FIELD_SEPARATOR);
//            p.logger.log(Level.CONFIG, "Received a message!!! - " + msg);
//            int fromProcId = Integer.parseInt(msgFields[MsgGen.processNo].trim());
//
//            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFields[MsgGen.msgContent]);
//            boolean shouldContinue = true;
//            switch (msgContent) {
//                case ABORT:
//                    p.abort();
//                    break;
//             }
//        }
//    }

    public void run() {
        while(true) {
            Iterator<Integer> it = p.up.iterator();
            while(it.hasNext())
                p.sendMsg(MsgContent.CHECKALIVE,"",it.next());
            p.up.clear();
            p.sleeping_for(2000);
            p.logger.log(Level.WARNING, p.up.toString());
        }
    }
//        startListening(0);
//        p.CheckUpstatus();
            //Syncronize the blocks where the notify and wait functions have been called
}
