/*
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

package org.mobicents.media.server.spi;
import org.mobicents.media.server.impl.common.*;
import org.mobicents.media.server.impl.common.events.*;
import org.mobicents.media.server.impl.common.dtmf.*;
/**
 *
 * @author Oleg Kulikov
 */
public class UnknownMediaResourceException extends Exception {

    /**
     * Creates a new instance of <code>UnknownMediaResourceException</code> without detail message.
     */
    public UnknownMediaResourceException() {
    }


    /**
     * Constructs an instance of <code>UnknownMediaResourceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public UnknownMediaResourceException(String msg) {
        super(msg);
    }
    public UnknownMediaResourceException(MediaResourceType type)
    {
    	super(type.toString());
    }
}
