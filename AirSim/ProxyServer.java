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
 * ProxyServer.java
 *
 * Created on July 9, 2004, 7:38 PM
 */

package AirSim;

import Machinetta.Communication.UDPCommon;
import Machinetta.Communication.UDPMessage;
import AirSim.Environment.*;
import AirSim.Environment.Assets.Asset;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author  pscerri
 */
public class ProxyServer {
    
    Env env = new Env();
    
    public int[] getSentMsgCounts() { return backend.getSentMsgCounts(); }
    public long getTotalSent() { return backend.getTotalSent(); }
    public long getTotalAcksSent() { return backend.getTotalAcksSent(); }
    public long getTotalAckReqSent() { return backend.getTotalAckReqSent(); }
    public long getTotalNoAckReqSent() { return backend.getTotalNoAckReqSent(); }
    public long getTotalResentAtLeastOnce() { return backend.getTotalResentAtLeastOnce(); }
    public long getTotalResentSum() { return backend.getTotalResentSum(); }
    public long getTotalFailed() { return backend.getTotalFailed(); }

    /** Creates a new instance of Switch */
    public ProxyServer() {
        
        
        // Machinetta.Debugger.debug("\n\n\n\n PROXY SERVER DISABLED \n\n\n\n", 1, this);
        
        Asset.setProxy(this);                        
    }
    
    protected UDPCommon backend = new UDPCommon() {
        public boolean processMessage(DatagramPacket packet) {
            
            if (UDPMessage.isRAPOutput(packet.getData())) {
                
                UDPMessage msg = UDPMessage.getMessage(packet.getData());
                AirSim.Machinetta.Messages.PRMessage oMsg = (AirSim.Machinetta.Messages.PRMessage)msg.getOutputMessage();
//                 Machinetta.Debugger.debug("Sending packet to sim: " + oMsg, 1, this);
                Machinetta.State.BeliefType.ProxyID to = msg.getSource();
                if (to != null) {
                    Asset a = env.getAssetByID(to.toString());
                    if (a != null) a.msgFromProxy(oMsg);
                    else Machinetta.Debugger.debug("Message from proxies (from "+msg.getSource()+") to unknown entity (to "+to+"): " + oMsg, 3, this);
                } else {
                    Machinetta.Debugger.debug("To field of message was empty", 3, this);
                }
                
                return true;
            } else {
                return false;
            }
        }
    };
    {
	backend.setOnlyRP(false);
	backend.setOnlyPR(true);
	backend.setOnlyPP(false);
	backend.setProxyID(new Machinetta.State.BeliefType.NamedProxyID(UDPMessage.SIM_NAME));
    }
    
    public synchronized void send(Object o, String id) {
        	
		//	System.err.println("Sending packet for proxy");
        synchronized (backend) {                   
            UDPMessage uMsg = new UDPMessage(new NamedProxyID(UDPMessage.SIM_NAME), new NamedProxyID(id), (InputMessage)o);
            backend.sendMessage(uMsg);
            Machinetta.Debugger.debug("Sent to id "+id+" msg="+o.toString() + " ReqsAck? " + uMsg.reqAck, 0, this);
        }       
    }
    
}
