/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public enum MsgType {

    Decision("Decision"),
    Response("Response"),
    ExpectRsponse("ExpectResponse");
    
    public String txt;

     MsgType(String type) {
        this.txt = "MsgType: "+type+" ;";
    }
    
}
