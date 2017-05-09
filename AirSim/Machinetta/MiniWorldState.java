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
 * MiniWorldState.java
 *
 */
package AirSim.Machinetta;

import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.CostMaps.Clust;
import java.awt.Point;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/*
 * Keep track of some bits of world state in a thread safe manner.
 * Might turn this into an observable later.
 *
 * @author sean
 */
public class MiniWorldState {
    
    
    private double currx=0;
    private double curry=0;
    private double currz=0;
    synchronized public double getCurrx() { return currx; }
    synchronized public double getCurry() { return curry; }
    synchronized public double getCurrz() { return currz; }
    synchronized public void setLocation(double newx, double newy, double newz) {
	currx = newx;
	curry = newy;
	currz = newz;
    }

    private Path3D path = null;
    synchronized public Path3D getPath() { return path; }
    synchronized public void setPath(Path3D value) { path = value; }

    private ArrayList<Point> uavs = new ArrayList<Point>();
    synchronized public ArrayList<Point> getUavs() { return uavs; }
    synchronized public void setUavs(ArrayList<Point> value) { uavs = value; }
    
    private ArrayList<Point> emitters = new ArrayList<Point>();
    synchronized public ArrayList<Point> getEmitters() { return emitters; }
    synchronized public void setEmitters(ArrayList<Point> value) { emitters = value; }
    
    private ArrayList<Clust> clusters;
    synchronized public ArrayList<Clust> getClusters() { return clusters; }
    synchronized public void setClusters(ArrayList<Clust> value) { clusters = value; }

    /** Creates a new instance of MiniWorldState */
    public MiniWorldState() {
    }
}
