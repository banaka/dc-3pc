/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bansal
 */
public class ProcessBackground extends Thread {

    Process p;

    ProcessBackground(Process p) {
        this.p = p;
    }

    public void run() {
        String msg;

        while ((msg = p.netController.getReceivedMsgBack()) != null) {
            String[] msgFeilds = msg.split(MessageGenerator.MSG_FIELD_SEPARATOR);
            int fromProcId = Integer.parseInt(msgFeilds[MessageGenerator.processNo].trim());
            MsgContent msgContent = Enum.valueOf(MsgContent.class, msgFeilds[MessageGenerator.msgContent]);
            switch (msgContent) {
                case ABORT:
                    p.abort();
                    break;
                case CHECKALIVE:
                    p.sendMsg(MsgContent.IAMALIVE, "", fromProcId);
                    break;
                case IAMALIVE:
                    p.updateUpSet(fromProcId);
                    break;
                case STATE_REQ:
                    p.sendStateRequestRes(fromProcId);
                    break;
            }
        }
        p.CheckUpstatus();
        try {
            //thread to sleep
            this.p.netController.objectToWait.wait(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessBackground.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
