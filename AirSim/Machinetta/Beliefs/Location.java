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
 * Location.java
 *
 * Created on July 19, 2005, 1:08 PM
 *
 * Some overkill to have as a class for now, but might eventually be useful.
 *
 */

package AirSim.Machinetta.Beliefs;

import AirSim.Environment.Assets.Asset;
import Machinetta.State.BeliefType.*;
import java.io.*;

/**
 * Used by proxies for assets that wish to share their current
 * location/heading with other proxies.
 *
 * NOTE: This class was getting way too overloaded with extra stuff on
 * it, so I'm trimming most of that out.  There are many places we use
 * Location as a simple 2-tuple and other places where we use it for
 * too much more.  I'd like to make this even simpler, i.e. maybe even
 * JUST a 'location'.  Hmm.  Well not really.  THis is a belief about
 * the location of a particular RAP (proxyID) at some particular time.
 * We added heading for GUI purposes.  
 *
 * We should sort all this out somehow - get rid of everything but the
 * tuple, or maybe stop USING this belief as a tuple.  But that's too
 * much sorting for the moment.
 * 
 * There's a much more comprehensive NavigationDataAP message that
 * replaces Location for RAP to Proxy comms and then we can either
 * generate a Location belief to share location information, or
 * perhaps we can make some kind of NavigationData/EntityState belief
 * to share.  
 * 
 * What we really need to do is create a belief equivalent for
 * NavigationDataAP (which should really be called something like
 * AssetStateAP) and then get rid of Location entirely - everywhere we
 * use Location we should instead use NavigationDataAP/Belief, or
 * java.awt.Point or Vector3D.  - SRO Mon Jan 26 23:39:58 EST 2009
 *
 * ok, looks like I've gotten rid of Location pretty much everywhere.
 * It has been replaced by either Util.PositionMeters for simple
 * 3-tuples of doubles, or by NavigationDataAP/AssetStateBelief
 * everywhere else.
 *
 * @deprecated
 * 
 * @author pscerri
 */
public class Location extends Belief {
//    
//    public ProxyID pid;
//    public Asset.Types type = Asset.Types.UNKNOWN;
//    public int x, y, z;
//    public double heading;
//    public long time;
//
//    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
//	pid = (ProxyID)in.readObject();
//	type = (Asset.Types)in.readObject();
//	x = in.readInt();
//	y = in.readInt();
//	z = in.readInt();
// 	heading = (double)in.readFloat();
//	time = in.readLong();
//    }
//
//    public void writeExternal(ObjectOutput out) throws IOException {
//	out.writeObject(pid);
//	out.writeObject(type);
//	out.writeInt(x);
//	out.writeInt(y);
//	out.writeInt(z);
// 	out.writeFloat((float)heading);
//	out.writeLong(time);
//    }
//
//     /** Creates a new instance of Location */
//     public Location(int x, int y, int z, double heading, long time, ProxyID pid, Asset.Types type) {
//          this.x = x;
//          this.y = y;
// 	 this.z = z;
// 	 this.heading = heading;
//          this.time = time;
//          this.pid = pid;
// 	 this.type = type;
//          id = makeID();
//      }
//
//     public Location(int x, int y, int z, long time, ProxyID pid, Asset.Types type) {
// 	this(x,y,z,0.0,time,pid,type);
//     }
//
//     public Location() {
// 	x = 0;
// 	y = 0;
// 	z = 0;
//  	heading = 0;
// 	time = 0;
// 	pid = null;
//     }
//
//     public Location(int x, int y, int z, long time) {
// 	this(x,y,z,0.0,time,null,null);
//     }
//     
//    public int getX() {
//        return x;
//    }
//
//    public int getY() {
//        return y;
//    }
//
//    public int getZ() {
//        return y;
//    }
//
//    public double getHeading() {
//        return heading;
//    }
//
//    public long getTime() {
//        return time;
//    }
//
//    public Asset.Types getType() {
//        return type;
//    }
//
//    public ProxyID getPid() {
//        return pid;
//    }
//
//    /**
//     * This is obviously a bit dodgy, but don't want to fix it either ... 
//     */
//    public Machinetta.State.BeliefID makeID() {
//        return new Machinetta.State.BeliefNameID(x+":"+y+":"+time+(pid == null? "" : ":"+pid.toString()));
//    }
    public Machinetta.State.BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("LocationIsDeprecated");
    }
//
//    public final static long serialVersionUID = 1L;
//
//    public String toString() {
//	return "LOCATION:"+time+" "+x+", "+y;
//    }
//
}
