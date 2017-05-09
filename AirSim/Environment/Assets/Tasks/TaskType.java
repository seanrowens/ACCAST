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
 * EventType.java
 *
 * Created on Wed Aug  3 18:04:14 EDT 2005
 */

package AirSim.Environment.Assets.Tasks;

public enum TaskType {
        noTask,
	halt,           // do nothing - default state - args none
	move,           // either follow line, or go to point by most intelligent method possible - args point or line
	follow,         // follow the other unit -  args reference to unit to follow
	defendGround,	// defend a spot against enemy coming from
			// SOME direction - is it multi unit
			// coordination if a platoon is intelligent
			// about dividing up the defense line,
			// positioning, etc? - args - left trp, right
			// trp, trip line?
	defendAir,	// same deal, slightly more complicated -
			// ignore WASMs unless they are either
			// attacking us or so close that they
			// obviously see us, in which case shoot at
			// them/take cover.  Initiate attacks against
			// high value targets like A10, F16, C130,
			// etc. - args - left/right trp?  trip line?
	patrol,		// follow a route more or less, repeatedly,
			// looking for the other guy - attack if you
			// see him.  Also radio back a report.
                        // Lets make this an area to patrol, rather than specifying the route. Paul 16/8
	coverFromAir,	// pick nearest cover from air (trees?) and run for it as
			// fast as possible
	coverFromGround,// pick nearest place that offers
			// cover from known enemy ground units
			// and run for it as fast as possible
	retreat,	// move toward predefined regroup point,
			// defending on the way, i.e. facing enemy and
			// firing at them- args - point
	flee,		// run as fast as possible in direction away
			// from known enemy units
        hold,           // stay still 
	scoutObserve,	// avoid combat and observe the enemy, report
			// enemy locations Is this really two
			// different actions (scout vs observe)? -
			// args location to observe? same as patrol?
	attack,		// move to within range of some point and fire
			// at it, moving towards it, until reach point
			// and all enemy units dead.
	UGVAttack,	// Just like attack but named differently so
			// only UGV's can do it.
	attackFromAirOrGround,	// attack role for AUAV or UGV -
				// AUAV's implement as attackfromair
				// and ugv's implement as UGVAttack.
	groundSupport,	// fly around some unit and both report on
			// enemy unit sightings and fire at enemy
			// ground units - args - friendly unit to
			// support
	bomb,		// fly to/over some point and drop bombs on it.
        determineEngagementAreas,   // For a given path determine the likely 
                        // enemy engagement areas
        determineLeastResistancePath, // For a given start and end, determine
                        // the path of least resistance
        attackFromAir,  // Fire a ASMissile at something
        BDAFromAir,     // Perform BDA from the air
        // The following are for the L3Comm project
        geolocateCompute,       // Perform the computation to determine the location of an emmitter, given
                                // a set of sensor readings
        geolocateSense,         // Take a sensor reading for a geolocation
        emitterDiscriminate,    // Determine whether a sensor reading is an emitter or not
        scan,                   // Search an area looking for emmitters.
        EOImage,                // Take an EO image of a location
        provideScanData,        // Send data from a scan to a detector for analysis
        mount,
        dismount,
        randomMove,
        deploy,
        transport,
        airdrop,
	intelSurveilRecon,
        activate,
        deactivate,
        detonate,
        spreadOut,
        land,
        launch,
        directFire,
        missileFire,
        circle;
        
}
