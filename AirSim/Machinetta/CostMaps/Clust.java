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
package AirSim.Machinetta.CostMaps;

import Machinetta.ConfigReader;
import Gui.StringUtils;
import Gui.DoubleGrid;

import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.*;
import java.awt.Point;

public class Clust {
    private final static DecimalFormat fmt = new DecimalFormat("0.0000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.0");
    public int channel=-1;
    public double x;
    public double y;
    public double meanx;
    public double meany;
    public String getKey() { return "Channel"+channel+":"+fmt2.format(x)+","+fmt2.format(y)+":"+fmt2.format(memberDistMean); }
    public double[][] members;

    public double memberDistMax = 0.0;
    public double memberDistMean = 0.0;
    public double memberDistVar = 0.0;
    public double memberDistStdDev = 0.0;

    public double maxMaxDist=0.0;
    public double maxMeanDist = 0.0;
    
    public boolean failed = false;

    public boolean clustersTooNear = false;
    public boolean membersTooFar = false;
    public boolean membersTooMany = false;
    public boolean membersTooFew = false;
    public double entropyPercent = 0.0;
    public boolean entropyNear = false;
    public double beliefPercent = 0.0;

    Ellipse2D.Double ellipse;

    public Clust(int channel, double x, double y, double[][] members) {
	this.channel = channel;
	this.x = x;
	this.y = y;
	this.members = members;
	calcMemberDistStats();
    }

    public double distSqd(Clust other) { 
	double xdiff = x - other.x;
	double ydiff = y - other.y;
	return (xdiff * xdiff)+ (ydiff*ydiff);
    }

    private void calcMemberDistStats() {
	double xdiff;
	double ydiff;
	double totaldist = 0.0;
	double distances[] = new double[members.length];

	double totalx = 0.0;
	double totaly = 0.0;
	for(int loopi = 0; loopi < members.length; loopi++) {
	    totalx += members[loopi][0];
	    totaly += members[loopi][1];
	}
	meanx = totalx/(double)members.length;
	meany = totaly/(double)members.length;

	for(int loopi = 0; loopi < members.length; loopi++) {
	    xdiff = members[loopi][0] - x;
	    ydiff = members[loopi][1] - y;
	    distances[loopi] = Math.sqrt((xdiff * xdiff)+ (ydiff*ydiff));
	    totaldist += distances[loopi];
	    if(distances[loopi] > memberDistMax)
		memberDistMax = distances[loopi];
	}

	memberDistMean = totaldist / distances.length;
	
	double diff;
	for(int loopi = 0; loopi < members.length; loopi++) {
	    diff = distances[loopi] - memberDistMean;
	    memberDistVar += diff * diff;
	}
	memberDistVar /= members.length;
	memberDistStdDev = Math.sqrt(totaldist);
    }

    // check the distance of each point in the polygon from each other
    // point.  If ANY of the distances are greater than the limit
    // (passed in as arg) then return true.  This also sets the member
    // variable membersTooFar.
    public boolean checkTooFar(double membersTooFarLimitSqdMeters) {
	double xdiff;
	double ydiff;
	membersTooFar = false;
	for(int loopi = 0; loopi < (members.length-1); loopi++) {
	    for(int loopj = loopi+1; loopj < members.length; loopj++) {
		xdiff = members[loopi][0] - members[loopj][0];
		ydiff = members[loopi][1] - members[loopj][1];
		double distSqd = (xdiff * xdiff)+ (ydiff*ydiff);
		if(distSqd > membersTooFarLimitSqdMeters) {
		    membersTooFar = true;
		    return membersTooFar;
		}
	    }
	}
	return membersTooFar;
    }

    public boolean checkTooFarOld(double membersTooFarLimitSqdMeters) {
	double xdiff;
	double ydiff;
	membersTooFar = false;
	for(int loopi = 0; loopi < (members.length-1); loopi++) {
	    for(int loopj = loopi+1; loopj < members.length; loopj++) {
		xdiff = members[loopi][0] - members[loopj][0];
		ydiff = members[loopi][1] - members[loopj][1];
		double distSqd = (xdiff * xdiff)+ (ydiff*ydiff);
		if(distSqd > membersTooFarLimitSqdMeters) {
		    membersTooFar = true;
		    return membersTooFar;
		}
	    }
	}
	return membersTooFar;
    }

    public boolean entropyNear(DoubleGrid entropyGrid, double entropyThreshold) {
	entropyPercent = 0.0;
	entropyNear = false;
	HashMap<String, Point> memberMap= new HashMap<String,Point>();
	for(int loopi = 0; loopi < members.length; loopi++) {
	    int x = entropyGrid.toGridX(members[loopi][0]);
	    int y = entropyGrid.toGridY(members[loopi][1]);
	    memberMap.put(x+"."+y,new Point(x,y));
	}
	HashMap<String, Point> pointMap= new HashMap<String,Point>();
	String key;
	for(int loopi = 0; loopi < members.length; loopi++) {
	    int x = entropyGrid.toGridX(members[loopi][0]);
	    int y = entropyGrid.toGridY(members[loopi][1]);
	    if(entropyGrid.insideGrid(x-1,y-1)) {
		key = (x-1)+"."+(y-1);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x-1,y-1));
	    }
	    if(entropyGrid.insideGrid(x,y-1)) {
		key = (x)+"."+(y-1);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x,y-1));
	    }
	    if(entropyGrid.insideGrid(x+1,y-1)) {
		key = (x+1)+"."+(y-1);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x+1,y-1));
	    }
	    if(entropyGrid.insideGrid(x+1,y)) {
		key = (x+1)+"."+(y);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x+1,y));
	    }
	    if(entropyGrid.insideGrid(x+1,y)) {
		key = (x+1)+"."+(y);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x+1,y));
	    }
	    if(entropyGrid.insideGrid(x-1,y+1)) {
		key = (x-1)+"."+(y+1);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x-1,y+1));
	    }
	    if(entropyGrid.insideGrid(x,y+1)) {
		key = (x)+"."+(y+1);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x,y+1));
	    }
	    if(entropyGrid.insideGrid(x+1,y+1)) {
		key = (x+1)+"."+(y+1);
		if(memberMap.get(key) == null)
		    pointMap.put(key,new Point(x+1,y+1));
	    }
	}
	Point[] pointAry = pointMap.values().toArray(new Point[1]);
	for(int loopi = 0; loopi < pointAry.length; loopi++) {
	    double ent = entropyGrid.getValue(pointAry[loopi].x,pointAry[loopi].y);
	    entropyPercent += ent;
// 	    if((ent != 0.0)&&(ent != 1.0))
// 		Machinetta.Debugger.debug("cluster "+fmt.format(x)+","+fmt.format(y)+" adding entropy at "+pointAry[loopi].x+","+pointAry[loopi].y+" = "+fmt.format(ent)+", total="+entropyPercent,1,this);
	}
	entropyPercent = entropyPercent/((double)pointAry.length);
	if(entropyPercent > entropyThreshold)
	    entropyNear = true;
	
	return entropyNear;
    }

    public boolean entropyNear2(DoubleGrid entropyGrid, double entropyThreshold, double entropyBoxWidthMeters) {
	entropyPercent = 0.0;
	entropyNear = false;
	
	int gridx = entropyGrid.toGridX(x);
	int gridy = entropyGrid.toGridY(y);
	int width = (int)(entropyBoxWidthMeters/(double)entropyGrid.getGridCellSize());
	
	int startx = gridx - (width/2);
	int endx = gridx + (width/2);
	int starty = gridy - (width/2);
	int endy = gridy + (width/2);
	if(startx < 0) startx = 0;
	if(endx >= entropyGrid.getWidth()) endx = entropyGrid.getWidth() - 1;
	if(starty < 0) starty = 0;
	if(endy >= entropyGrid.getHeight()) endy = entropyGrid.getHeight() - 1;
	
	int counter=0;
	double entropySum = 0;
	for(int loopx = startx; loopx <= endx; loopx++) {
	    for(int loopy = starty; loopy <= endy; loopy++) {
		counter++;
		entropySum += entropyGrid.getValue(loopx, loopy);
	    }
	}
	entropyPercent = entropySum/(double)counter;
	Machinetta.Debugger.debug("entropyNear2: entropySum="+entropySum+" for "+counter+" cells, percent="+entropyPercent + " at grid "+gridx+","+gridy+" from x "+startx+" to "+endx+", y "+starty+" to "+endy,1,this);
	if(entropyPercent > entropyThreshold)
	    entropyNear = true;
	
	return entropyNear;
    }

    // Check for high entropy grid cells near this cluster.  Set
    // member variables entropyNear and entropyPercent and return
    // entropyNear.
    public boolean entropyNear3(DoubleGrid entropyGrid, double entropyThreshold, double entropyBoxWidthMeters) {
	entropyPercent = 0.0;
	entropyNear = false;
	
// 	int gridx = entropyGrid.toGridX(x);
// 	int gridy = entropyGrid.toGridY(y);
	double scale=entropyGrid.getGridCellSize();
	int width = (int)(entropyBoxWidthMeters/scale);
	int gridx = (int)(x/scale);
	int gridy = (int)(y/scale);
	
	int startx = gridx - (width/2);
	int endx = gridx + (width/2);
	int starty = gridy - (width/2);
	int endy = gridy + (width/2);
	if(startx < 0) startx = 0;
	if(endx >= entropyGrid.getWidth()) endx = entropyGrid.getWidth() - 1;
	if(starty < 0) starty = 0;
	if(endy >= entropyGrid.getHeight()) endy = entropyGrid.getHeight() - 1;
	
	double counter=0;
	double entropyZeroCount = 0;
	for(int loopx = startx; loopx <= endx; loopx++) {
	    for(int loopy = starty; loopy <= endy; loopy++) {
		counter++;
		if(0 == entropyGrid.getValue(loopx, loopy))
		    entropyZeroCount++;
	    }
	}
	entropyPercent = entropyZeroCount/counter;
	Machinetta.Debugger.debug("entropyNear2: entropyZeroCount="+entropyZeroCount+" for "+counter+" cells, percent="+entropyPercent + " at grid "+gridx+","+gridy+" from x "+startx+" to "+endx+", y "+starty+" to "+endy,1,this);
	if(entropyPercent < entropyThreshold)
	    entropyNear = true;
	
	return entropyNear;
    }

    // calculate average 'belief' that an emitter is present, for an
    // circle of radius 'range' centered on the x,y of this cluster.
    public void beliefNear(double[][] beliefs, double scale, double beliefThreshold, double range) {
	int gridx = (int)(x/scale);
	int gridy = (int)(y/scale);
	int irange = (int)(range/scale);
	int irangeSqd = (int)((range/scale)*(range/scale));

	int startx = gridx - (irange/2);
	int endx = gridx + (irange/2);
	int starty = gridy - (irange/2);
	int endy = gridy + (irange/2);
	if(startx < 0) startx = 0;
	if(endx >= beliefs.length) endx = beliefs.length - 1;
	if(starty < 0) starty = 0;
	if(endy >= beliefs.length) endy = beliefs.length - 1;
	
	int counter=0;
	double beliefSum = 0;
	for(int loopx = startx; loopx <= endx; loopx++) {
	    for(int loopy = starty; loopy <= endy; loopy++) {
		int diffx = gridx-loopx;
		int diffy = gridy-loopy;
		int diffSqd = (diffx*diffx) + (diffy*diffy);
		if(diffSqd < irangeSqd) {
		    counter++;
		    beliefSum += beliefs[loopx][loopy];
		}
	    }
	}
	beliefPercent = beliefSum/(double)counter;

	Machinetta.Debugger.debug("    beliefNear: beliefSum="+beliefSum+" for "+counter+" cells, beliefPercent="+beliefPercent,1,this);
    }

    public String toString() {
	return "center="+fmt.format(x)+","+fmt.format(y)
	    //	    +" mean="+fmt.format(meanx)+","+fmt.format(meany)
	    +" beliefPercent="+fmt.format(beliefPercent)
	    +" nmembers="+members.length
	    +" maxDist="+fmt.format(memberDistMax)
	    +" meanDist="+fmt.format(memberDistMean)
	    +" stddev="+fmt.format(memberDistStdDev);
    }
}
