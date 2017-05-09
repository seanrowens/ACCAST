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


public class PhaseDIAssault extends ScenarioPhase {

    private class AssaultMission {
	double xPos;
	double yPos;
	
	private ArrayList<NamedProxyID> subteam;
	private PlanAgent planAgent;
    }

    private AssaultMission assault1;
    private AssaultMission assault2;
    private AssaultMission assault3;
    private AssaultMission assault4;
    private AssaultMission assault5;

    private ArrayList<AssaultMission> assaults = new ArrayList<AssaultMission>();

    private ProxyState state = new ProxyState();

    public PhaseDIAssault(int phaseNum,  WorldStateMgr wsm, SimUserUtils suu, SimUserSubTeams sust) {
	super(phaseNum, wsm, suu, sust);

	// @TODO: NOTE IF YOU CHANGE THESE XPOS/YPOS VALUES YOU NEED
	// TO CHANGE THEM ALSO IN ACCASTCREATE.makeEnvFileACCAST

	// Airfield west
	// TASK DI-BLUE0 ATTACK 500 2250 0
	// TASK DI-BLUE1 ATTACK 500 2250 0
	assault1 = new AssaultMission();
	assault1.xPos = 500;
	assault1.yPos = 2250;
	assault1.subteam = sust.getSubTeam(sust.AIRSTRIP_TEAM1);
	assaults.add(assault1);

	// Airfield east
	// TASK DI-BLUE2 ATTACK 2500 2250 0
	// TASK DI-BLUE3 ATTACK 2500 2250 0
	assault2 = new AssaultMission();
	assault2.xPos = 2500;
	assault2.yPos = 2250;
	assault2.subteam = sust.getSubTeam(sust.AIRSTRIP_TEAM2);
	assaults.add(assault2);

	// Airfield to terminal
	// TASK DI-BLUE4 ATTACK 1500 1500 0
	// TASK DI-BLUE5 ATTACK 1500 1500 0
	assault3 = new AssaultMission();
	assault3.xPos = 1500;
	assault3.yPos = 1500;
	assault3.subteam = sust.getSubTeam(sust.TERMINAL_TEAM1);
	assaults.add(assault3);

	// Front of terminal 
	// TASK AUGV-BLUE2 ATTACK 1950 1200 0
	// TASK AUGV-BLUE3 ATTACK 1950 1200 0
	// TASK DI-BLUE6 ATTACK 1950 1200 0
	// TASK DI-BLUE7 ATTACK 1950 1200 0
	// TASK DI-BLUE8 ATTACK 1950 1200 0
	// TASK DI-BLUE9 ATTACK 1950 1200 0
	assault4 = new AssaultMission();
	assault4.xPos = 1950;
	assault4.yPos = 1250;
	assault4.subteam = sust.getSubTeam(sust.C2_TEAM);
	assaults.add(assault4);
    }

    private boolean phaseDone = false;
    private static final long DURATION_MS = 15000;	// testing - run for 15 seconds
    private long startTime;

    // Start the phase
    public void instantiatePhasePlans() {
	// Create AUGV assault plan.
	for(AssaultMission assault: assaults) {
	    assault.planAgent = suu.createAssaultPlan(assault.subteam, assault.xPos, assault.yPos);
	}

// 	state.addChangeListener(new StateChangeListener() {
// 		public void stateChanged(BeliefID[] b) {
// 		    for (BeliefID bel: b) {
// 			Belief belief = state.getBelief(bel);
// 			if(belief instanceof ProxyEventData) {
// 			    ProxyEventData ped = (ProxyEventData) belief;
// 			    if(ped.type == EventType.DISMOUNTED_CONTENTS) {
// 				for(AssaultMission assault: assaults) {
// 				    for(ProxyID pid: assault.subteam) {
// 					if(ped.proxyID.equals(pid))
// 					    assault.dismountedContentsMap.put(pid,true);
// 				    }
// 				}
// 			    }
// 			}
// 			else {
// 			    // ignore Belief
// 			}
// 		    }
// 		}
// 	    });

	startTime = System.currentTimeMillis();
    }

    private boolean missionReady(AssaultMission assault) {
	// We're checking that all of our subteam members either have
	// arrived at destination and unloaded their passengers, OR
	// are dead.
	for(NamedProxyID pid: assault.subteam) {
	    // If this guy has dismounted his contents, go on to check
	    // the other guys.
// 	    if(true == assault.dismountedContentsMap.get(pid))
// 		continue;
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
	// ready for next phase when all assaults have dismonuted
	// their contents or have been destroyed.
	for(AssaultMission assault: assaults) {
	    if(!missionReady(assault))
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
	for(AssaultMission assault: assaults) {
	    assault.planAgent.terminate();
	}
    }
    
}
