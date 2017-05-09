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
 * Patrol.java
 *
 * Created on August 16, 2005, 6:18 PM
 *
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Area;
import AirSim.Machinetta.Messages.*;
import AirSim.Environment.Assets.Aircraft;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;

import Machinetta.Debugger;

import java.util.*;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author pscerri
 */
public class Patrol extends Task {
    public static boolean SPIRAL_MODE_DEFAULT = false;
    
    public final static double DEFAULT_AIRCRAFT_ALT = 100.0;
    public final static double DEFAULT_GROUND_ALT = 0.0;
    public final static double SPIRAL_WIDTH = 600;
    public final static double CIRCLE_STEP_LENGTH = 300;
    
    public final static double MAX_SPIRAL_WIDTH = 10000;
    public final static double MAX_SPIRAL_HEIGHT = 10000;
    
    private boolean spiralMode = false;

    private Asset myAsset = null;
    public Area patrolArea = null;
    java.util.Random rand = new java.util.Random();
    Move move = null;
    double x1;
    double y1;
    double x2;
    double y2;
    double leftX;
    double rightX;
    double botY;
    double topY;
    double xcenter;
    double ycenter;
    public double getXCenter() { return xcenter; }
    public double getYCenter() { return ycenter; }
    double width;
    double height;
    Rectangle2D.Double rect;

    ArrayList<Vector3D> scanRoute;
    int scanRouteIndex = 0;
    private boolean doSpiral = false;

    // Are we in the area?  If not then we move to center.
    private boolean insidePatrolArea = false;
    
    public Patrol(Area a, Asset myAsset) {
	this(a, myAsset, SPIRAL_MODE_DEFAULT);
    }

    /** Creates a new instance of Patrol */
    public Patrol(Area a, Asset myAsset, boolean spiralMode) {
	Debugger.debug(1,"Creating patrol task for asset "+myAsset.getID()+" area = "+a+", spiral="+spiralMode);
	this.spiralMode = spiralMode;
        patrolArea = a;
        this.myAsset = myAsset;
        x1 = patrolArea.getX1();
        y1 = patrolArea.getY1();
        x2 = patrolArea.getX2();
        y2 = patrolArea.getY2();
	if(x1 < x2) {
	    leftX = x1;
	    rightX = x2;
	}
	else {
	    leftX = x2;
	    rightX = x1;
	}
	if(y1 < y2) {
	    botY = y1;
	    topY = y2;
	}
	else {
	    botY = y2;
	    topY = y1;
	}
	    
        xcenter = (x1 + x2)/2;
        ycenter = (y1 + y2)/2;
        width = x1 - x2;
        height = y1 - y2;
        if(width < 0)
            width = -width;
        if(height < 0)
            height = -height;
	rect = new Rectangle2D.Double(leftX,botY,width,height);
        
        if(spiralMode && (width < MAX_SPIRAL_WIDTH) && (height < MAX_SPIRAL_HEIGHT)) {
            doSpiral = true;
            makeSpiral(x1, y1, x2, y2);
        }
    }
    
    /**
     * In the interests of not being predictable to the enemy, a patrol consists
     * of randomly moving around the area to patrol.
     *
     * Ha ha.  OK, I admit, random movement was just the simplest thing to implement.
     *
     * Sean 16/8/05 (only joking, it was really Paul.)
     *
     * TODO: Wed Sep 28 13:18:26 EDT 2005 SRO
     *
     * New plan, we're going to first go to the center, then corkscrew
     * out.  Since we still need random patrolling, we're going to
     * control this with a static boolean, CORKSCREW.  Yes this is a
     * crappy way to do this.  Ideally this should be a parameter to
     * this task.
     */
    public void step(Asset asset, long time) {
	if(asset.getSpeed() <= 0)
	    asset.setSpeed(asset.getMaxSpeed());

        if (move == null || move.finished(asset)) {
            double x, y, z;
            if (asset instanceof Aircraft) {
                z = DEFAULT_AIRCRAFT_ALT;
            } else {
                z = DEFAULT_GROUND_ALT;
            }
            if(!doSpiral) {
                x = rand.nextInt((int)(patrolArea.getX2() - patrolArea.getX1())) + patrolArea.getX1();
                y = rand.nextInt((int)(patrolArea.getY2() - patrolArea.getY1())) + patrolArea.getY1();
            } else if(null != scanRoute) {
                // TODO: SRO Thu Sep 29 23:14:52 EDT 2005
                //
                // Note that we don't grab z - we use the z we just
                // calculated right above here.  Also, this code needs to
                // be updated/fixed to deal with turns (like Move does)
                // as well as to deal with the case when the points along
                // our route are less than the distance of our step.
                if(scanRouteIndex >= scanRoute.size())
                    scanRouteIndex = 0;
                Vector3D point = scanRoute.get(scanRouteIndex++);
                x = point.x;
                y = point.y;
            } else {
                x = asset.location.x;
                y = asset.location.y;
            }
            move = new Move(new Vector3D(x, y, z));
            //             Debugger.debug("Patrol: Asset "+myAsset.getID()+": New path point ="+x+","+y+","+z, 1, this);
        }
        move.step(asset, time);
	if(!insidePatrolArea) checkPatrolArea(asset);
    }
    
    private void checkPatrolArea(Asset asset) {
	if(insidePatrolArea)
	    return;

	if((asset.location.x > leftX) && (asset.location.x < rightX)
	   && (asset.location.y > botY) && (asset.location.y > topY)) {
	    insidePatrolArea = true;
	    InPatrolAreaAP aap = new InPatrolAreaAP(rect, true);
	    Debugger.debug(1,"checkPatrolArea: Asset "+asset.getID()+" is inside patrol area = "+patrolArea+", sending to proxy InPatrolAreaAP="+aap);
	    asset.sendToProxy(aap);
	}
	else {
	    //	    Debugger.debug(1,"checkPatrolArea: Asset "+asset.getID()+" NOT inside patrol area");
	}
    }

    /** Patrol is a never ending task ... */
    public boolean finished(Asset asset) { return false; }
        
    private void makeSpiral(double x1, double y1, double x2, double y2) {
        scanRoute = new ArrayList<Vector3D>();
        
        double maxRadius;
        if(width> height)
            maxRadius = width * 5;
        else
            maxRadius = height * 5;
        
        // NOTE, we don't know at this point whether our asset is an
        // air or ground vehicle.  So we're just using
        // DEFAULT_AIRCRAFT_ALT because, well, why not.  In the step
        // method we'll have to check the asset instanceof and
        // substitute the correct value for z.
        scanRoute.add(new Vector3D(xcenter, ycenter, DEFAULT_AIRCRAFT_ALT));
        
        Debugger.debug("Patrol: Asset "+myAsset.getID()+": Creating spiral, box="+x1+","+y1+" to "+x2+","+y2+", center="+xcenter+","+ycenter+", maxRadius="+maxRadius+", SPIRAL_WIDTH="+SPIRAL_WIDTH+", CIRCLE_STEP_LENGTH="+CIRCLE_STEP_LENGTH,1,this);
        
        for(double loopRadius = SPIRAL_WIDTH; loopRadius < maxRadius; loopRadius += SPIRAL_WIDTH) {
            //	    Debugger.debug("Patrol: Asset "+myAsset.getID()+": spiral radius "+loopRadius, 1, this);
            double circumference = Math.PI * 2 * loopRadius;
            double stepsPerCircle = circumference/CIRCLE_STEP_LENGTH;
            //	    double stepsPerCircle = 16;
            double radIncr = (Math.PI * 2) / stepsPerCircle;
            double radius2 = loopRadius - SPIRAL_WIDTH;
            for(double radAngle = 0; radAngle < Math.PI * 2; radAngle += radIncr) {
                radius2 += SPIRAL_WIDTH/stepsPerCircle;
                // Note, we cast to int merely to truncate the part
                // past the decimal point.
                double x = (int) (xcenter + (radius2 * Math.cos(radAngle)));
                double y = (int) (ycenter + (radius2 * Math.sin(radAngle)));
                scanRoute.add(new Vector3D(x, y, DEFAULT_AIRCRAFT_ALT));
                //		Debugger.debug("Patrol: Asset "+myAsset.getID()+": Added path point ="+x+","+y+","+DEFAULT_AIRCRAFT_ALT, 1, this);
            }
        }
    }
}
