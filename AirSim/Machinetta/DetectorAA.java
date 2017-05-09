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
 * DetectorAA.java
 *
 * Created on February 7, 2006, 4:59 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Machinetta.Beliefs.TMAScanResult;
import AirSim.Machinetta.Messages.DetectDataPA;
import Machinetta.AA.BeliefChangeHandler;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;

/**
 *
 * @author pscerri
 */
public class DetectorAA extends Machinetta.AA.SimpleAA {
    
    /** Creates a new instance of DetectorAA */
    public DetectorAA() {
    }
    
    /**
     * Tell the RAP that it is no longer performing this role.
     * Note: this is not supposed to be "intelligent", AA reasoning
     * for deciding to get rid of role should have been performed already
     *
     * @todo Implement
     */
    protected void removeRole(RoleBelief r) {
        Machinetta.Debugger.debug("Detector removed role - ignoring", 3, this);
    }
    
    /**
     * Tell the RAP that it is to perform this role.
     * Note: this is not supposed to be "intelligent", AA reasoning
     * for deciding to accept role should have been performed already
     *
     * @todo Implement
     */
    protected void addRole(RoleBelief r) {
        if (r instanceof BasicRole) {
            
            BasicRole basic = (BasicRole)r;
            
            switch(basic.getType()) {
                case emitterDiscriminate:
                    // At the moment, we don't really need to tell anyone anything.
                    // the tricky part is to work out when we should assume that we 
                    // are no longer doing the role ... for now we won't even put it 
                    // in the RAPBelief, hence it looks like it never happened ... 
                    // Eventually may require a "done" message from agent
                    Machinetta.Debugger.debug("Detector now has role : " + basic, 2, this);
                    break;
                default:
                    Machinetta.Debugger.debug("Unhandled role type for DetectorAA: " + basic.getType(), 4, this);
            }
        }
    }
    
    /**
     *
     * @todo This is just an example.
     *
     */
    public void requestCapability(RoleBelief role) {
        
        if (role instanceof BasicRole) {
            
            BasicRole basic = (BasicRole)role;
            CapabilityBelief cap = state.getSelf().getCapability(role.roleName);
            
            switch(basic.getType()) {
                
                case move:
                    
                    // This one is a bit tricky.  The actual capability to move is constant.
                    Machinetta.Debugger.debug("Set move capability to 100 with params " + role.getParams(), 1, this);
                    if (role.getParams() != null) cap.setCapability(role.getParams(), new Integer(100));
                    break;
                    
                default:
                    Machinetta.Debugger.debug("Don't know how to compute capability for " + role + " assuming 0", 1, this);
                    cap.setCapability(role.getParams(), new Integer(0));
            }
            state.addBelief(cap);
            state.notifyListeners();
        } else {
            Machinetta.Debugger.debug("Don't know how to get capability for " + role, 5, this);
        }
    }
    
    /**
     * In general, probably should only handle these beliefs as specifically proscribed by roles, 
     * but in this case, only ones proscribed by roles will even arrive.
     */
    protected BeliefChangeHandler roleHandler = new BeliefChangeHandler() {
        public void beliefChange(Belief b) {
            // @todo handle the case where there might be multiple blocks
            Machinetta.Debugger.debug("TMAScanResult arrived, processing: " + b, 1, this);
            TMAScanResult tmssr = (TMAScanResult)b;
            DetectDataPA msg = new DetectDataPA();
            msg.blockCount = tmssr.blockCount;
            msg.blockCounter = tmssr.blockCounter;
            msg.data = tmssr.data;
            msg.readingID = tmssr.readingID;
            msg.sensorID = tmssr.sensorID;
            
            messageToRAP(msg);
        }
    };
    
    /* This code sets up the handlers for belief changes and input messages*/
    {
        addBCHandler("AirSim.Machinetta.Beliefs.TMAScanResult", roleHandler);        
    }
}
