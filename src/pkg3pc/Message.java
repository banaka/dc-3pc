/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public class Message {
    
    MsgContent content;
    MsgType type; 
    int senderProcessor; 
    int recieverProcessor;
    String data;  

    public Message(MsgContent content, MsgType type, int senderProcessor, String data) {
        this.content = content;
        this.type = type;
        this.senderProcessor = senderProcessor;
        this.data = data;
    }

    public Message(MsgContent content, MsgType type, int senderProcessor) {
        this.content = content;
        this.type = type;
        this.senderProcessor = senderProcessor;
    }

    
    public MsgContent getContent() {
        return content;
    }

    public void setContent(MsgContent content) {
        this.content = content;
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public int getSenderProcessor() {
        return senderProcessor;
    }

    public void setSenderProcessor(int senderProcessor) {
        this.senderProcessor = senderProcessor;
    }

    public int getRecieverProcessor() {
        return recieverProcessor;
    }

    public void setRecieverProcessor(int recieverProcessor) {
        this.recieverProcessor = recieverProcessor;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "Message{" + "content=" + content + ", type=" + type + ", senderProcessor=" + senderProcessor + ", recieverProcessor=" + recieverProcessor + ", data=" + data + '}';
    }
    
}
