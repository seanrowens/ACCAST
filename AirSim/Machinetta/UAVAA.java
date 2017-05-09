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
 * UAVAA.java
 *
 * Created on February 7, 2006, 4:53 PM
 *
 */

package AirSim.Machinetta;

import Machinetta.RAPInterface.OutputMessages.NewRoleMessage;
import Machinetta.RAPInterface.OutputMessages.RoleCancelMessage;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;

/**
 *
 * @author pscerri
 */
public class UAVAA extends Machinetta.AA.SimpleAA {
    
    /** Creates a new instance of UAVAA */
    public UAVAA() {
    }
    
    /**
     * Tell the RAP that it is no longer performing this role.
     * Note: this is not supposed to be "intelligent", AA reasoning
     * for deciding to get rid of role should have been performed already
     *
     */
    protected void removeRole(RoleBelief r) {
        RoleCancelMessage msg = new RoleCancelMessage(r);
        messageToRAP(msg);
    }
    
    /**
     * Tell the RAP that it is to perform this role.
     * Note: this is not supposed to be "intelligent", AA reasoning
     * for deciding to accept role should have been performed already
     *
     */
    protected void addRole(RoleBelief r) {
        if (r instanceof BasicRole) {
            
            BasicRole basic = (BasicRole)r;
            
            switch(basic.getType()) {
                
	        case intelSurveilRecon:
                    
                    Machinetta.Debugger.debug(1, "UAV sent to ISR!!");
                    NewRoleMessage msg = new NewRoleMessage(basic);
                    messageToRAP(msg);
                    
                    break;
                
                case geolocateSense:
                    
                    Machinetta.Debugger.debug(1, "UAV sent to geo-locate!!");
                    msg = new NewRoleMessage(basic);
                    messageToRAP(msg);
                    
                    break;
                
                case scan:
                    Machinetta.Debugger.debug(1, "UAV being told to scan");
                    msg = new NewRoleMessage(basic);
                    messageToRAP(msg);

                    break;
                    
                case patrol:
                    Machinetta.Debugger.debug(1, "UAV being told to patrol");
                    msg = new NewRoleMessage(basic);
                    messageToRAP(msg);

                    break;
                    
                case attackFromAir:
                case attackFromAirOrGround:
                    Machinetta.Debugger.debug(1, "UAV being told to attack a ground target");
                    msg = new NewRoleMessage(basic);
                    messageToRAP(msg);
                    break;
                            
                case provideScanData:
                    Machinetta.Debugger.debug(1, "UAV asked to provide scan data - AA needs to do nothing.");
                    break;
                    
                case EOImage:
                    Machinetta.Debugger.debug(1, "UAV asked to take EOImage");
                    msg = new NewRoleMessage(basic);
                    messageToRAP(msg);
                    break;
                    
                default:
                    Machinetta.Debugger.debug(4, "Unhandled role type for UAVAA: " + basic.getType());
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
}
