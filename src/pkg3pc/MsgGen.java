/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public class MsgGen {

    //using these values to ensure that we can change the order to the msg components
    public static int processNo = 0;
    public static int msgContent = 1;
    public static int msgData = 2;
    public static int ups = 3;

    public final static String MSG_FIELD_SEPARATOR = ";";

    public static String genMsg(MsgContent msgtype, String data, int senderProcessNo) {
        StringBuilder output = new StringBuilder(senderProcessNo + MSG_FIELD_SEPARATOR);
        output.append(msgtype.content + MSG_FIELD_SEPARATOR).append(data + MSG_FIELD_SEPARATOR);
        return output.toString();
    }
}
