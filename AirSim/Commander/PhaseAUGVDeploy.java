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

import AirSim.Machinetta.Beliefs.*;
import AirSim.Environment.Area;
import AirSim.Machinetta.Beliefs.ProxyEventData;
import AirSim.Machinetta.Beliefs.ProxyEventData.EventType;

import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.Debugger;

import java.util.*;

public class PhaseAUGVDeploy extends ScenarioPhase {

    // average of 40 runs, this phase takes 23 seconds.  If it's not
    // ready for next phase after this many seconds, override and go
    // onto next phase.
    private static final long DURATION_MS = 53000;

    private class PatrolMission {
	Area area;

	private ArrayList<NamedProxyID> subteam;
	private PlanAgent planAgent;
	private HashMap<ProxyID,Boolean> inAreaMap = new HashMap<ProxyID,Boolean>();
	private boolean inArea = false;
    }
    
    private PatrolMission patrol1;
    private PatrolMission patrol2;

    private ArrayList<PatrolMission> patrols = new ArrayList<PatrolMission>();

    private ProxyState state = new ProxyState();

    public PhaseAUGVDeploy(int phaseNum,  WorldStateMgr wsm, SimUserUtils suc, SimUserSubTeams sust) {
	super(phaseNum, wsm, suc, sust);

	// TASK AUGV-1 PATROL 0, 500, 1500, 2500
	patrol1 = new PatrolMission();
	patrol1.subteam = sust.getSubTeam(sust.AUGV_PATROL1);
	patrol1.area = new Area(0, 500, 1000, 2500);
	for(NamedProxyID pid: patrol1.subteam) {
	    patrol1.inAreaMap.put(pid, false);
	}
	patrols.add(patrol1);
	

	// TASK AUGV-2 PATROL 1500, 500, 3500, 2500
	patrol2 = new PatrolMission();
	patrol2.subteam = sust.getSubTeam(sust.AUGV_PATROL2);
	patrol2.area = new Area(2000, 500, 3000, 2500);
	for(NamedProxyID pid: patrol2.subteam) {
	    patrol2.inAreaMap.put(pid, false);
	}
	patrols.add(patrol2);

    }

    private long startTime;

    // Start the phase
    public void instantiatePhasePlans() {
	startTime = System.currentTimeMillis();

	// Create AUGV patrol plan.
	for(PatrolMission patrol: patrols) {
	    patrol.planAgent = suu.createPatrolPlan(patrol.subteam, patrol.area);
	}

	state.addChangeListener(new StateChangeListener() {
		public void stateChanged(BeliefID[] b) {
		    for (BeliefID bel: b) {
			Belief belief = state.getBelief(bel);
			if(belief instanceof ProxyEventData) {
			    ProxyEventData ped = (ProxyEventData) belief;
			    Debugger.debug(1,"Got a ProxyEventData = "+ped);
			    if(ped.type == EventType.IN_PATROL_AREA) {
				for(PatrolMission patrol: patrols) {
				    for(ProxyID pid: patrol.subteam) {
					if(ped.proxyID.equals(pid)) {
					    patrol.inArea = true;
					    Debugger.debug(1,"Marking inArea true for patrol "+patrol.area+" pid "+pid);
					}
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

    private boolean missionReady(PatrolMission patrol) {
	// We're checking that all of our subteam members either have
	// entered the patrol area OR are dead.
	for(NamedProxyID pid: patrol.subteam) {

	    // If we don't have a location yet, we're not ready.
	    StateData sd = wsm.get(pid);
	    if(null == sd.getLocation())
		return false;

	    // If this guy has entered patrol area, go on to check
	    // the other guys.
	    if(patrol.area.inside(sd.getLocation().x,sd.getLocation().y))
		continue;
	    // If this guy has not entered patrol area and is not
	    // assumed dead then we're not ready. (I.e. he's still on
	    // his way.)
	    if(!sd.checkAssumedDead())
		return false;
	}
	// Everyone is either dead or has already entered patrol area.
	// We're good to go!
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

	// ready for next phase when all AUGV's are in their patrol
	// area or have been destroyed.
	for(PatrolMission patrol: patrols) {
	    if(!missionReady(patrol)) {
		Debugger.debug(1,"Not ready for next phase because for patrol "+patrol.area+" subteam has not entered area.");
		return false;
	    }
	}
	return true;
    }

    // Check if the phase is done
    public boolean phaseDone() {
	// This mission never ends.
	return false;
    }

    // do any cleanup on the phase after it is done.  I guess
    // theoretically at least, this should also terminate the phase
    // (and any running plans) even if phaseDone() returns false.
    public void endPhase() {
	for(PatrolMission patrol: patrols) {
	    patrol.planAgent.terminate();
	}
    }
}
