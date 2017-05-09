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
 * RAPInterface.java
 *
 * Created on June 17, 2005, 9:51 AM
 *
 */

package AirSim.Machinetta;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Machinetta.Beliefs.*;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.Machinetta.Messages.NavigationDataAP;
import Machinetta.Communication.*;
import Machinetta.Coordination.MAC.RoleAgent;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.Debugger;
import Machinetta.State.BeliefType.NamedProxyID;
import Util.*;

import java.net.*;
import java.util.Hashtable;

/**
 *
 * @author pscerri
 */
//@TODO: Should we change this to extend SimpleRI?
public class WASMRI extends Machinetta.RAPInterface.RAPInterfaceImplementation {
    
    Machinetta.State.BeliefType.ProxyID proxyID = null;
    
    /** Creates a new instance of RAPInterface */
    public WASMRI() {
        Debugger.debug(0,"Creating RAPInterface");
    }
    
    public void sendMessage(Machinetta.RAPInterface.OutputMessages.OutputMessage msg) {
        if (proxyID == null) {
            Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
            proxyID = state.getSelf().getProxyID();
        }
        send(msg, proxyID);
    }
    
    public void run() {
        // Not required, UDPCommon does the work.
    }
    
    /*
     * Location is directly added to state, not an input message
     */
    public Machinetta.RAPInterface.InputMessages.InputMessage[] getMessages() {
        Debugger.debug(3,"Unimplemented getMessages() being called ... ");
        return null;
    }
    
    int sent = 0, recv = 0;
    protected UDPCommon backend = new UDPCommon() {
        public boolean processMessage(DatagramPacket packet) {
            
            Debugger.debug(0,"RAPInterface processing message: " + packet);
            
            if (proxyID == null) {
                Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
		while (state.getSelf() == null) {
                    Debugger.debug(3,"WARNING No self in beliefs!  Sleeping for 100ms and then trying again");
		    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
                if (state.getSelf() == null) {
                    Debugger.debug(5,"ERROR ERROR ERROR ERROR ERROR No self in beliefs!! - exiting");
                    System.exit(-1);
                }
                proxyID = state.getSelf().getProxyID();
            }
            
            if (proxyID == null)
		return false;
	    if (!(UDPMessage.isRAPInput(packet.getData(), proxyID))) 
		return false;
	    UDPMessage msg = UDPMessage.getMessage(packet.getData());
	    Debugger.debug(0,"Sending " + ++recv + "th message to proxy from RAP" + msg);
	    Machinetta.RAPInterface.InputMessages.InputMessage iMsg = msg.getInputMessage();
	    if (!(iMsg instanceof RPMessage))
		return true;

	    RPMessage rpMsg = (RPMessage)iMsg;
	    switch (rpMsg.type) {
	    case FREEFORM:
		Debugger.debug(3,"Don't know what to do with freeform message");
		break;

	    case NAVIGATION_DATA:
		{
		    // NOTE: Since we're maybe getting rid of LOCATION
		    // as a message type and replacing it with
		    // NAVIGATION_DATA, we need to make sure we do
		    // anything LOCATION did (below).
		    NavigationDataAP nMsg = (NavigationDataAP)iMsg;
                                
		    AssetStateBelief asb = new AssetStateBelief(proxyID, nMsg);

		    // set locallySensed true in order to trigger any DIRs.
		    asb.setLocallySensed(true);
		    state.addBelief(asb);
		    state.notifyListeners();
                                                                
		    state.getSelf().miscInfo.put("AssetState", asb);
		    state.addBelief(state.getSelf());
		}
		break;
                                
	    case SENSOR:
		Debugger.debug(1,"Got sensor reading");
		AirSim.SensorReading sr = (AirSim.SensorReading)rpMsg.params.firstElement();
		if (sr.type.equals(Asset.Types.M2)) {
		    Debugger.debug(1,"Not Ignoring M2 sensor reading ");
		    SensorReading msr = new SensorReading(AirSim.Environment.Assets.Asset.Types.M2, new PositionMeters(sr.x, sr.y, sr.z), state.getSelf().getProxyID());
		    state.addBelief(msr);
		    state.notifyListeners();
		} else {
		    Debugger.debug(1,"Ignoring Sensor reading about " + sr.type);
		}
		break;
	    case SAR_SENSOR:
		Debugger.debug(1,"Got SAR sensor reading");
		AirSim.SARSensorReading ssr = (AirSim.SARSensorReading)rpMsg.params.firstElement();
		SensorReading msr = new SensorReading(ssr);
		state.addBelief(msr);
		state.notifyListeners();
		break;
	    default:
		Debugger.debug(1, "Proxy to RAP message of type " + rpMsg.type + " not handled");
	    }
	    return true;
        }
    };
    
    public synchronized void send(Object o, Machinetta.State.BeliefType.ProxyID id) {
        Debugger.debug("Sending " + ++sent + "th RAP message", 0, this);
        synchronized(backend) {
            UDPMessage uMsg = new UDPMessage(id, new NamedProxyID(UDPMessage.SIM_NAME), (OutputMessage)o);
            backend.sendMessage(uMsg);
        }
    }
}
