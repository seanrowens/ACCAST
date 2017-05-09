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

import AirSim.Environment.Vector3D;
import AirSim.Machinetta.CostMaps.SimpleDynamicCostMap;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.Machinetta.Messages.*;
import AirSim.Machinetta.Beliefs.*;
import Machinetta.Debugger;
import Machinetta.Communication.UDPCommon;
import Machinetta.Communication.UDPMessage;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.RAPInterface.OutputMessages.NewRoleMessage;
import Machinetta.RAPInterface.OutputMessages.RoleCancelMessage;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import java.net.DatagramPacket;

/**
 *
 * @author junyounk
 */
public class C130RI extends SimpleRI {

    private SimplePlanner planner = null;
    
    /** Creates a new instance of RAPInterface */
    public C130RI() {
	super();
        Debugger.debug("Creating RAPInterface", 0, this);
        planner = new SimplePlanner(this);
    }
    
    public void sendMessage(Machinetta.RAPInterface.OutputMessages.OutputMessage msg) {
        if (proxyID == null) {
            Machinetta.State.ProxyState state = new Machinetta.State.ProxyState();
            proxyID = state.getSelf().getProxyID();
        }

	// The RI intercepts these messages, so it can do some of the
	// work for the agent - basically the 'planner' actually does
	// the work of executing the role - it changes the costmaps
	// and replans movement based on whatever the current Role is.
        
        if (msg instanceof NewRoleMessage) {
            Debugger.debug("sendMessage: intercepted " + msg, 1, this);
            BasicRole role = (BasicRole)((NewRoleMessage)msg).getRole();
            planner.addRole(role);
        } else if (msg instanceof RoleCancelMessage) {
            Debugger.debug("sendMessage: intercepted " + msg, 1, this);
            BasicRole role = (BasicRole)((RoleCancelMessage)msg).getRole();
            planner.removeRole(role);
        } else {
            
            /*@NOTE: for C130, consider following things:
                     Take off
                     Land
                     Deploy Intelligent Mines
                     Deploy Infantry
                     Loiter (after they're done we want to move them off screen), etc?
             */
            
            send(msg, proxyID);            
        }
    }

    /**
     * This simply starts the planner off.
     *
     * Unfortunately, it can start the planner off too soon, hence the wait.
     */
    public void run() {
        do {
            try {
                sleep(100);
            } catch (InterruptedException e) {}
        } while(state.getSelf() == null);
        
        try {
            sleep(2000);
        } catch (InterruptedException e) {}
        planner.start();
    }
   
    protected void process(RPMessage rpMsg) {
	// @TODO: Add any additional rpMsg processing here before
	// calling parent.process
	super.process(rpMsg);
    }
}
