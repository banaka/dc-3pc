/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public interface Coordinator { 
     public void sendVoteRequests();
     
     public void processVotes() throws InterruptedException;
    
    
}
