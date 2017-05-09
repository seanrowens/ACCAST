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
 * Vector.java
 *
 * Created on June 8, 2004, 8:57 PM
 */

package AirSim.Environment;

/**
 *
 * @author  paul
 */
public class Vector3D implements java.io.Serializable {
    
    public double x;
    public double getX() { return x; }
    public void setX(double value) { x = value; }
    public double y;
    public double getY() { return y; }
    public void setY(double value) { y = value; }
    public double z;
    public double getZ() { return z; }
    public void setZ(double value) { z = value; }
    public double length = 1.0;
    
    public boolean equals(Object o) {

        if(null == o)
            return false;
        if(!(o instanceof Vector3D))
            return false;
        
        Vector3D v = (Vector3D) o;
        return ((v.x == this.x)
        && (v.y == this.y)
        && (v.z == this.z)
        && (v.length == this.length));
    }
    
    private void init(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Creates a new instance of Vector */
    public Vector3D(double x, double y, double z) {
	init(x,y,z);
    }

    public Vector3D(int x, int y, int z) {
	init((double)x,(double)y,(double)z);
    }
    
    public Vector3D(Vector3D v) {
	init(v.x, v.y, v.z);
    }
    
    public void add(Vector3D v) {
	x = x + v.x;
	y = y + v.y;
	z = z + v.z;
    }

    public void setLength(double l) {
        length = l;
        normalize();
    }
    
    /** Normalizes, so vector length = length */
    public void normalize() {
        double tot = length();
        x *= (length/tot);
        y *= (length/tot);
        z *= (length/tot);
    }
    
    public double length() { return Math.sqrt(x*x + y*y + z*z); }
    
    public double lengthSqd() { return (x*x + y*y + z*z); }
    
    /** Vector from x,y,z to the point represented as this vector */
    public Vector3D toVector(double x2, double y2, double z2) {
        return new Vector3D(x - x2, y - y2, z - z2);
    }
    public Vector3D toVector(Vector3D v) { return toVector(v.x, v.y, v.z); }
    
    public double toVectorLengthSqd(double x2, double y2, double z2) {
	double xd = x - x2;
	double yd = y - y2;
	double zd = z - z2;
	return (xd * xd + yd * yd + zd * zd);
    }

    public double toVectorLengthSqd(Vector3D v) { 
	return toVectorLengthSqd(v.x, v.y, v.z);
    }

    public double toVectorLength(double x2, double y2, double z2) {
	return Math.sqrt(toVectorLengthSqd(x2,y2,z2));
    }

    public double toVectorLength(Vector3D v) { 
	return Math.sqrt(toVectorLengthSqd(v));
    }
    
    /** Normalizes, so vector length in x-y plane = length */
    public void normalize2d() {
        //System.out.println("Before norm: " + this);
        double tot = Math.sqrt(x*x + y*y);
        //System.out.println("Length: " + length + " curr: " + tot + " factor: " + (length/tot));
        if (tot > 0.0) {
            x *= (length/tot);
            y *= (length/tot);            
        } else {
            Machinetta.Debugger.debug("Avoided /0", 0, this);
        }
        //System.out.println("After norm: " + this);
    }
    
    /** Returns the angle to the given Vector from this (in degrees).
     * Notice, only considers x-y plane.
     */
    public double angleToXY(Vector3D d) {
        double ret = 0.0;
        
        double a1 = angle(), a2 = d.angle();
        ret = a2 - a1;
        if (Math.abs(ret) > 180.0) {
            if (ret > 180.0) ret = -360.0 + ret;
            else if (ret < -180.0) ret = 360.0 + ret;
        }
        return ret;
    }
    
    /** Calculates angle in x-y plane */
    public double angle() {
        double ret = 0.0;
        
        if (x == 0.0 && y == 0.0) {
            ret = 0.0;
        } else if (x >= 0 && y >= 0) {
            ret = Math.atan(y/x);
        } else if (x <= 0 && y >= 0) {
            ret = Math.PI + Math.atan(y/x);
        } else if (x >= 0 && y <= 0) {
            ret = Math.atan(y/x);
        } else if (x <= 0 && y <= 0) {
            ret = -Math.PI + Math.atan(y/x);
        }
        
        return Math.toDegrees(ret);
    }
    
    // @TODO: Something about these two methods looks not right.  Hmm
    // One should take radians, one should take degrees... Aha!  Wait,
    // what the?
    // 
    /** Turns vector by delta in x-y plane */
    public void turn(double delta) {
        double na = Math.toRadians(angle() + delta);
        x = length * Math.cos(na);
        y = length * Math.sin(na);
    }

    // @TODO: I'm fairly sure this one is broken and turn is
    // not - angle() returns degrees.
//     public void turnDegrees(double deltaDegrees) {
//         double na = Math.toRadians(angle()) + Math.toRadians(deltaDegrees);
//         x = length * Math.cos(na);
//         y = length * Math.sin(na);
//     }
    
    /** Turns vector by delta in x-y plane */
    public void setXYHeading(double heading) {
        double na = Math.toRadians(heading);
        x = length * Math.cos(na);
        y = length * Math.sin(na);
    }
    
    public Vector3D makeCopy() {
        Vector3D copy = new Vector3D(x, y, z);
        copy.length = length;
        return copy;
    }
    
    public String toString() { return "(" + dts(x) + "," + dts(y) + "," + dts(z) + ")"; }
    
    public static Vector3D interpolate(double x1, double y1, double z1, double x2, double y2, double z2, double percent) {
	double x3, y3, z3;
	x3 = x1 + percent*(x2 - x1);
	y3 = y1 + percent*(y2 - y1);
	z3 = z1 + percent*(z2 - z1);
        return new Vector3D(x3,y3,z3);
    }

    public static Vector3D interpolate(Vector3D p1, Vector3D p2, double percent) {
	return interpolate(p1.x,p1.y,p1.z,p2.x,p2.y,p2.z,percent);
    }

    public static String dts(double d) {
        String s = "" + d;
        s = s.substring(0, s.lastIndexOf('.') + 2);
        return s;
    }
    
    private static final long serialVersionUID = 1L;
    
    /** For testing */
    public static void main(String argv[]) {
        /*
        Vector v1 = new Vector3D(1, 10, 0);
        Vector v2 = new Vector3D(2, 1, 0);
        Vector v3 = new Vector3D(-2, 1, 0);
        Vector v4 = new Vector3D(2, -3, 0);
        Vector v5 = new Vector3D(-3, -2, 0);
        System.out.println(v1 + ": " + v1.angle());
        System.out.println(v2 + ": " + v2.angle());
        System.out.println(v3 + ": " + v3.angle());
        System.out.println(v4 + ": " + v4.angle());
        System.out.println(v5 + ": " + v5.angle());
        System.out.println(v1.angleToXY(v2) + " " + v2.angleToXY(v1));
        System.out.println(v4.angleToXY(v1) + " " + v1.angleToXY(v4));
        System.out.println(v3.angleToXY(v2) + " " + v2.angleToXY(v3));
        System.out.println(v3.angleToXY(v5) + " " + v5.angleToXY(v3));
        System.out.println(v2.angleToXY(v4) + " " + v4.angleToXY(v2));
        System.out.println(v5.angleToXY(v2) + " " + v2.angleToXY(v5));
         */
        Vector3D v1 = new Vector3D(1, 0, 0);
        System.out.println("Vector :" + v1 + " angle : " + v1.angle());
        v1.turn(45);
        System.out.println("Vector :" + v1 + " angle : " + v1.angle());
        v1.turn(45);
        System.out.println("Vector :" + v1 + " angle : " + v1.angle());
        v1.turn(45);
        System.out.println("Vector :" + v1 + " angle : " + v1.angle());
        v1.turn(-45);
        System.out.println("Vector :" + v1 + " angle : " + v1.angle());
        v1.turn(-45);
        System.out.println("Vector :" + v1 + " angle : " + v1.angle());
        v1.turn(-5);
        System.out.println("Vector :" + v1 + " angle : " + v1.angle());
    }
}
