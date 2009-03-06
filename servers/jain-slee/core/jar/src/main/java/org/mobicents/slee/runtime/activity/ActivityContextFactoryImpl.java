/*
 * Created on Jul 8, 2004
 *
 * The Open SLEE project.
 * The source code contained in this file is in in the public domain.          
 * It can be used in any project or product without prior permission, 	      
 * license or royalty payments. There is no claim of correctness and
 * NO WARRANTY OF ANY KIND provided with this code.
 *
 */
package org.mobicents.slee.runtime.activity;

import java.util.Set;

import javax.slee.SLEEException;
import javax.slee.resource.ActivityAlreadyExistsException;
import javax.slee.resource.ActivityFlags;
import javax.transaction.SystemException;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.SleeContainer;
import org.mobicents.slee.runtime.cache.ActivityContextFactoryCacheData;
import org.mobicents.slee.runtime.facilities.nullactivity.NullActivityContext;
import org.mobicents.slee.runtime.facilities.nullactivity.NullActivityHandle;
import org.mobicents.slee.runtime.transaction.TransactionalAction;

/**
 * Activity context factory -- return an activity context given an activity or
 * create one and put it in the table. This also implements the activity context
 * naming facility for the SLEE.
 * 
 * @author F.Moggia
 * @author M. Ranganathan
 * @author Tim Fox
 * @author eduardomartins second version
 * @version 2.0
 * 
 * 
 */
public class ActivityContextFactoryImpl implements ActivityContextFactory {

	private static Logger logger = Logger.getLogger(ActivityContextFactoryImpl.class);
	
	/**
	 * the container for this factory
	 */
	private final SleeContainer sleeContainer;
	
	private ActivityContextFactoryCacheData cacheData;
	
	public ActivityContextFactoryImpl(SleeContainer sleeContainer) {		
		this.sleeContainer = sleeContainer;		
		cacheData = sleeContainer.getCache().getActivityContextFactoryCacheData();
		if (!cacheData.exists()) {
			cacheData.create();
		}
	}

	/*
	 * creates an instance of the activity context for specified ac handle and id
	 */
	private ActivityContext createActivityContextInstance(ActivityContextHandle ach, String acId, boolean updateAccessTime, Integer activityFlags) {
		if (ach.getActivityType() == ActivityType.nullActivity) {
			return new NullActivityContext(ach,acId,updateAccessTime, activityFlags);
		}
		else {
			return new ActivityContext(ach,acId,updateAccessTime,activityFlags);
		}
	}
	
	private String createActivityContextId(ActivityContextHandle ach) {
		String acId = null;
		if (ach.getActivityType() != ActivityType.nullActivity) {
			acId = "ac:" + sleeContainer.getUuidGenerator().createUUID();
		}
		else {
			// lets reuse its id
			acId = "ac:" + ((NullActivityHandle) ach.getActivityHandle()).getId();			
		}	
		return acId;
	}
	
	public ActivityContext createActivityContext(final ActivityContextHandle ach) throws ActivityAlreadyExistsException {
		return createActivityContext(ach,ActivityFlags.NO_FLAGS);
	}

	public ActivityContext createActivityContext(final ActivityContextHandle ach, int activityFlags) throws ActivityAlreadyExistsException {
		
		// create id
		final String acId = createActivityContextId(ach);
		
		if (cacheData.addActivityContext(ach, acId)) {
			// create ac
			ActivityContext ac = createActivityContextInstance(ach,acId,true, Integer.valueOf(activityFlags));
			// warn event router about it
			sleeContainer.getEventRouter().activityStarted(acId);
			// add rollback tx action to remove state created
			TransactionalAction action = new TransactionalAction() {
				public void execute() {
					sleeContainer.getEventRouter().activityEnded(acId);				
				}
			};
			try {
				sleeContainer.getTransactionManager().addAfterRollbackAction(action);
			} catch (SystemException e) {
				throw new SLEEException("failed to add rollback action to remove activity runtime resources",e);
			}
			return ac;
		}
		else {
			throw new ActivityAlreadyExistsException(ach.toString());
		}
	}

	public ActivityContext getActivityContext(ActivityContextHandle ach, boolean updateAccessTime) {
		String acId = cacheData.getActivityContextId(ach);
		if (acId != null) {
			return createActivityContextInstance(ach,acId,updateAccessTime,null);
		}
		else {
			return null; 
		}
	}
	
	public ActivityContext getActivityContext(String acId, boolean updateAccessTime) {
		ActivityContextHandle ach = (ActivityContextHandle) cacheData.getActivityContextHandle(acId);
		if (ach != null) {
			return createActivityContextInstance(ach,acId,updateAccessTime,null);
		}
		else {
			return null; 
		}
	}

	public Set<ActivityContextHandle> getAllActivityContextsHandles() {
		return cacheData.getActivityContextHandles();
	}

	public Set<String> getAllActivityContextsIds() {
		return cacheData.getActivityContextIds();
	}
	
	public void removeActivityContext(final ActivityContext ac) {

		// remove references to this AC in timer and ac naming facility
		ac.removeNamingBindings();
		ac.removeFromTimers(); // Spec 7.3.4.1 Step 10
		
		ac.removeFromCache();
		
		cacheData.removeActivityContext(ac.getActivityContextHandle(),ac.getActivityContextId());
		
		sleeContainer.getEventRouter().activityEnded(ac.getActivityContextId());
		
		// add rollback tx action to recreate state removed
		TransactionalAction action = new TransactionalAction() {
			public void execute() {
				sleeContainer.getEventRouter().activityStarted(ac.getActivityContextId());				
			}
		};	
		try {
			sleeContainer.getTransactionManager().addAfterRollbackAction(action);
		} catch (SystemException e) {
			throw new SLEEException("failed to add rollback action to readd aruntime activity resources",e);
		}
		
		// since the ac ended we may need to warn the activity source that it has ended
		ac.getActivityContextHandle().activityEnded();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Activity context with handle "+ac.getActivityContextHandle()+" removed");
		}
	}
	
	public int getActivityContextCount() {		
		return getAllActivityContextsHandles().size();
	}
	
	@Override
	public String toString() {
		return "ActivityContext Factory: " 
			+ "\n+-- Number of ACs: " + getActivityContextCount();
	}

	public ActivityContextHandle getActivityContextHandle(String acId) {
		return (ActivityContextHandle) cacheData.getActivityContextHandle(acId);
	}
	
}