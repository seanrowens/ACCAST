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
 * UGSRI.java
 *
 * Created on March 13, 2006, 11:52 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Messages.RPMessage;
import Machinetta.Communication.UDPCommon;
import Machinetta.Communication.UDPMessage;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import java.net.DatagramPacket;

/**
 *
 * @author pscerri
 */
public class UGSRI extends Machinetta.RAPInterface.RAPInterfaceImplementation {
    
    /** Creates a new instance of UGSRI */
    public UGSRI() {
        
        // @todo Move this to the default role the UGSs take on
        // or something ... 
        try {
            InformationAgentFactory.addDirectedInformationRequirement(
                    new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.UGSSensorReading"), new NamedProxyID("Operator0")));
	    Class cAssetStateBelief = Class.forName("AirSim.Machinetta.Beliefs.AssetStateBelief");
	    DirectedInformationRequirement d4 = new DirectedInformationRequirement(cAssetStateBelief, new NamedProxyID("Operator0"));
	    InformationAgentFactory.addDirectedInformationRequirement(d4);
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class : " + "AirSim.Machinetta.Beliefs.UGSSensorReading", 3, this);
        }
	Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
	ProxyID proxyID = state.getSelf().getProxyID();
	backend.setProxyID(proxyID);
    }
    
    public void sendMessage(OutputMessage msg) {
        // @todo Implement
    }
    
    public void run() {
        // Do nothing
    }
    
    public InputMessage[] getMessages() {
        
        // @todo Probab
        return null;
    }
    
    int sent = 0, recv = 0;
    protected UDPCommon backend = new UDPCommon() {
        
        ProxyID proxyID = null;
        
        public boolean processMessage(DatagramPacket packet) {
            
	    //            Machinetta.Debugger.debug("UGSRI processing message: " + packet, 1, this);
            
            // This first if statement is basically just checking that the beliefs
            // from the XML file are properly loaded.
            if (proxyID == null) {
                Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
                while (state.getSelf() == null) {
                    Machinetta.Debugger.debug("WARNING No self in beliefs!  Sleeping for 100ms and then trying again", 5, this);;
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
                if (state.getSelf() == null) {
                    Machinetta.Debugger.debug("No self in beliefs!! - exiting", 5, this);
                    System.exit(-1);
                }
                proxyID = state.getSelf().getProxyID();
            }
            
            if (UDPMessage.isRAPInput(packet.getData(), proxyID)) {
                UDPMessage msg = UDPMessage.getMessage(packet.getData());
		//                Machinetta.Debugger.debug("Sending " + ++recv + "th message to proxy from RAP" + msg, 1, this);
                Machinetta.RAPInterface.InputMessages.InputMessage iMsg = msg.getInputMessage();
                if (iMsg instanceof RPMessage) {
                    RPMessage rpMsg = (RPMessage)iMsg;
                    switch (rpMsg.type) {
                        case SEARCH_SENSOR_READING:
			    Machinetta.Debugger.debug("Handling SEARCH_SENSOR_READING " + msg, 5, this);
		
                            int x = ((Integer)rpMsg.params.get(0)).intValue();
                            int y = ((Integer)rpMsg.params.get(1)).intValue();
                            boolean found = ((Boolean)rpMsg.params.get(2)).booleanValue();
                            
                            UGSSensorReading sr = new UGSSensorReading(proxyID, x, y, System.currentTimeMillis(), found);
                            sr.setLocallySensed(true);
                            state.addBelief(sr);
                            state.notifyListeners();
                            break;
                            
                        default:
                            Machinetta.Debugger.debug("Unhandled message type: " + rpMsg.type, 3, this);
                    }
                } else {
                    Machinetta.Debugger.debug("Not RPMessage msg, class="+iMsg.getClass().getName()+", msg="+iMsg, 3, this);
                }
            } else {
		//                Machinetta.Debugger.debug("Not RAPInput, ignoring", 0, this);
            }
            
            return false;
        }
    };
}
