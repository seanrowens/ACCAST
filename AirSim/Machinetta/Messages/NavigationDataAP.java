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
 * NavigationData.java
 *
 * Created on January 25, 2006, 3:34 PM
 *
 */

package AirSim.Machinetta.Messages;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Env;
import Machinetta.State.BeliefType.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.text.DecimalFormat;

/**
 * From a UAV agent to its proxy.
 *
 * @author pscerri
 */
public class NavigationDataAP extends RPMessage {
    private final static DecimalFormat fmt = new DecimalFormat("0.00");

    private void writeObject(ObjectOutputStream oos)
	throws IOException {
	oos.defaultWriteObject();
    }

    // assumes "static java.util.Date aDate;" declared
    private void readObject(ObjectInputStream ois)
	throws ClassNotFoundException, IOException {
	ois.defaultReadObject();
    }

    public Asset.Types type = Asset.Types.UNKNOWN;
    public double xMeters = -Double.MAX_VALUE;
    public double yMeters = -Double.MAX_VALUE;
    public double zMeters = -Double.MAX_VALUE;
    public double headingDegrees = -Double.MAX_VALUE;
    public double groundSpeed = -Double.MAX_VALUE;
    public double NS_Velocity = -Double.MAX_VALUE;
    public double EW_Velocity = -Double.MAX_VALUE;
    public double verticalVelocity = -Double.MAX_VALUE;
    public double accelerationX = -Double.MAX_VALUE;
    public double accelerationY = -Double.MAX_VALUE;
    public double accelerationZ = -Double.MAX_VALUE;
    public double curFuelRatio = -Double.MAX_VALUE;
    public int isMounted = 0; //mounted:1, unmounted:0;
    public int isLive = 1; //live:1, dead:0
    public double armor = 0.0; // When damage exceeds... armor?  You're dead!
    public double damage = 0.0; // When damage exceeds... armor?  You're dead!
    public State state = State.UNKNOWN;
    public ForceId forceId = ForceId.UNKNOWN;
    
    // Units are the same units as simTime, which is ms since the
    // start of the run.  We measure time internally in Sanjaya/AirSim
    // as 'steps', with a notion of 'steps per second' (i.e. we can
    // set how many steps per second we want), then when we get a time
    // we convert from the current step to the 'simulated time' (since
    // we can run faster than real time) using the steps per second.
    public long time = -1L;     
    
    /** Creates a new instance of NavigationData */
    public NavigationDataAP() {
        super(MessageTypes.NAVIGATION_DATA);
	time = ((new Env()).getSimTimeMs());
    }
    
    public String toString() {
	return super.toString() 
	    +", type: "+type
	    +" loc (m) ("+fmt.format(xMeters)+", "+fmt.format(yMeters)+", "+fmt.format(zMeters)+")"
	    +" headingDegrees "+fmt.format(headingDegrees)
	    +" groundSpeed "+fmt.format(groundSpeed)
	    +" NS_vel "+((NS_Velocity == -Double.MAX_VALUE) ? "" : fmt.format(NS_Velocity))
	    +" EW_vel "+((EW_Velocity == -Double.MAX_VALUE) ? "" : fmt.format(EW_Velocity))
	    +" vert_vel "+fmt.format(verticalVelocity)
	    +" acc ("
	    +((accelerationX == -Double.MAX_VALUE) ? "" : fmt.format(accelerationX))
	    +", "+((accelerationY == -Double.MAX_VALUE) ? "" : fmt.format(accelerationY))
	    +", "+((accelerationZ == -Double.MAX_VALUE) ? "" : fmt.format(accelerationZ))
	    +")"
	    +", Current Fuel Ratio: " + ((curFuelRatio == -Double.MAX_VALUE) ? "0.0" : fmt.format(curFuelRatio))
            +", isMounted: " + ((isMounted == 1)? "Mounted" : "Unmounted")
            +", isLive: " + ((isLive == 1)? "Live" : "Dead")
	    +", armor: "+armor
	    +", damage: "+damage
	    +", state: "+state
	    +", forceid: "+forceId
	    ;	    
    }

    static final long serialVersionUID = 1L;
}
