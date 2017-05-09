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
 * M35A2CargoTruck.java
 *
 * Created on Thu Jan 19 19:20:36 EST 2006
 */

package AirSim.Environment.Assets;

import AirSim.Environment.*; 
import AirSim.Environment.Assets.Tasks.RandomMove;

/**
 *
 * @author owens
 */
public class M35A2CargoTruck extends GroundVehicle {
    
    /** Creates a new instance of M35A2CargoTruck */
    public M35A2CargoTruck(String id, int x, int y) {
        super(id, x, y, 0, new Vector3D(1.0, 0.0, 0.0));
        state = State.LIVE;
        setSpeed(Asset.mphToms(58));
        setSpeed(0.00000001);
	fireAtSurfaceCapable = false;
	fireAtAirCapable = false;
    }

    // Trucks don't sense.  Save some CPU time.
    public void sense() { }
    
    public Asset.Types getType() { return Asset.Types.M35; }
    
}


// M35 - aka M35A2 aka "deuce and a half" truck aka 6x6 2.5 ton truck
// 
// Specification	 Description
// Engine Model	 LD465 MF
// Engine HP	 135
// Engine Brake	 None
// Transmission	 5 SPEED
// Trans. Speed	 5 Speed
// Sleeper		 Non
// Suspension	 Spring
// Rear End Ratio	 6.72
// Front Axle Capacity	6,320 Lbs.
// Rear Axle Capacity	16,680 Lbs.
// Front Wheels		Disc
// Rear Wheels		Disc
// Tire Size		9.00/20
// Interior		Standard
// Serial No.		0125-19219
// Turbo  Yes
// Air Conditioning	No
// Power Steering		No
// Tank Capacity		50 gallons
// Truck Type		Military
// Frame Setup		Single
// Refrigerated		No
// Odometer		165,109
// Unit Number		4999
// 
// M35A2 2-1/2 Ton Truck
// 
// The M35A2 Cargo Truck was designed for the US Armed Forces in the
// 1950s.  Since then many variants have emerged, built by contract
// holders such as REO, Curtiss-Wright, Studebaker, Kaiser-Jeep and AM
// General.  The M35A2 has been manufactured in greater numbers than any
// other military truck in the world.  It has seen various updates,
// including turbo-diesel engines, air-assisted brakes, seat belts,
// better lights, etc.  The M35A2 is primarily used to haul troops and
// cargo.  Some are setup as maintenance vehicles with tools and parts
// carried in the cargo area, and a winch mounted on the front.  Some are
// configured for supply or communications.  The "deuce" has a capacity
// of 5000 pounds of cargo, along with a 6000 pound trailer load.
// 
// http://www.sarafan.com/m35a2.html
// 
// General Information
// Nomenclature: TRUCK, CARGO: 2 Model Number: M35A2
// Crew/Cab Capacity: 2
// NSN: 2320-00-077-1616
// LIN: X40009
// SSN: D131030
// TM: 9-2320-209-Series
// 
// Characteristics:
// Horsepower: 140 bhp @ 2,600 rpm
// Transmission: Manual; 5 Fwd, 1 Rev
// Electrical System: 24 Volt
// Tires: 9:00 x 20, 8-ply
// Brakes: Air over hydraulics
// Blackout Lights: Yes
// 
// Performance Data
// Fording
// W/Kit: 72 in
// WO/Kit: 32 in
// Approach Angle: 47 Degrees
// Maximum Speed: 58 mph
// Range: 350 miles
// 
// Shipping Data
// Weight: 13,700 lbs.
// Cube: 1,190 cu ft
// Ground Clearance: 10 15/16 in
