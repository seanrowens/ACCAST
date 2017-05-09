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

import Machinetta.State.BeliefType.NamedProxyID;

import java.util.*;


public class PhaseIMDeploy extends ScenarioPhase {

    // Tell each IM to move to where it is supposed to be and then
    // activate
    
    private class IMReady {
	int destX;
	int destY;
	boolean activated;
    }

    IMReady im1;
    IMReady im2;
    IMReady im3;

    // @TODO: I'm not sure if there's really even anything to do here
    // exactly.  I suppose theoretically we want to not activate the
    // IM until it has spread out/moved to where it is supposed to be,
    // to avoid noise from one IM in the field detonating another IM
    // nearby.  Also I suppose the IM should not detonate if it is not
    // in the correct place, i.e. if it happens to land near some
    // civilians but belongs elsewhere, it should not detonate until
    // it has moved elsewhere.
    //
    //  But now we're modeling an IM field as one asset, so really we
    //  just need to tell it to go where it belongs and perhaps shape
    //  itself.
    public PhaseIMDeploy(int phaseNum,  WorldStateMgr wsm, SimUserUtils suc, SimUserSubTeams sust) {
	super(phaseNum, wsm, suc, sust);
    }

    private static final long DURATION_MS = 3000;	// testing - run for 3 seconds
    private long startTime;

    // Start the phase
    public void instantiatePhasePlans() {
	// @TODO: add code to create this phase's plan
	startTime = System.currentTimeMillis();
    }

    // Check if ready for next phase.
    public boolean readyForNextPhase() {
	// @TODO: add code to check if we're ready for next phase

	// We want to avoid the IMs being activated while our AUGV's
	// are traveling through them.  Can we be sure that our AUGV
	// route won't go too near our IMs?  Is this even really an
	// issue?
	long now = System.currentTimeMillis();
	if((now - startTime) > DURATION_MS)
	    return true;
	return false;
    }

    // Check if the phase is done
    public boolean phaseDone() {
	// @TODO: add code to check if we're ready for next phase
	long now = System.currentTimeMillis();
	if((now - startTime) > DURATION_MS)
	    return true;
	return false;
    }

    // do any cleanup on the phase after it is done.  I guess
    // theoretically at least, this should also terminate the phase
    // (and any running plans) even if phaseDone() returns false.
    public void endPhase() {

    }

    
}
