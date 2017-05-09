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
 * Infantry.java
 *
 * Created on September 27, 2007, 12:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets;

import AirSim.Environment.*;
import AirSim.Environment.Assets.Tasks.Patrol;
import AirSim.Environment.Assets.Tasks.Move;
import AirSim.Environment.Assets.Tasks.RandomMove;
import AirSim.Environment.Assets.Tasks.DirectFire;
import AirSim.Environment.Assets.Tasks.Defend;
import AirSim.Environment.Assets.Tasks.Mount;
import AirSim.Environment.Assets.Tasks.MoveToBuilding;
import AirSim.SensorReading;
import AirSim.Machinetta.Messages.DefendPA;
import AirSim.Machinetta.Messages.MountPA;
import AirSim.Machinetta.Messages.DismountPA;
import AirSim.Machinetta.Messages.DirectfirePA;
import AirSim.Environment.Assets.Weapons.Weapon;
import AirSim.Environment.Buildings.*;
        
import Machinetta.Debugger;
import java.util.*;

/**
 *
 * @author junyounk
 */
public class Infantry extends GroundVehicle {
    
    public static final double MAX_SPEED_METERS_PER_SEC = Asset.mphToms(8);
    public final double CONVOY_SPACING = 3.0;
    
    /** If this is not null, then the tank is part of a convoy */
    public Tank follow = null;
    public Road road = null;
    
    /** Creates a new instance of Infantry 
     * @Deprecated Use version without z
     */
    public Infantry(String id, int x, int y, int z) {
        super(id, x, y, z, new Vector3D(0.0, 0.0, 0.0));
	SEE_ID_DIST = 350;
	SEE_STATE_DIST = 350;
	FIRE_DIST = 200; // original 500
	SENSE_RADIUS = 350; //original 200
        setMaxSpeed(MAX_SPEED_METERS_PER_SEC); // SPEED_4MPH);
        setSpeed(MAX_SPEED_METERS_PER_SEC); // SPEED_4MPH);
        
        state = State.LIVE;
	fireAtSurfaceCapable = true;
	fireAtAirCapable = true;

	// Infantry's default behavior is to patrol the specific area
// 	Area area = new Area(x-1000, y-1000, x+1000, y+1000);
// 	addTask(new Patrol(area, this));
        
	// @TODO: should infantry report sensing?  On the one hand
	// they need to sense as Assets to work, no? But on the other
	// hand, they shouldn't be reporting every little thing they
	// see should they?  On the third hand they SHOULD be able to
	// call for air support or whatever. Hmm.
	//        dontReportSense = true;
        senseMap = new HashMap<String,SensingTimeStep>();
        
	armor = .3;
        /*
        this.armor = 200;
        env.setDamageMode(true);
        Weapon wp = new Weapon();
        wp.setDamage(50.0);
        this.setWeapon(wp);
         */
        
    }
    
    public Infantry(String id, int x, int y) {
        this (id, x, y, 0);
    }
 
    public Asset.Types getType() {
        return Asset.Types.INFANTRY;
    }

    // @TODO: This code for sensing and reacting should be refactored
    // into a reusable class elsewhere.
    public final double RETREAT_DISTANCE = 200;
    public HashMap<String,SensingTimeStep> senseMap = null;
    private double avgOpDistribution = 0.0;
    private double avgBlueDistribution = 0.0;
    private int totOtherSideNum = 0;
    private int totMySideNum = 0;
    private int smallestNum = Integer.MAX_VALUE;
    private int mySideNum = 0;
    private Asset currentlyEngagedTarget = null;
    
    // @TODO: This has been replaced by getForceRatio() in Asset.java,
    // which is cleaner and in someways definitely better, but may
    // have missed a trick or two from here.
    // 
    // @deprecated
    private boolean isOtherSideBigger() {
        int numMySide = 0;
        int numOtherSide = 0;
        double totMySidePower = 0.0;
        double totOtherSidePower = 0.0;        
                
        for (ListIterator li = env.getAssetsInBox((int)(location.x - SENSE_RADIUS), (int)(location.y - SENSE_RADIUS), -10, (int)(location.x + SENSE_RADIUS), (int)(location.y + SENSE_RADIUS), 10000).listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
            
            if(a.getType() == Asset.Types.CIVILIAN)
                continue;
            if(a.state == State.DESTROYED)
                continue;
            if(a.getContainingBuilding() != null) {
                Building b = a.getContainingBuilding();
                if(!b.getContentsVisible())
                    continue;
            }
            
            SensorReading reading = new SensorReading((int)a.location.x, (int)a.location.y, (int)a.location.z, (a.heading != null? a.heading.angle() : 0.0), null, a.getType(), State.UNKNOWN, getPid());
	    reading.asset = a;

	    reading.forceId = ForceId.UNKNOWN;
	    double distSqd =  location.toVectorLengthSqd(a.location);
            if (distSqd < SEE_ID_DIST*SEE_ID_DIST)  {
		reading.id = a.id; 
		reading.forceId = a.getForceId();
	    }
            if ( distSqd < SEE_STATE_DIST*SEE_STATE_DIST) reading.state = a.state;

	    // if me, add power/count to myside
	    if(this == a) {
		if (env.getCumulativeDamageMode() == true) 
		    totMySidePower += this.getWeapon().getDamage();
		else 
		    numMySide++;
		continue;
	    }
		
	    // This is simply the number of ANY assets (bluefor or
	    // opfor or civilian) in the box defined by +/- 'radius'
	    int countNearbyAssets = getDistributionOtherSide(a.location, (int)SENSE_RADIUS/5);
                
	    if(a.forceId == this.forceId) 
		totMySideNum += countNearbyAssets;
	    else 
		totOtherSideNum += countNearbyAssets;

	    if(a.forceId == this.forceId) {
		if (env.getCumulativeDamageMode() == true) {
		    totMySidePower += a.getWeapon().getDamage();
		} else {
		    numMySide++;
		}
                    
	    } else {
		if (env.getCumulativeDamageMode() == true) {
		    totOtherSidePower += a.getWeapon().getDamage();
		} else {
		    numOtherSide++;
		}                    
		// So basically this looks like we "engage the guy
		// with fewest people around him".  Except... this is
		// confusing.
		if(countNearbyAssets < smallestNum) {
		    smallestNum = countNearbyAssets;
		    currentlyEngagedTarget = a;
		}
	    }
        } // end of for loop
        
	if(numOtherSide > 0)
	    avgOpDistribution = totOtherSideNum/numOtherSide;
	else
	    avgOpDistribution = 1.0;
	if(numMySide > 0) 
	    avgBlueDistribution = totMySideNum/numMySide;
	else 
	    avgBlueDistribution = 1.0;
        mySideNum = numMySide;
        
        if (env.getCumulativeDamageMode() == true) {
            if (totMySidePower >= totOtherSidePower) return true;
            else return false;
        } else {
            if (numMySide >= numOtherSide) return true;
            else return false;
        }
        
    }
    
    // @TODO: See comments on isOtherSideBigger(), this has been
    // replaced by Asset.getForceRatio().
    //
    // @deprecated
    private int getDistributionOtherSide(Vector3D loc, int radius) {
        LinkedList lists = env.getAssetsInBox((int)(loc.x - radius), (int)(loc.y - radius), -10, (int)(loc.x + radius), (int)(loc.y + radius), 10000);
        return lists.size();
    }
    
    // TODO: Almost everything in here should probably be pushed out
    // to some new kind of Memory or Sense class.
    public void sense() {
	if(state == State.DESTROYED)
	    return;
	// Can't see/shoot if we're inside something.
	if(null != getMountingAsset())
	    return;

        super.sense();
        
        //int count1 = 0;
        //int count2 = 0;
        int avgOpNum = 0;       
        int totOtherSideNum = 0;
        int countOp = 0;
        
	if(this.state == State.DESTROYED)
	    return;

	boolean firedAlready = false;
	
	StringBuffer firstObservedBuf = new StringBuffer("");
	StringBuffer againObservedBuf = new StringBuffer("");

	double forceRatio = getForceRatio(SENSE_RADIUS,200,env.getCumulativeDamageMode());

        for (ListIterator li = env.getAssetsInBox((int)(location.x - SENSE_RADIUS), (int)(location.y - SENSE_RADIUS), -10, (int)(location.x + SENSE_RADIUS), (int)(location.y + SENSE_RADIUS), 10000).listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
                        
      	    if(this == a)
		continue;
	    if(a instanceof Munition)
		continue;
            if(a.getType() == Asset.Types.CIVILIAN)
                continue;
	    if(a.getType() == Asset.Types.C130)
		continue;
	    if(a.getType() == Asset.Types.SMALL_UAV)
		continue;
            if(a.state == State.DESTROYED)
                continue;
	    if(a.forceId == this.forceId)
		continue;

	    // If we're not both outside or both in the same building
	    if(a.getContainingBuilding() != getContainingBuilding()) {
		// check if he's in a building that isn't observeable
		if(a.getContainingBuilding() != null) {
		    Building b = a.getContainingBuilding();
		    if(!b.getContentsVisible())
			continue;
		}
		// check if I'm in a building that isn't observeable
		if(getContainingBuilding() != null) {
		    Building b = getContainingBuilding();
		    if(!b.getContentsVisible())
			continue;
		}
	    }
            
	    // for each asset that isn't me, civilian, destroyed, on
	    // my side, or in a building that I can't look in;

            SensorReading reading = new SensorReading((int)a.location.x, (int)a.location.y, (int)a.location.z, (a.heading != null? a.heading.angle() : 0.0), null, a.getType(), State.UNKNOWN, getPid());
	    reading.asset = a;

	    // @TODO: note, it just so happens that SEE_ID_DIST and
	    // SEE_STATE_DIST are == in GroundVehicle and inherited
	    // here, so reading.id is always set to a.id.  If that is
	    // changed, possibly bad things are going to happen when
	    // we no longer have ids for the SensorReading.
	    double distSqd =  location.toVectorLengthSqd(a.location);
            if (distSqd < SEE_ID_DIST*SEE_ID_DIST)  {
		reading.id = a.id; 
		reading.forceId = a.getForceId();
	    }
            if ( distSqd < SEE_STATE_DIST*SEE_STATE_DIST) reading.state = a.state;

	    // @TODO: SRO Thu Feb 19 21:18:59 EST 2009 - I don't
	    // understand what the rest of the loop really does - this
	    // needs more work, more sorting out.  Probably can move
	    // stuff out of the loop entirely.

	    boolean needToReact = false;
	    //NOTE: set the time to renew the current sensor information
	    if (!senseMap.containsKey(reading.id)) {
		SensingTimeStep sts = new SensingTimeStep(reading,this.getSimTimeMs());
		senseMap.put(reading.id, sts);
                
		firstObservedBuf.append(a.getID()+",");
		needToReact = true;
	    }
	    else {
		SensingTimeStep sts = (SensingTimeStep)senseMap.get(reading.id);
		long diffT = this.getSimTimeMs() - sts.getTimeStep();
                    
		//NOTE: set the time to update sensing information with simulated 10secs
		if(diffT >= 10000) {
		    againObservedBuf.append(a.getID()+",");
		    senseMap.remove(reading.id);
		    sts.setSensorReading(reading);
		    sts.setTimeStep(this.getSimTimeMs());
		    senseMap.put(reading.id, sts);
		    needToReact = true;
		}
	    }
	    
	    if((!needToReact) || (firedAlready))
		continue;
	    
	    // ratio of our 'power' to their 'power' - 100 means
	    // there's nobody on the other side within visual range
	    // We're not gonna run just because there's a few more of
	    // them - but if there's 50% more, we're outa here!
	    if(forceRatio >= .666) {
		// If there are more of us than them, shoot at them!
		directFireAt(a);
		firedAlready = true;
		continue;
	    } 

	    Machinetta.Debugger.debug(1, getID()+" their side is bigger!  Running away! forceRatio == "+forceRatio);

	    // Ok, crap, there's more of them!  Find a building to hide in!
	    Building b = env.findClosestBuilding(this.location.x, this.location.y, SENSE_RADIUS, 100.0);
	    if (b != null) {
		MoveToBuilding mtb = new MoveToBuilding(new Vector3D(b.getLocation().x, b.getLocation().y, 0.0), b);
		addTask(mtb);
		Machinetta.Debugger.debug(1, getID()+" creating "+mtb.toString() 
					  + " to building " + b.getID());
		firedAlready = true;
		continue;
	    } 

	    // No building!  Unless we're in pretty much the exact
	    // same place, run in the opposite direction
	    Vector3D directionThemToMe = location.toVector(a.location);
	    directionThemToMe.setLength(RETREAT_DISTANCE);
	    Vector3D dest = new Vector3D(location);
	    dest.add(directionThemToMe);
	    Move task = new Move(dest);
	    addTask(task);
	    Machinetta.Debugger.debug(1, getID()+" is retreated to location "+dest+"by "+a.getID());
	    firedAlready = true;
	    continue;

	}

	if((firstObservedBuf.length() > 0) || (againObservedBuf.length() > 0))
	    Machinetta.Debugger.debug(1, getID()+"."+getGroup() +" first observes ("+firstObservedBuf.toString()+"), again observes ("+againObservedBuf.toString()+")");
    }
    
    public void msgFromProxy(AirSim.Machinetta.Messages.PRMessage msg) {
        
    	switch (msg.type) {
            
        	case DEFEND:
                    DefendPA dfMsg = (DefendPA)msg;
                    Machinetta.Debugger.debug(1, "msgFromProxy: Infantry " + this.getID() + " received defend command!!!");
        		
                    int defX = (int)dfMsg.xPos;
                    int defY = (int)dfMsg.yPos;
                    Defend defendTask = new Defend(true, true, defX, defY);
                    addTask(defendTask);
        		
                    break;
        		
        	case MOUNT:
                    MountPA mtMsg = (MountPA)msg;
                    Machinetta.Debugger.debug(1, "msgFromProxy: Infantry " + this.getID() + " received mount command!!!");
        		
                    Asset mountedAsset = env.getAssetByID(mtMsg.mountedAsset);
                    Mount mountTask = new Mount(true, mountedAsset);
                    addTask(mountTask);
                
                    break;
                
                case DISMOUNT:
                    DismountPA dismtMsg = (DismountPA)msg;
                    Machinetta.Debugger.debug(1, "msgFromProxy: Infantry " + this.getID() + " received dismount command!!!");
        		
                    Mount dismountTask = new Mount(false, null);
                    addTask(dismountTask);
                
                    break;
                
                case DIRECTFIRE:
                    DirectfirePA drfMsg = (DirectfirePA)msg;
                    Machinetta.Debugger.debug(1, "msgFromProxy: Infantry " + this.getID() + " received directfire command!!!");
                    
                    int drfX = (int)drfMsg.xPos;
                    int drfY = (int)drfMsg.yPos;
                    int drfZ = (int)drfMsg.zPos;
                    DirectFire task = new DirectFire(new Vector3D(drfX, drfY, drfZ));
                    addTask(task);
                    
                    break;
                    
                default:
                    super.msgFromProxy(msg);
    	}
    }
    
    public String toString() { return "Infantry " + id; }    
    
}
