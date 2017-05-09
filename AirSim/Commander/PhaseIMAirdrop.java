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
import AirSim.Machinetta.Beliefs.ProxyEventData;
import AirSim.Machinetta.Beliefs.ProxyEventData.EventType;
import AirSim.Machinetta.SimTime;

import Machinetta.Debugger;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;

import java.util.*;

public class PhaseIMAirdrop extends ScenarioPhase {

    // average of 40 runs, this phase takes 34 seconds.  If it's not
    // ready for next phase after this many seconds, override and go
    // onto next phase.
    private static final long DURATION_MS = 64000;

    private class AirdropMission {
	public double xPos;
	public double yPos;
	public double atDestRange;
	public double baseXPos;
	public double baseYPos;
	
	private ArrayList<NamedProxyID> subteam;
	private PlanAgent planAgent;

	private boolean landed = false;
	private HashMap<ProxyID,Boolean> droppedMap = new HashMap<ProxyID,Boolean>();
    }


    private AirdropMission drop1;
    private AirdropMission drop2;
    private AirdropMission drop3;

    private ArrayList<AirdropMission> drops = new ArrayList<AirdropMission>();

    private ProxyState state = new ProxyState();

    // @TODO: Possibly we should do the 3 airdrops as one big airdrop?
    // I.e. three airdrop roles as part of a single plan.
    public PhaseIMAirdrop(int phaseNum,  WorldStateMgr wsm, SimUserUtils suc, SimUserSubTeams sust) {
	super(phaseNum, wsm, suc, sust);

	// @TODO: Should get these locations, etc from somewhere else, not hardcoded

	// C130 C130-1 7000 1000 1000 BLUEFOR INTMINE1 0
	// TASK C130-1 DEPLOY 0500 1500 1000
	drop1 = new AirdropMission();
	drop1.subteam = sust.getSubTeam(sust.AIRDROP_TEAM1);
	drop1.atDestRange = 150;
	drop1.baseXPos = 7000;
	drop1.baseYPos = 1000;
	drop1.xPos = 0500;
	drop1.yPos = 1500;
	for(NamedProxyID pid: drop1.subteam) {
	    drop1.droppedMap.put(pid, false);
	}
	drops.add(drop1);

	// C130 C130-2 7000 2000 1000 BLUEFOR INTMINE2 0
	// TASK C130-2 DEPLOY 2500 1500 1000
	drop2 = new AirdropMission();
	drop2.subteam = sust.getSubTeam(sust.AIRDROP_TEAM2);
	drop2.atDestRange = 150;
	drop2.baseXPos = 7000;
	drop2.baseYPos = 2000;
	drop2.xPos = 2500;
	drop2.yPos = 1500;
	for(NamedProxyID pid: drop2.subteam) {
	    drop2.droppedMap.put(pid, false);
	}
	drops.add(drop2);

	// C130 C130-3 7000 3000 1000 BLUEFOR INTMINE3 0
	// TASK C130-3 DEPLOY 1500 2500 1000
	drop3 = new AirdropMission();
	drop3.subteam = sust.getSubTeam(sust.AIRDROP_TEAM3);
	drop3.atDestRange = 150;
	drop3.baseXPos = 7000;
	drop3.baseYPos = 3000;
	drop3.xPos = 1500;
	drop3.yPos = 2500;
	for(NamedProxyID pid: drop3.subteam) {
	    drop3.droppedMap.put(pid, false);
	}
	drops.add(drop3);
    }

    private long startTime;


    // Start the phase
    public void instantiatePhasePlans() {
	startTime = System.currentTimeMillis();

	// Create Airdrop plan.
	for(AirdropMission drop: drops) {
	    drop.planAgent = suu.createAirdropPlan(drop.subteam, 
						   drop.atDestRange, 
						   drop.baseXPos, 
						   drop.baseYPos, 
						   drop.xPos, 
						   drop.yPos);
	}

	// @TODO: Check here also for our C130's being dead - i.e. if
	// the c130 got shot down before getting a chance to drop the
	// IM then of course it will NEVER have a chance to drop the
	// IM.  Hmm.  How do we know it's dead?  Buh.  Ok, we wait
	// until we haven't heard an AssetStateBelief for a while.

	// @TODO: Really we should have a the SimUserRI in general (or
	// perhaps WorldStateMgr) keep track of who we haven't heard
	// form in a while and inject a belief about that asset being
	// dead into it's own ProxyState,
	// i.e. AssumedDead(confidence=x)

	state.addChangeListener(new StateChangeListener() {
		public void stateChanged(BeliefID[] b) {
		    for (BeliefID bel: b) {
			//                    Machinetta.Debugger.debug("Sim Operator new belief: " + bel, 1, this);
                    
			Belief belief = state.getBelief(bel);
			if(belief instanceof ProxyEventData) {
			    ProxyEventData ped = (ProxyEventData) belief;
			    if(ped.type == EventType.DISMOUNTED_CONTENTS) {
				for(AirdropMission am: drops) {
				    for(ProxyID pid: am.subteam) {
					if(ped.proxyID.equals(pid)) {
					    Debugger.debug(1,"Marking airdrop mission "+am.xPos+","+am.yPos+" team member "+pid+" as dropped.");
					    am.droppedMap.put(pid,true);
					}
				    }
				}
			    }
			    else if(ped.type == EventType.LANDED) {
				for(AirdropMission am: drops) {
				    for(ProxyID pid: am.subteam) {
					if(ped.proxyID.equals(pid))
					    Debugger.debug(1,"Marking airdrop mission "+am.xPos+","+am.yPos+" as landed.");
					    am.landed = true;
				    }
				}
			    }
			}
		    }
		}
	    });
    }

    private boolean missionReady(AirdropMission drop) {
	for(NamedProxyID pid: drop.subteam) {
	    // If this guy has dismounted his contents, go on to check
	    // the other guys.
	    if(true == drop.droppedMap.get(pid))
		continue;

	    // If this guy has not dismounted his contents and is not
	    // assumed dead then we're not ready. (I.e. he's still on
	    // his way.)
	    StateData sd = wsm.get(pid);
	    if(!sd.checkAssumedDead()) {
		Debugger.debug(1,"missioNready: drop at "+drop.xPos+","+drop.yPos+" not marked dropped and not assumed dead. (last update="+sd.getLastReportedTimeMs()+" elapsed "+System.currentTimeMillis()+sd.getLastReportedTimeMs()+")");
		
		return false;
	    }
	}
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

	// ready for next phase when all C130's have either dropped
	// their IM or been destroyed.
	for(AirdropMission am: drops) {
	    if(!missionReady(am)) {
		Debugger.debug(1,"readyForNextPhase: drop at "+am.xPos+","+am.yPos+" not ready, phase not ready.");
		return false;
	    }
	}
	return true;
    }

    private boolean missionDone(AirdropMission drop) {
	if(drop.landed) 
	    return true;
	// Not all missions are dismounted (i.e. dropped IMs) - we are
	// only ready for next phase if everyone in this subteam is
	// already dead.  If even one asset is alive then we are not
	// ready.

	// @TOOD: Should change this to be a for loop over the
	// subteam, should have landedMap like dropMap
	StateData sd = wsm.get(drop.subteam.get(0));
	if(!sd.checkAssumedDead())
	    return false;

	return true;
    }

    // Check if the phase is done
    public boolean phaseDone() {
	// Phase is done when all the C130s have returned to base or
	// been destroyed.
	for(AirdropMission am: drops) {
	    if(!missionDone(am))
		return false;
	}
	return true;
    }

    // do any cleanup on the phase after it is done.  I guess
    // theoretically at least, this should also terminate the phase
    // (and any running plans) even if phaseDone() returns false.
    public void endPhase() {
	for(AirdropMission drop: drops) {
	    drop.planAgent.terminate();
	}
    }
    
}
