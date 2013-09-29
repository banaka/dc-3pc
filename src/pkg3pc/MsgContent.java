package pkg3pc;

/**
 *
 * @author bansal
 */
public enum MsgContent {
    //decision types
    ABORT("ABORT"),
    COMMIT("COMMIT"),

    //Requests which expect responses 
    VOTE_REQ("VOTE_REQ"), //should include partcipant list 
    STATE_REQ("STATE_REQ"), //can include up list
    PRECOMMIT("PRECOMMIT"),
    
    //Other messages 
    U_R_COORDINATOR("U ARE COORDINATOR"),
    
    //Vote responses
    VoteYes("VoteYes"),
    VoteNo("VoteNo"),
    //PreCommitResponse
    ACK("ACK"),
    
    //State Req Responses
    Uncertain("Uncertain"),
    COMMITABLE("COMMITABLE"),
    COMMITED("COMMITED"),
    ABORTED("ABORTED");
    
    public String content;

    MsgContent(String content) {
        this.content=content;
    }
    
}
