package org.mobicents.slee.runtime.eventrouter;

import javax.slee.Address;
import javax.transaction.SystemException;

import org.mobicents.slee.runtime.activity.ActivityContextHandle;

/**
 * Useful activity end event version of the {@link DeferredEvent}.
 * 
 * @author martins
 *
 */
public class DeferredActivityEndEvent extends DeferredEvent {

	public DeferredActivityEndEvent(ActivityContextHandle ach, Address address) throws SystemException {
		super(ActivityEndEventImpl.getEventTypeID(),ActivityEndEventImpl.SINGLETON,ach,address);
	}
}