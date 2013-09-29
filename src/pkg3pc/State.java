/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public enum State {
    //Partcipants States
    GotVoteRequest,
    AfterVoteYes,
    AfterVoteNo,
    Uncertain,
    Precommit,
    SedningACK,
    Abort,
    Commit,
    
    //Other states possible
    GotStateReq,
    
    //Coordinator States
    Coordinator,
    VoteReq,
    VoteCounting,
    DecidePreCommit,
    DecideCommit,
    DecideAbort,
    
    //New Coordinator States
    interimCoordinator,
    StateReq,
    StateEvaluation
    //The next states will be same as that of Coordinator 
    //DecidePreCommit,
    //DecideCommit,
    //DecideAbort,
    
    ;    
}
