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
            p.upReply.clear();
            p.sleeping_for(p.aliveTimeout);
            synchronized (p.upReply) {
                p.upReply.add(p.procNo);
            }
            p.logger.log(Level.CONFIG, "Old UpSet:" + p.up.toString() + " Current State :" + p.currentState);
            p.logger.log(Level.INFO, LogMsgType.UPSET.txt + "  " + p.upReply.toString());
            synchronized (p.up) {
                p.up = p.upReply;
            }
        }
    }
}
