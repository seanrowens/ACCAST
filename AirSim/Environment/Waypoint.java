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
 * Waypoint.java
 *
 * Created on June 8, 2004, 8:48 PM
 */

package AirSim.Environment;

/**
 *
 * @author  paul
 */
public class Waypoint extends Vector3D {
    
    public long time;
    public long getTime() { return time; }
    public long addTime(long value) { time += value; return time; }
    public void setTime(long value) { time = value; }

    public Waypoint(double x, double y, double z) {
	super(x,y,z);
	time = -1;
    }

    public Waypoint(Vector3D vec) {
	super(vec);
	time = -1;
    }
    public Waypoint(double x, double y, double z, long time) {
	super(x,y,z);
	this.time = time;
    }

    public boolean equals(Object o) {

        if(null == o)
            return false;
	if(o instanceof Vector3D) 
	    return super.equals(o);

        if(!(o instanceof Waypoint))
            return false;
        
        Waypoint w = (Waypoint) o;
        return ((w.time == this.time)
		&& (w.x == this.x)
		&& (w.y == this.y)
		&& (w.z == this.z)
		&& (w.length == this.length));
    }

//     public Vector3D vectorTo(double x2, double y2, double z2) {
// 	return toVector(x2,y2,z2);
//     }

    public String toString() { return "(" + dts(x) + "," + dts(y) + "," + dts(z) + ", t="+time+ ")"; }

    public static Waypoint interpolate(double x1, double y1, double z1, long t1,  double x2, double y2, double z2, long t2, double percent) {
	double x3, y3, z3;
	long t3;
	x3 = x1 + percent*(x2 - x1);
	y3 = y1 + percent*(y2 - y1);
	z3 = z1 + percent*(z2 - z1);
	if((t1 != -1) && (t2 != -1))
	    t3 = (long)(t1 + percent*(t2-t1));
	else
	    t3 = -1;
	return new Waypoint(x3,y3,z3,t3);
    }

    public static Waypoint interpolate(Waypoint p1, Waypoint p2, double percent) {
	return interpolate(p1.x,p1.y,p1.z,p1.getTime(),p2.x,p2.y,p2.z,p2.getTime(),percent);
    }

    public static Waypoint interpolate(Waypoint p1, Waypoint p2, long simTimeMs) {
	if(p2.getTime() < p1.getTime())
	    return null;

	double span = p2.getTime() - p1.getTime();
	double partialSpan = simTimeMs-p1.getTime();
	//	double percent = ((double)(simTimeMs-p1.getTime()))/((double)(p2.getTime() - p1.getTime()));
	double percent = partialSpan/span;
	//	System.err.println("wholeSpan = "+span+", partial="+partialSpan+" percent is "+percent);
	//	Machinetta.Debugger.debug("interpolate: p1.time="+p1.getTime()+" p2.time="+p2.getTime()+" wholeSpan = "+span+", partial="+partialSpan+" percent is "+percent,1,"Waypoint");
	return interpolate(p1,p2,percent);
    }


    public static void main(String[] argv) {
	Waypoint w1 = new Waypoint(100,100,100,1000);
	Waypoint w2 = new Waypoint(1100,100,100,2000);
	Waypoint w3 = interpolate(w1,w2,1500);
	System.err.println("Interpolate between "+w1+" and "+w2+" at time 1500, new wp is "+w3);
	Waypoint w4 = new Waypoint(100,100,100,1000);
	Waypoint w5 = new Waypoint(1100,110,1100,101000);
	Waypoint w6 = interpolate(w4,w5,26000);
	Waypoint w7 = interpolate(w4,w5,51000);
	Waypoint w8 = interpolate(w4,w5,76000);
	System.err.println("Interpolate between "+w4+" and "+w5+" at time 26000, new wp is "+w6);
	System.err.println("Interpolate between "+w4+" and "+w5+" at time 51000, new wp is "+w7);
	System.err.println("Interpolate between "+w4+" and "+w5+" at time 76000, new wp is "+w8);

    }
}
