package org.mobicents.mgcp.stack.test.restartinprogress;

import jain.protocol.ip.mgcp.JainMgcpCommandEvent;
import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpResponseEvent;
import jain.protocol.ip.mgcp.message.Constants;
import jain.protocol.ip.mgcp.message.RestartInProgress;
import jain.protocol.ip.mgcp.message.RestartInProgressResponse;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.NotifiedEntity;
import jain.protocol.ip.mgcp.message.parms.RestartMethod;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;

import java.util.TooManyListenersException;

import org.apache.log4j.Logger;
import org.mobicents.mgcp.stack.JainMgcpExtendedListener;
import org.mobicents.mgcp.stack.JainMgcpStackProviderImpl;
import org.mobicents.mgcp.stack.test.auditendpoint.AuditEndpointTest;

public class CA implements JainMgcpExtendedListener {

	private static Logger logger = Logger.getLogger(CA.class);

	private JainMgcpStackProviderImpl caProvider;

	private boolean responseSent = false;

	public CA(JainMgcpStackProviderImpl caProvider, JainMgcpStackProviderImpl mgwProvider) {
		this.caProvider = caProvider;
		try {
			this.caProvider.addJainMgcpListener(this);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
			AuditEndpointTest.fail("Unexpected Exception");
		}
	}

	public void checkState() {
		AuditEndpointTest.assertTrue("Expect to send RSIP Response", responseSent);

	}

	public void transactionEnded(int handle) {
		logger.info("transactionEnded " + handle);

	}

	public void transactionRxTimedOut(JainMgcpCommandEvent command) {
		logger.info("transactionRxTimedOut " + command);

	}

	public void transactionTxTimedOut(JainMgcpCommandEvent command) {
		logger.info("transactionTxTimedOut " + command);

	}

	public void processMgcpCommandEvent(JainMgcpCommandEvent jainmgcpcommandevent) {
		logger.info("processMgcpCommandEvent " + jainmgcpcommandevent);

		switch (jainmgcpcommandevent.getObjectIdentifier()) {
		case Constants.CMD_RESTART_IN_PROGRESS:
			RestartInProgress restartInProgress = (RestartInProgress) jainmgcpcommandevent;

			EndpointIdentifier endpointIdentifier = restartInProgress.getEndpointIdentifier();
			RestartInProgressTest.assertNotNull(endpointIdentifier);
			RestartInProgressTest.assertEquals("media/trunk/Announcement/enp-1@127.0.0.1:"
					+ this.caProvider.getJainMgcpStack().getPort(), endpointIdentifier.toString());

			RestartMethod restartMethod = restartInProgress.getRestartMethod();
			RestartInProgressTest.assertNotNull(restartMethod);
			RestartInProgressTest.assertEquals(RestartMethod.GRACEFUL, restartMethod.getRestartMethod());

			RestartInProgressTest.assertEquals(0, restartInProgress.getRestartDelay());

			RestartInProgressTest.assertNull(restartInProgress.getReasonCode());

			RestartInProgressResponse response = new RestartInProgressResponse(jainmgcpcommandevent.getSource(),
					ReturnCode.Transaction_Executed_Normally);

			NotifiedEntity notifiedEntity = new NotifiedEntity("127.0.0.1");
			response.setNotifiedEntity(notifiedEntity);
			response.setTransactionHandle(jainmgcpcommandevent.getTransactionHandle());

			caProvider.sendMgcpEvents(new JainMgcpEvent[] { response });

			responseSent = true;

			break;
		default:
			logger.warn("This REQUEST is unexpected " + jainmgcpcommandevent);
			break;

		}
	}

	public void processMgcpResponseEvent(JainMgcpResponseEvent jainmgcpresponseevent) {
		// logger.debug("processMgcpResponseEvent = " + jainmgcpresponseevent);

	}

}