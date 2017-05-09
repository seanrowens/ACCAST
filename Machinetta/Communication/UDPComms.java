/*******************************************************************************
 * Copyright (C) 2017, Paul Scerri, Sean R Owens
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
/*
 * UDPComms.java
 *
 * Created on June 9, 2005, 10:56 AM
 */

package Machinetta.Communication;

import Machinetta.ConfigReader;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author pscerri
 */
public class UDPComms extends CommImplementation {
    private final static int QOS_SERVER_PORT_DEFAULT = 4433;
    private QOSServer qosServer;
    private QOSImpl qosImpl;
    
    public int received = 0, sent = 0;
    protected UDPCommon backend = new UDPCommon() {
        
        public boolean processMessage(DatagramPacket packet) {
	    // Short circuit quick test - if the first byte is not set
	    // to UDPMessage.PP then this is no a proxy to proxy
	    // message, so throw it away.
            if(!UDPMessage.isCommToLocal(packet.getData()))
	       return false;

	    //            Machinetta.Debugger.debug( 0,"Comms for " + id + " got message ... ");

	    // this version of isCommToLocal also takes our a proxy
	    // id, in this case our own, and returns false if this
	    // packet is not addressed to us, i.e. we drop this
	    // packet if it isn't meant for our id.
            if (UDPMessage.isCommToLocal(packet.getData(), id)) {
                UDPMessage msg = UDPMessage.getMessage(packet.getData());
                newMessage(msg.getMessage());
		//                Machinetta.Debugger.debug( 0,"Sending " + ++received + "th message to proxy: " + msg.getMessage());
                incomingMessage();
                
                return true;
            }
            return false;
        }
    };
    {
	backend.setOnlyRP(false);
	backend.setOnlyPR(false);
	backend.setOnlyPP(true);
    }

    
    
    
    /** Creates a new instance of UDPComms */
    public UDPComms() {
        
        /*  For APTIMs */
        /*
	int serverPort = ConfigReader.getConfig("QOS_SERVER_PORT", QOS_SERVER_PORT_DEFAULT, false);
	qosImpl = new QOSImpl();
        qosServer = new QOSServer(serverPort,qosImpl);
	qosServer.start();
         */
    }
    
    public void sendMessage(Message m) {
        synchronized(backend) {
            Machinetta.Debugger.debug( 0,"Sending " + ++sent + "th message");
            UDPMessage uMsg = new UDPMessage(id, m.getDest(), m);
            backend.sendMessage(uMsg);
        }
    }
    
    public Message[] recvMessages() {
        return backend.recvMessages();
    }
    
    public void setProxyID(Machinetta.State.BeliefType.ProxyID id) {
        super.setProxyID(id);
        backend.setProxyID(id);
    }
    
    
}
