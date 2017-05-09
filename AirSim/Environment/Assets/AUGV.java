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
 * AUGV.java
 *
 * Created on May 13, 2005, 4:11 PM
 */

package AirSim.Environment.Assets;

import AirSim.Environment.*;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.Tasks.Hold;

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
 * @author pscerri
 */
public class AUGV extends Tank {
    public final static double AUGV_WEAPON_DAMAGE = .3;
    
    /** Creates a new instance of AUGV */
    public AUGV(String id, int x, int y) {
        super(id, x, y, 0);
        
	SEE_ID_DIST = 300;
	SEE_STATE_DIST = 300;
	FIRE_DIST = 320;
	SENSE_RADIUS = 420;

        state = State.LIVE;
        setSpeed(Asset.mphToms(40*2));
        setMaxSpeed(Asset.mphToms(60*2));
        
	// @todo: Can this really fire at both air and surface?
	fireAtSurfaceCapable = true;
	fireAtAirCapable = false;
	armor = 2.0;

        Weapon wp = new Weapon();
        wp.setDamage(AUGV_WEAPON_DAMAGE);
        this.setWeapon(wp);
    }
    
    public Asset.Types getType() { return Asset.Types.HMMMV; }

    // @TODO: This should probable be moved to Asset but need to think about it.
    //    public final double RETREAT_DISTANCE = 200;
    public HashMap<String,SensingTimeStep> senseMap = null;
    private double avgOpDistribution = 0.0;
    private double avgBlueDistribution = 0.0;
    private int totOtherSideNum = 0;
    private int totMySideNum = 0;
    private int smallestNum = Integer.MAX_VALUE;
    private int mySideNum = 0;
    private Asset currentlyEngagedTarget = null;
    
    // @TODO: This should probable be moved to Asset but need to think about it.
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
            if (location.z < SEE_ID_DIST) reading.id = a.id;
            if (location.z < SEE_STATE_DIST) reading.state = a.state;
            
	    // if me, add power/count to myside
	    if(this == a) {
		if (env.getCumulativeDamageMode() == true)
		    totMySidePower += this.getWeapon().getDamage();
		else 
		    numMySide++;
		continue;
	    }
		
	    int temp = getDistributionOtherSide(a.location, (int)SENSE_RADIUS/5);
                
	    if(a.getForceId() == this.getForceId()) {
		if (env.getCumulativeDamageMode() == true) {
		    totMySidePower += a.getWeapon().getDamage();
		} else {
		    numMySide++;
		}
                    
		totMySideNum += temp;
		continue;
	    } else {
		if (env.getCumulativeDamageMode() == true) {
		    totOtherSidePower += a.getWeapon().getDamage();
		} else {
		    numOtherSide++;
		}                    
		if(temp < smallestNum) {
		    smallestNum = temp;
		    currentlyEngagedTarget = a;
		}
		totOtherSideNum += temp;
                    
		continue;
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
    
    // @TODO: This should probable be moved to Asset but need to think about it.
    // This just counts the number of ALL assets in the box defined by
    // radius.
    private int getDistributionOtherSide(Vector3D loc, int radius) {
        LinkedList lists = env.getAssetsInBox((int)(loc.x - radius), (int)(loc.y - radius), -10, (int)(loc.x + radius), (int)(loc.y + radius), 10000);
        return lists.size();
    }
    
    private void shootAt(Asset target) {
        target.directFiredBy(this);	
    }

    // TODO: Almost everything in here should probably be pushed out
    // to some new kind of Memory or Sense class.
    public void sense() {
	//	Machinetta.Debugger.debug(1, getID()+"."+getGroup()+" sense: entering");
	// Can't see/shoot if we're inside something.
	if(null != getMountingAsset())
	    return;
	
	if(null == senseMap) 
	    senseMap = new HashMap<String,SensingTimeStep>();

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


	LinkedList assetsInSenseRadius = env.getAssetsInBox((int)(location.x - SENSE_RADIUS), (int)(location.y - SENSE_RADIUS), -10, (int)(location.x + SENSE_RADIUS), (int)(location.y + SENSE_RADIUS), 10000);
	//	Machinetta.Debugger.debug(1, getID()+"."+getGroup()+" sense: entering for loop with list of assets size="+assetsInSenseRadius.size());

        for (ListIterator li = assetsInSenseRadius.listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
                        
      	    if(this == a)
		continue;
	    if(a.getForceId() == this.getForceId())
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

	    //	    Machinetta.Debugger.debug(1, getID()+"."+getGroup()+" sense: examining asset "+a.getID()+"."+a.getGroup());

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
            if (location.z < SEE_ID_DIST) reading.id = a.id;
            if (location.z < SEE_STATE_DIST) reading.state = a.state;

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
	    
	    // If there are more of us than them, shoot at them!
	    if(!isOtherSideBigger()) {
		shootAt(a);
		firedAlready = true;
		continue;
	    } 
	    else {
		Machinetta.Debugger.debug(1, getID()+" their side is bigger!  Running away!");
	    }

	    // I'm not sure exactly what this does, but I think it's
	    // kinda the same idea as above.
	    if(smallestNum < avgBlueDistribution) {
		shootAt(currentlyEngagedTarget);
		firedAlready = true;
		continue;
	    } 
	    else {
		//		Machinetta.Debugger.debug(1, getID()+" smallesNum >= avgBlueDistribution - whatever THAT means.  This is probably broken come to think of it.  Running away!");
	    }

	    // Adding this since I'm not sure what's going on above
	    // and the UGV should never retreat (for now.)
	    if(null != a)
		shootAt(a);
	    
	    // UGV's don't run away.
	}
	if((firstObservedBuf.length() > 0) || (againObservedBuf.length() > 0))
	    Machinetta.Debugger.debug(1, getID()+"."+getGroup() +" first observes ("+firstObservedBuf.toString()+"), again observes ("+againObservedBuf.toString()+")");
    }
}
