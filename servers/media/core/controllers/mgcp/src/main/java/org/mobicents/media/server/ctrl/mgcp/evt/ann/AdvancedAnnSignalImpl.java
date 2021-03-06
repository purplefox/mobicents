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

package org.mobicents.media.server.ctrl.mgcp.evt.ann;

import org.mobicents.media.server.ctrl.mgcp.Request;
import org.mobicents.media.server.ctrl.mgcp.evt.SignalGenerator;
import org.mobicents.media.server.spi.Connection;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.resource.audio.AdvancedAudioPlayer;

/**
 *
 * @author kulikov
 */
public class AdvancedAnnSignalImpl extends SignalGenerator {

    private AdvancedAudioPlayer audioPlayer;
    private String url;
    private String text;
    
    public AdvancedAnnSignalImpl(String resourceName, String params) {
        super(resourceName, params);
        
        url = null;
        text = null;
        
        String tokens[] = params.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("ts(")) {
                text = tokens[i].substring(3, tokens[i].length() - 1);
            }
        }
        
        if (text == null) {
            url = params;
        }
    }

    @Override
    protected boolean doVerify(Connection connection) {
        audioPlayer = (AdvancedAudioPlayer) connection.getComponent(getResourceName(), Connection.CHANNEL_TX);
        return audioPlayer != null;
    }

    @Override
    protected boolean doVerify(Endpoint endpoint) {
        audioPlayer = (AdvancedAudioPlayer)endpoint.getComponent(getResourceName());
        return audioPlayer != null;
    }

    @Override
    public void start(Request request) {
        if (url != null) {
            audioPlayer.setURL(url);
        } else {
            audioPlayer.setText(text);
        }
        audioPlayer.start();
    }

    @Override
    public void cancel() {
        audioPlayer.stop();
        audioPlayer = null;
    }


}
