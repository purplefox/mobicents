/*
 * RtpConnectionImpl.java
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
package org.mobicents.media.server.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;

import org.apache.log4j.Logger;
import org.mobicents.media.Format;
import org.mobicents.media.server.impl.rtp.RtpSocket;
import org.mobicents.media.server.impl.rtp.RtpSocketImpl;
import org.mobicents.media.server.impl.rtp.sdp.RTPFormat;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.ResourceUnavailableException;

import javax.naming.InitialContext;
import org.mobicents.media.server.impl.dsp.Processor;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.sdp.RTPAudioFormat;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.ConnectionState;

/**
 * 
 * @author Oleg Kulikov
 */
public class RtpConnectionImpl extends BaseConnection {

    private String localAddress;
    private int localPort;
//    private HashMap audioFormats;
//    private HashMap videoFormats;
    private String localDescriptor;
    private String remoteDescriptor;
    
    private transient RtpSocket rtpSocket;
    private transient SdpFactory sdpFactory = SdpFactory.getInstance();
    private transient Processor inDsp;
    private transient Processor outDsp;
    
//    private HashMap codecs;
    /**
     * Creates a new instance of RtpConnectionImpl.
     * 
     * @param endpoint
     *            the endpoint executing this connection.
     * @param connectionID the identifier of the connection to be created
     * @param mode
     *            the mode of this connection.
     */
    public RtpConnectionImpl(Endpoint endpoint, ConnectionMode mode) throws ResourceUnavailableException {
        super(endpoint, mode);
        logger = Logger.getLogger(RtpConnectionImpl.class);


        //create and initialize RTP socket
        if (logger.isDebugEnabled()) {
            logger.debug(this + " Initializing RTP stack");
        }
        initRTPSocket();

        inDsp = new Processor("InputProcess : " + endpointName + " : " + localAddress + "." + localPort);
        outDsp = new Processor("OutputProcess : " + endpointName + " : " + localAddress + "." + localPort);

        inDsp.getOutput().connect(demux.getInput());
        inDsp.getInput().connect(rtpSocket.getReceiveStream());

        if (getMode() == ConnectionMode.RECV_ONLY || getMode() == ConnectionMode.SEND_RECV) {
            demux.start();
        }
        setState(ConnectionState.HALF_OPEN);
    }

    /**
     * Gets the reference to used RTP Factory instance.
     * 
     * @return RtpFactory instance.
     */
    private RtpFactory getRtpFactory() throws NamingException {
        String jndiName = endpoint.getRtpFactoryName();
        if (jndiName != null) {
            InitialContext ic = new InitialContext();
            return (RtpFactory) ic.lookup(endpoint.getRtpFactoryName());
        } else {
            return new RtpFactory();
        }
    }

    /**
     * Provides intialization of the RTP stack.
     * 
     * @throws org.mobicents.media.server.spi.ResourceUnavailableException
     */
    private void initRTPSocket() throws ResourceUnavailableException {
        try {
            //obtain RTP factory
            RtpFactory rtpFactory = getRtpFactory();

            //try to open rtp socket
            rtpSocket = rtpFactory.getRTPSocket();

            //hold local address and port
            localAddress = rtpFactory.getBindAddress();
            localPort = rtpSocket.getPort();

            //obtain rtp formats
            //audioFormats = rtpFactory.getAudioFormats();

            //start rtp receiver and sender
            rtpSocket.start();
        } catch (SocketException e) {
            logger.error(this + " Fail while binding RTP socket", e);
            throw new ResourceUnavailableException(e.getMessage());
        } catch (NamingException e) {
            logger.error(this + " Could not obtain RTP Factory", e);
            throw new ResourceUnavailableException(e.getMessage());
        }
    }

    /**
     * Checks is format presented in the list.
     * 
     * @param fmts the list of formats to check
     * @param fmt the format instance to check.
     * @return true if fmt is in list of fmts.
     */
    private boolean contains(Format[] fmts, Format fmt) {
        for (int i = 0; i < fmts.length; i++) {
            if (fmts[i].matches(fmt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * (Non-Javadoc).
     * 
     * @see org.mobicents.media.server.spi.Connection#getLocalDescriptor();
     */
    public String getLocalDescriptor() {
        SessionDescription localSDP = null;
        try {
            this.lockState();

            if (state == ConnectionState.NULL || state == ConnectionState.CLOSED) {
                throw new IllegalStateException("State is " + state);
            }
            if (state == ConnectionState.NULL || state == ConnectionState.CLOSED) {
                throw new IllegalStateException("State is " + state);
            }

            String userName = "MediaServer";
            long sessionID = System.currentTimeMillis() & 0xffffff;
            long sessionVersion = sessionID;

            String networkType = javax.sdp.Connection.IN;
            String addressType = javax.sdp.Connection.IP4;
            String address = null;

            RtpSocketImpl rtpSocketImpl = (RtpSocketImpl) this.rtpSocket;

            int audioPort = 0;
            if (!rtpSocketImpl.isUseStun()) {
                address = localAddress;
                audioPort = rtpSocket.getPort();
            } else {
                address = rtpSocketImpl.getPublicAddressFromStun();
                audioPort = rtpSocketImpl.getPublicPortFromStun();
            }

            try {
                localSDP = sdpFactory.createSessionDescription();
                localSDP.setVersion(sdpFactory.createVersion(0));
                localSDP.setOrigin(sdpFactory.createOrigin(userName, sessionID, sessionVersion, networkType, addressType, address));
                localSDP.setSessionName(sdpFactory.createSessionName("session"));
                localSDP.setConnection(sdpFactory.createConnection(networkType, addressType, address));

                Vector descriptions = new Vector();

                // encode formats
                HashMap<Integer, Format> rtpMap = rtpSocket.getRtpMap();
                //System.out.println("RTP MAP=" + rtpMap);
                //Format[] supported = endpoint.getSupportedFormats();
                Format[] supported = inDsp.getInput().getFormats();


                HashMap fmts = new HashMap();
                Set<Integer> map = rtpMap.keySet();

                for (Integer pt : map) {
                    Format f = (Format) rtpMap.get(pt);
                    //System.out.println("Checking format: " + f + ", class=" + f.getClass().getCanonicalName());
                    if (contains(supported, f)) {
                        fmts.put(pt, f);
                    }
                }

                Object[] payloads = getPayloads(fmts).toArray();

                int[] formats = new int[payloads.length];
                for (int i = 0; i < formats.length; i++) {
                    formats[i] = ((Integer) payloads[i]).intValue();
                }

                // generate media descriptor
                MediaDescription md = sdpFactory.createMediaDescription("audio", audioPort, 1, "RTP/AVP", formats);

                boolean g729 = false;
                boolean dtmf = false;
                
                int g729payloadType = -1; // g729 payload type is usually 18
                int dtmfPayload = -1;
                
                // set attributes for formats
                Vector attributes = new Vector();
                for (int i = 0; i < formats.length; i++) {
                    RTPAudioFormat format = (RTPAudioFormat) fmts.get(new Integer(formats[i]));
                    attributes.add(sdpFactory.createAttribute("rtpmap", format.toSdp()));
                    if (format.getEncoding().contains("g729")) {
                        g729 = true;
                        g729payloadType = format.getPayload(); // should be 18
                    }
                    if (format.getEncoding().equals("telephone-event/8000")) {
                        dtmf = true;
                        dtmfPayload = format.getPayload();
                    }
                }

                // This options forces the remote g728 side to avoid using annexb, which is not supported right now
                if (g729) {
                    attributes.add(sdpFactory.createAttribute("fmtp", g729payloadType + " annexb=no"));
                }
                if (dtmf) {
                    attributes.add(sdpFactory.createAttribute("fmtp", dtmfPayload + " 0-15"));
                }
                // generate descriptor
                md.setAttributes(attributes);
                descriptions.add(md);

                localSDP.setMediaDescriptions(descriptions);
            } catch (SdpException e) {
                logger.error("Could not create descriptor", e);
            }
        } catch (InterruptedException e) {
            logger.error("Failed to lock connection due to exception, possibly server is shutting down.");
            e.printStackTrace();
        // FIXME: baranowb: shouldnt we close here instead?
        // throw new ResourceUnavailableException(e);
        } finally {
            this.releaseState();
        }
        //System.out.println("Local SDP: " + localSDP.toString());
        this.localDescriptor = localSDP.toString();
        return localDescriptor;
    }

    /**
     * (Non-Javadoc).
     * 
     * @see org.mobicents.media.server.spi.Connection#getRemoteDescriptor();
     */
    public String getRemoteDescriptor() {
        return remoteDescriptor; 
    }

    /**
     * Extracts address and port of the remote party.
     * 
     * @param sdp
     *            session description.
     * @return socket address of the remote party.
     */
    private InetSocketAddress getPeer(SessionDescription sdp) throws SdpException {
        javax.sdp.Connection connection = sdp.getConnection();

        Vector list = sdp.getMediaDescriptions(false);
        MediaDescription md = (MediaDescription) list.get(0);

        try {
            InetAddress address = InetAddress.getByName(connection.getAddress());
            int port = md.getMedia().getMediaPort();
            return new InetSocketAddress(address, port);
        } catch (UnknownHostException e) {
            throw new SdpException(e);
        }
    }

    /**
     * (Non-Javadoc).
     * 
     * @see org.mobicents.media.server.spi.Connection#setRemoteDescriptor();
     */
    public void setRemoteDescriptor(String descriptor) throws SdpException, IOException, ResourceUnavailableException {
        this.remoteDescriptor = descriptor;
        SessionDescription remoteSDP = null;
        try {
            this.lockState();

            if (state != ConnectionState.HALF_OPEN && state != ConnectionState.OPEN) {
                throw new IllegalStateException("State is " + state);
            }
            if (state != ConnectionState.HALF_OPEN && state != ConnectionState.OPEN) {
                throw new IllegalStateException("State is " + state);
            }

            remoteSDP = sdpFactory.createSessionDescription(descriptor);

            // add peer to RTP socket
            InetSocketAddress peer = getPeer(remoteSDP);
            rtpSocket.setPeer(peer.getAddress(), peer.getPort());

            if (logger.isDebugEnabled()) {
                logger.debug(this + " Set peer: " + peer);
            }

            // negotiate codecs
            HashMap<Integer, Format> offer = RTPFormat.getFormats(remoteSDP);
            //System.out.print("Offer: " + offer);
            
            if (logger.isDebugEnabled()) {
                logger.debug(this + " Offered formats: " + offer);
            }            
            
            HashMap rtpMap = rtpSocket.getRtpMap();
            HashMap subset = this.subset(offer, rtpMap);
            //System.out.print("Subset: " + subset);
            
            if (subset.isEmpty()) {
                throw new IOException("Codecs are not negotiated");
            }
            
            this.updateRtpMap(subset);
            rtpSocket.getSendStream().setFormats(subset.values());
            
 
            
            
//            HashMap rtpMap = select(inDsp.getInput().getFormats(), offer);
//            if (logger.isDebugEnabled()) {
//                logger.debug(this + " Selected formats: " + rtpMap);
//            }

            //Set<Integer> keys = rtpMap.keySet();
            //for (Integer key : keys) {
            //    rtpSocket.addFormat(key, (Format) rtpMap.get(key));
            //}
//            narrow(rtpMap);
                HashMap map = rtpSocket.getRtpMap();
                //System.out.println("*** RTP MAP=" + map);
            
            // @FIXME
            // DTMF may be negotiated but speech codecs no

            if (logger.isDebugEnabled()) {
                logger.debug(this + " Codecs are negotiated");
            }

            try {
                rtpSocket.setPeriod(getPacketizationPeriod(remoteSDP));
            } catch (Exception e) {
                // silence here
            }

            //rtpSocket.setRtpMap(rtpMap);

            outDsp.getOutput().connect(rtpSocket.getSendStream());
            outDsp.getInput().connect(mux.getOutput());
            
            if (getMode() == ConnectionMode.SEND_ONLY || getMode() == ConnectionMode.SEND_RECV) {
                mux.getOutput().start();
            }
            //System.out.println("CODEC MAP" + outDsp.showCodecMap());
            setState(ConnectionState.OPEN);
        } catch (InterruptedException e) {
            logger.error("Failed to lock connection due to exception, possibly server is shutting down.");
            e.printStackTrace();
            // FIXME: baranowb: shouldnt we close here instead?
            throw new ResourceUnavailableException(e);
        } finally {
            this.releaseState();
        }
    }

    private HashMap subset(HashMap<Integer, Format> remote, HashMap<Integer, Format> local) {
        HashMap<Integer, Format> subset = new HashMap();
        for (Integer k : remote.keySet()) {
            Format rf = remote.get(k);
            for (Integer l : local.keySet()) {
                Format lf = local.get(l);
                if (lf.matches(rf)) {
                    subset.put(k, rf);
                }
            }
        }
        return subset;
    }
    
    private void updateRtpMap(HashMap<Integer, Format> offer) {
        HashMap<Integer, Integer> updates = new HashMap<Integer, Integer>();
        HashMap<Integer, Format> rtpMap = rtpSocket.getRtpMap();
        for (Integer k : rtpMap.keySet()) {
            Format rf = rtpMap.get(k);
            for (Integer l : offer.keySet()) {
                Format lf = offer.get(l);
                if (lf.matches(rf) && !(k.equals(l))) {
                    updates.put(l, k);
                }
            }
        }
        
        for (Integer l : updates.keySet()) {
            Integer k = updates.get(l);
            Format f = rtpMap.remove(k);
            ((RTPAudioFormat)f).setPayload(l);
            rtpMap.put(l, f);
        }
    }
    
    private boolean contains(Format fmt, HashMap<Integer, Format> map) {
        Collection<Format> formats = map.values();
        for (Format format : formats) {
            if (fmt.matches(format)) {
                return true;
            }
        }
        return false;
    }
    
    private void narrow(HashMap<Integer, Format> selected) {
        HashMap<Integer, Format> newRtpMap = new HashMap<Integer, Format>();
        
        HashMap<Integer, Format> rtpMap = rtpSocket.getRtpMap();
        Set <Integer> keys = selected.keySet();
        for (Integer key: keys) {
            Set<Integer> payloads = rtpMap.keySet();
            Format fmt = selected.get(key);
            for (Integer p : payloads) {
                Format f = rtpMap.get(p) ;
                if (f.matches(fmt)) {
                    newRtpMap.put(key, f);
                }
            }
        }
        
        
        keys = newRtpMap.keySet();
        for (Integer key : keys) {
            Format f = newRtpMap.get(key);
            rtpSocket.addFormat(key, f);
        }
    }
    
    /**
     * Gets packetization period encoded in session descriptor.
     * 
     * @param sd
     *            the session descriptor
     * @return value of the packetization period in milliseconds.
     * @throws javax.sdp.SdpException
     */
    private int getPacketizationPeriod(SessionDescription sd) throws Exception {
        MediaDescription md = (MediaDescription) sd.getMediaDescriptions(false).get(0);
        String value = md.getAttribute("ptime");
        return Integer.parseInt(value);

    }

    /**
     * (Non-Javadoc).
     * 
     * @throws InterruptedException
     * 
     * @see org.mobicents.media.server.spi.Connection#setRemoteDescriptor();
     */
    public void setOtherParty(Connection other) {
    }

    /**
     * Gets the collection of payload types.
     * 
     * @param fmts
     *            the map with payload type as key and format as a value.
     * @return sorted collection of payload types.
     */
    private Collection getPayloads(HashMap fmts) {
        Object[] payloads = fmts.keySet().toArray();

        ArrayList list = new ArrayList();
        for (int i = 0; i < payloads.length; i++) {
            list.add(payloads[i]);
        }

        Collections.sort(list);
        return list;
    }

    /**
     * Selects codecs shared between supported and offered.
     * 
     * @param supported
     *            the map with the supported codecs.
     * @param offered
     *            the list with the offered codecs.
     */
    private HashMap<Integer, Format> select(Format[] supported, HashMap<Integer, Format> offered) {
        HashMap<Integer, Format> formats = new HashMap<Integer, Format>();
        Set<Integer> offer = offered.keySet();
        for (Integer po : offer) {
            Format ofmt = (Format) offered.get(po);
            if (contains(supported, ofmt)) {
                formats.put(po, ofmt);
            }
        }
        return formats;
    }

    /**
     * Releases all resources requested by this connection.
     * 
     * @throws InterruptedException
     */
    @Override
    public void close() {
        try {
            demux.stop();
            inDsp.getInput().disconnect(rtpSocket.getReceiveStream());
            inDsp.getOutput().disconnect(demux.getInput());

            mux.getOutput().stop();
            outDsp.getInput().disconnect(mux.getOutput());
            outDsp.getOutput().disconnect(rtpSocket.getSendStream());

            rtpSocket.close();
        } finally {
            super.close();
        }
    }

    /**
     * Gets the text representation of the connection.
     * 
     * @return text representation of the connection.
     */
    @Override
    public String toString() {
        return "(connectionID=" + id + ", endpoint=" + endpointName + ", state=" + state + ")";
    }

    public void error(Exception e) {
        this.endpoint.deleteConnection(id);
    }

    public String getOtherEnd() throws IllegalArgumentException {

        return "Remote";
    }
}
