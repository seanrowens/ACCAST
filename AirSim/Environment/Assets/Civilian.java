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
 * Civilian.java
 *
 * Created on September 19, 2007, 4:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets;

import AirSim.Environment.*;
import AirSim.Environment.Assets.Tasks.RandomMove;
import AirSim.Environment.Assets.Tasks.Flee;
import AirSim.Environment.Assets.IntelligentMine;
import AirSim.SensorReading;
import AirSim.Environment.Buildings.*;

import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author junyounk
 */
public class Civilian extends GroundVehicle {
    
    //public double SPEED = 1.0;
    public final double CONVOY_SPACING = 3.0;
    
    /** If this is not null, then the tank is part of a convoy */
    public Civilian follow = null;
    public Road road = null;
    
    public boolean forward = true;
    public HashMap<String,SensingTimeStep> ht = null;
    
    /** Creates a new instance of Civilian 
     * @Deprecated Use version without z
     */
    public Civilian(String id, int x, int y, int z) {
        super(id, x, y, z, new Vector3D(0.0, 0.0, 0.0));
	SEE_STATE_DIST = 100;
	//FIRE_DIST = 2500;
	SENSE_RADIUS = 200;
        setSpeed(Asset.mphToms(4));
	setMaxSpeed(mphToms(4));
        
        state = State.LIVE;
	fireAtSurfaceCapable = false;
	fireAtAirCapable = false;

	// Civilians default behavior is wandering randomly
	RandomMove task = new RandomMove(x, y, AirSim.Configuration.width, AirSim.Configuration.length);
	addTask(task);
        
        dontReportSense = true;
        ht = new HashMap<String,SensingTimeStep>();

    }

    public Asset.Types getType() {
        return Asset.Types.CIVILIAN;
    }
        
    public Civilian(String id, int x, int y) {
        this (id, x, y, 0);
    }
 
    //TODO: sense and if there is enemy asset, addTask(Flee);
    public void sense() {
        super.sense();
        
        //int count1 = 0;
        //int count2 = 0;
        
        for (ListIterator li = env.getAssetsInBox((int)(location.x - SENSE_RADIUS), (int)(location.y - SENSE_RADIUS), -10, (int)(location.x + SENSE_RADIUS), (int)(location.y + SENSE_RADIUS), 10000).listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
            //count1++;
            
            //TODO: if other civilians are sensed through sensor reading?
	    if(this == a)
		continue;
	    if(a.forceId == this.forceId)
		continue;
            if(a.getType() == Asset.Types.CIVILIAN)
                continue;
	    if(a.getType() == Asset.Types.SMALL_UAV)
		continue;
	    if(a.getType() == Asset.Types.IM)
		continue;
            if(a.state == State.DESTROYED)
                continue;
            if(a.getContainingBuilding() != null) {
                Building b = a.getContainingBuilding();
                if(!b.getContentsVisible())
                    continue;
            }
            
            //count2++;
            // This asset is within the WASMs sense range
            // System.out.println("Wasm " + this + " can sense " + a);
            SensorReading reading = new SensorReading((int)a.location.x, (int)a.location.y, (int)a.location.z, (a.heading != null? a.heading.angle() : 0.0), null, a.getType(), State.UNKNOWN, getPid());
	    reading.asset = a;
            if (location.z < SEE_ID_DIST) reading.id = a.id;
            if (location.z < SEE_STATE_DIST) reading.state = a.state;
            
       	    if(reading != null) {
                //TODO: set the time to renew the current sensor information
                if (!ht.containsKey(reading.id)) {
                    SensingTimeStep sts = new SensingTimeStep(reading, this.getSimTimeMs());
                    ht.put(reading.id, sts);
                
		    //                    Machinetta.Debugger.debug(1, "New reading data " + a.getID() + " is observed by Civilian " + this.getID());
                    //memory.addSensorReading(reading);
            
                    //TODO: To enforce to move long distance
                    double xDiff = this.location.x - a.location.x;
                    double yDiff = this.location.y - a.location.y;
                    double xDiffUp = 0.0;
                    double yDiffUp = 0.0;
                
                    //double width = 5000;
                    //double height = 5000;
                    double width = env.MAP_WIDTH_METERS;
                    double height = env.MAP_HEIGHT_METERS;
                    
                    Vector3D direction = null;
                    if(xDiff < 0.001 && yDiff < 0.001) {
                        xDiffUp = rand.nextInt((int)width);
                        yDiffUp = rand.nextInt((int)height);
                    } else {
                        Vector3D vec = new Vector3D(xDiff, yDiff, 0.0);
                        vec.normalize2d();
                        xDiffUp = vec.x*10+this.location.x;
                        yDiffUp = vec.y*10+this.location.y;
                    }
                    
                    if(xDiffUp > width) xDiffUp = width;
                    else if (xDiffUp < 0) xDiffUp = 0;
                    
                    if(yDiffUp > height) yDiffUp = height;
                    else if (yDiffUp < 0) yDiffUp = 0;
                    
                    direction = new Vector3D(xDiffUp, yDiffUp, 0.0);
                    
                    Flee task = new Flee(direction);
                    addTask(task);
		    //                    Machinetta.Debugger.debug(1, "Create " + task.toString() + " from " + a.getID() + " for Civilian " + this.getID());
                } else {
                    SensingTimeStep sts = (SensingTimeStep)ht.get(reading.id);
                    long diffT = this.getSimTimeMs() - sts.getTimeStep();
                    
                    //TODO: set the time to update sensing information with simulated 20secs
                    if(diffT > 20000) {
                        ht.remove(reading.id);
                        
                        sts.setSensorReading(reading);
                        sts.setTimeStep(this.getSimTimeMs());
                        
                        ht.put(reading.id, sts);
                        
			//                        Machinetta.Debugger.debug(1, "Update reading data " + a.getID() + " is observed by Civilian " + this.getID());
                        //memory.addSensorReading(reading);
            
                        //TODO: To enforce to move long distance
                        double xDiff = this.location.x - a.location.x;
                        double yDiff = this.location.y - a.location.y;
                        double xDiffUp = 0.0;
                        double yDiffUp = 0.0;
                
                        //double width = 5000;
                        //double height = 5000;
                        double width = env.MAP_WIDTH_METERS;
                        double height = env.MAP_HEIGHT_METERS;
                    
                        Vector3D direction = null;
                        if(xDiff < 0.001 && yDiff < 0.001) {
                            xDiffUp = rand.nextInt((int)width);
                            yDiffUp = rand.nextInt((int)height);
                        } else {
                            Vector3D vec = new Vector3D(xDiff, yDiff, 0.0);
                            vec.normalize2d();
                            xDiffUp = vec.x*10+this.location.x;
                            yDiffUp = vec.y*10+this.location.y;
                        }
                    
                        if(xDiffUp > width) xDiffUp = width;
                        else if (xDiffUp < 0) xDiffUp = 0;
                        
                        if(yDiffUp > height) yDiffUp = height;
                        else if (yDiffUp < 0) yDiffUp = 0;
                    
                        direction = new Vector3D(xDiffUp, yDiffUp, 0.0);
                    
                        Flee task = new Flee(direction);
                        addTask(task);
			//                        Machinetta.Debugger.debug(1, "Create " + task.toString() + " from " + a.getID() + " for Civilian " + this.getID());
                    }
                } // end of else
                
            }
        }
        
        //Machinetta.Debugger.debug(1, getID() + "******* No of assets: " + count1 + "  No of visible assets except itself: " + count2);
        
    }
    
    public String toString() { return "Civilian " + id; }
    
}
