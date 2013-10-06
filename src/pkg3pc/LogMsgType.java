/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 * @author bansal
 */
public enum LogMsgType {

    COMMIT("COMMIT"),
    ABORT("ABORT"),
    PRECOMMIT("PRECOMMIT"),
    START3PC("START 3PC"),
    VOTEYES("SENT VOTE - YES"),
    REC_VOTE_REQ("RECIEVED VOTE REQ"),
    NEWTX("NEW TX - WAITING FOR VOTE REQ"),
    UPSET("CURRENT UP SET");

    public String txt;

    LogMsgType(String txt) {
        this.txt = txt;
    }

}
