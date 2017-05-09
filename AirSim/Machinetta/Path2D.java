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
 * Path.java
 *
 * Created on September 26, 2005, 2:02 PM
 *
 */

package AirSim.Machinetta;

import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * A path in 2d; generally a path that an asset is planning to follow.
 * This class should probably be deprecated in favor of PlannedPath.
 *
 * @author pscerri
 */
public class Path2D extends Belief {
    
    public long startTime, endTime;
    public LinkedList<Point2D> wps = null;            
            
    /** Creates a new instance of Path */
    public Path2D(long startTime, long endTime, Point2D startLoc) {
        wps = new LinkedList<Point2D>();
        wps.add(startLoc);
        this.startTime = startTime;
        this.endTime = endTime;
        
        makeID();
    }

    public void add(Point2D loc) {
	wps.add(loc);
    }

    public Machinetta.State.BeliefID makeID() {
        if (id == null) {
            id = new BeliefNameID("Path:" + startTime + ":" + endTime + ":" + wps.getFirst().getID());     
        }
        return id;
    }
        
    public Point2D removeFirst() {
        if (wps.size() > 0) {
            return wps.removeFirst();
        } else 
            return null;
    }
    
    public Point2D get(int index) {
	return wps.get(index);
    }

    public Point2D viewLast() {
	Point2D last = wps.get(wps.size() - 1);
	return new Point2D(last.getX(), last.getY());
    }
    
    //    public String toString() { return getID().toString() + ": " + wps; }
    public String toString() { 
	String pathString =   getID().toString() + ": ";
	Iterator iter = wps.iterator();
	while(iter.hasNext()) {
	    Point2D p = (Point2D)iter.next();
	    pathString += ",("+p+")";
	}
	return pathString;
    }
    
    public static final long serialVersionUID = 1L;    
}
