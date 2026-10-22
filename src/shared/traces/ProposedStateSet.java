package shared.traces;

import util.annotations.ComponentWidth;
import util.annotations.DisplayToString;
import util.trace.TraceableInfo;

@DisplayToString(true)
@ComponentWidth(1000)
public class ProposedStateSet extends TraceableInfo {
    public ProposedStateSet(String aMessage, Object aSource, String anObjectName, float aProposalNumber, Object aProposal) {
        super(aMessage, aSource);
    }

    public static util.trace.port.consensus.ProposedStateSet newCase(Object aSource, String anObjectName, float aProposalNumber, Object aProposal) {
        String aMessage = anObjectName + "," + aProposalNumber + "=" + aProposal;
        util.trace.port.consensus.ProposedStateSet retVal = new util.trace.port.consensus.ProposedStateSet(aMessage, aSource, anObjectName, aProposalNumber, aProposal);
        retVal.announce();
        return retVal;
    }
}
