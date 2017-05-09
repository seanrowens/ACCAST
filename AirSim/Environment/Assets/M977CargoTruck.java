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
 * M977CargoTruck.java
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
public class M977CargoTruck extends GroundVehicle {
    
    /** Creates a new instance of M977CargoTruck */
    public M977CargoTruck(String id, int x, int y) {
        super(id, x, y, 0, new Vector3D(1.0, 0.0, 0.0));
        state = State.LIVE;
        setSpeed(Asset.mphToms(57));
        setSpeed(0.00000001);
	fireAtSurfaceCapable = false;
	fireAtAirCapable = false;
    }

    // Trucks don't sense.  Save some CPU time.
    public void sense() { }
    
    public Asset.Types getType() { return Asset.Types.M977; }
    
}

// M977
// 8x8 10 ton truck
// 
// http://usmilitary.about.com/library/milinfo/arfacts/blm977.htm
// 
// M-977 HEMITT
// 
// 
// M-977 HEMTT
// Type: Heavy Expanded Mobility
// Tactical Truck
// Entered Army service: 1983
// Variants in service: M-977/978/983/984/985
// Specifications: (Basic M-977)
// Length: 33 ft 4.5 in
// Weight: 62,000 lbs
// Range: 300 mi
// Crew: 2
// 
// http://www.fas.org/man/dod-101/sys/land/hmett.htm
// 
//     The Heavy Expanded Mobility Tactical Truck (HEMTT) provides
//     transport capabilities for re-supply of combat vehicles and
//     weapons systems. There are five basic configurations of the
//     HEMTT series trucks: M977 cargo truck with Material Handling
//     Crane (MHC), M978 2500 gallon fuel tanker, M984 wrecker, M983
//     tractor and M985 cargo truck with MHC. A self-recovery winch is
//     also available on certain models. This vehicle family is
//     rapidly deployable and is designed to operate in any climatic
//     condition where military operations are expected to occur.
// 
//     Vehicle Specifications
// 
//     Vehicle Manufacturer: Oshkosh Truck Corporation
// 
//     Dimensions:
// 
//           Length: M977 / M978 / M985 - 401"; M983 - 351"; M984 - 392"
//           Height: Operational 112", Transport 102" - All
//           Width: 96" - All
//           Wheelbase: M977 / M978 / M985 - 210"; M983 - 181"; M984 - 191"
//           Turning Circle: M977 / M978 / M985 - 100'; M983 - 91'; M984 - 95'
//           Vehicle Curb Weight: M977 With Winch - 38,800 lb.,
//               Without Winch - 37,900 lb.; M978 With Winch - 38,200 lb.,
//               Without Winch - 37,300 lb.; M983 With Crane - 39,200 lb.,
//               Without Crane - 32,200 lb.; M984 - 50,900 lb.; M985 With
//               Winch - 39,600 lb., Without Winch - 38,700 lb.
//           Ground Clearance: 24" - All
// 
//     Performance:
// 
//           Maximum Speed: 57 mph Governed - All
//           Cruising Range: 300 mi. @ Gross Vehicle Weight Rating - All
//           Maximum Grade: 60% - All
//           Approach Angle: 41 deg. - All
//           Departure Angle: 45 deg. - All
//           Side Slope: 30% - All
//           Maximum Fording Depth: 48" - All
// 
//     Equipment Specifications:
// 
//           Cab: Crew Seating: 2 Man
//           Seat Design: Fore/Aft Adjustable
//           Steering Type: Dual Gear With Integrated Hydraulic Power Assist
// 
//           Engine: Manufacturer: Detroit Diesel Allison
//           Model: 8V92TA
//           Type: 8 cylinder, 2-stroke, V-type Diesel
//           Rating: 450 hp, @ 2100 rpm
//           Fuel: Diesel, DF-2, JP-4, JP-8, VV-F-800
//           Oil: 30 qt. With Filter (MIL-L-2104D, MIL-L-46167)
//           Cooling System: 80 qt., Water, Radiator
//           Fan: Engine-driven, Clutch Type
// 
//           Transmission: Manufacturer: Allison, Automatic
//           Model: HT740D
//           Speeds: 4 Speeds Forward/ 1 Reverse
//           Oil: 38 qt. With Filter
// 
//           Transfer: Manufactuer: Oshkosh Truck Corporation, 55000
//           Type: Air Operated, Front Tandem Disconnect
//           Oil: 6.5 qt.
// 
//           Axles: Manufacturer: Front Tandem - Oshkosh Truck Corporation/Eaton; Rear Tandem - Eaton
//           Models:
//           No. 1 - RS480
//           No. 2 - DS480-P - All
