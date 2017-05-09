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
 * AssetStateBelief
 *
 * Created Thu Jan 29 18:42:54 EST 2009
 *
 */

package AirSim.Machinetta.Beliefs;

import AirSim.Machinetta.Messages.NavigationDataAP;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import java.io.*;
import java.text.DecimalFormat;

/**
 * From a UAV agent to its proxy.
 *
 * @author pscerri
 */
public class AssetStateBelief extends Belief {
    private final static DecimalFormat fmt = new DecimalFormat("0.000");

    public ProxyID pid;
    public Asset.Types type = Asset.Types.UNKNOWN;
    public double xMeters = -Double.MAX_VALUE;	// east/west
    public double yMeters = -Double.MAX_VALUE;	// north/south
    public double zMeters = -Double.MAX_VALUE;	// altitude
    public double latitude = -Double.MAX_VALUE;
    public double longitude = -Double.MAX_VALUE;
    public double altitudeMeters = -Double.MAX_VALUE;
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
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	pid = (ProxyID)in.readObject();
	type = (Asset.Types)in.readObject();
	xMeters = in.readFloat();
	yMeters = in.readFloat();
	zMeters = in.readFloat();
	latitude = in.readDouble();
	longitude = in.readDouble();
	altitudeMeters = in.readFloat();
 	headingDegrees = in.readFloat();
 	groundSpeed = in.readFloat();
	NS_Velocity = in.readFloat();
	EW_Velocity = in.readFloat();
	verticalVelocity = in.readFloat();
	accelerationX = in.readFloat();
	accelerationY = in.readFloat();
	accelerationZ = in.readFloat();
	curFuelRatio = in.readFloat();
	isMounted = in.readInt();
	isLive = in.readInt();
	time = in.readLong();
	armor = in.readFloat();
	damage = in.readFloat();
	state = (State)in.readObject();
	forceId = (ForceId)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
	out.writeObject(pid);
	out.writeObject(type);
	out.writeFloat((float)xMeters);
	out.writeFloat((float)yMeters);
	out.writeFloat((float)zMeters);
	out.writeDouble(latitude);
	out.writeDouble(longitude);
	out.writeFloat((float)altitudeMeters);
 	out.writeFloat((float)headingDegrees);
 	out.writeFloat((float)groundSpeed);
 	out.writeFloat((float)NS_Velocity);
 	out.writeFloat((float)EW_Velocity);
 	out.writeFloat((float)verticalVelocity);
 	out.writeFloat((float)accelerationX);
 	out.writeFloat((float)accelerationY);
 	out.writeFloat((float)accelerationZ);
 	out.writeFloat((float)curFuelRatio);
 	out.writeInt(isMounted);
	out.writeInt(isLive);
	out.writeLong(time);
 	out.writeFloat((float)armor);
 	out.writeFloat((float)damage);
 	out.writeObject(state);
 	out.writeObject(forceId);
    }

    /** Creates a new instance of NavigationData */
    public AssetStateBelief() {
    }

    public AssetStateBelief(ProxyID pid, NavigationDataAP nMsg) {
	this.pid = pid;
	this.xMeters = nMsg.xMeters;
	this.yMeters = nMsg.yMeters;
	this.zMeters = nMsg.zMeters;
	this.headingDegrees = nMsg.headingDegrees;
	this.groundSpeed = nMsg.groundSpeed;
	this.NS_Velocity = nMsg.NS_Velocity;
	this.EW_Velocity = nMsg.EW_Velocity;
	this.verticalVelocity = nMsg.verticalVelocity;
	this.accelerationX = nMsg.accelerationX;
	this.accelerationY = nMsg.accelerationY;
	this.accelerationZ = nMsg.accelerationZ;
	this.curFuelRatio = nMsg.curFuelRatio;
	this.isMounted = nMsg.isMounted;
	this.isLive = nMsg.isLive;
	this.type = nMsg.type;
	this.time = nMsg.time;
	this.armor = nMsg.armor;
	this.damage = nMsg.damage;
	this.state = nMsg.state;
	this.forceId = nMsg.forceId;
    }
    
    // stolen from Location - why is it dodgy?
    /**
     * This is obviously a bit dodgy, but don't want to fix it either ... 
     */
    public Machinetta.State.BeliefID makeID() {
        return new Machinetta.State.BeliefNameID(xMeters+":"+yMeters+":"+zMeters+":"+time+(pid == null ? "" : ":"+pid.toString()));
    }

    static final long serialVersionUID = 1L;

    public String toString() {
	return
	
	"ASSETSTATE: "+pid
	    +" ("+ fmt.format(xMeters)+","+fmt.format(yMeters)+","+fmt.format(zMeters)+")"
	    +" type: "+type
	    +" latlonalt: ("+fmt.format((latitude != -Double.MAX_VALUE) ? latitude : -1)+","+fmt.format((longitude != -Double.MAX_VALUE) ? longitude : -1)+","+((altitudeMeters != -Double.MAX_VALUE) ? altitudeMeters : -1)+")"
	    +" heading: "+fmt.format(headingDegrees)
	    +" speed: "+fmt.format(groundSpeed)
	    
	    +" NS_vel: "+fmt.format((NS_Velocity != -Double.MAX_VALUE) ? NS_Velocity : -1)
	    +" EW_vel: "+fmt.format((EW_Velocity != -Double.MAX_VALUE) ? EW_Velocity : -1)
	    +" vert_vel: "+fmt.format(verticalVelocity)

	    +" accel: ("+fmt.format((accelerationX != -Double.MAX_VALUE) ? accelerationX : -1)+","+fmt.format((accelerationY != -Double.MAX_VALUE) ? accelerationY : -1)+","+((accelerationZ != -Double.MAX_VALUE) ? accelerationZ : -1)+")"

	    +" fuel ratio:" +fmt.format(curFuelRatio)
	    +" mounted:" +isMounted
	    +" live: "+ isLive
	    +" State: "+ state
	    +" ForceId: "+ forceId
	    +" armor: "+ fmt.format(armor)
	    +" damage: "+fmt.format(damage)
	    ;
    }
}
