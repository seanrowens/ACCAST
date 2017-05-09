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

import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.Debugger;
import AirSim.Environment.Area;

import java.util.*;


public class PhaseUavIsrAndPrepFires extends ScenarioPhase {

    private ArrayList<NamedProxyID> subteam;
    private Area patrolArea;
    private PlanAgent planAgent;

    private static final long DURATION_MS = 300000;
    // private static final long DURATION_MS = 15000;	// testing - run for 15 seconds
    private long startTime;

    public PhaseUavIsrAndPrepFires(int phaseNum,  WorldStateMgr wsm, SimUserUtils suu, SimUserSubTeams sust) {
	super(phaseNum, wsm, suu, sust);
	subteam = sust.getSubTeam(sust.AIRSUPPORT_TEAM);
	// @TODO: Should get this from somewhere else, at least as a constructor param.
	patrolArea = new Area(0,0,3000,3000);
    }


    // Start the phase
    public void instantiatePhasePlans() {
	// Create UAV patrol plan.
	//	planAgent = suu.createPatrolPlan(subteam, patrolArea);
	planAgent = suu.createISRPlan(subteam,patrolArea);
	startTime = System.currentTimeMillis();
    }

    // Check if ready for next phase.
    public boolean readyForNextPhase() {
	// @TODO: Hacking this as a simple time duration for now, will add 'coverage' tracking later.	
	long now = System.currentTimeMillis();
	if((now - startTime) > DURATION_MS)
	    return true;
	return false;
    }

    // Check if the phase is done
    public boolean phaseDone() {
	// @TODO: Hacking this as a simple time duration for now, will add 'coverage' tracking later.	
	long now = System.currentTimeMillis();
	if((now - startTime) > DURATION_MS)
	    return true;
	return false;
    }

    // do any cleanup on the phase after it is done.  I guess
    // theoretically at least, this should also terminate the phase
    // (and any running plans) even if phaseDone() returns false.
    public void endPhase() {
	Debugger.debug(1,"Terminating plan agent.");
	planAgent.terminate();
    }

}
