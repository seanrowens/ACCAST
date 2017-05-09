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

/**
 * This is a simple 2D cost map that does not care about time.
 *
 * @author pscerri
 */
public class SimpleStaticCostMap implements CostMap, Serializable {
    
    protected ArrayList<WeightedShape> shapes = new ArrayList<WeightedShape>();
    protected double minCost = 0.0, maxCost = 0.0;
    
    /** Creates a new instance of SimpleStaticCostMap */
    public SimpleStaticCostMap() {
    }
    
    public void addCostRect(Rectangle r, double cost) {
        
        synchronized(shapes) {
            shapes.add(new WeightedRect(r,cost));
        }
        if (cost > maxCost) maxCost = cost;
        if (cost < minCost) minCost = cost;
    }
    
    public void addCostCircle(double x, double y, double radius, double cost) {
	double cx = x - radius;
	double cy = y - radius;
	double width = radius * 2;
	double height = radius * 2;
	Ellipse2D.Double circle = new Ellipse2D.Double(cx,cy,width,height);
        
        synchronized(shapes) {
            shapes.add(new WeightedCircle(circle,cost));
        }
        if (cost > maxCost) maxCost = cost;
        if (cost < minCost) minCost = cost;
    }
    
    /**
     * Return the cost if a vehicle moves from (x1,y1,z1) at t1 to (x2,y2,z2) at t2
     *
     * Since this is a 2D cost map, return 2D cost
     */
    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        return getCost(x1, y1, t1, x2, y2, t2);
    }
    
    /**
     * Return the cost if a vehicle moves from (x1,y1) at t1 to (x2,y2) at t2
     */
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        double cost = 0;
        synchronized (shapes) {
            for (WeightedShape wr: shapes) {
                if (wr.intersectsLine(x1, y1, x2, y2)) {
                    cost += wr.value;
                    // System.out.println("yes " + wr.value);
                } else {
                    // System.out.println("Nup " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + wr);
                }
            }
        }
        
        // System.out.println("Returning " + cost + " for " + shapes);
        
        return cost;
    }
    
    /**
     * Which Rectangles are good to be in at time t.
     *
     * Assumes good to be in best 20%
     */
    public ArrayList<Rectangle> getGoodAreas(long t) {
        ArrayList<Rectangle> ret = new ArrayList<Rectangle>();
        double goodCost = minCost + (maxCost-minCost)*0.2;
        
        synchronized(shapes) {
            for (WeightedShape wr: shapes) {
		// @TODO: ignore the circle shapes for now - going to
		// have to do something about this later.
		if(wr instanceof WeightedRect)
		   if (wr.value < goodCost) ret.add(((WeightedRect)wr).r);
            }
        }
        return ret;
    }
    
    /**
     * Which Rectangles are bad to be in at time t.
     *
     * Assumes bad to be in worst 20%
     */
    public ArrayList<Rectangle> getBadAreas(long t) {
        ArrayList<Rectangle> ret = new ArrayList<Rectangle>();
        double badCost = maxCost - (maxCost-minCost)*0.2;
        
        synchronized(shapes) {
            for (WeightedShape wr: shapes) {
		// @TODO: ignore the circle shapes for now - going to
		// have to do something about this later.
		if(wr instanceof WeightedRect)
		   if (wr.value > badCost) ret.add(((WeightedRect)wr).r);
            }
        }
        return ret;
    }
    
    public void timeElapsed(long t) {
        // The "Static" in this classes name makes this comment superfluous
    }
    
    protected abstract class WeightedShape implements Serializable {
        double value = 0.0;
	public abstract boolean intersectsLine(double x1, double y1, double x2, double y2);
    }

    protected class WeightedRect extends WeightedShape implements Serializable {
        Rectangle r = null;
        
        public WeightedRect(Rectangle rect, double v) {
            r = rect;
            value = v;
        }
	
	public boolean intersectsLine(double x1, double y1, double x2, double y2) {
	    return r.intersectsLine(x1, y1, x2, y2);
	}
        
        public String toString() {
            return r.toString() + ", cost = " + value;
        }
    }

    protected class WeightedCircle extends WeightedShape implements Serializable {
        Ellipse2D.Double c = null;
        
        public WeightedCircle(Ellipse2D.Double circle, double v) {
            c = circle;
            value = v;
        }
	
	public boolean intersectsLine(double x1, double y1, double x2, double y2) {
	    Rectangle garbage = new Rectangle(400,400,200,200);
	    if(garbage.intersectsLine(x1,y1,x2,y2))
		return true;
	    double xdiff = x2 - x1;
	    double ydiff = y2 - y1;
	    double radius = c.width/2;
	    double circlex = c.x + radius;
	    double circley = c.y + radius;
	    double a = xdiff*xdiff + ydiff*ydiff;
	    double b = 2*( xdiff * (x1 - circlex) + ydiff *(y1 - circley)  );
	    double c = circlex*circlex + circley*circley + x1*x1 + y1*y2 - 2*(circlex*x1 + circley*y1) - radius*radius;
	    double testval = b * b - 4 * a * c;
	    if(testval <= 0)
		return false;
	    else
		return true;
	}
    }
    
}
