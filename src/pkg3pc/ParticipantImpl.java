/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

import ut.distcomp.framework.NetController;

/**
 *
 * @author bansal
 */
public class ParticipantImpl extends Process implements Participant {
    ParticipantImpl(NetController netController, int procNo, ProcessState stateToDie, Boolean voteInput){
        super(netController, procNo, stateToDie, voteInput);        
    }
    
    public void processVoteRequest(){
        
        
    }
    
    public void processStateRequest(){
        
    }
    
    
    public void precommit(){
        
    }
    
}
