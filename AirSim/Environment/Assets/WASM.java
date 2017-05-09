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
 * WASM.java
 *
 * Created on June 8, 2004, 7:12 PM
 */

package AirSim.Environment.Assets;
import AirSim.Environment.*;
import AirSim.Environment.Assets.Tasks.Move;
import AirSim.Environment.Assets.Tasks.Task;
import AirSim.Environment.Assets.Tasks.Patrol;
//import Machinetta.DomainSpecific.AS2.*;
import Machinetta.State.BeliefType.TeamBelief.*;


/**
 *
 * If currWaypoint and currTarget are both non-null
 * then the WASM will fly through the waypoint on the
 * way to the target.  If currTarget is not null and
 * currWaypoint is null then the WASM takes the most direct
 * path to the waypoint. <br>
 *
 * If currWaypoint and currTarget are both null then
 * the WASM maintains its current heading. <br>
 * @author  paul
 */
public class WASM extends Aircraft {
    
    private GroundVehicle currTarget = null;
    private GroundVehicle currBDA = null;
    private RoleBelief role = null;
    
    private double DESTROY_DIST = 200.0;
    
    /** Distance a WASM wants to get to target, before hitting ground. */
    private double AT_TARGET_DIST = 0.3;
    
    private double SPEED = Asset.kphToms(350); // SPEED_350KPH;
    
    /** Allows a WASM to do scheduling */
    private int absTime = 0;
    private int scheduled = -1;
    
    /** Creates a new instance of WASM */
    public WASM(String id, int x, int y, int z) {
        super(id, x, y, z, new Vector3D(1.0, 0.0, 0.0));
        
        forceId = ForceId.BLUEFOR;
        
	Area area = new Area(0,0,50000, 50000);
	addTask(new Patrol(area, this));
        state = State.LIVE;
        
        setSpeed(SPEED);        
    }
    
    /** When self explodes, uses location here. */
    public void causeExplosion(double dist) {
        causeExplosionAt(location, dist, this);
	suicide(false, "Exploded self on (hopefully) target.");
    }

    public void setTarget(GroundVehicle v, int time) { currTarget = v; scheduled = time; }
    public void clearTarget() { currTarget = null; }
    public void setWaypoint(Waypoint w) { currWaypoint = w; }
    public void clearWaypoint() { currWaypoint = null; }
    public void setBDA(GroundVehicle v) { currBDA = v; }
    public void clearBDA() { currBDA = null; }
    
    public String toString() { return "WASM " + id; }
    
    public Asset.Types getType() { return Asset.Types.WASM; }
}
