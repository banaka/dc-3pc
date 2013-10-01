/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import static pkg3pc.MsgContent.ABORTED;
import static pkg3pc.MsgContent.ACK;
import static pkg3pc.MsgContent.COMMIT;
import static pkg3pc.MsgContent.COMMITABLE;
import static pkg3pc.MsgContent.COMMITED;
import static pkg3pc.MsgContent.PRECOMMIT;
import static pkg3pc.MsgContent.STATE_REQ;
import static pkg3pc.MsgContent.Uncertain;
import static pkg3pc.MsgContent.VoteNo;

/**
 *
 * @author bansal
 */
public class MessageGenerator {

    public static int processNo = 0;
    public static int msgContent = 1;
    public static int msgData = 2;
    public final static String MSG_FIELD_SEPARATOR = ";";

    public static String genMsg(MsgContent msgtype, String data, int senderProcessNo) {
        StringBuilder output = new StringBuilder(senderProcessNo + "; ");
        output.append(msgtype.content).append(data);
//        switch (msgtype) {
//
//            case VOTE_REQ:
//                return output + MsgContent.VOTE_REQ.content + data;
//
//            case ABORT:
//                return output
//                        //+ MsgType.Decision.txt
//                        + MsgContent.ABORT.content + data;
//            case COMMIT:
//                return output
//                        //+ MsgType.Decision.txt
//                        + MsgContent.COMMIT.content + data;
//
//            case ABORTED:
//                return output
//                        + MsgType.Response.txt
//                        + MsgContent.ABORTED.content;
//
//            case COMMITABLE:
//                return output
//                        + MsgType.Response.txt
//                        + MsgContent.COMMITABLE.content;
//
//            case COMMITED:
//                return output
//                        + MsgType.Response.txt
//                        + MsgContent.COMMITED.content;
//
//            case Uncertain:
//                return output
//                        + MsgType.Response.txt
//                        + MsgContent.Uncertain.content;
//
//            case PRECOMMIT:
//                return output
//                        + MsgType.ExpectRsponse.txt
//                        + MsgContent.PRECOMMIT.content;
//
//            case STATE_REQ:
//                return output
//                        + MsgType.ExpectRsponse.txt
//                        + MsgContent.STATE_REQ.content;
//
//            case VoteNo:
//                return output
//                        + MsgType.Response.txt
//                        + MsgContent.VoteNo.content;
//
//            case VoteYes:
//                return output
//                        + MsgType.Response.txt
//                        + MsgContent.VoteYes.content;
//
//            case U_R_COORDINATOR:
//                return output
//                        + MsgType.Decision.txt
//                        + MsgContent.U_R_COORDINATOR.content;
//            case ACK:
//                return output
//                        + MsgType.Response.txt
//                        + MsgContent.ACK.content;
//
//
//        }
        return output.toString(); //will be called in case of vote req that function needs something else too 
    }
}
