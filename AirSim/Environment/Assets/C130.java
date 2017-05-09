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
 * C130.java
 *
 * Created on May 13, 2005, 2:29 PM
 */

package AirSim.Environment.Assets;

import AirSim.Machinetta.Messages.*;
import AirSim.Environment.*;
import AirSim.Environment.Assets.Tasks.Transport;
import AirSim.Environment.Assets.Tasks.Airdrop;
import AirSim.Machinetta.Messages.TransportPA;

/**
 *
 * @author pscerri
 */
public class C130 extends MannedAircraft {
    public static final double MAX_MAX_TURN_RATE_150MS=1;
    public static final double MAX_MAX_TURN_RATE_100MS=15;
    public static final double MAX_MAX_TURN_RATE_50MS=30;
    
    {
        //setSpeed(0.0025);

	// http://en.wikipedia.org/wiki/C-130_Hercules
	// Specifications (C-130H)
	// Maximum speed: 329 knots (379 mph, 610 km/h)
	// Cruise speed: 292 knots (336 mph, 540 km/h)
	//
	// http://www.simviation.com/rinfolocc130.htm
	//
	// The new C-130J has the familiar silhouette, but in fact
	// it's a brand new airplane with performance to prove it.
	// 
	// Max. cruising speed 348 kts / 645 km/h
	// Econ. cruising speed 339 kts / 628 km/h
	// 
	// And according to;
	//  http://www.aviationtrivia.info/Lockheed-C-130-Hercules.php
	// max climb rate is 2750 feet per minute == 13.97 m/s
	// 
	// According to;
	// http://www.wingsoverkansas.com/alford/article.asp?id=833
	// 
	// when landing you descend at 1500 feet per minute (7.62 m/s)
	// until 100 feet (30.48 m) altitude at which point you slow
	// (stall?)  to 750 fpm (3.81 m/s) and finally touch down at
	// 300 fpm (1.524 m/s).

        setSpeed(Asset.kphToms(645));
        setMaxSpeed(Asset.kphToms(645));
        MAX_CLIMB_RATE = 13.97;
        MAX_DESCEND_RATE = 7.62;
	//        MAX_TURN_RATE = 1;
	MAX_TURN_RATE = 0.2;
	armor = .9;
    }
    
    /** Creates a new instance of A10 */
    public C130(String id, int x, int y, int z) {
        super(id, x, y, z, new Vector3D(1.0, 0.0, 0.0));
        fireAtSurfaceCapable = false;
        fireAtAirCapable = false;
    }
    
    public void msgFromProxy(AirSim.Machinetta.Messages.PRMessage msg) {
        
        switch (msg.type) {
            
            case TRANSPORT:
		{
		    TransportPA trMsg = (TransportPA)msg;
		    Machinetta.Debugger.debug(1, "msgFromProxy: C130 " + this.getID() + " received transport command!!!");
                
		    int x = (int)trMsg.xPos;
		    int y = (int)trMsg.yPos;
		    Transport transportTask = new Transport(new Vector3D(x, y, 0));
		    addTask(transportTask);
                
		}                
		break;
            case AIRDROP:
		{
		    AirdropPA trMsg = (AirdropPA)msg;
		    Machinetta.Debugger.debug(1, "msgFromProxy: C130 " + this.getID() + " received airdrop command!!!");
                
		    int x = (int)trMsg.xPos;
		    int y = (int)trMsg.yPos;
		    int basex = (int)trMsg.baseXPos;
		    int basey = (int)trMsg.baseYPos;
		    double atDestRange = 100.0;
		    Airdrop airdropTask = new Airdrop(new Vector3D(x, y, 0), new Vector3D(basex, basey, 0), atDestRange);
		    addTask(airdropTask);
                }
                break;
                
            default:
                super.msgFromProxy(msg);
                
        }
    }
    
    public String toString() { return id; }
    
    public Asset.Types getType() { return Asset.Types.C130; }
}
