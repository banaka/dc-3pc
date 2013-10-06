/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author bansal
 */
public class ProcessBackground extends Thread {

    Process p;

    ProcessBackground(Process p) {
        this.p = p;
    }


    public void run() {
        while (true) {
            Iterator<Integer> it = p.up.iterator();
            while (it.hasNext())
                p.sendMsg(MsgContent.CHECKALIVE, "", it.next());
            p.sleeping_for(p.aliveTimeout);
            p.upReply.add(p.procNo);
            p.logger.log(Level.CONFIG, "Old UpSet:" + p.up.toString() + " Current State :" + p.currentState);
            p.logger.log(Level.INFO, LogMsgType.UPSET + "  " + p.upReply.toString());
            p.up = p.upReply;
        }
    }
}
