package org.mobicents.media.msc.common.events;

/**
 * When the state of MsConnection is changed MsConnectionEvent is fired which
 * has MsConnectionEventCause representing the cause of event
 * 
 * <ul>
 * <li><code>NORMAL</code> Represents the MsConnectionEvent is fired because
 * the operation is successful. For example creation of MsConnection</li>
 * <li><code>FACILITY_FAILURE</code> Represents the MsConnectionEvent
 * creation failed due to facility failure like Endpoint already has reached its
 * maximum number of MsConnection that it can handle</li>
 * 
 * <li><code>RESOURCE_UNAVAILABLE</code> Represents the MsConnectionEvent
 * creation failed due to facility failure like Scoket creation failed while
 * creatng Connection</li>
 * 
 * <li><code>REMOTE_SDP_INVALID</code> Represents the MsConnectionEvent
 * creation failed due to SDP passed is invalid</li>
 * 
 * <li><code>REMOTE_SDP_MISSING</code> Represents the MsConnectionEvent
 * creation failed due to SDP passed is null</li>
 * 
 * </ul>
 * 
 * @author Oleg Kulikov
 * 
 * 
 */
public enum MsConnectionEventCause {

	NORMAL, FACILITY_FAILURE, ENDPOINT_UNKNOWN, RESOURCE_UNAVAILABLE, REMOTE_SDP_INVALID, REMOTE_SDP_MISSING;
}
