/*
 * BaseEndpoint.java
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.PushBufferStream;

import org.apache.log4j.Logger;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.Connection;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.media.Format;
import org.mobicents.media.server.impl.sdp.AVProfile;
import org.mobicents.media.server.spi.MediaResource;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.ResourceUnavailableException;
import org.mobicents.media.server.spi.TooManyConnectionsException;
import org.mobicents.media.server.spi.UnknownMediaResourceException;
import org.mobicents.media.server.spi.events.NotifyEvent;

/**
 * The basic implementation of the endpoint.
 *
 * An BaseEndpoint is a logical representation of a physical entity, such as an
 * analog phone or a channel in a trunk. Endpoints are sources or sinks of data
 * and can be physical or virtual. Physical endpoint creation requires hardware
 * installation while software is sufficient for creating a virtual BaseEndpoint.
 * An interface on a gateway that terminates a trunk connected to a PSTN switch
 * is an example of a physical BaseEndpoint. An audio source in an audio-content
 * server is an example of a virtual BaseEndpoint.
 *
 * @author Oleg Kulikov.
 */
public abstract class BaseEndpoint implements Endpoint {

    private String localName;
    private InetAddress bindAddress;
    protected int packetizationPeriod;
    protected int jitter;
    private boolean hasConnections;
    protected int lowPortNumber;
    protected int highPortNumber;
    private ConcurrentReaderHashMap connections = new ConcurrentReaderHashMap();
    private ArrayList<NotificationListener> listeners = new ArrayList();
    private HashMap resources = new HashMap();
    protected HashMap formats = new HashMap();
    private BaseResourceManager resourceManager;
    private transient Logger logger = Logger.getLogger(BaseEndpoint.class);

    public BaseEndpoint(String localName) {
        this.localName = localName;
        this.resourceManager = initResourceManager();
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#getLocalName();
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Gets the maximum amount of the connections that endpoint can implement.
     *
     * @return the maximum available connections.
     */
    public int getMaxConnectionsAvailable() {
        return connections.capacity();
    }

    /**
     * Sets the maximum amount of the connections that endpoint can implement.
     *
     * @return the maximum available connections.
     */
    public void setMaxConnectionsAvailable(int amount) {
        connections = new ConcurrentReaderHashMap(amount);
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#getLocalName);
     */
    public InetAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(InetAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#getPcketizationPeriod();
     */
    public Integer getPacketizationPeriod() {
        return packetizationPeriod;
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#setPcketizationPeriod(Integer);
     */
    public void setPacketizationPeriod(Integer period) {
        packetizationPeriod = period;
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#getJitter();
     */
    public Integer getJitter() {
        return jitter;
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#setJitter(Integer);
     */
    public void setJitter(Integer jitter) {
        this.jitter = jitter;
    }

    /**
     * Gets the supported formats.
     * 
     * @return the map where key is an RTP payload number and value is a 
     * format instance.
     */
    public HashMap getFormats() {
        return formats;
    }

    public BaseResourceManager initResourceManager() {
        return new BaseResourceManager();
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#configure(String, Properties);
     */
    public void configure(String resourceName, Properties config) throws UnknownMediaResourceException {
        MediaResource mediaResource = resourceManager.getResource(this, resourceName, config);
        resources.put(resourceName, mediaResource);
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#configure(String, String, Properties);
     */
    public synchronized void configure(String resourceName, Connection connection, Properties config) throws UnknownMediaResourceException {
        MediaResource mediaResource = resourceManager.getResource(this, resourceName, connection, config);

        if (mediaResource == null) {
            return;
        }

        try {
            mediaResource.configure(config);
            resources.put(resourceName + "_" + connection.getId(), mediaResource);
        } catch (Exception e) {
            logger.error("Cold not configure resource " + resourceName + ", connection = " + connection, e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public synchronized void prepare(String resourceName, String connectionID, PushBufferStream media) throws UnsupportedFormatException {
        MediaResource res = (MediaResource) resources.get(resourceName + "_" + connectionID);
    //res.prepare(media);
    }

    public void addFormat(int pt, Format fmt) {
        formats.put(pt, fmt);
    }

    public void removeFormat(Format fmt) {
        formats.remove(getPayload(fmt));
    }

    public void setPCMU(int payload) {
        formats.put(payload, AVProfile.PCMU);
    }

    public int getPCMU() {
        return this.getPayload(AVProfile.PCMU);
    }

    public void setPCMA(int payload) {
        formats.put(payload, AVProfile.PCMA);
    }

    public int getPCMA() {
        return this.getPayload(AVProfile.PCMA);
    }

    protected int getPayload(Format fmt) {
        Collection<Integer> list = formats.values();
        for (Integer payload : list) {
            Format format = (Format) formats.get(payload);
            if (fmt.matches(format)) {
                return payload;
            }
        }
        return -1;
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#getPortRange();
     */
    public String getPortRange() {
        return lowPortNumber + "-" + highPortNumber;
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#setPortRange(String);
     */
    public void setPortRange(String portRange) {
        String[] tokens = portRange.split("-");
        lowPortNumber = Integer.parseInt(tokens[0]);
        highPortNumber = Integer.parseInt(tokens[1]);
    }

    /**
     * Indicates that endpoint has connections.
     * 
     * @return true if endpoint executing one or more connection and false otherwise.
     */
    public boolean hasConnections() {
        return this.hasConnections;
    }

    /**
     * Gets connection with specified identifier.
     * 
     * @param connectionID the identifier of the connection to return
     */
    public BaseConnection getConnection(String connectionID) {
        return (BaseConnection) connections.get(connectionID);
    }

    /**
     * Gets all connections which are executed by this endpoint.
     *  
     * @return collection of BaseConnection objects.
     */
    public Collection<BaseConnection> getConnections() {
        return connections.values();
    }

    public Object getResource(String resourceName, String connectionID) {
        return resources.get(resourceName + "_" + connectionID);
    }

    public Object getResource(String resourceName) {
        return resources.get(resourceName);
    }

    /**
     * Used for internal connection creation.
     * 
     * @param endpoint the endpoint which creates connection.
     * @param mode the mode of connection been created.
     * @return Object implementing connection.     * 
     * @throws org.mobicents.media.server.spi.ResourceUnavailableException
     */
    private Connection doCreateConnection(Endpoint endpoint, int mode)
            throws ResourceUnavailableException {
        return new BaseConnection(endpoint, mode);
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#createConnection(int);
     */
    public synchronized Connection createConnection(int mode) throws TooManyConnectionsException, ResourceUnavailableException {
        hasConnections = true;
        try {
            if (connections.size() == connections.capacity()) {
                throw new TooManyConnectionsException("Maximum " +
                        connections.capacity() + " connections allowed");
            }

            //Connection connection = new BaseConnection(this, mode);
            Connection connection = doCreateConnection(this, mode);
            connections.put(connection.getId(), connection);

            return connection;
        } finally {
            hasConnections = connections.size() > 0;
        }
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#deleteConnection();
     */
    public synchronized void deleteConnection(String connectionID) {
        Connection connection = (Connection) connections.remove(connectionID);

        if (connection != null) {
            connection.close();
        }

        hasConnections = connections.size() > 0;
        logger.info("Deleted connection " + connection);
        
        //clean all resources associated with this connection
        Set<String> names = resources.keySet();
        List<String> connectionResources = new ArrayList();

        for (String name : names) {
            if (name.endsWith(connection.getId())) {
                connectionResources.add(name);
                MediaResource mediaResource = (MediaResource) resources.get(name);
                if (logger.isDebugEnabled()) {
                    logger.debug("Releasing resource: " + mediaResource);
                }
                mediaResource.release();
            }
        }

        for (String name : connectionResources) {
            MediaResource mediaResource = (MediaResource) resources.remove(name);
            if (logger.isDebugEnabled()) {
                logger.debug("Disposed resource: " + mediaResource);
            }
        }
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#deleteAllConnections();
     */
    public synchronized void deleteAllConnections() {
        Iterator list = connections.values().iterator();
        while (list.hasNext()) {
            Connection connection = (Connection) list.next();
            deleteConnection(connection.getId());
        }
    }

    /**
     * Adds a Listener.
     *
     * @param listener the listener instance to add.
     */
    public void addNotifyListener(NotificationListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener instance for remove.
     */
    public void removeNotifyListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Sends specified event to registered listeners.
     *
     * @param event the event to be sent.
     */
    public synchronized void sendEvent(NotifyEvent event) {
        for (NotificationListener listener : listeners) {
            listener.update(event);
        }
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#detect(int, NotificationListener, boolean);
     */
    public void subscribe(int eventID, NotificationListener listener,
            boolean persistent) {
        EventTrigger eventDetector =
                new EventTrigger(this, eventID, listener, persistent);
        this.addNotifyListener(eventDetector);
    }

    /**
     * (Non Java-doc).
     *
     * @see org.mobicents.media.server.spi.Endpoint#detect(int, NotificationListener, boolean);
     */
    public void subscribe(int eventID, String connectionID,
            String params[], NotificationListener listener) {
    }
}
