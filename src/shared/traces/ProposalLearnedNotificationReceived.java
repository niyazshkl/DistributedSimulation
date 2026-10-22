package shared.traces;

import util.annotations.ComponentWidth;
import util.annotations.DisplayToString;
import util.trace.port.rpc.RemoteCallbackInfo;

@DisplayToString(true)
@ComponentWidth(1000)
public class ProposalLearnedNotificationReceived extends RemoteCallbackInfo {
    public ProposalLearnedNotificationReceived(String aMessage, Object aSource, String anObjectName, float aProposalNumber, Object aProposal) {
        super(aMessage, aSource);
    }

    public static util.trace.port.consensus.ProposalLearnedNotificationReceived newCase(Object aSource, String anObjectName, float aProposalNumber, Object aProposal) {
        String aMessage = callerPrefix() + anObjectName + "," + aProposalNumber + "=" + aProposal;
        util.trace.port.consensus.ProposalLearnedNotificationReceived retVal = new util.trace.port.consensus.ProposalLearnedNotificationReceived(aMessage, aSource, anObjectName, aProposalNumber, aProposal);
        retVal.announce();
        return retVal;
    }
}