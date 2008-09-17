package org.mobicents.slee.resource.diameter.base;

import java.io.Serializable;

import org.jdiameter.api.Answer;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.Request;

public interface RADiameterListener extends NetworkReqListener, Serializable, EventListener<Request, Answer>{

}
