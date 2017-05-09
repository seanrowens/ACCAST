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
package AirSim.Commander;

import AirSim.UnitIDs;

import AirSim.Machinetta.Beliefs.*;
import AirSim.Machinetta.Beliefs.ProxyEventData;
import AirSim.Machinetta.Beliefs.ProxyEventData.EventType;
import AirSim.Machinetta.SimTime;

import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;

import java.util.*;


public class PhaseDIAirliftAndConvoy extends ScenarioPhase {

    // average of 40 runs, this phase takes 69 seconds.  If it's not
    // ready for next phase after this many seconds, override and go
    // onto next phase.
    private static final long DURATION_MS = 99000;

    private class TransportMission {
	double xPos;
	double yPos;
	
	private ArrayList<NamedProxyID> subteam;
	private PlanAgent planAgent;
	private HashMap<ProxyID,Boolean> dismountedContentsMap = new HashMap<ProxyID,Boolean>();
    }

    private TransportMission trans1;
    private TransportMission trans2;

    private ArrayList<TransportMission> transports = new ArrayList<TransportMission>();

    private ProxyState state = new ProxyState();

    public PhaseDIAirliftAndConvoy(int phaseNum,  WorldStateMgr wsm, SimUserUtils suu, SimUserSubTeams sust) {
	super(phaseNum, wsm, suu, sust);

	// TASK AUGV-BLUE3 ATTACK 1500 250 0
	// TASK AUGV-BLUE4 ATTACK 1500 250 0
	// TASK HU-BLUE1 TRANSPORT 1500 250 0
	// TASK HU-BLUE2 TRANSPORT 1500 250 0
	trans1 = new TransportMission();
	trans1.xPos = 1500;
	trans1.yPos = 250;
	trans1.subteam = sust.getSubTeam(sust.CONVOY_TEAM);
	for(NamedProxyID pid: trans1.subteam) {
	    trans1.dismountedContentsMap.put(pid, false);
	}
	transports.add(trans1);

	// TASK C130-BLUE0 TRANSPORT 1500 1200 0000
	trans2 = new TransportMission();
	trans2.xPos = 1450;
	trans2.yPos = 2100;
	trans2.subteam = sust.getSubTeam(sust.AIRLIFT_TEAM);
	for(NamedProxyID pid: trans2.subteam) {
	    trans2.dismountedContentsMap.put(pid, false);
	}
	transports.add(trans2);
    }

    private boolean phaseDone = false;
    private long startTime;

    // Start the phase
    public void instantiatePhasePlans() {
	startTime = System.currentTimeMillis();

	// Create AUGV transport plan.
	for(TransportMission transport: transports) {
	    transport.planAgent = suu.createTransportPlan(transport.subteam, transport.xPos, transport.yPos);
	}

	state.addChangeListener(new StateChangeListener() {
		public void stateChanged(BeliefID[] b) {
		    for (BeliefID bel: b) {
			Belief belief = state.getBelief(bel);
			if(belief instanceof ProxyEventData) {
			    ProxyEventData ped = (ProxyEventData) belief;
			    if(ped.type == EventType.DISMOUNTED_CONTENTS) {
				for(TransportMission transport: transports) {
				    for(ProxyID pid: transport.subteam) {
					if(ped.proxyID.equals(pid))
					    transport.dismountedContentsMap.put(pid,true);
				    }
				}
			    }
			}
			else {
			    // ignore Belief
			}
		    }
		}
	    });

	startTime = System.currentTimeMillis();
    }

    private boolean missionReady(TransportMission transport) {
	// We're checking that all of our subteam members either have
	// arrived at destination and unloaded their passengers, OR
	// are dead.
	for(NamedProxyID pid: transport.subteam) {
	    // If this guy has dismounted his contents, go on to check
	    // the other guys.
	    if(true == transport.dismountedContentsMap.get(pid))
		continue;
	    // If this guy has not dismounted his contents and is not
	    // assumed dead then we're not ready. (I.e. he's still on
	    // his way.)
	    StateData sd = wsm.get(pid);
	    if(!sd.checkAssumedDead())
		return false;
	}
	// Everyone is either dead or has already dismounted.  We're
	// good to go!
	return true;
    }

    // Check if ready for next phase.
    public boolean readyForNextPhase() {
	// Time limit override
	long now = System.currentTimeMillis();
	if((now - startTime) > DURATION_MS) {
	    Machinetta.Debugger.debug(1,"Overriding end of phase conditions - time "+(now - startTime)+" is beyond limit "+DURATION_MS);
	    return true;
	}

	// ready for next phase when all transports have dismonuted
	// their contents or have been destroyed.
	for(TransportMission trans: transports) {
	    if(!missionReady(trans))
		return false;
	}
	phaseDone = true;
	return true;
    }

    // Check if the phase is done
    public boolean phaseDone() {
	return phaseDone;
    }

    // do any cleanup on the phase after it is done.  I guess
    // theoretically at least, this should also terminate the phase
    // (and any running plans) even if phaseDone() returns false.
    public void endPhase() {
	phaseDone = true;
	for(TransportMission transport: transports) {
	    transport.planAgent.terminate();
	}
    }
    
}
