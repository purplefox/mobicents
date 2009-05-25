/*
 * Mobicents, Communications Middleware
 * 
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party
 * contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 *
 * Boston, MA  02110-1301  USA
 */
package org.mobicents.slee.resource.diameter.sh.client;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.java.slee.resource.diameter.base.DiameterAvpFactory;
import net.java.slee.resource.diameter.base.events.avp.VendorSpecificApplicationIdAvp;
import net.java.slee.resource.diameter.sh.client.DiameterShAvpFactory;
import net.java.slee.resource.diameter.sh.client.MessageFactory;
import net.java.slee.resource.diameter.sh.client.events.avp.DiameterShAvpCodes;
import net.java.slee.resource.diameter.sh.client.events.avp.SupportedApplicationsAvp;
import net.java.slee.resource.diameter.sh.client.events.avp.SupportedFeaturesAvp;
import net.java.slee.resource.diameter.sh.client.events.avp.UserIdentityAvp;

import org.apache.log4j.Logger;
import org.jdiameter.api.Stack;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.dictionary.AvpRepresentation;
import org.mobicents.slee.resource.diameter.base.DiameterAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.sh.client.events.avp.SupportedApplicationsAvpImpl;
import org.mobicents.slee.resource.diameter.sh.client.events.avp.SupportedFeaturesAvpImpl;
import org.mobicents.slee.resource.diameter.sh.client.events.avp.UserIdentityAvpImpl;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * Start time:16:49:19 2009-05-23<br>
 * Project: diameter-parent<br>
 * Implementation of Sh AVP factory.
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @see DiameterShAvpFactory
 */
public class DiameterShAvpFactoryImpl implements DiameterShAvpFactory {

	protected DiameterAvpFactory baseAvpFactory = null;
	private DocumentBuilder docBuilder = null;
	protected final transient Logger logger = Logger.getLogger(this.getClass());
	protected Stack stack = null;

	public DiameterShAvpFactoryImpl(DiameterAvpFactory baseAvpFactory, Stack stack) {
		super();
		this.baseAvpFactory = baseAvpFactory;
		this.stack = stack;
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(DiameterShAvpFactoryImpl.class.getClassLoader().getResource("ShDataType.xsd"));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//factory.setValidating(true);
			factory.setSchema(schema);

			docBuilder = factory.newDocumentBuilder();
			docBuilder.setErrorHandler(new ErrorHandler(){

				public void error(SAXParseException exception) throws SAXException {
					throw exception;
					
				}

				public void fatalError(SAXParseException exception) throws SAXException {
					throw exception;
					
				}

				public void warning(SAXParseException exception) throws SAXException {
					throw exception;
					
				}});
		} catch (Exception e) {
			logger.error("Failed to initialize Sh-Data schema validator. No validation will be available.", e);
		}
	}

	public DiameterShAvpFactoryImpl(Stack stack) {
		super();
		this.stack = stack;
		this.baseAvpFactory = new DiameterAvpFactoryImpl();
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(DiameterShAvpFactoryImpl.class.getClassLoader().getResource("ShDataType.xsd"));

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setSchema(schema);
			//factory.setValidating(true);
			
			docBuilder = factory.newDocumentBuilder();
			
			docBuilder.setErrorHandler(new ErrorHandler(){

				public void error(SAXParseException exception) throws SAXException {
					throw exception;
					
				}

				public void fatalError(SAXParseException exception) throws SAXException {
					throw exception;
					
				}

				public void warning(SAXParseException exception) throws SAXException {
					throw exception;
				}});
		} catch (Exception e) {
			logger.error("Failed to initialize Sh-Data schema validator. No validation will be available.", e);
		}
	}

	public SupportedApplicationsAvp createSupportedApplications(long authApplicationId, long acctApplicationId, VendorSpecificApplicationIdAvp vendorSpecificApplicationId) {
		SupportedApplicationsAvp saAvp = this.createSupportedApplications();

		saAvp.setAcctApplicationId(acctApplicationId);
		saAvp.setAuthApplicationId(authApplicationId);

		return saAvp;
	}

	public SupportedApplicationsAvp createSupportedApplications() {
		AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(DiameterShAvpCodes.SUPPORTED_APPLICATIONS, MessageFactory._SH_VENDOR_ID);

		int mandatoryAvp = avpRep.getRuleMandatory().equals("mustnot") || avpRep.getRuleMandatory().equals("shouldnot") ? 0 : 1;
		int protectedAvp = avpRep.getRuleProtected().equals("must") ? 1 : 0;

		SupportedApplicationsAvpImpl saAvp = new SupportedApplicationsAvpImpl(DiameterShAvpCodes.SUPPORTED_APPLICATIONS, MessageFactory._SH_VENDOR_ID, mandatoryAvp, protectedAvp, new byte[] {});

		return saAvp;
	}

	public SupportedFeaturesAvp createSupportedFeatures(long vendorId, long featureListId, long featureList) {
		SupportedFeaturesAvp sfAvp = this.createSupportedFeatures();

		sfAvp.setVendorId(vendorId);
		sfAvp.setFeatureList(featureList);
		sfAvp.setFeatureListId(featureListId);

		return sfAvp;
	}

	public SupportedFeaturesAvp createSupportedFeatures() {
		AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(DiameterShAvpCodes.SUPPORTED_FEATURES, MessageFactory._SH_VENDOR_ID);

		int mandatoryAvp = avpRep.getRuleMandatory().equals("mustnot") || avpRep.getRuleMandatory().equals("shouldnot") ? 0 : 1;
		int protectedAvp = avpRep.getRuleProtected().equals("must") ? 1 : 0;

		SupportedFeaturesAvpImpl sfAvp = new SupportedFeaturesAvpImpl(DiameterShAvpCodes.SUPPORTED_FEATURES, MessageFactory._SH_VENDOR_ID, mandatoryAvp, protectedAvp, new byte[] {});

		return sfAvp;
	}

	public UserIdentityAvp createUserIdentity() {
		AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(DiameterShAvpCodes.USER_IDENTITY, MessageFactory._SH_VENDOR_ID);

		int mandatoryAvp = avpRep.getRuleMandatory().equals("mustnot") || avpRep.getRuleMandatory().equals("shouldnot") ? 0 : 1;
		int protectedAvp = avpRep.getRuleProtected().equals("must") ? 1 : 0;

		UserIdentityAvpImpl uiAvp = new UserIdentityAvpImpl(DiameterShAvpCodes.USER_IDENTITY, MessageFactory._SH_VENDOR_ID, mandatoryAvp, protectedAvp, new byte[] {});

		return uiAvp;
	}

	public DiameterAvpFactory getBaseFactory() {
		return this.baseAvpFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.java.slee.resource.diameter.sh.client.DiameterShAvpFactory#
	 * validateUserData(byte[])
	 */
	public boolean validateUserData(byte[] userData) {
		if (docBuilder != null && userData!=null) {
			try {
				docBuilder.parse(new ByteArrayInputStream(userData));
				return true;
			} catch (Throwable e) {
			//} catch (Exception e) {
				e.printStackTrace();
				
				logger.error("Failure while validating User-Data:", e);
				

			}
		}
		return false;
	}

}
