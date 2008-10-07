/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mobicents.media.server.impl.rtp;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mobicents.media.Buffer;
import org.mobicents.media.Format;
import org.mobicents.media.format.AudioFormat;
import org.mobicents.media.server.impl.AbstractSink;
import org.mobicents.media.server.impl.clock.Quartz;
import org.mobicents.media.server.impl.clock.Timer;

/**
 *
 * @author Oleg Kulikov
 */
public class ReceiveStreamTest {

    private final static int TEST_DURATION = 20;
    private final static double ERRORS = 5;
    private int MAX_ERRORS;
    private int period = 20;
    private int jitter = 30;
    private RtpSocket rtpSocket = new RtpSocketImpl(period, jitter);
    private ReceiveStream stream;
    private int expected = 0;
    //private int seq = 0;
    private int count = 0;
    private String message;
    private boolean completed = false;
    private boolean failed = false;
    private int errors;
    private Timer timer = new Timer();
    private AudioFormat PCMA = new AudioFormat("PCMA", 8000, 8, 1);
    private AudioFormat PCMU = new AudioFormat("PCMU", 8000, 8, 1);
    private AudioFormat DTMF = new AudioFormat("telephone-event", 8000, 8, 1);
    private ArrayList packets = new ArrayList();
    private int errorCount;

    public ReceiveStreamTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        int ps = 1000 / Quartz.HEART_BEAT;
        MAX_ERRORS = (int) Math.round(100.0 / ps * ERRORS);

        rtpSocket.addFormat(8, PCMA);
        rtpSocket.addFormat(0, PCMU);
        rtpSocket.addFormat(101, DTMF);

        stream = new ReceiveStream(rtpSocket, period, jitter);
    }

    @After
    public void tearDown() {
    }

    @Test
    @SuppressWarnings("static-access")
    public void testStream() {
        timer.setListener(new Sender());
        timer.start();

        try {
            stream.connect(new Sink());
            Thread.currentThread().sleep(TEST_DURATION * 1000);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        stream.stop();

        int k = 0;
        for (int i = 0; i < packets.size(); i++) {
            if (packets.get(i) != null) {
                k = i;
                break;
            }
        }

        long exp = 1;
        for (int i = k; i < packets.size(); i++) {
            Buffer buffer = (Buffer) packets.get(i);
            String s = new String((byte[]) buffer.getData(), buffer.getOffset(), buffer.getLength());
            int seq = 0;
            try {
                seq = Integer.parseInt(s);
            } catch (Exception e) {
                errorCount++;
                continue;
            }
            if (exp != seq) {
                exp = seq + 1;
                errorCount++;
            } else {
                exp++;
            }
        }

        if (errorCount > MAX_ERRORS) {
            fail("Too many errors: " + errorCount + ", max=" + MAX_ERRORS);
        }
        System.out.println("Total errors: " + errorCount + ", max=" + MAX_ERRORS);
    }

    public class Sender implements Runnable {

        private int seq = 0;

        @SuppressWarnings("static-access")
        public void run() {
            RtpPacket p = new RtpPacket((byte) 8, seq, seq, 1L,
                    new Integer(seq).toString().getBytes());
            stream.push(p);
            seq++;
        }
    }

    public class Sink extends AbstractSink {
    	
    	public Sink(){
    		super("ReceiveStreamTest.Sink");
    	}

        private AudioFormat f = new AudioFormat(AudioFormat.ALAW, 8000, 8, 1);
        
        public void receive(Buffer buffer) {
            packets.add(buffer);
        }

        public Format[] getFormats() {
            return new Format[]{f};
        }

        public boolean isAcceptable(Format format) {
            return format.matches(f);
        }

    }
}
