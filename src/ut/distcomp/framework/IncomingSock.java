/**
 * This code may be modified and used for non-commercial purposes as long as
 * attribution is maintained.
 *
 * @author: Isaac Levy
 */
package ut.distcomp.framework;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import pkg3pc.MessageGenerator;
import pkg3pc.Process;

public class IncomingSock extends Thread {

    final static String MSG_SEP = "&";
    Socket sock;
    InputStream in;
    private volatile boolean shutdownSet;
    private final ConcurrentLinkedQueue<String> queueBack;
    private final ConcurrentLinkedQueue<String> queueMain;
    int bytesLastChecked = 0;
    List<String> msgsMainList;
    NetController netController;

    protected IncomingSock(Socket sock, List<String> msgsMainList, NetController netController1) throws IOException {
        this(sock);
        this.msgsMainList = msgsMainList;
        this.netController = netController1;
    }

    protected IncomingSock(Socket sock) throws IOException {
        this.sock = sock;
        in = new BufferedInputStream(sock.getInputStream());
        //in = sock.getInputStream();
        sock.shutdownOutput();
        queueMain = new ConcurrentLinkedQueue<String>();
        queueBack = new ConcurrentLinkedQueue<String>();
    }

    protected String getMsgsBack() {
        String msg = null;
        String tmp;
        while ((tmp = queueBack.poll()) != null) {
            String[] arr = tmp.split(";");
            if (msgsMainList.contains(arr[1])) {
                queueMain.offer(tmp);
            } else {
                msg = tmp;
                break;
            }
        }
        return msg;
    }

    protected String getMsgsMain() {
        return queueMain.poll();
    }

    public void run() {
        while (!shutdownSet) {
            synchronized (this) {
                try {
                    int avail = in.available();
                    if (avail == bytesLastChecked) {
                        sleep(10);
                    } else {
                        in.mark(avail);
                        byte[] data = new byte[avail];
                        in.read(data);
                        String dataStr = new String(data);
                        int curPtr = 0;
                        int curIdx;
                        while ((curIdx = dataStr.indexOf(MSG_SEP, curPtr)) != -1) {
                            String tmp = dataStr.substring(curPtr, curIdx);

                            String[] arr = tmp.split(MessageGenerator.MSG_FIELD_SEPARATOR);
                            if (msgsMainList.contains(arr[MessageGenerator.msgContent])) {
                                queueMain.offer(tmp);
                                this.netController.objectToWait.notify();
                            } else {
                                queueBack.offer(tmp);
                            }
                            curPtr = curIdx + 1;
                        }
                        in.reset();
                        in.skip(curPtr);
                        bytesLastChecked = avail - curPtr;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        shutdown();
    }

    public void cleanShutdown() {
        shutdownSet = true;
    }

    protected void shutdown() {
        try {
            in.close();
        } catch (IOException e) {
        }

        try {
            sock.shutdownInput();
            sock.close();
        } catch (IOException e) {
        }
    }
}
