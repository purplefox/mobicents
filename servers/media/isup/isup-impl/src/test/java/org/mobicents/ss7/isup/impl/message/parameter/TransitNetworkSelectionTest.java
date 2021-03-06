/**
 * Start time:14:11:03 2009-04-23<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski
 *         </a>
 * 
 */
package org.mobicents.ss7.isup.impl.message.parameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.mobicents.ss7.isup.ISUPComponent;
import org.mobicents.ss7.isup.ParameterRangeInvalidException;
import org.mobicents.ss7.isup.impl.message.parameter.RedirectionNumberImpl;
import org.mobicents.ss7.isup.impl.message.parameter.TransitNetworkSelectionImpl;

/**
 * Start time:14:11:03 2009-04-23<br>
 * Project: mobicents-isup-stack<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski
 *         </a>
 */
public class TransitNetworkSelectionTest extends ParameterHarness {

	/**
	 * @throws IOException
	 */
	public TransitNetworkSelectionTest() throws IOException {
		super.badBodies.add(new byte[1]);

	}

	public void testBody1EncodedValues() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, ParameterRangeInvalidException {
		TransitNetworkSelectionImpl bci = new TransitNetworkSelectionImpl(getBody(false, TransitNetworkSelectionImpl._NIP_PDNIC, TransitNetworkSelectionImpl._TONI_ITU_T, getSixDigits()));

		String[] methodNames = { "isOddFlag", "getNetworkIdentificationPlan", "getTypeOfNetworkIdentification",  "getAddress" };
		Object[] expectedValues = { false, TransitNetworkSelectionImpl._NIP_PDNIC, TransitNetworkSelectionImpl._TONI_ITU_T, getSixDigitsString()};
		super.testValues(bci, methodNames, expectedValues);
	}

	private byte[] getBody(boolean isODD, int _NIP, int _TONI, byte[] digits) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// we will use odd number of digits, so we leave zero as MSB

		if (isODD) {
			bos.write(0x80 | (_TONI << 4) | _NIP);
		} else {
			bos.write((_TONI << 4) | _NIP);
		}

		bos.write(digits);
		return bos.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.isup.messages.parameters.ParameterHarness#getTestedComponent
	 * ()
	 */
	@Override
	public ISUPComponent getTestedComponent() {
		return new TransitNetworkSelectionImpl("11", 1, 1);
	}

}
