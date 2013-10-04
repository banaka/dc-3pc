/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public interface Participant {
    
    public void processVoteRequest(String command, int from);
    
    public void processStateRequest();

}
