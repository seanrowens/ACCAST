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

import AirSim.Machinetta.Beliefs.UGSSensorReading;
//import AirSim.Machinetta.Beliefs.IMSensorReading;
import AirSim.Machinetta.Messages.RPMessage;
import Machinetta.Communication.UDPCommon;
import Machinetta.Communication.UDPMessage;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.NamedProxyID;
import java.net.DatagramPacket;

/**
 *
 * @author junyounk
 */
public class IMRI extends SimpleRI {
    
    /** Creates a new instance of RAPInterface */
    public IMRI() {
        Machinetta.Debugger.debug("Creating RAPInterface", 0, this);
        
        // @todo Move this to the default role the IMs take on or
        // something ...
        try {
	    NamedProxyID oper = new NamedProxyID("Operator0");
	    DirectedInformationRequirement dir;
	    Class dirClass;

	    dirClass = Class.forName("AirSim.Machinetta.Beliefs.Location");
	    dir = new DirectedInformationRequirement(dirClass, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir);
	    dirClass = Class.forName("AirSim.Machinetta.Beliefs.IMSensorReading");
	    dir = new DirectedInformationRequirement(dirClass, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(dir);

	    Class cAssetStateBelief = Class.forName("AirSim.Machinetta.Beliefs.AssetStateBelief");
	    DirectedInformationRequirement d4 = new DirectedInformationRequirement(cAssetStateBelief, oper);
	    InformationAgentFactory.addDirectedInformationRequirement(d4);
	    //	    InformationAgentFactory.addBeliefShareRequirement(
	    //                new BeliefShareRequirement(Class.forName("AirSim.Machinetta.Beliefs.Location"), 5));
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class : " + "AirSim.Machinetta.Beliefs.Location", 3, this);
        }        
    }
   
    protected void process(RPMessage rpMsg) {
	// @TODO: Add any additional rpMsg processing here before
	// calling parent.process
	switch (rpMsg.type) {
                            
	    // The IMRI is going to need a sensor        
	case SEARCH_SENSOR_READING:
	    {
		Machinetta.Debugger.debug("Handling SEARCH_SENSOR_READING " + rpMsg, 5, this);
		
		int x = ((Integer)rpMsg.params.get(0)).intValue();
		int y = ((Integer)rpMsg.params.get(1)).intValue();
		boolean found = ((Boolean)rpMsg.params.get(2)).booleanValue();
                            
		UGSSensorReading sr = new UGSSensorReading(proxyID,x, y, System.currentTimeMillis(), found);
		sr.setLocallySensed(true);
		state.addBelief(sr);
		state.notifyListeners();
		break;
	    }

	default:
	    super.process(rpMsg);
	}

    }
}
