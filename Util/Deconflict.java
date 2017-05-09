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
 * Deconflict.java
 *
 * Created on August 15, 2005, 3:51 PM
 *
 */

package Util;

import java.awt.Point;
import java.util.*;

/**
 *
 * @author pscerri
 */
public class Deconflict {
    
    LinkedList <Waypoint> p1, p2 = null;
    Random rand = new Random();
    final double maxX = 100.0, maxY = 100.0, maxZ = 100.0;
    
    /** Creates a new instance of Deconflict */
    public Deconflict(boolean quiet) {
        
        p1 = new LinkedList<Waypoint>();
        p2 = new LinkedList<Waypoint>();
        
        for (int i = 0; i < 20; i++) {
            p1.add(new Waypoint(rand.nextDouble() * maxX, rand.nextDouble() * maxY, rand.nextDouble() * maxZ, (double)i));
            p2.add(new Waypoint(rand.nextDouble() * maxX, rand.nextDouble() * maxY, rand.nextDouble() * maxZ, (double)i));
        }
        
        if (!quiet) {
            System.out.println("UAV 1 Path: " + p1);
            System.out.println("UAV 2 Path: " + p2);
            System.out.println("Closest they get is : " + closestDist(p1, p2));
        } else {
            closestDist(p1, p2);
        }
        
        
        
    }
    
    private double closestDist(LinkedList<Waypoint> p1, LinkedList<Waypoint> p2) {
        
        ListIterator<Waypoint> li1 = p1.listIterator(), li2 = p2.listIterator();
        Waypoint w1 = li1.next(), w2 = li2.next();
        
        double x1 = w1.x, y1 = w1.y, x2 =  w2.x, y2 = w2.y, z1 = w1.z, z2 = w2.z;
        boolean done1 = false, done2 = false;
        double closest = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) + (z1-z2)*(z1-z2));
        double t = 0.0, inc = 0.001;
        
        Waypoint nw1 = li1.next(), nw2 = li2.next();
        PathSegment path1 = new PathSegment(w1, nw1), path2 = new PathSegment(w2, nw2);
        
        while (!done1 && !done2) {
            t += inc;
            x1 += (path1.dx * inc);
            y1 += (path1.dy * inc);
            z1 += (path1.dz * inc);
            
            if (t > nw1.t) {
                w1 = nw1;
                if (li1.hasNext()) {
                    nw1 = li1.next();
                    path1 = new PathSegment(w1, nw1);
                } else {
                    done1 = true;
                }
            }
            
            x2 += (path2.dx * inc);
            y2 += (path2.dy * inc);
            z2 += (path2.dz * inc);
            
            if (t > nw2.t) {
                w2 = nw2;
                if (li2.hasNext()) {
                    nw2 = li2.next();
                    path2 = new PathSegment(w2, nw2);
                } else {
                    done2 = true;
                }
            }
            
            double dist = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) + (z1-z2)*(z1-z2));
            if (dist < closest) closest = dist;
        }
        
        return closest;
    }
    
    public static void main(String argv[]) {
        int runs = 1000;
        Date start = new Date();
        for (int i = 0; i < runs; i++)
            new Deconflict(true);
        Date end = new Date();
        System.out.println("To do " + runs + " conflict detections took " + (end.getTime()-start.getTime()) + "ms");
        System.out.println("Here is an example of the deconfliction: ");
        new Deconflict(false);
    }
    
    
    class Waypoint {
        public double x, y, z, t;
        public Waypoint(double x, double y, double z, double t) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
        }
        public String toString() { return "<" + Util.PrintHelpers.doubleToString(x, 3) + ", " + Util.PrintHelpers.doubleToString(y, 3) + ", " + Util.PrintHelpers.doubleToString(z, 3) + "> @ " + Util.PrintHelpers.doubleToString(t, 3) ; }
    }
    
    class PathSegment {
        public double dx, dy, dz;
        public PathSegment(Waypoint w1, Waypoint w2) {
            dx = w2.x - w1.x;
            dy = w2.y - w1.y;
            dz = w2.z - w1.z;
            
            dx *= 1/(w2.t - w1.t);
            dy *= 1/(w2.t - w1.t);
            dz *= 1/(w2.t - w1.t);
        }
    }
}
