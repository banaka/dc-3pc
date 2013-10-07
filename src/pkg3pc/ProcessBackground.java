/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
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
//            p.sendMsgToAll(MsgContent.CHECKALIVE);
            Set<Integer> oldUp = p.up;
            Set<Integer> oldAllWhoRUp = p.allWhoRUp;

            synchronized (p.up) {
                Set<Integer> temp = new HashSet<Integer>();
                for (int i=0; i< p.config.numProcesses;i++){
                    if(!available(8080+i))
                        temp.add(i);
                }
                temp.add(p.procNo);
                p.up.add(p.procNo);
                p.allWhoRUp = temp;
                p.up.retainAll(p.allWhoRUp);
            }

//            synchronized (p.up) {
//                Set<Integer> temp = new HashSet<Integer>();
//                Iterator<Integer> it = p.up.iterator();
//                while(it.hasNext()){
//                    int proc = it.next();
//                    if(!available(8080+proc))
//                        temp.add(proc);
////                    else if(!available(8080+proc))
////                        temp.add(proc);
//                }
//                temp.add(p.procNo);
//                p.up = temp;
//            }

//            synchronized (p.upReply) {
//                p.upReply.clear();
//            }
            p.sleeping_for(p.aliveTimeout);
//            synchronized (p.upReply) {
//                p.upReply.add(p.procNo);
//            }
            String margin = "...............................................................";
            p.logger.log(Level.CONFIG, margin + "Old UpSet:" + oldUp + " Current State :" + p.currentState);
            p.logger.log(Level.INFO, margin+ LogMsgType.UPSET.txt + "  " + p.up);
            p.logger.log(Level.CONFIG, margin + "Old allWhoRUp:" + oldAllWhoRUp );
            p.logger.log(Level.CONFIG, margin+ LogMsgType.UPSET.txt + "  " + p.allWhoRUp);
//            synchronized (p.up) {
//                p.up = p.upReply;
//            }
        }
    }

    public static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                /* should not be thrown */
                }
            }
        }

        return false;
    }

}
