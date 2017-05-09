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
 * Circle.java
 *
 * Created on November 5, 2007, 5:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Area;
import AirSim.Environment.Assets.Aircraft;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;

import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class Circle extends Task {
    private static Random rand = new java.util.Random();

    public final static double DEFAULT_AIRCRAFT_ALT = 100.0;
    public final static double DEFAULT_GROUND_ALT = 0.0;

    private double radius = -Double.MAX_VALUE;
    double angle = -Double.MAX_VALUE;
    Vector3D curWaypoint = null;

    Move move = null;
    
    /** Creates a new instance of Circle */
    public Circle(Vector3D waypoint) {
        this.curWaypoint = waypoint;
        radius = 1000.0;
        angle = 2*Math.PI/8;
        Vector3D startingPos = findClosest(curWaypoint);
        move = new Move(startingPos);
    }

    public Circle(Vector3D waypoint, double radius, double angle) {
        this.curWaypoint = waypoint;
        this.radius = radius;
        this.angle = angle;
        Vector3D startingPos = findClosest(curWaypoint);
        move = new Move(startingPos);
        
    }
            
    private Vector3D findClosest(Vector3D waypoint) {
        Vector3D[] tempWaypoint = new Vector3D[4];
        tempWaypoint[0] = new Vector3D(waypoint.x, waypoint.y+radius, waypoint.z);
        tempWaypoint[1] = new Vector3D(waypoint.x+radius, waypoint.y, waypoint.z);
        tempWaypoint[2] = new Vector3D(waypoint.x, waypoint.y-radius, waypoint.z);
        tempWaypoint[3] = new Vector3D(waypoint.x-radius, waypoint.y, waypoint.z);
        
        double[] dist = new double[4];
        for(int i=0; i<dist.length; i++) {
            dist[i] = waypoint.toVector(tempWaypoint[i]).lengthSqd();
        }
        
        double minDist = Double.MAX_VALUE;
        int returnIndex = -1;
        
        for(int i=0; i<dist.length; i++) {
            if(minDist > dist[i]) {
                minDist = dist[i];
                returnIndex = i;
            }
        }
        
        return tempWaypoint[returnIndex];
    }
    
    public void step(Asset asset, long time) {  
	if (move == null || move.finished(asset)) {
	    double x, y, z;
	    x = Math.cos(angle)*(asset.location.x-curWaypoint.x) - Math.sin(angle)*(asset.location.y-curWaypoint.y) + curWaypoint.x;
	    y = Math.sin(angle)*(asset.location.x-curWaypoint.x) + Math.cos(angle)*(asset.location.y-curWaypoint.y) + curWaypoint.y;
            z = asset.location.z;
	    move = new Move(new Vector3D(x, y, z));
	} 
	move.step(asset, time);
    }
    
    /** Circle is a never ending task ... */
    public boolean finished(Asset asset) { return false; }
    
}
