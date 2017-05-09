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

package AirSim.Environment.Assets.Events;

public enum EventType {
    noEvent,
    unitCreated, // Every unit event should include a reference of some sort to the unit
    unitRemoved,	// unit removed from system - this should only
    // happen by user command, i.e. never happens
    // due to simulation running.
    unitMoved,	// This may be too much.
    unitHit,	// hit by enemy action
    unitDamaged,	// any damage, some kind of % damage value?
    unitMobilityKill,	// the unit has lost ability to move
    unitFirepowerKill,	// the unit has lost ability to fire
    unitTotalKill,	// The unit is completely dead
    unitOnFire,	// unit is visibly flaming, burning, smoking
    unitSensed,	// sensed somthing - this is NOT for passing to other units, this is for logging.
    unitIdentified,
    unitFired,	// entity has fired at someone/something
    unitOutOfAmmo,
    unitOutOfFuel,
    explosion,	// location, some kind of type/strength
    taskAssigned,	// User/someone assinged a task - every task event should have task id + task type
    taskStarted,	// Task actually started (i.e. might assign 2
    // or more tasks in succession, when one ends,
    // the next one starts)
    taskTripped,	// for those tasks that involve waiting for
    // something, i.e. until an enemy unit crosses
    // a line, when it happens we send a Tripped
    // event
    taskEnded,	// Task reached its end (possibly end condition of some sort, or it got interrupted)
    taskInterrupted,	// Something interrupted our task -
    // either user, or perhaps a reaction
    // task.
    taskReaction; 	// Reaction spawned a task
}
