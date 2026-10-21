package common.consensus;

import util.annotations.ComponentWidth;
import util.annotations.DisplayToString;
import util.trace.TraceableInfo;

@DisplayToString(true)
@ComponentWidth(1000)
public class RemoteProposeRequestSent extends TraceableInfo {
    public RemoteProposeRequestSent(String aMessage, Object aSource, String anObjectName, float aProposalNumber, Object aProposal) {
        super(aMessage, aSource);
    }

    public static util.trace.port.consensus.RemoteProposeRequestSent newCase(Object aSource, String anObjectName, float aProposalNumber, Object aProposal) {
        String aMessage = anObjectName + "," + aProposalNumber + "=" + aProposal;
        util.trace.port.consensus.RemoteProposeRequestSent retVal = new util.trace.port.consensus.RemoteProposeRequestSent(aMessage, aSource, anObjectName, aProposalNumber, aProposal);
        retVal.announce();
        return retVal;
    }
}