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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AirSim.Machinetta;

import AirSim.Machinetta.Beliefs.ProxyEventData;
import AirSim.Machinetta.Beliefs.ProxyEventData.EventType;
import AirSim.Machinetta.Beliefs.Location;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.Messages.NavigationDataAP;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.Machinetta.Messages.*;
import AirSim.Machinetta.Beliefs.*;
import AirSim.Environment.Assets.Asset;
import AirSim.SARSensorReading;
import AirSim.ConfusionMatrixReading;
import AirSim.MarkOneEyeballReading;
import Machinetta.Communication.UDPCommon;
import Machinetta.Communication.UDPMessage;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.Debugger;
import Util.*;
import java.net.DatagramPacket;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;


/**
 *
 * @author junyounk
 */
public abstract class SimpleRI extends RAPInterfaceImplementation {

    protected Machinetta.State.BeliefType.ProxyID proxyID = null;
        
    /** Creates a new instance of RAPInterface */
    public SimpleRI() {
        Debugger.debug(0,"Creating RAPInterface");
       
        try {
	    NamedProxyID oper = new NamedProxyID("Operator0");
	    DirectedInformationRequirement dir;
	    Class dirClass;

	    dirClass = Class.forName("AirSim.Machinetta.Beliefs.ProxyEventData");
	    dir = new DirectedInformationRequirement(dirClass, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir);

	    dirClass = Class.forName("AirSim.Machinetta.Beliefs.AssetStateBelief");
	    dir = new DirectedInformationRequirement(dirClass, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir);

	    dirClass = Class.forName("AirSim.Machinetta.SensorReading");
	    dir = new DirectedInformationRequirement(dirClass, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir);

	    dirClass = Class.forName("AirSim.Machinetta.Beliefs.VehicleBelief");
	    dir = new DirectedInformationRequirement(dirClass, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir);
        } catch (ClassNotFoundException e) {
            Debugger.debug(3,"Could not find class : " + e);
        }        
    }
    
    public void sendMessage(Machinetta.RAPInterface.OutputMessages.OutputMessage msg) {
        if (proxyID == null) {
            Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
            proxyID = state.getSelf().getProxyID();
	    backendSI.setProxyID(proxyID);
        }
        
	send(msg, proxyID);
    }
 
    public InputMessage[] getMessages() {
        //throw new UnsupportedOperationException("Not supported yet.");
        Debugger.debug(3,"Unimplemented getMessages() being called ... ");
        return null;
    }
    
    /**
     * If needed, this method will be implemented in the subclass.
     */
    public void run() {
        
    }

    protected void process(RPMessage rpMsg) {
	//	Debugger.debug(1,"In SimpleRI, processing rpMsg="+rpMsg.toString());
	switch (rpMsg.type) {
                            
	case AIRDROP: {
	    Debugger.debug(1,"process: AIRDROP msg="+rpMsg.toString());
	    AirdropAP adap = (AirdropAP)rpMsg;
	    if(adap.returnedToBase) {
		ProxyEventData ped = new ProxyEventData(proxyID,EventType.LANDED,adap.time);
		ped.setLocallySensed(true);
		state.addBelief(ped);
		state.notifyListeners();
	    }
	    else if(adap.airdropExecuted) {
		ProxyEventData ped = new ProxyEventData(proxyID,EventType.DISMOUNTED_CONTENTS,adap.time);
		ped.setLocallySensed(true);
		state.addBelief(ped);
		state.notifyListeners();
	    }
	}
	    break;
	case SENSOR: {
	    Debugger.debug(1,"process: SENSOR msg="+rpMsg.toString());

	    Object srObj = rpMsg.params.get(0);

	    if(srObj instanceof SARSensorReading) {
		SARSensorReading ssr = (SARSensorReading) srObj;
		Machinetta.Debugger.debug(1, "process: SENSOR is SARSensorReading:"+ssr);
		Asset.Types type = ssr.getMostLikelyExClutter();
		double confidence = ssr.getProb(type);
		VehicleBelief vb = new VehicleBelief(ssr.time, type, confidence, ssr.x, ssr.y, ssr.z, ssr.state);
		vb.setSensor(proxyID);
		vb.setForceId(ssr.forceId);
		vb.setHeading(ssr.heading);
		Debugger.debug(1,"Sending "+vb);
		vb.setLocallySensed(true);
		state.addBelief(vb);
		state.notifyListeners();
	    }
	    else if(srObj instanceof MarkOneEyeballReading) {
		MarkOneEyeballReading mr = (MarkOneEyeballReading) srObj;
		Machinetta.Debugger.debug(1, "process: SENSOR is MarkOneEyeballReading:"+mr);
		Asset.Types type = mr.getMostLikelyExClutter();
		double confidence = mr.getProb(type);
		VehicleBelief vb = new VehicleBelief(mr.time, type, confidence, mr.x, mr.y, mr.z, mr.state);
		vb.setSensor(proxyID);
		vb.setForceId(mr.forceId);
		vb.setHeading(mr.heading);
		Debugger.debug(1,"Sending "+vb);
		vb.setLocallySensed(true);
		state.addBelief(vb);
		state.notifyListeners();
	    }
	    else if(srObj instanceof AirSim.SensorReading) {
		AirSim.SensorReading sr = (AirSim.SensorReading) srObj;
		Machinetta.Debugger.debug(1, "process: SENSOR is SensorReading:" +sr);
		Asset.Types type = sr.type;
		double confidence = 1.0;
		VehicleBelief vb = new VehicleBelief(sr.time, type, confidence, sr.x, sr.y, sr.z, sr.state);
		vb.setSensor(proxyID);
		vb.setForceId(sr.forceId);
		vb.setHeading(sr.heading);
		Debugger.debug(1,"Sending "+vb);
		vb.setLocallySensed(true);
		state.addBelief(vb);
		state.notifyListeners();
	    }
	}
	    break;
	case SAR_SENSOR: {
	    Debugger.debug(1,"process: SAR_SENSOR msg="+rpMsg.toString());
	    AirSim.SARSensorReading ssr = (AirSim.SARSensorReading)rpMsg.params.firstElement();
	    AirSim.Machinetta.SensorReading msr = new AirSim.Machinetta.SensorReading(ssr);
	    state.addBelief(msr);
	    state.notifyListeners();

	    Asset.Types type = ssr.getMostLikelyExClutter();
	    double confidence = ssr.getProb(type);
	    VehicleBelief vb = new VehicleBelief(ssr.time, type, confidence, ssr.x, ssr.y, ssr.z, ssr.state);
	    vb.setSensor(proxyID);
	    vb.setForceId(ssr.forceId);
	    vb.setHeading(ssr.heading);
	    Debugger.debug(1,"Sending "+vb);
	    vb.setLocallySensed(true);
	    state.addBelief(vb);
	    state.notifyListeners();
	}
	    break;
	case NAVIGATION_DATA: {
	    // NOTE: Since we're maybe getting rid of LOCATION
	    // as a message type and replacing it with
	    // NAVIGATION_DATA, we need to make sure we do
	    // anything LOCATION did (below).
	    NavigationDataAP nMsg = (NavigationDataAP)rpMsg;
                                
	    AssetStateBelief asb = new AssetStateBelief(proxyID, nMsg);		    
	    //	    Machinetta.Debugger.debug(0,"process: NAVIGATION_DATA msg resulting in AssetStateBelief = "+asb);

	    //	    Debugger.debug(1,"Sending "+asb);
	    // set locallySensed true in order to trigger any DIRs.
	    asb.setLocallySensed(true);
	    state.addBelief(asb);
	    state.notifyListeners();

	    state.getSelf().miscInfo.put("AssetState", asb);
	    state.addBelief(state.getSelf());
	}
	    break;

	case DISMOUNTED_CONTENTS: {
	    Debugger.debug(1,"process: DISMOUNTED_CONTENTS msg="+rpMsg.toString());
	    DismountedContentsAP dcap = (DismountedContentsAP)rpMsg;
	    ProxyEventData ped = new ProxyEventData(proxyID,EventType.DISMOUNTED_CONTENTS,dcap.loc);
	    ped.setLocallySensed(true);
	    state.addBelief(ped);
	    state.notifyListeners();
	}
	    break;

	case LANDED: {
	    Debugger.debug(1,"process: LANDED msg="+rpMsg.toString());
	    LandedAP landedAP = (LandedAP)rpMsg;
	    ProxyEventData ped = new ProxyEventData(proxyID,EventType.LANDED,landedAP.loc);
	    ped.setLocallySensed(true);
	    state.addBelief(ped);
	    state.notifyListeners();
	}
	    break;
	case IN_PATROL_AREA: {
	    Debugger.debug(1,"process: IN_PATROL_AREA msg="+rpMsg.toString());
	    InPatrolAreaAP ipaap = (InPatrolAreaAP)rpMsg;
	    ProxyEventData ped = new ProxyEventData(proxyID,EventType.IN_PATROL_AREA,ipaap.rect);
	    ped.setLocallySensed(true);
	    state.addBelief(ped);
	    state.notifyListeners();
	}
	    break;

	default:
	    Debugger.debug(1, "process: RAP to Proxy: unknown AP (Agent to Proxy) message = "+rpMsg.toString());
	}
    }
    
    int sent = 0, recv = 0;
    protected UDPCommon backendSI = new UDPCommon() {
        @Override
        public boolean processMessage(DatagramPacket packet) {
	    recv++;

            // This first if statement is basically just checking that the beliefs
            // from the XML file are properly loaded.
            if (proxyID == null) {
                Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
                while (state.getSelf() == null) {
                    Debugger.debug(3,"WARNING No self in beliefs!  Sleeping for 100ms and then trying again");
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }
                if (state.getSelf() == null) {
                    Debugger.debug(5,"No self in beliefs!! - exiting");
                    System.exit(-1);
                }
                proxyID = state.getSelf().getProxyID();
            }
            
            // Now process the message
            if (proxyID == null) {
                Debugger.debug(0,"Sending " + recv + "th message to proxy from RAP, but no proxyID");
		return false;
            }

	    if (!(UDPMessage.isRAPInput(packet.getData(), proxyID))) {
		// Leaving this debug in because with the new comms
		// lib fixes this really shouldn't ever happen.
		Debugger.debug(1,"Not consuming non RAPInput message, ID="+UDPMessage.getID(packet.getData())+" summary="+UDPMessage.summary(packet.getData()));
		return false;
	    }

	    UDPMessage msg = UDPMessage.getMessage(packet.getData());
	    Debugger.debug(0,"Sending " + recv + "th message to proxy from RAP: " + msg);
	    Machinetta.RAPInterface.InputMessages.InputMessage iMsg = msg.getInputMessage();
	    if (!(iMsg instanceof RPMessage)) {
		Debugger.debug(1,"RI got: " + iMsg);
		return true;
	    }
	    RPMessage rpMsg = (RPMessage)iMsg;
	    process(rpMsg);
	    return true;
        }        
    };
    
    // This function sends messages to the actual RAP.
    public synchronized void send(Object o, Machinetta.State.BeliefType.ProxyID id) {
        Debugger.debug(0,"Sending " + sent + "th message to RAP");
        synchronized(backendSI) {
            UDPMessage uMsg = new UDPMessage(id, new NamedProxyID(UDPMessage.SIM_NAME), (OutputMessage)o);
            backendSI.sendMessage(uMsg);
        }
    }   
}
