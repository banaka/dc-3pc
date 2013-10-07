/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package pkg3pc;

import ut.distcomp.framework.Config;
import ut.distcomp.framework.NetController;

/**
 * @author bansal
 */
public class ParticipantImpl extends Process implements Participant {
    ParticipantImpl(NetController netController, int procNo, Boolean voteInput, int msgCount, Config config) {
        super(netController, procNo, voteInput, msgCount, config);
        this.timeout = config.timeout * 2;
    }
}
