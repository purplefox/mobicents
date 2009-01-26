/*
 * MsNotifyEventImpl.java
 *
 * The Simple Media API RA
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

package org.mobicents.mscontrol.impl;

import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.events.MsEventIdentifier;

/**
 *
 * @author Oleg Kulikov
 */
public class MsNotifyEventImpl implements MsNotifyEvent {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -5899563273313400237L;
	
	private Object  source;
    private MsEventIdentifier id;
    
    /** Creates a new instance of MsNotifyEventImpl */
    public MsNotifyEventImpl(Object source, MsEventIdentifier id) {
        this.source = source;
        this.id = id;
    }

    public Object getSource() {
        return source;
    }
    
    public MsEventIdentifier getEventID() {
        return id;
    }

}