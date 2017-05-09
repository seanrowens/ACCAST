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
 * RoadInfo.java
 *
 * Created on December 10, 2007, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment;

import AirSim.Environment.Vector3D;
import java.awt.Polygon;
import java.awt.Point;

/**
 *
 * @author junyounk
 */
public class RoadInfo {
    
    public int tlid;
    public String type = null;
    //original road point
    public Point p1, p2;
    public double width;
    public Polygon poly;
    
    /** Creates a new instance of RoadInfo */
    public RoadInfo(int tlid, String type, int x1, int y1, int x2, int y2, double width) {
        this.tlid = tlid;
        this.type = type;
        if(x1 < x2) {
            this.p1 = new Point(x1, y1);
            this.p2 = new Point(x2, y2);        
        } else {
            this.p1 = new Point(x2, y2);
            this.p2 = new Point(x1, y1);
        }
        
        //@NOTE: do we need to multiply relavant factor value? (in this case, 10.)
        //this.width = width*10;
        this.width = width;
        
        Vector3D v1 = new Vector3D(p1.x, p1.y, 0);
        Vector3D v2 = new Vector3D(p2.x, p2.y, 0);
        Vector3D dTempVec = v1.toVector(v2);
        double length = dTempVec.length();
        Vector3D dVec = new Vector3D(dTempVec.x*this.width/length, dTempVec.y*this.width/length, 0);
        
        Vector3D tempV1 = new Vector3D(v1.x+dVec.x, v1.y+dVec.y, 0);
        Vector3D tempV2 = new Vector3D(v2.x-dVec.x, v2.x-dVec.y, 0);
        
        Point tp1 = transform(new Point((int)tempV1.x, (int)tempV1.y), p1, -Math.PI/2);
        Point tp2 = transform(new Point((int)tempV1.x, (int)tempV1.y), p1, Math.PI/2);
        Point tp3 = transform(new Point((int)tempV2.x, (int)tempV2.y), p2, -Math.PI/2);
        Point tp4 = transform(new Point((int)tempV2.x, (int)tempV2.y), p2, Math.PI/2);
        
        /*
        Machinetta.Debugger.debug(1, "tp1.x: " + tp1.x + "      , tp1.y: "+tp1.y);
        Machinetta.Debugger.debug(1, "tp2.x: " + tp2.x + "      , tp2.y: "+tp2.y);
        Machinetta.Debugger.debug(1, "tp3.x: " + tp3.x + "      , tp3.y: "+tp3.y);
        Machinetta.Debugger.debug(1, "tp4.x: " + tp4.x + "      , tp4.y: "+tp4.y);
        */
        
        poly = new Polygon();
        poly.addPoint(tp1.x, tp1.y);
        poly.addPoint(tp2.x, tp2.y);
        poly.addPoint(tp3.x, tp3.y);
        poly.addPoint(tp4.x, tp4.y);
    }
    
    public RoadInfo() {        
    }
    
    private Point transform(Point p1, Point p2, double rad) {
        int dx = p1.x-p2.x;
        int dy = p1.y-p2.y;
        Point ret = new Point();
        ret.x = (int)(Math.cos(rad)*dx - Math.sin(rad)*dy + p2.x);
        ret.y = (int)(Math.sin(rad)*dx + Math.cos(rad)*dy + p2.y);
        
        return ret;
    }
    
    public boolean isContain(int x, int y) {
        return poly.contains(new Point(x, y));
    }
}
