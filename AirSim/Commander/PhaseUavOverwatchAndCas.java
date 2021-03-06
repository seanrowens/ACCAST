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
import AirSim.Environment.Area;

import java.util.*;


public class PhaseUavOverwatchAndCas extends ScenarioPhase {

    private ArrayList<NamedProxyID> subteam;
    private Area patrolArea;
    private PlanAgent planAgent;

    public PhaseUavOverwatchAndCas(int phaseNum,  WorldStateMgr wsm, SimUserUtils suu, SimUserSubTeams sust) {
	super(phaseNum, wsm, suu, sust);
	subteam = sust.getSubTeam(sust.AIRSUPPORT_TEAM);
	// @TODO: Should get this from somewhere else, at least as a constructor param.
	patrolArea = new Area(0,0,3000,3000);
    }

    // Start the phase
    public void instantiatePhasePlans() {
	// Create UAV patrol plan.
	planAgent = suu.createPatrolPlan(subteam, patrolArea);
    }

    // Check if ready for next phase.
    public boolean readyForNextPhase() {
	// We're ready for next phase right away.
	return true;
    }

    // Check if the phase is done
    public boolean phaseDone() {
	// This phase really never finishes.  We're just hanging
	// around, looking for bad guys and waiting to supply Close
	// Air Support.
	return false;
    }

    // do any cleanup on the phase after it is done.  I guess
    // theoretically at least, this should also terminate the phase
    // (and any running plans) even if phaseDone() returns false.
    public void endPhase() {
	planAgent.terminate();
    }
    


}
