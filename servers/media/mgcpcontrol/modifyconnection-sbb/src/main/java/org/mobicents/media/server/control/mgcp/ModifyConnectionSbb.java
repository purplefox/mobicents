/*
 * DeleteConnectionSbb.java
 *
 * Mobicents Media Gateway
 *
 * The source code contained in this file is in in the public domain.
 * It can be used in any project or product without prior permission,
 * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
 * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
 * AND DATA ACCURACY.  We do not warrant or make any representations
 * regarding the use of the software or the  results thereof, including
 * but not limited to the correctness, accuracy, reliability or
 * usefulness of the software.
 */

package org.mobicents.media.server.control.mgcp;

import jain.protocol.ip.mgcp.JainMgcpEvent;
import jain.protocol.ip.mgcp.JainMgcpProvider;
import jain.protocol.ip.mgcp.message.ModifyConnection;
import jain.protocol.ip.mgcp.message.ModifyConnectionResponse;
import jain.protocol.ip.mgcp.message.parms.CallIdentifier;
import jain.protocol.ip.mgcp.message.parms.ConnectionDescriptor;
import jain.protocol.ip.mgcp.message.parms.ConnectionIdentifier;
import jain.protocol.ip.mgcp.message.parms.EndpointIdentifier;
import jain.protocol.ip.mgcp.message.parms.LocalOptionValue;
import jain.protocol.ip.mgcp.message.parms.ReturnCode;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.RolledBackContext;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.facilities.ActivityContextNamingFacility;

import net.java.slee.resource.mgcp.MgcpActivityContextInterfaceFactory;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;

/**
 * 
 * @author amit bhayani
 */
public abstract class ModifyConnectionSbb implements Sbb {

	private SbbContext sbbContext;
	private Logger logger = Logger.getLogger(ModifyConnectionSbb.class);

	private JainMgcpProvider mgcpProvider;
	private MgcpActivityContextInterfaceFactory mgcpAcif;

	private ActivityContextNamingFacility activityContextNamingfacility;

	/** Creates a new instance of DeleteConnectionSbb */
	public ModifyConnectionSbb() {
	}

	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = sbbContext;
		try {

			Context ctx = (Context) new InitialContext().lookup("java:comp/env");
			mgcpProvider = (JainMgcpProvider) ctx.lookup("slee/resources/jainmgcp/2.0/provider");
			mgcpAcif = (MgcpActivityContextInterfaceFactory) ctx.lookup("slee/resources/jainmgcp/2.0/acifactory");
			activityContextNamingfacility = (ActivityContextNamingFacility) ctx
					.lookup("slee/facilities/activitycontextnaming");
		} catch (NamingException ne) {
			logger.warn("Could not set SBB context:" + ne.getMessage());
		}
	}

	public void onModifyConnection(ModifyConnection event, ActivityContextInterface aci) {
		int txID = event.getTransactionHandle();
		logger.info("--> MDCX TX ID = " + txID);

		this.setTxId(txID);

		EndpointIdentifier endpointID = event.getEndpointIdentifier();
		CallIdentifier callID = event.getCallIdentifier();
		ConnectionIdentifier connectionID = event.getConnectionIdentifier();

		LocalOptionValue[] localOptionValue = event.getLocalConnectionOptions();

		ConnectionDescriptor remoteConnectionDescriptor = event.getRemoteConnectionDescriptor();

		// TODO : Wildcard conventions shall not be used for EndpointIdentifier.
		// Send Error Response if found?

		logger.debug("Modifing Connection for ConnectionIdentifier = " + connectionID + " EndpointIdentifier = "
				+ endpointID + " CallIdentifier = " + callID);

		MsConnection msConnection = this.getMediaConnection();
		msConnection.modify(msConnection.getEndpoint(), remoteConnectionDescriptor.toString());

	}

	public void onConnectionModified(MsConnectionEvent evt, ActivityContextInterface aci) {
		ModifyConnectionResponse response = new ModifyConnectionResponse(this, ReturnCode.Transaction_Executed_Normally);
		int txID = this.getTxId();
		logger.debug("Deletion of Connection Successful for TxID = " + txID);
		response.setTransactionHandle(txID);

		mgcpProvider.sendMgcpEvents(new JainMgcpEvent[] { response });
	}

	private MsConnection getMediaConnection() {
		ActivityContextInterface[] activities = sbbContext.getActivities();
		for (ActivityContextInterface aci : activities) {
			if (aci.getActivity() instanceof MsConnection) {
				return (MsConnection) aci.getActivity();
			}
		}
		return null;
	}

	public void unsetSbbContext() {
	}

	public void sbbCreate() throws CreateException {
	}

	public void sbbPostCreate() throws CreateException {
	}

	public void sbbActivate() {
	}

	public void sbbPassivate() {
	}

	public void sbbLoad() {
	}

	public void sbbStore() {
	}

	public void sbbRemove() {
	}

	public void sbbExceptionThrown(Exception exception, Object object, ActivityContextInterface activityContextInterface) {
	}

	public void sbbRolledBack(RolledBackContext rolledBackContext) {
	}

	public abstract int getTxId();

	public abstract void setTxId(int txId);

}
