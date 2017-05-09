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
 * Path3D.java
 *
 * Created on March 8, 2006, 11:43 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Waypoint;
import AirSim.Environment.Vector3D;
import Machinetta.State.BeliefType.ProxyID;
import java.io.Serializable;
import java.util.ArrayList;
import java.text.DecimalFormat;

/**
 * @fix This needs to have time added to it.
 *
 * @author pscerri
 */
public class Path3D implements Serializable {
    private final static DecimalFormat fmt = new DecimalFormat("0.0");
    public static boolean DEBUG = false;
    public static boolean DONE_AFTER_FIRST_CONFLICT = true;
    public static int ITERATIONS_PER_PATH=100;
    
    private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
    private ProxyID assetID = null;
    private boolean loop = false;
    private boolean approved = false;
    Waypoint[] waypointAryCache = null;
    private double score = 0.0;
    
    /** Creates a new instance of Path3D */
    public Path3D() {
    }
    
    public Path3D(Path3D original) {
        
        if(null != original) {
            waypoints = (ArrayList<Waypoint>)original.waypoints.clone();
            assetID = original.assetID;
            loop = original.loop;
            approved = original.approved;
        }
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public Waypoint[] getWaypointsAry() {
	// @TODO: Why is the cache disabled?  Does it matter?  Should
	// either get rid of the cache code or enable it.

// 	if(null == waypointAryCache)
// 	    waypointAryCache = waypoints.toArray(new Waypoint[1]);
//         return waypointAryCache;
	return waypoints.toArray(new Waypoint[1]);
    }
    
    public int size() {
        return waypoints.size();
    }
    
    public void addPoint(Waypoint p) {
        waypoints.add(p);
	waypointAryCache = null;
    }
    
    public void addPoint(Waypoint p, int index) {
        waypoints.add(index, p);
	waypointAryCache = null;
    }
    
    public void removeFirst() {
        if(waypoints.size() <= 0) {
            Machinetta.Debugger.debug("can't removeFirst from an empty path", 5, this);
        } else {
            Waypoint removed = waypoints.remove(0);
            if (loop) waypoints.add(removed);
	    waypointAryCache = null;
        }
    }
    
    public void removeLast() {
        if (!isLoop()) {
            waypoints.remove(waypoints.size()-1);
	    waypointAryCache = null;
	}
    }
    
    public Waypoint getNext() {
        return waypoints.size() > 0 ? waypoints.get(0) : null;
    }
    
    public int getSize() {
        return waypoints.size();
    }
    
    /**
     * A lookahead of 1 means the next point, a lookahead of 2 means next but one.
     */
    public Waypoint lookAhead(int n) {
        if (loop) n = n % waypoints.size();
        return waypoints.size() > n ? waypoints.get(n-1) : null;
    }
    
    /**
     * Assumes that we are at the first waypoint then computes the distance remaining
     * in the plan. <br>
     *
     * Assumes straight lines between waypoints.
     */
    public double getRemainingDist(double cx, double cy, double cz) {
        double dist = 0.0;
        if (loop) {
            dist = Double.MAX_VALUE;
        } else if (waypoints.size() > 0) {
            Waypoint s = waypoints.get(0);
            dist = Math.sqrt(
                    (s.x - cx)*(s.x - cx) +
                    (s.y - cy)*(s.y - cy) +
                    (s.z - cz)*(s.z - cz));
            
            for (int i = 1; i < waypoints.size(); i++) {
                s = waypoints.get(i-1);
                Waypoint e = waypoints.get(i);
                dist += Math.sqrt(
                        (s.x - e.x)*(s.x - e.x) +
                        (s.y - e.y)*(s.y - e.y) +
                        (s.z - e.z)*(s.z - e.z));
            }
        }
        return dist;
    }
    
    public long getTimeAtEnd() {
        if(waypoints.size() <= 0)
            return SimTime.getEstimatedTime();
        Waypoint v =  waypoints.get(waypoints.size() - 1);
        if(v instanceof Waypoint)
            return ((Waypoint)v).getTime();
        else
            return SimTime.getEstimatedTime();
    }

    public long getTimeAtStart() {
        if(waypoints.size() <= 0)
            return SimTime.getEstimatedTime();
        Waypoint v =  waypoints.get(0);
        if(v instanceof Waypoint)
            return ((Waypoint)v).getTime();
        else
            return SimTime.getEstimatedTime();
    }

    public Waypoint getEndPoint() {
        if(waypoints.size() <= 0)
	    return null;
        return waypoints.get(waypoints.size() - 1);
    }
    
    public long getDuration() {
        if(waypoints.size() <= 0)
            return -1;
        Waypoint first =  waypoints.get(0);
        Waypoint last =  waypoints.get(waypoints.size() - 1);
        if(!(first instanceof Waypoint)
	    || !(last instanceof Waypoint))
	    return -1;
	return last.getTime() - first.getTime();
    }

    public double getLength() {
        if(waypoints.size() <= 1)
            return 0;
	double length = 0;
	Vector3D prev = waypoints.get(0);
	for(int loopi = 1; loopi < waypoints.size(); loopi++) {
	    Vector3D cur = waypoints.get(loopi);
	    length += prev.toVector(cur).length();
	}
	return length; 
    }
    
    public boolean isDone(long simTime) {
        if(waypoints.size() == 0)
            return true;
        Waypoint v =  waypoints.get(waypoints.size() - 1);
        if(v instanceof Waypoint) {
            if(((Waypoint)v).getTime() <= simTime)
                return true;
        }
        return false;
    }
    
    public void expireOldPathSegments(long simTime) {
        if(waypoints.size() == 0)
            return;
        while(true) {
            Waypoint v =  waypoints.get(waypoints.size() - 1);
            if(!(v instanceof Waypoint))
                return;
            if(((Waypoint)v).getTime() <= simTime) {
                waypoints.remove(0);
		waypointAryCache = null;
	    }
            else
                return;
        }
    }
    
    
    private static boolean interpolateAndCheckCollision(Waypoint actual, Waypoint[] interpAry, int interpIndex, double minDistSqd) {
        // If we can't interpolate because we're at beginning of the
        // array - just give up and assume we don't collide (@todo: we
        // could estimate, i.e. if we need to interpolate between last
        // and last+1, we could estimate using the difference between
        // last and last-1, and vice versa for the beginning of the
        // array)
        if(interpIndex <= 0) {
            if(DEBUG)Machinetta.Debugger.debug("        Can't interpolate, at beginning of array",1,"Path3D");
            return false;
        }
        
        // We know that actual occurs at a time between interp1 and
        // interp2
        Waypoint interp1 = interpAry[interpIndex-1];
        Waypoint interp2 = interpAry[interpIndex];
        double timediff = interp2.getTime() - interp1.getTime();
        double ratio = (actual.getTime() - interp1.getTime())/timediff;
        
        double xnew = interp1.x + (ratio * (interp2.x - interp1.x));
        double ynew = interp1.y + (ratio * (interp2.y - interp1.y));
        double znew = interp1.z + (ratio * (interp2.z - interp1.z));
        
        Waypoint interpolated = new Waypoint(xnew, ynew, znew, actual.getTime());
        Vector3D to = actual.toVector(interpolated);
        
        if(DEBUG)Machinetta.Debugger.debug("        checking actual "+actual+" against interpolated "+interpolated+" (between "+interp1+" and "+interp2+"), distSqd = "+to.lengthSqd()+", minDistSqd = "+minDistSqd,1,"Path3D");
        
        if(to.lengthSqd() < minDistSqd) {
            Machinetta.Debugger.debug("        WAYPOINT CONFLICT at ACTUAL "+actual+" and INTERPOLATED "+interpolated+" between actuals "+interp1+" and "+interp2+" distSqd= "+to.lengthSqd()+", less than limit ="+minDistSqd,1,"Path3D");
            return true;
        }
        if(DEBUG)Machinetta.Debugger.debug("        NO conflict at "+interpolated,1,"Path3D");
        return false;
    }
    
    public static boolean conflictsOld(Waypoint[] oneAry, Waypoint[] twoAry, double minDist, double minZDist) {
        boolean conflict = false;
        boolean doneChecking = false;
        
        if (oneAry.length <= 1 || twoAry.length <= 1) {
            Machinetta.Debugger.debug("Conflict check on path <= 1 can't be done", 1, "Path3D");
            return false;
        }
        
        long time = Math.min(oneAry[0].time, twoAry[0].time);
        long end = Math.min(oneAry[oneAry.length - 1].time, twoAry[twoAry.length - 1].time);
        long timeDelta = (end - time)/ITERATIONS_PER_PATH;
        
        double x1 = -1.0, y1 = -1.0, z1 = -1.0, x2 = -1.0, y2 = -1.0, z2 = -1.0;
        double dx1 = 0.0, dy1 = 0.0, dz1 = 0.0, dx2 = 0.0, dy2 = 0.0, dz2 = 0.0;
        int wp1 = 1, wp2 = 1;
        double minDistSqrd = minDist * minDist;
        
        if (oneAry[0].time <= time) {
            x1 = oneAry[0].x;
            y1 = oneAry[0].y;
            z1 = oneAry[0].z;
            dx1 = (oneAry[1].x - oneAry[0].x)/(oneAry[1].time - oneAry[0].time);
            dy1 = (oneAry[1].y - oneAry[0].y)/(oneAry[1].time - oneAry[0].time);
            dz1 = (oneAry[1].z - oneAry[0].z)/(oneAry[1].time - oneAry[0].time);
        }
        
        if (twoAry[0].time <= time) {
            x2 = twoAry[0].x;
            y2 = twoAry[0].y;
            z2 = twoAry[0].z;
            
            dx2 = (twoAry[1].x - twoAry[0].x)/(twoAry[1].time - twoAry[0].time);
            dy2 = (twoAry[1].y - twoAry[0].y)/(twoAry[1].time - twoAry[0].time);
            dz2 = (twoAry[1].z - twoAry[0].z)/(twoAry[1].time - twoAry[0].time);
        }
        
        
        Machinetta.Debugger.debug("Simulation from " + time + " -> " + end, -1, "Path3D");
        
        while (!doneChecking && time < end) {
            
            if (x1 >= 0) {
                // Check if at waypoint
                if (oneAry[wp1].time <= time) {
		    if(DEBUG) Machinetta.Debugger.debug("Next waypoint for 1: " + time + " " + oneAry[wp1].time, 1, "Path3D");
                    if (oneAry.length > wp1+1) {
                        // Should be here, but just to make sure
                        x1 = oneAry[wp1].x;
                        y1 = oneAry[wp1].y;
                        z1 = oneAry[wp1].z;
                        dx1 = (oneAry[wp1+1].x - oneAry[wp1].x)/(oneAry[wp1+1].time - oneAry[wp1].time);
                        dy1 = (oneAry[wp1+1].y - oneAry[wp1].y)/(oneAry[wp1+1].time - oneAry[wp1].time);
                        dz1 = (oneAry[wp1+1].z - oneAry[wp1].z)/(oneAry[wp1+1].time - oneAry[wp1].time);
                    } else {
                        // We are done.
                        break;
                    }
                    wp1++;
                }
                
                // Update position
                x1 += dx1; y1 += dy1; z1 += dz1;
                
            } else {
                // Check if starting
                if (oneAry[0].time <= time) {
                    x1 = oneAry[0].x;
                    y1 = oneAry[0].y;
                    z1 = oneAry[0].z;
                    dx1 = (oneAry[1].x - oneAry[0].x)/(oneAry[1].time - oneAry[0].time);
                    dy1 = (oneAry[1].y - oneAry[0].y)/(oneAry[1].time - oneAry[0].time);
                    dz1 = (oneAry[1].z - oneAry[0].z)/(oneAry[1].time - oneAry[0].time);
                }
            }
            
            if (x2 >= 0) {
                
                // Check if at waypoint
                if (twoAry[wp2].time <= time) {
		    if(DEBUG) Machinetta.Debugger.debug("Next waypoint for 2: " + time + " " + twoAry[wp2].time, 1, "Path3D");
                    if (twoAry.length > wp2 + 1) {
                        // Should be here, but just to make sure
                        x2 = twoAry[wp2].x;
                        y2 = twoAry[wp2].y;
                        z2 = twoAry[wp2].z;
                        dx2 = (twoAry[wp2+1].x - twoAry[wp2].x)/(twoAry[wp2+1].time - twoAry[wp2].time);
                        dy2 = (twoAry[wp2+1].y - twoAry[wp2].y)/(twoAry[wp2+1].time - twoAry[wp2].time);
                        dz2 = (twoAry[wp2+1].z - twoAry[wp2].z)/(twoAry[wp2+1].time - twoAry[wp2].time);
                    } else {
                        // We are done.
                        break;
                    }
                    wp2++;
                }                               
                
                // Update position
                x2 += dx2; y2 += dy2; z2 += dz2;
                
            } else {
                // Check if starting
                if (twoAry[0].time <= time) {
                    x2 = twoAry[0].x;
                    y2 = twoAry[0].y;
                    z2 = twoAry[0].z;
                    
                    dx2 = (twoAry[1].x - twoAry[0].x)/(twoAry[1].time - twoAry[0].time);
                    dy2 = (twoAry[1].y - twoAry[0].y)/(twoAry[1].time - twoAry[0].time);
                    dz2 = (twoAry[1].z - twoAry[0].z)/(twoAry[1].time - twoAry[0].time);
                }
            }
            
            if (x1 > 0 && x2 > 0) {
 		double zdiff = (z1 - z2);
 		if(zdiff < 0)
 		    zdiff = -zdiff;
 		if(zdiff < minZDist) {
		    double distSqrd = (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) + (z2-z1)*(z2-z1);

		    // 		int oneind = (wp1 > 0) ? wp1 - 1 : 0;
		    // 		int twoind = (wp2 > 0) ? wp2 - 1 : 0;
		    // if(DEBUG) Machinetta.Debugger.debug("Checking at "+time+" wp1["+oneind+"]="+oneAry[oneind]+", wp2["+twoind+"]="+twoAry[twoind]+",  ("+x1+","+y1+","+z1+") against ("+x2+","+y2+","+z2+"), dist="+(Math.sqrt(distSqrd)),1,"Path3D");

		    if (distSqrd < minDistSqrd) {
			if(DEBUG) Machinetta.Debugger.debug("CONFLICT @ " + time + "(" + fmt.format(x1) + ", " + fmt.format(y1) + ", " + fmt.format(z1) + ") and (" + fmt.format(x2) + ", " + fmt.format(y2) + ", " + fmt.format(z2) + ") have zdiff "+fmt.format(zdiff)+" < minZdist "+minZDist+", dist = "+fmt.format(Math.sqrt(distSqrd)), 1, "Path3D");
			conflict = true;
			if(DONE_AFTER_FIRST_CONFLICT) {
			    doneChecking = true;
			    continue;
			}
		    }
		    else {
			if(DEBUG) Machinetta.Debugger.debug(time + "(" + fmt.format(x1) + ", " + fmt.format(y1) + ", " + fmt.format(z1) + ") and (" + fmt.format(x2) + ", " + fmt.format(y2) + ", " + fmt.format(z2) + ") have zdiff "+fmt.format(zdiff)+" < minZdist "+minZDist+", dist = "+fmt.format(Math.sqrt(distSqrd)), 1, "Path3D");
		    }
		}
		else {
		    if(DEBUG) Machinetta.Debugger.debug(time + "(" + fmt.format(x1) + ", " + fmt.format(y1) + ", " + fmt.format(z1) + ") and (" + fmt.format(x2) + ", " + fmt.format(y2) + ", " + fmt.format(z2) + ") have zdiff "+fmt.format(zdiff)+" > minZdist "+minZDist, 1, "Path3D");
		}
            }
            
	    //	    if(DEBUG) Machinetta.Debugger.debug("Time: " + time + "(" + fmt.format(x1) + ", " + fmt.format(y1) + ", " + fmt.format(z1) + ") and (" + fmt.format(x2) + ", " + fmt.format(y2) + ", " + fmt.format(z2) + ")", 1, "Path3D");
            
            time+= timeDelta;
        }
        
        return conflict;
    }
    
    public static boolean conflicts(Waypoint[] oneAry, Waypoint[] twoAry, double minDist, double minZDist) {
        boolean conflict = false;
        boolean doneChecking = false;
        
        if (oneAry.length <= 1 || twoAry.length <= 1) {
            Machinetta.Debugger.debug("Conflict check on path <= 1 can't be done", 1, "Path3D");
            return false;
        }

	double rangeSqd = minDist * minDist;
	
	int ind1 = 0;
	int ind2 = 0;
	while(true) {
	    if(oneAry[ind1].time > twoAry[ind2].time) {
		while(oneAry[ind1].time > twoAry[ind2].time) {
		    ind2++;
		    if(ind2 >= twoAry.length)
			return false;
		}
	    }
	    else if(oneAry[ind1].time < twoAry[ind2].time) {
		while(oneAry[ind1].time < twoAry[ind2].time) {
		    ind1++;
		    if(ind1 >= oneAry.length)
			return false;
		}
	    }
	    Waypoint wp1 = oneAry[ind1];
	    Waypoint wp2 = twoAry[ind2];
	    double zdiff = (wp1.z - wp2.z);
	    if(zdiff < 0) zdiff = -zdiff;
	    if(zdiff < minZDist) {
		double distSqd = (wp2.x-wp1.x)*(wp2.x-wp1.x) + (wp2.y-wp1.y)*(wp2.y-wp1.y) + (wp2.z-wp1.z)*(wp2.z-wp1.z);
		if(distSqd <= rangeSqd) {
		    double dist = Math.sqrt(distSqd);
		    Machinetta.Debugger.debug("conflicts: " +wp1.time+" "+fmt.format(dist)+" "+wp1+" "+wp2,1,"Path3D");
		    return true;
		}
	    }
	    ind1++;
	    ind2++;
	    
	    if(ind2 >= twoAry.length)
		return false;
	    if(ind1 >= oneAry.length)
		return false;
	}
    }
    
    public static boolean conflictsWaypointsOnly(Waypoint[] oneAry, Waypoint[] twoAry, double minDist) {
        double minDistSqd = minDist * minDist;
        int oneLoop = 0;
        int twoLoop = 0;
        while(true) {
            if(oneLoop >= oneAry.length)
                return false;
            if(twoLoop >= twoAry.length)
                return false;
            
            if(null == oneAry[oneLoop]) {
                oneLoop++;
                continue;
            }
            if(null == twoAry[twoLoop]) {
                twoLoop++;
                continue;
            }
            
            if(DEBUG)Machinetta.Debugger.debug("    oneLoop = "+oneLoop+", "+oneAry[oneLoop]+" , twoLoop = "+twoLoop+", "+twoAry[twoLoop],1,"Path3D");
            
            if(oneAry[oneLoop].time < twoAry[twoLoop].time) {
                if(DEBUG)Machinetta.Debugger.debug("    Interpolating between twoLoop-1  and twoLoop",1,"Path3D");
                // interpolate the point at oneWp.time between
                // twoAry[twoLoop - 1] and twoAry[twoLoop]
                if(interpolateAndCheckCollision(oneAry[oneLoop], twoAry, twoLoop, minDistSqd))
                    return true;
                oneLoop++;
            } else {
                if(DEBUG)Machinetta.Debugger.debug("    Interpolating between oneLoop-1  and oneLoop",1,"Path3D");
                // interpolate the point at two.time between
                // oneAry[oneLoop - 1] and oneAry[oneLoop]
                if(interpolateAndCheckCollision(twoAry[twoLoop], oneAry, oneLoop, minDistSqd))
                    return true;
                twoLoop++;
            }
        }
    }
    
    public void append(Path3D other) {
        waypoints.addAll(other.waypoints);
	waypointAryCache = null;
    }
    
    /**
     * Helper method for creating a loop around a point.
     */
    public static Path3D makeLoop(double x, double y, double z, double radius) {
        Path3D path = new Path3D();

	double qPi = Math.PI/4;
	for(double loopd = 0; loopd < 7; loopd++)
	    path.addPoint(new Waypoint(x+radius*Math.cos(qPi*loopd), 
				       y+radius*Math.sin(qPi*loopd), 
				       z));
        path.setLoop(true);
        return path;
    }
    
    public static Path3D makeLoop(double x, double y, double z) {
	return makeLoop(x,y,z,500);
    }

    public ProxyID getAssetID() {
        return assetID;
    }
    
    public void setAssetID(ProxyID assetID) {
        this.assetID = assetID;
    }
    
    public boolean isLoop() {
        return loop;
    }
    
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AssetID="+assetID+", ");
        for (Waypoint v: waypoints) {
            sb.append(v.toString() + "->");
        }
        sb.append("X");
        return sb.toString();
    }
    
    public static Path3D interpolatePath(Path3D path, long startTime, long endTime, long increment) {
	Waypoint[] ary = path.getWaypointsAry();
	Path3D newp = new Path3D();
	newp.assetID = path.assetID;
	newp.loop = path.loop;
	newp.approved = path.approved;

	Waypoint last=ary[0];
	Waypoint next = ary[1];
	int loopAry = 2;
	
	for(long loopTime = startTime; loopTime < endTime; loopTime += increment) {
	    while(loopTime > next.time) {
		if((loopAry+1) > ary.length)
		    break;
		last = next;
		next = ary[loopAry++];
	    }
	    newp.addPoint(Waypoint.interpolate(last,next,loopTime));
	}
	return newp;
    }

    public static void main(String argv[]) {
        
        Path3D path1 = new Path3D();
        path1.addPoint(new Waypoint(0,0,0,150));
        path1.addPoint(new Waypoint(1000,1000,0,250));
        path1.addPoint(new Waypoint(2000,2000,0,300));
        path1.addPoint(new Waypoint(3000,3000,0,400));
        path1.addPoint(new Waypoint(4000,4000,0,500));
        Waypoint[] path1Ary = path1.getWaypointsAry();
        
        // Should conflict with path1 at 2000,2000,0,300
        Path3D path2 = new Path3D();
        path2.addPoint(new Waypoint(4000,4000,0,100));
        path2.addPoint(new Waypoint(3000,3000,0,200));
        path2.addPoint(new Waypoint(2000,2000,0,300));
        path2.addPoint(new Waypoint(1000,1000,0,400));
        path2.addPoint(new Waypoint(0,0,0,500));
        Waypoint[] path2Ary = path2.getWaypointsAry();
        
        // Should NOT conflict with path1
        Path3D path2high = new Path3D();
        path2high.addPoint(new Waypoint(4000,4000,500,100));
        path2high.addPoint(new Waypoint(3000,3000,500,200));
        path2high.addPoint(new Waypoint(2000,2000,500,300));
        path2high.addPoint(new Waypoint(1000,1000,500,400));
        path2high.addPoint(new Waypoint(0,0,500,500));
        Waypoint[] path2highAry = path2high.getWaypointsAry();
        
        // Parallel to path1, 1000 to the right, should not conflict
        Path3D path3 = new Path3D();
        path3.addPoint(new Waypoint(1000,0,0,100));
        path3.addPoint(new Waypoint(2000,1000,0,200));
        path3.addPoint(new Waypoint(3000,2000,0,300));
        path3.addPoint(new Waypoint(4000,3000,0,400));
        path3.addPoint(new Waypoint(5000,4000,0,500));
        Waypoint[] path3Ary = path3.getWaypointsAry();
        
        // Parallel to path1, 1000 to the right, should not conflict
        Path3D path4 = new Path3D();
        path4.addPoint(new Waypoint(1000,0,0,100));
        path4.addPoint(new Waypoint(2000,1000,0,200));
        path4.addPoint(new Waypoint(3000,2000,0,300));
        path4.addPoint(new Waypoint(4000,3000,0,400));
        path4.addPoint(new Waypoint(5000,4000,0,500));
        Waypoint[] path4Ary = path4.getWaypointsAry();
        
        // Runs straight up and down, should conflict at 4000,4000,0,500
        Path3D path5 = new Path3D();
        path5.addPoint(new Waypoint(4000,0,0,100));
        path5.addPoint(new Waypoint(4000,1000,0,200));
        path5.addPoint(new Waypoint(4000,2000,0,300));
        path5.addPoint(new Waypoint(4000,3000,0,400));
        path5.addPoint(new Waypoint(4000,4000,0,500));
        Waypoint[] path5Ary = path5.getWaypointsAry();
        
        // Runs straight up and down, should NOT conflict
        Path3D path5high = new Path3D();
        path5high.addPoint(new Waypoint(4000,0,500,100));
        path5high.addPoint(new Waypoint(4000,1000,500,200));
        path5high.addPoint(new Waypoint(4000,2000,500,300));
        path5high.addPoint(new Waypoint(4000,3000,500,400));
        path5high.addPoint(new Waypoint(4000,4000,500,500));
        Waypoint[] path5highAry = path5high.getWaypointsAry();
        
        // Runs straigth up and down, should conflict somewhere
        Path3D path6 = new Path3D();
        path6.addPoint(new Waypoint(3000,0,0,100));
        path6.addPoint(new Waypoint(3000,1000,0,200));
        path6.addPoint(new Waypoint(3000,2000,0,300));
        path6.addPoint(new Waypoint(3000,3000,0,400));
        path6.addPoint(new Waypoint(3000,4000,0,500));
        Waypoint[] path6Ary = path6.getWaypointsAry();
        
	double minZDist = 100;

        Machinetta.Debugger.debug("Checking path1 against path2 (SHOULD CONFLICT)",1,"Path3D"); 
        if(Path3D.conflicts(path1Ary,path2Ary,100,minZDist)) {
            Machinetta.Debugger.debug("    path1 CONFLICTs with path2",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path1 does NOT conflict with path2",1,"Path3D");
        }
        Machinetta.Debugger.debug("Checking path1 against path2high (SHOULD NOT CONFLICT)",1,"Path3D"); 
        if(Path3D.conflicts(path1Ary,path2highAry,100,minZDist)) {
            Machinetta.Debugger.debug("    path1 CONFLICTs with path2high",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path1 does NOT conflict with path2high",1,"Path3D");
        }
        Machinetta.Debugger.debug("Checking path1 against path3 (SHOULD NOT CONFLICT)",1,"Path3D");
        if(Path3D.conflicts(path1Ary,path3Ary,100,minZDist)) {
            Machinetta.Debugger.debug("    path1 CONFLICTs with path3",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path1 does NOT conflict with path3",1,"Path3D");
        }
        Machinetta.Debugger.debug("Checking path1 against path4 (SHOULD NOT CONFLICT)",1,"Path3D");
        if(Path3D.conflicts(path1Ary,path4Ary,100,minZDist)) {
            Machinetta.Debugger.debug("    path1 CONFLICTs with path4",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path1 does NOT conflict with path4",1,"Path3D");
        }
        Machinetta.Debugger.debug("Checking path1 against path5 (SHOULD CONFLICT)",1,"Path3D");
        if(Path3D.conflicts(path1Ary,path5Ary,100,minZDist)) {
            Machinetta.Debugger.debug("    path1 CONFLICTs with path5",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path1 does NOT conflict with path5",1,"Path3D");
        }
        Machinetta.Debugger.debug("Checking path1 against path5high (SHOULD NOT CONFLICT)",1,"Path3D");
        if(Path3D.conflicts(path1Ary,path5highAry,100,minZDist)) {
            Machinetta.Debugger.debug("    path1 CONFLICTs with path5high",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path1 does NOT conflict with path5high",1,"Path3D");
        }
        Machinetta.Debugger.debug("Checking path1 against path6 (SHOULD CONFLICT)",1,"Path3D");
        if(Path3D.conflicts(path1Ary,path6Ary,100,minZDist)) {
            Machinetta.Debugger.debug("    path1 CONFLICTs with path6",1,"Path3D");
        } else {
            Machinetta.Debugger.debug("    path1 does NOT conflict with path6",1,"Path3D");
        }
        Machinetta.Debugger.debug("Done.",1,"Path3D");
        
    }
}
