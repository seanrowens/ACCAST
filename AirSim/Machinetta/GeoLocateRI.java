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
 * GeoLocateRI.java
 *
 * Created on January 30, 2006, 12:52 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Machinetta.Beliefs.GeoLocateResult;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.NamedProxyID;


import Machinetta.RAPInterface.RAPInterfaceImplementation;

/**
 *
 * @author pscerri
 */
public class GeoLocateRI extends RAPInterfaceImplementation  {
    
    /** Creates a new instance of GeoLocateRI */
    public GeoLocateRI() {
        // Not sure this is necessary, since it is on the plan
        /*
        try {
	    NamedProxyID oper = new NamedProxyID("Operator0");
	    DirectedInformationRequirement dir1;
	    Class cGeoLocateResult = Class.forName("AirSim.Machinetta.Beliefs.GeoLocateResult");
	    dir1 = new DirectedInformationRequirement(cGeoLocateResult, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir1);
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class : " + e, 3, this);
        }
         */
    }
        
    /**
     * @todo Need to implement
     *
     * Actually, this probably doesn't need to do anything.
     */
    public void run() {
        
    }
    
    /**
     * @todo Need to implement
     */
    public void sendMessage(Machinetta.RAPInterface.OutputMessages.OutputMessage msg) {
        
    }
    
    /**
     * @todo Need to implement
     */
    public Machinetta.RAPInterface.InputMessages.InputMessage[] getMessages() {
        
        return null;
    }

}
