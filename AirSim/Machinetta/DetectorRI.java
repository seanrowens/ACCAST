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
 * DetectorRI.java
 *
 * Created on January 30, 2006, 12:49 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Machinetta.Beliefs.GeoLocateRequest;
import AirSim.Machinetta.Messages.DetectDataPA;
import AirSim.Machinetta.Messages.GeoLocateRequestAP;
import AirSim.Machinetta.Messages.PRMessage;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.NamedProxyID;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author pscerri
 */
public class DetectorRI extends RAPInterfaceImplementation {
    
    Vector<InputMessage> incomingMessages = new Vector<InputMessage>();
    
    /**
     * For purposes of CMU testing,
     */
    private Hashtable<Long,Vector<DetectDataPA>> data = new Hashtable<Long,Vector<DetectDataPA>>();
    
    /** Creates a new instance of DetectorRI */
    public DetectorRI() {
        try {
	    NamedProxyID oper = new NamedProxyID("Operator0");
	    DirectedInformationRequirement dir1;
	    Class cGeoLocateRequest = Class.forName("AirSim.Machinetta.Beliefs.GeoLocateRequest");
	    dir1 = new DirectedInformationRequirement(cGeoLocateRequest, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir1);
	    Class cAssetStateBelief = Class.forName("AirSim.Machinetta.Beliefs.AssetStateBelief");
	    DirectedInformationRequirement d4 = new DirectedInformationRequirement(cAssetStateBelief, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(d4);
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class : " + e, 3, this);
        }

    }
    
    /**
     * @fix Need to implement
     *
     * Actually, this probably doesn't need to do anything.
     */
    public void run() {
        
    }
    
    /**
     * For purposes of CMU testing, "output messages" are actually just processed
     * here and an appropriate response generated.
     *
     * The Detector agent only gets one type of message (DETECT_DATA)
     */
    public void sendMessage(Machinetta.RAPInterface.OutputMessages.OutputMessage msg) {
        
        PRMessage m = (PRMessage)msg;
        
        switch(m.type) {
            
            case DETECT_DATA:
                
                Machinetta.Debugger.debug(1, "DetectorRI processing data for discerning");
                
                final DetectDataPA ddm = (DetectDataPA)m;
                Vector<DetectDataPA> soFar = data.get(ddm.readingID);
                if (soFar == null) {
                    soFar = new Vector<DetectDataPA>();
                    data.put(ddm.readingID, soFar);
                }
                
                soFar.add(ddm);
                
                if (soFar.size() == ddm.blockCount) {
                    Machinetta.Debugger.debug(1, "Have all blocks for detection");
                    
                    // This is a CMU cheat, pretending to be the "agent" thinking away ....
                    // But do it in a thread so that RI can continue on
                    (new Thread() {
                        public void run() {
                            
                            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ddm.data));
                            short type = -1;
                            int emitterx = -1;
			    int emittery = -1;
			    int emitterz = 0;
                            int sensorx = -1;
			    int sensory = -1;
			    int sensorz = 0;

                            try {
                                type = dis.readShort();
                                emitterx = dis.readInt();
                                emittery = dis.readInt();
                                emitterz = dis.readInt();
                                sensorx = dis.readInt();
                                sensory = dis.readInt();
                                sensorz = dis.readInt();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            if (type == 1) { // Its an emitter, otherwise we don't care
                                // Sleep some time to simulate the fact that the dectector agent will be slow.
                                try {
                                    sleep((new Random()).nextInt(500) + 1000);
                                } catch (InterruptedException e) {}
                                
                                Machinetta.Debugger.debug(1, "Detect agent has recognized an emitter!");
                                                             
                                // Now it needs to pretend to send a message from the agent
                                GeoLocateRequestAP inMsg = new GeoLocateRequestAP();
                                inMsg.bandwidth = 77777.7;
                                inMsg.emitterID = ddm.readingID;
                                inMsg.frequency = 77777.7;
                                inMsg.pulseWidth = 77777.7;
                                inMsg.longtitude = sensorx;
                                inMsg.latitude = sensory;
                                
                                // Now, really this will go via some interface from agent to proxy
                                // but, because we are cheating .... 
                                newGeolocateRequest(inMsg);
                            }
                        }
                    }).start();
                } else {
                    Machinetta.Debugger.debug(1, "Waiting for more data");
                }
                
                break;
                
            default:
                
                Machinetta.Debugger.debug(5, "Unexpected Message received by Detector RI of type: " + m.type);
                
        }
        
    }
    
    /**
     * Handle an incoming geolocate request by creating a belief and putting
     * it in the state.
     */
    private void newGeolocateRequest(GeoLocateRequestAP msg) {
        GeoLocateRequest reqBel = new GeoLocateRequest();
        reqBel.bandwidth = msg.bandwidth;
        reqBel.emitterID = msg.emitterID;
        reqBel.frequency = msg.frequency;
        reqBel.pulseWidth = msg.pulseWidth;
        reqBel.latitude = msg.latitude;
        reqBel.longtitude = msg.longtitude;
        
        reqBel.setLocallySensed(true);
        Machinetta.Debugger.debug(1, "GeoLocateRequest being added to state");
        state.addBelief(reqBel);
        state.notifyListeners();
        Machinetta.Debugger.debug(1, "GeoLocateRequest added to state");
    }
    
    /**
     * Sends back messages in incomingMessages then empties the vectory
     *
     * (Might want some sort of sync here.)
     */
    public Machinetta.RAPInterface.InputMessages.InputMessage[] getMessages() {
        
        InputMessage [] msgs = incomingMessages.toArray(new InputMessage[1]);
        
        incomingMessages.clear();
        
        return msgs;
    }
    
}
