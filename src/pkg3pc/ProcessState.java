/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg3pc;

/**
 *
 * @author bansal
 */
public enum ProcessState {
    //Partcipants States
    WaitForVotReq("READY"),
    Uncertain("UNCERTAIN"),
    Commitable("COMMITABLE"),
//    SentACK,
    
//    LoggedAbort("ABORTED"),
    Aborted("ABORTED"),
//    LoggedCommit("COMMITED"),
    Commited("COMMITED"),
    ReceivedVoteReq("STARTED"),
    //Coordinator States
    VoteReq("STARTED"),
//    VoteCounting("")

//    //New Coordinator States
//    interimCoordinator,
//    StateReq,
//    StateEvaluation
    ;
    public String msgState;

    ProcessState(String msgState) {
        this.msgState = msgState;
    }
}
