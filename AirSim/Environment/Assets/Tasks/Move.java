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
 * Move.java
 *
 * Created on Wed Aug  3 18:04:14 EDT 2005
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.*;
import AirSim.Environment.Assets.*;
import AirSim.Machinetta.Path2D;
import AirSim.Machinetta.Point2D;
import Machinetta.Communication.Message;
import java.util.Random;
import java.text.DecimalFormat;

public class Move extends Task {
    
    
    boolean speedSet = false, isFinished = false;
    double speed = 0.01;
    Vector3D lastInformedDestination;
    Vector3D destination;
    Path2D path = null;
    Vector3D goal;
    int pathPointIndex = -1;
    
    public double distanceToGoal(Asset a) {
        if(null != goal) {
            goal.z = a.location.z;
            return a.location.toVector(goal.x, goal.y, goal.z).length();
        } else
            return -1;
    }
    public double getGoalX() { return goal.x; }
    public double getGoalY() { return goal.y; }
    public double getGoalZ() { return goal.z; }
    public double getDestinationX() { return destination.x; }
    public double getDestinationY() { return destination.y; }
    public double getDestinationZ() { return destination.z; }
    
    public Move(Vector3D destination, Path2D path) {
        this.destination = destination;
        this.path = path;
        
        // Actually ignoring the destination, just keeping it for reference ...
        if (path != null) {
            Point2D loc = path.get(0);
            destination = new Vector3D(loc.getX(), loc.getY(), 0.0);
            Point2D last = path.viewLast();
            goal = new Vector3D(last.getX(), last.getY(), 0);
        }
    }
    
    public Move(Vector3D destination) {
        this.destination = destination;
        goal = new Vector3D(destination);
    }
    
    public Move(Vector3D destination, double speed) {
        this.destination = destination;
        this.speed = speed;
        goal = new Vector3D(destination);
        speedSet = true;
    }
    
    public void step(Asset asset, long time) {
        if((null != path) && (pathPointIndex < 0)) {
            Point2D loc = path.get(0);
            destination.length = 0;
            pathPointIndex = 0;
            Machinetta.Debugger.debug(asset.getID()+".move: starting on first point="+destination+", of path="+path, 1, this);
            
        }
        
	// TODO: SRO - in turnToVector we increment rotNum if the
	// abs(reqdTurn) is exactly equal 30 degrees, which means
	// basically it can never happen unless TURN_RATE >= 30
	// degrees and it will rarely happen unless TURN_RATE == 30,
	// in which case it probably will happen when we miss a
	// waypoint OR when we get a new waypoint and we're simply
	// facing the wrong way so we need to turn as fast as
	// possible.  At which point it... what, we stop turning, keep
	// going in the same direction we _were_ going?  Whats the
	// point of that?  This code should probably go away.
         Vector3D toWaypoint = null;
//         if(asset.rotNum > 4) {            
//             toWaypoint = new Vector3D(asset.heading.x, asset.heading.y, asset.heading.z);
//             asset.rotNum = 0;
//         } else {
             toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
//         }                        
        
        
	// TODO: Fix the spinning problem
	//
	// Thoughts.  At first I thought, ok, if you're moving AWAY
	// from the waypoint then mark it as done - but then I
	// realized often you get a waypoint and you're facing the
	// wrong way and it takes a while to turn towards it, so that
	// would be wrong.
	//
	// Then it just occurred to me, ok, you have a test that says
	// "last time I moved, distance to waypoint decreased, now
	// when I move, distance to waypoint increased (or at least
	// stayed the same?) - I missed the waypoint, let's just say
	// we're done with it and move on to the next one.
        if (toWaypoint.length() < asset.getAtLocationDistance()) {
            if (path == null) {
		// @TODO: What does "MP: fix the bug" mean? - SRO
                //MP: fix the bug
		if(isFinished == false) {
		    Machinetta.Debugger.debug(asset.getID()+".move: move finished, now dist "+toWaypoint.length()+" (within "+asset.getAtLocationDistance()+") from dest "+destination, 1, this);
		    asset.setEndOfPathAtStep(asset.getStep());
		    isFinished = true;
                    this.speed = 0.0;
                    //asset.setSpeed(0.0);
		}
            } else {
		asset.setEndOfPathAtStep(-1);
                ++pathPointIndex;
                if(pathPointIndex >= path.wps.size()) {
                    isFinished = true;
                    Machinetta.Debugger.debug(asset.getID()+".move: path finished, now at "+destination, 1, this);
                } else {
                    Point2D loc = path.get(pathPointIndex);
                    if (loc == null) {
                        isFinished = true;
                        Machinetta.Debugger.debug(asset.getID()+".move: path finished, now at "+destination, 1, this);
                    } else {
                        destination = new Vector3D(loc.getX(), loc.getY(), 0.0);
                        Machinetta.Debugger.debug(asset.getID()+".move: continuing towards point "+pathPointIndex+" at "+destination+" on path "+path, 1, this);
                    }
                }
            }
            
        }
	else {
// 	    if((asset instanceof Aircraft) && (toWaypoint.length() < (asset.getAtLocationDistance()*10)))
// 		Machinetta.Debugger.debug(1, asset.getID()+".move: cur waypoint="+destination.toString()+" distance="+Asset.fmt.format(toWaypoint.length())+" atLocationDist="+Asset.fmt.format(asset.getAtLocationDistance())+" speed="+asset.getSpeed() +"(mph = "+Asset.msTomph(asset.getSpeed())+")");
	}
        
        if ((toWaypoint.length() < asset.getAtLocationDistance()) && (destination != lastInformedDestination)) {
	    // Send a high criticality message to proxy that we've
	    // reached this waypoint.  If this doesn't get through, it
	    // isn't the end of the world, but it does help in terms
	    // of controlling movement if the proxy knows right away
	    // that we're 'at' the waypoint.  On the other hand there
	    // is a tradeoff that if this doesn't get through it will
	    // resend it and meanwhile an even more recent (and hence
	    // more accurate) navigationDataAP may get through
	    // instead.
	    asset.sendNavigationDataToProxyMed();
	    lastInformedDestination = destination;
        } else {
            //            if (path != null)
            //		Machinetta.Debugger.debug(asset.getID()+" still "+toWaypoint.length()+" from path point "+destination+" Remaining path: "+path, 1, this);
        }
        
        // Aircraft cannot turn on a dime, unlike ground vehicles which can.
        
        if (asset instanceof Aircraft) {
            
            if (toWaypoint.length() < asset.getMoveFinishedHoldRadius() && isFinished) {
                // Do nothing, just hold on line
                //Machinetta.Debugger.debug(1, asset.getID() + "     hold on line!!!");        
            } else {
                //Machinetta.Debugger.debug(1, asset.getID() + "     wayPoint length: " + toWaypoint.length() + "   ,angle: " + toWaypoint.angle());
        
                turnToVector((Aircraft)asset, toWaypoint);
                if (asset.heading.length() == 0.0) {
                    //Machinetta.Debugger.debug(1, asset.getID() + " random rotating????????????");
                    asset.heading.x = (new Random()).nextDouble();
                    asset.heading.y = (new Random()).nextDouble();
                }                   
                asset.heading.setLength(asset.getSpeed());
            }
        } else
            asset.heading = toWaypoint;
        
        asset.heading.normalize2d();
        
        // Machinetta.Debugger.debug("Move worked for " + asset.getID() + " " + asset.heading.length(), 1, this);
    }
    
    protected void turnToVector(Aircraft ac, Vector3D toWaypoint) {
        
        // Changing heading up or down
        double nz = 0.0;
        if (toWaypoint.z > 0) {
            nz = Math.min(ac.MAX_CLIMB_RATE, toWaypoint.z);
        } else {
            nz = Math.max(-ac.MAX_DESCEND_RATE, toWaypoint.z);
        }
        ac.heading.z = nz;
        //System.out.println("Climb: " + nz);
        // Change heading in x-y plane
        double reqdTurn = ac.heading.angleToXY(toWaypoint);
        
        //System.out.println("Raw required turn: " + reqdTurn);
        //Machinetta.Debugger.debug(1, ac.getID() + "     MAX_TURN_RATE: " + ac.MAX_TURN_RATE);
        if (reqdTurn > 0) reqdTurn = Math.min(ac.MAX_TURN_RATE, reqdTurn);        
        else if (reqdTurn < 0) reqdTurn = Math.max(-ac.MAX_TURN_RATE, reqdTurn);
        //System.out.println("Normalized required turn: " + reqdTurn);
        
        ac.heading.turn(reqdTurn);            
        //System.out.println("New heading: " + heading.angle() + " " + heading);
        
        if (reqdTurn > 0)
	    ac.rollDegrees = 15 * reqdTurn/ac.MAX_TURN_RATE;
        else if (reqdTurn < 0)
	    ac.rollDegrees = 15 * reqdTurn/ac.MAX_TURN_RATE;
	else 
	    ac.rollDegrees = 0;

        // Need to adjust the speed, based on the turn
    }
    
    public boolean finished(Asset a) {
        return isFinished;
    }
    
    public String toString() { return "Move:" + destination; }
}
