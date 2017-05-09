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
 * Area.java
 *
 * Created on August 16, 2005, 6:10 PM
 *
 */

package AirSim.Environment;

import java.awt.Rectangle;

// @TODO: Why does this exist?  Why don't we replace it with something
// like java.awt.geom.Rectangle2D.Double?

/**
 *
 * @author pscerri
 */
public class Area implements java.io.Serializable {
    
    public double x1,y1,x2,y2;
    
    /**
     * Creates a new instance of Area
     */
    public Area(double x1, double y1, double x2, double y2) {
	if(x1> x2) {
	    double temp = x1;
	    x1 = x2;
	    x2 = temp;
	}
	if(y1> y2) {
	    double temp = y1;
	    y1 = y2;
	    y2 = temp;
	}
        this.setX1(x1);
        this.setY1(y1);
        this.setX2(x2);
        this.setY2(y2);
    }
    
    private static final long serialVersionUID = 1L;
    
    public double getX1() { return x1; }    
    public void setX1(double x1) { this.x1 = x1; }    
    public double getY1() { return y1; }    
    public void setY1(double y1) { this.y1 = y1; }    
    public double getX2() { return x2; }    
    public void setX2(double x2) { this.x2 = x2; }    
    public double getY2() { return y2; }    
    public void setY2(double y2) { this.y2 = y2; }

    public double getCenterX() { return (x1+x2)/2;}
    public double getCenterY() { return (y1+y2)/2;}

    public double getWidth() { 
	return Math.abs(x2 - x1); 
    }
    public double getHeight() {
	return Math.abs(y2 - y1); 
    }
    
    public String toString() {
	return "x1="+x1+", y1="+y1+", x2="+x2+", y2="+y2;
    }
    
    public Rectangle getAsRect () { return new Rectangle((int)x1, (int)y1, (int)(x2-x1), (int)(y2-y1)); }
    
    public boolean inside(double x, double y) {
	return (x > x1) && (x < x2) && (y > y1) && (y < y2);
    }

}
