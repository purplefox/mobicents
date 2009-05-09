/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mobicents.media.server;

import java.net.URL;
import java.util.HashMap;
import static org.junit.Assert.assertEquals;

import java.util.Hashtable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.media.Buffer;
import org.mobicents.media.Component;
import org.mobicents.media.ComponentFactory;
import org.mobicents.media.Format;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.clock.TimerImpl;
import org.mobicents.media.server.impl.resource.audio.AudioPlayerEvent;
import org.mobicents.media.server.impl.resource.audio.AudioPlayerFactory;
import org.mobicents.media.server.impl.rtp.RtpFactory;
import org.mobicents.media.server.impl.rtp.sdp.AVProfile;
import org.mobicents.media.server.resource.ChannelFactory;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.ConnectionMode;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.NotificationListener;
import org.mobicents.media.server.spi.Timer;
import org.mobicents.media.server.spi.events.NotifyEvent;
import org.mobicents.media.server.spi.resource.AudioPlayer;

/**
 *
 * @author kulikov
 */
public class AnnouncementTest {

    private Timer timer;
    private EndpointImpl sender;
    private EndpointImpl receiver;
    
    private AudioPlayerFactory playerFactory;
    private TestSinkFactory sinkFactory;
    
    private ChannelFactory channelFactory;
    private RtpFactory rtpFactory;
    
    private Semaphore semaphore;
    private boolean res;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        semaphore = new Semaphore(0);
        res = false;
        
        timer = new TimerImpl();
        
        HashMap<Integer, Format> rtpmap = new HashMap();
        rtpmap.put(0, AVProfile.PCMA);
        rtpmap.put(8, AVProfile.PCMU);
        
        rtpFactory = new RtpFactory();
        rtpFactory.setBindAddress("localhost");
        rtpFactory.setPortRange("1024-65535");
        rtpFactory.setJitter(60);
        rtpFactory.setTimer(timer);
        rtpFactory.setFormatMap(rtpmap);
        
        Hashtable<String, RtpFactory> rtpFactories = new Hashtable();
        rtpFactories.put("audio", rtpFactory);
        
        playerFactory = new AudioPlayerFactory();
        playerFactory.setName("audio.player");
        
        sinkFactory = new TestSinkFactory();
        
        channelFactory = new ChannelFactory();
        channelFactory.start();
        
        sender = new EndpointImpl("test/announcement/sender");
        sender.setTimer(timer);
        
        sender.setRtpFactory(rtpFactories);
        sender.setSourceFactory(playerFactory);
        sender.setTxChannelFactory(channelFactory);
        
        sender.start();
        
        receiver = new EndpointImpl("test/announcement/receiver");
        receiver.setTimer(timer);
        
        receiver.setRtpFactory(rtpFactories);
        receiver.setSinkFactory(sinkFactory);
        receiver.setRxChannelFactory(channelFactory);
        
        receiver.start();        
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getLocalName method, of class EndpointImpl.
     */
    @Test
    public void testTransmission() throws Exception {
        System.out.println("======1");
        Connection rxConnection = receiver.createConnection(ConnectionMode.RECV_ONLY);
        System.out.println("======2");
        Connection txConnection = sender.createConnection(ConnectionMode.SEND_ONLY);
        
        txConnection.setRemoteDescriptor(rxConnection.getLocalDescriptor());
        rxConnection.setRemoteDescriptor(txConnection.getLocalDescriptor());
        
        Component c = sender.getComponent("audio.player");
            AudioPlayer player = (AudioPlayer)c;
        URL url = AnnouncementTest.class.getClassLoader().getResource(
		 "org/mobicents/media/server/impl/addf8-Alaw-GW.wav");
        player.setURL(url.toExternalForm());
        player.addListener(new PlayerListener());
        player.start();

        System.out.println("Started");
        semaphore.tryAcquire(10, TimeUnit.SECONDS);
        assertEquals(true, res);
    }

    private class PlayerListener implements NotificationListener {

        public void update(NotifyEvent event) {
            if (event.getEventID() == AudioPlayerEvent.END_OF_MEDIA) {
                semaphore.release();
                System.out.println("End of announcement");
            }
        }
        
    }
    
    private class TestSinkFactory implements ComponentFactory {

        public Component newInstance(Endpoint endpoint) {
            return new TestSink("test-sink");
        }
        
    }

    private class TestSink extends AbstractSink {

        public TestSink(String name) {
            super(name);
        }
        
        public Format[] getFormats() {
            return new Format[] {AVProfile.PCMA};
        }

        public boolean isAcceptable(Format format) {
            return true;
        }

        public void receive(Buffer buffer) {
            System.out.println("Receive " + buffer);
            res = true;
        }
        
    }
}