package pkg3pc;

/**
 *
 * @author bansal
 */
public enum MsgContent {
    //decision types

    ABORT("ABORT"),
    COMMIT("COMMIT"),

    TIMEOUT("TIMEOUT"),
    //Requests which expect responses 
    VOTE_REQ("VOTE_REQ"), //should include partcipant list 
    STATE_REQ("STATE_REQ"), //can include up list
    PRECOMMIT("PRECOMMIT"),
    STATUS_REQ("STATUS_REQ"),
    //Other messages
    U_R_COORDINATOR("U_R_COORDINATOR"),
    //Vote responses
    VoteYes("VoteYes"),
    VoteNo("VoteNo"),
    //PreCommitResponse
    ACK("ACK"),
    //State Req Responses

    UNCERTAIN("UNCERTAIN"),
    COMMITABLE("COMMITABLE"),
    COMMITED("COMMITED"),
    ABORTED("ABORTED"),
    READY("READY"),
    STARTED("STARTED"),
    //Check if the process is alive 
    CHECKALIVE("CHECKALIVE"),
    IAMALIVE("IAMALIVE");
    
    public String content;

    MsgContent(String content) {
        this.content = content;
    }
}
