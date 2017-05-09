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
 * VehicleData.java
 *
 * Created Tue Apr 25 20:38:05 EDT 2006
 *
 */

package AirSim.Commander;

import java.util.*;
import java.text.DecimalFormat;
import java.awt.image.BufferedImage;

import Machinetta.Debugger;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Asset.Types;
import AirSim.Machinetta.PlannedPath;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import Util.*;

/**
 * VehicleData is the old class used to maintain world state that the
 * new {@link StateData} in the SimUser API is based on. It is not
 * part of the SimUser API, but it may be instructive to review. This
 * class was originally meant only for the fuser, and represented
 * vehicles observed by sensors.  It has now been expanded so it can
 * represent our own vehicles as well.
 *
 * @author owens
 */
public class VehicleData {
    private final static DecimalFormat fmt = new DecimalFormat("000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");

    // Keep n most recent beliefs
    private final static int NUM_BELIEFS_TO_KEEP = 20;

    private static Integer idCounter = 1;
    
    // NOTE: Pretty much all of these fields have getters and setters
    // below.

    private String id = null;
    private boolean expirable=false;
    // We calculate heading from the most recent two successive
    // locations.  When we are checking to see if a new observation
    // matches this one, we can calcuate a 'presumed' heading from the
    // difference between the new observation location and the most
    // recent observation location, then check if that matches this
    // vehicle for speed and heading.

    private LinkedList<VehicleBelief> beliefs = new LinkedList<VehicleBelief>();

    private long lastUpdateTimeMs = System.currentTimeMillis();
    private BufferedImage img = null;
    private Vector3D location = null;
    private Vector3D heading = null;
    private double speed = 0.0;
    private double ugsRange = 0.0;
    private boolean ugsPresent = false;
    private Asset.Types type = null;
    private double confidence = 0.0;
    private ArrayList<Vector3D> trace = new ArrayList<Vector3D>();
    private PlannedPath path = null;
    private PositionMeters destination = null;
    private UGSSensorReading lastUGSReading = null;

    /**
     * Creates the instance of VehicleData with a given VehicleBelief
     * 
     * 
     * @param vb Vehicle belief containing location, id, type, confidence, etc...
     */
    public VehicleData(VehicleBelief vb) {
	expirable = true;
	id = makeId("veh");
	beliefs.add(vb);
	location = new Vector3D(vb.getX(), vb.getY(), 0);
	heading = new Vector3D(0, 0, 0);
	speed = 0.0;
	type = vb.getType();
	confidence = vb.getConfidence();
    }
    
    /**
     * Creates the instance of VehicleData with image data
     * 
     * 
     * @param imageData Actual image data
     * @param img BufferdImage to set the image field
     */
    public VehicleData(ImageData imageData, BufferedImage img) {
	expirable = true;
	id = makeId("img");
	this.img = img;
	location = imageData.loc;
	heading = new Vector3D(0, 0, 0);
	type = Asset.Types.CLUTTER;
	speed = 0.0;
	confidence = 0.0;
    }
    
    
    /**
     * Creates the instance of VehicleData with specific information
     * 
     * 
     * @param id Vehicle id
     * @param type Vehicle type
     * @param time Current time
     * @param posx X value of the current location
     * @param posy Y value of the current location
     * @param posz Z value of the current location
     * @param headingDegrees Degree value for heading information
     * @param speed Current speed
     */
    public VehicleData(String id, Asset.Types type, long time,
		       double posx, double posy, double posz, 
		       double headingDegrees, double speed ) {
	expirable = false;
	this.id = id;
	this.type = type;
	lastUpdateTimeMs = time;
	location = new Vector3D(posx, posy, posz);
	setHeading(headingDegrees);
	this.speed = speed;
	confidence = 100.0;
    }
    
    /**
     * Calculates the distance between the vehicle's location and
     * given one, and if that is bigger than the given threshold value
     * return 0 (this means we do not need to fuse those information).
     * Otherwise, return the normalized comparison value
     * 
     * 
     * @param loc2 Given location value of some vehicle
     * @param maxFuseDistanceSqd Threshold value
     * @return Normalized comparison value
     */
    public double compareTo(Vector3D loc2, double maxFuseDistanceSqd) {
	// @TODO: We now have a SimTime object that estimates
	// simulation time - would be a good idea to use that here.
	//
	// If we had some notion of the _simulation_ time (not real
	// time) in VehicleBelief we could project from this.location
	// forward to a presumed current location, and compare that to
	// the new vb location.  But we don't.
	Vector3D heading2 = location.toVector(loc2);

	// Note that distance is not only the distance between last
	// sighting of 'this' and loc2, but is also the speed in some
	// sense.
	double distanceSqd = heading2.lengthSqd();	
	//	heading2.normalize2d();
	
	// @todo: This is very simplistic - our comparison is simply
	// the ratio of the distance between 'this' location and loc2
	// location, over the max possible fuse distance.  If we
	// wanted to do this better we could take into account
	// heading, and possibly speed, both of which would make this
	// much better.
	double comparison;
	if(distanceSqd >  maxFuseDistanceSqd)
	    comparison = 0;
	else
	    comparison = (maxFuseDistanceSqd - distanceSqd)/maxFuseDistanceSqd;
	//	Debugger.debug("compareTo: "+getId()+" myloc ="+location+", belief loc="+loc2+", distanceSqd="+fmt2.format(distanceSqd)+", comparison="+comparison, 1, this);	
	return comparison;
    }

    /**
     * Makes location information from a given VehicleBelief, and
     * compares with the existing method
     * 
     * 
     * @param vb VehicleBelief to get the location info
     * @param maxFuseDistanceSqd Threshold value
     * @return Normalized comparison value
     */
    public double compareTo(VehicleBelief vb, double maxFuseDistanceSqd) {
	Vector3D loc2 = new Vector3D(vb.getX(), vb.getY(), 0);
	return compareTo(loc2, maxFuseDistanceSqd);
    }

    /**
     * Makes location info from ImageData, and compares with the existing method
     * 
     * 
     * @param imageData ImageData of the vehicle
     * @param maxFuseDistanceSqd Threshold value
     * @return Normalized comparison value
     */
    public double compareTo(ImageData imageData, double maxFuseDistanceSqd) {
	return compareTo(imageData.loc, maxFuseDistanceSqd);
    }
    
    /**
     * Adds a given VehicleBelief to the belief set, and update the
     * vehicle's information with the most recent data
     * 
     * 
     * @param vb VehicleBelief
     */
    public void addVehicleBelief(VehicleBelief vb) {
	beliefs.add(vb);
	location = new Vector3D(vb.getX(), vb.getY(), 0);
	if(beliefs.size() >= 2) {
	    VehicleBelief lastBelief = beliefs.getLast();
	    Vector3D lastLoc = new Vector3D(lastBelief.getX(), lastBelief.getY(), 0);
	    heading = lastLoc.toVector(location);
	    speed = heading.length();
	    heading.normalize2d();
	}
	// @TODO: Should this be simtime?
	lastUpdateTimeMs = System.currentTimeMillis();
	while(beliefs.size() > NUM_BELIEFS_TO_KEEP) {
	    beliefs.removeLast();
	}
//      // vote majority rules on vehicle type
// 	for(VehicleBelief b: beliefs) {
//	    
// 	}
    }

    /**
     * Returns the id of this vehicle data
     * 
     * 
     * @return The id string of this vehicle data
     */
    public String getId() { return id; }
    
    /**
     * Generates a new id with the given belief type
     * 
     * 
     * @param belieftype To indicate a belief type
     * @return Generated id
     */
    private String makeId(String belieftype) { 
	String newid=null;
	synchronized(idCounter) {
	    newid = belieftype+fmt.format(idCounter++);
	}
	return newid;
    }

    /**
     * Returns the flag content indicating whether this object is expired
     * 
     * 
     * @return boolean flag
     */
    public boolean isExpirable() { return expirable; }
    
    /**
     * Set the expirable flag with the given value
     * 
     * @param value To set the expirable value
     */
    public void setExpirable(boolean value) { expirable = value; }

    /**
     * Get the lsst update time in milliseconds
     * 
     * 
     * @return Time value
     */
    public long getLastUpdateTimeMs() { return lastUpdateTimeMs; }
    
    /**
     * Set the last update time with a given value
     * 
     * 
     * @param value Time value
     */
    public void setLastUpdateTimeMs(long value) { lastUpdateTimeMs = value; }

    /**
     * Return the vehicle image?
     * 
     * 
     * @return Vehicle image data
     */
    public BufferedImage getImg() { return img; }

    /**
     * Returns the current location information of the vehicle
     * 
     * 
     * @return Location information including x, y and z
     */
    public Vector3D getLocation() { return location; }
    
    /**
     * Sets the location and time field with given values
     * 
     * @param value Location value
     * @param time Time information
     */
    public void setLocation(Vector3D value, long time) { location = value; lastUpdateTimeMs = time; }
    
    /**
     * Return the current location's x value
     * 
     * 
     * @return X value of the current location
     */
    public double getX() { return location.x; }
    
    /**
     * Return the current location's y value
     * 
     * 
     * @return Y value of the current location
     */
    public double getY() { return location.y; }

    /**
     * Return the current heading information of the vehicle
     * 
     * 
     * @return Heading information
     */
    public Vector3D getHeading() { return heading; }
    
    /**
     * Sets the heading field of the vehicle with a given value
     * 
     * 
     * @param value Heading information in Vector3D type
     */
    public void setHeading(Vector3D value) { heading = value; }
    
    /**
     * Sets the heading field of the vehicle with a given degree value
     * 
     * 
     * @param headingDegrees Degree value for a heading information
     */
    public void setHeading(double headingDegrees) {
	heading = new Vector3D(0, 0, 0);
	heading.setXYHeading(headingDegrees);
    }

    /**
     * Returns of the current speed of the vehicle
     * 
     * 
     * @return The speed value in double precision
     */
    public double getSpeed() { return speed; }
    
    /**
     * Sets the current speed field with a given value
     * 
     * 
     * @param value Speed value
     */
    public void setSpeed(double value) { speed = value; }

    /**
     * Returns UGS range value of the vehicle
     * 
     * 
     * @return UGS range
     */
    public double getUgsRange() { return ugsRange; }
    
    /**
     * Sets UGS range field with a given value
     * 
     * 
     * @param value UGS range value
     */
    public void setUgsRange(double value) { ugsRange = value; }

    /**
     * Returns the flag indicating whether this vehicle includes UGS
     * 
     * 
     * @return The flag indicating whether this vehicle includes UGS
     */
    public boolean getUgsPresent() { return ugsPresent; }
    
    /**
     * Sets the flag for USG with a given value
     * 
     * 
     * @param value The flag indicating whether this vehicle includes UGS
     */
    public void setUgsPresent(boolean value) { ugsPresent = value; }

    /**
     * Returns the type of this vehicle
     * 
     * 
     * @return The type value of this vehicle
     */
    public Asset.Types getType() { return type; }

    /**
     * Returns of the confidence value
     * 
     * 
     * @return Confidence value
     */
    public double getConfidence() { return confidence; }

    /**
     * Returns a list of trace information
     * 
     * 
     * @return ArrayList containing traces of the vehicle
     */
    public ArrayList<Vector3D> getTrace() { return trace; }
    
    /**
     * Sets the trace information with a given value
     * 
     * 
     * @param value List containing trace information
     */
    public void setVector3D(ArrayList<Vector3D> value) { trace = value; }

    /**
     * Returns the path information of the vehicle
     * 
     * 
     * @return Planned path
     */
    public PlannedPath getPath() { return path; }
    
    /**
     * Sets the path field with a given value
     * 
     * 
     * @param value To set a planned path
     */
    public void setPath(PlannedPath value) { path = value; }

    /**
     * Returns the destination info of the vehicle
     * 
     * 
     * @return Destination info including x, y, z, and etc.
     */
    public PositionMeters getDestination() { return destination; }
    
    /**
     * Sets the destination field with a given field
     * 
     * 
     * @param value Destination value in PositionMeters type
     */
    public void setDestination(PositionMeters value) { destination = value; }

    /**
     * Returns UGS sensor reading value if this vehicle has UGS
     * 
     * 
     * @return UGS sensor reading
     */
    public UGSSensorReading getLastUGSReading() { return lastUGSReading; }
    
    /**
     * Sets UGS sensor reading field with a given value
     * 
     * 
     * @param value USG sensor reading value
     */
    public void setLastUGSReading(UGSSensorReading value) { lastUGSReading = value; }

}
