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
 * SimpleStaticCostMap.java
 *
 * Created on March 15, 2006, 2:32 PM
 *
 */

package AirSim.Machinetta.CostMaps;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.awt.Polygon;
import java.awt.Point;

/**
 * This is a simple 3D cost map that does not care about time.
 *
 * @author pscerri
 */
public class SimpleStaticCostMap3D implements CostMap, Serializable {
    
    protected ArrayList<WeightedShape> shapes = new ArrayList<WeightedShape>();
    protected double minCost = 0.0, maxCost = 0.0;
    
    /** Creates a new instance of SimpleStaticCostMap */
    public SimpleStaticCostMap3D() {
    }
    
    public void clear() {
	shapes.clear();
    }

    public void addCostBox(double x1, double y1, double z1, double x2, double y2, double z2, double cost) {
        
        synchronized(shapes) {
            shapes.add(new WeightedBox(x1,y1,z1,x2,y2,z2,cost));
        }
        if (cost > maxCost) maxCost = cost;
        if (cost < minCost) minCost = cost;
    }
    
    public void addCostPoly(Polygon poly, double cost, boolean invert) {
        synchronized(shapes) {
            shapes.add(new WeightedPolygon(poly,cost,invert));
        }
        if (cost > maxCost) maxCost = cost;
        if (cost < minCost) minCost = cost;
    }

    public void addCostPoly(Point[] pointAry, double cost, boolean invert) {
        synchronized(shapes) {
            shapes.add(new WeightedPolygon(pointAry,cost,invert));
        }
        if (cost > maxCost) maxCost = cost;
        if (cost < minCost) minCost = cost;
    }
    public void addCostPoly(Polygon poly, double cost) {
	addCostPoly(poly,cost,false);
    }
    public void addCostPoly(Point[] pointAry, double cost) {
	addCostPoly(pointAry,cost,false);
    }
    
    /**
     * Return the cost if a vehicle moves from (x1,y1) at t1 to (x2,y2) at t2
     */
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        return getCost(x1, y1, 0, t1, x2, y2, 0, t2);
    }
    
    /**
     * Return the cost if a vehicle moves from (x1,y1,z1) at t1 to (x2,y2,z2) at t2
     */
    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        double cost = 0;
        synchronized (shapes) {
            for (WeightedShape wr: shapes) {
                if (wr.intersectsLine(x1, y1,z1, x2, y2,z2)) cost += wr.value;
            }
        }
        
        return cost;
    }
    
    /**
     * Which Rectangles are good to be in at time t.
     *
     * Assumes good to be in best 20%
     */
    public ArrayList<Rectangle> getGoodAreas(long t) {
	return null;
    }
    
    /**
     * Which Rectangles are bad to be in at time t.
     *
     * Assumes bad to be in worst 20%
     */
    public ArrayList<Rectangle> getBadAreas(long t) {
        return null;
    }
    
    public void timeElapsed(long t) {
        // The "Static" in this classes name makes this comment superfluous
    }
    
    protected abstract class WeightedShape implements Serializable {
        double value = 0.0;
	public abstract boolean intersectsLine(double x1, double y1, double z1, double x2, double y2, double z2);
    }

    protected class WeightedBox extends WeightedShape implements Serializable {
	double x1;
	double y1;
	double z1;
	double x2;
	double y2;
	double z2;
        
        public WeightedBox(double x1, double y1, double z1, double x2, double y2, double z2, double v) {
	    double temp;
	    if(x1 > x2) {
		temp = x1;
		x1 = x2;
		x2 = temp;
	    }
	    if(y1 > y2) {
		temp = y1;
		y1 = y2;
		y2 = temp;
	    }
	    if(z1 > z2) {
		temp = z1;
		z1 = z2;
		z2 = temp;
	    }
	    this.x1 = x1;
	    this.y1 = y1;
	    this.z1 = z1;
	    this.x2 = x2;
	    this.y2 = y2;
	    this.z2 = z2;
            value = v;
        }
	
	private boolean contains(double px, double py, double pz) {
	    if((px >= x1) 
	       && (px <= x2)
	       && (py >= y1) 
	       && (py <= y2)
	       && (pz >= z1) 
	       && (pz <= z2))
		return true;
	    else
		return false;
	}

	public boolean intersectsLine(double x1, double y1, double z1, double x2, double y2, double z2) {
	    // @TODO: Make this work _right_ - right now it jsut
	    // checks the end point.  Google on "line segment bounding
	    // box intersection"
	    if(contains(x1,y1,z2) && contains(x2,y2,z2))
		return true;
	    else 
		return false;
	}
    }

    protected class WeightedPolygon extends WeightedShape implements Serializable {
	Polygon poly;
        boolean invert = true;
        public WeightedPolygon(Point[] pointAry, double v, boolean invert) {
            value = v;
	    this.invert = invert;
	    poly = new Polygon();
	    for(int loopi = 0; loopi < pointAry.length; loopi++)
		poly.addPoint(pointAry[loopi].x, pointAry[loopi].y);
        }
	
        public WeightedPolygon(Polygon poly, double v, boolean invert) {
	    this.poly = poly;
	    value = v;
	    this.invert = invert;
        }
	
	private boolean contains(double px, double py, double pz) {
	    if(invert) {
		return !poly.contains(px,py);
	    }
	    else {
		return poly.contains(px,py);
	    }
	}

	public boolean intersectsLine(double x1, double y1, double z1, double x2, double y2, double z2) {
	    // @TODO: Make this work _right_ - right now it jsut
	    // checks the end point.  Google on "line segment bounding
	    // box intersection"
	    if(contains(x1,y1,z2) && contains(x2,y2,z2))
		return true;
	    else 
		return false;
	}
    }
}
