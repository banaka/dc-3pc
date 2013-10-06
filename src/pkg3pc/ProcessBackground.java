/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.util.Iterator;

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
        while(true) {
            Iterator<Integer> it = p.up.iterator();
            while(it.hasNext())
                p.sendMsg(MsgContent.CHECKALIVE,"",it.next());
            p.up.clear();
            p.up.add(p.procNo);
            p.sleeping_for(p.aliveTimeout);
//            p.logger.log(Level.WARNING, p.up.toString());
        }
    }
}
