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
import AirSim.Machinetta.UAVRI;
import Util.KMeans.*;

import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.*;
import java.awt.Point;

public class ClusterUtils {
    private final static DecimalFormat fmt = new DecimalFormat("0.0");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.000000000000");
    private final static double HIGH_BELIEF_THRESHOLD = 1.0;
    private final static int KMEANS_MIN_NUM_CLUSTERS = 1;
    private final static int KMEANS_MAX_NUM_CLUSTERS = 5;

    double mapScale;
    double maxClusterSizeMeters;
    int gridSize;

    int membersTooManyLimit = 500;
    int membersTooFewLimit = 16;
    double membersTooFarLimitMeters = 100;
    double membersTooFarLimitSqdMeters = membersTooFarLimitMeters*membersTooFarLimitMeters;
    double clustersTooNearLimitMeters = 80;
    double clustersTooNearLimitSqdMeters = clustersTooNearLimitMeters*clustersTooNearLimitMeters;
    double entropyNearThreshold = .90;
    double entropyNearBoxWidthMeters = 80.0;
    double beliefNearThreshold = HIGH_BELIEF_THRESHOLD;

    private ArrayList<Point> emitters=new ArrayList<Point>();

    double maxMaxDist=0.0;
    double maxMeanDist = 0.0;

    private ArrayList<ArrayList<Clust>> listOfClusterLists;
    private ArrayList<Clust> bestClusterList;
    //    public ArrayList<Clust> getBestClusterList() { return bestClusterList; }

    double bestMaxMeanDist = Double.MAX_VALUE;

    
    public ClusterUtils(double mapScale, double maxClusterSizeMeters, int gridSize) {
	this.mapScale = mapScale;
	this.maxClusterSizeMeters = maxClusterSizeMeters;
	this.gridSize = gridSize;
	
	// @TODO: the default values for these variables are really
	// dependent on sensor range and emitter model.  So they
	// should somehow be calculated from those values.
	membersTooFarLimitMeters = membersTooFarLimitMeters*mapScale;
 	membersTooFarLimitSqdMeters = membersTooFarLimitMeters*membersTooFarLimitMeters;
	clustersTooNearLimitMeters = clustersTooNearLimitMeters*mapScale;
	clustersTooNearLimitSqdMeters = clustersTooNearLimitMeters*clustersTooNearLimitMeters;
	entropyNearBoxWidthMeters = entropyNearBoxWidthMeters*mapScale;

	Machinetta.Debugger.debug("Clustering parameters set as follows;"
				  +" mapScale="+fmt.format(mapScale)
				  +" membersTooFarLimitMeters ="+fmt.format(membersTooFarLimitMeters)
				  +" membersTooFarLimitSqdMeters ="+fmt.format(membersTooFarLimitSqdMeters)
				  +" clustersTooNearLimitMeters ="+fmt.format(clustersTooNearLimitMeters)
				  +" clustersTooNearLimitSqdMeters ="+fmt.format(clustersTooNearLimitSqdMeters)
				  +" entropyNearBoxWidthMeters ="+fmt.format(entropyNearBoxWidthMeters)
				  ,1,this);

	// @TODO: Delete emitter stuff when things are working again -
	// I don't think it's used anywhere but why borrow trouble
	// till then?
	int emitterCount = ConfigReader.getConfigInt("EMITTER_COUNT", 0, false);
	for(int loopi = 0; loopi < emitterCount; loopi++) {
	    String fieldName = "EMITTER_"+loopi;
	    String emitterCoords = ConfigReader.getConfigString(fieldName, null, false);
	    if(null == emitterCoords)
		continue;
	    if(emitterCoords.equalsIgnoreCase("RANDOM"))
		continue;
	    double[] coords = StringUtils.parseDoubleList(2,emitterCoords);
	    if(null == coords)
		continue;
	    if(coords.length < 2)
		continue;
	    emitters.add(new Point((int)coords[0],(int)coords[1]));
	}
        Machinetta.Debugger.debug("Found "+emitters.size()+" emitters in .cfg file.",1,this);
    }

    public int countHighPixels(double[][] beliefs) {
	int highCount = 0;

// 	for(int loopx = 0; loopx < (beliefs.length-1); loopx++) {
// 	    for(int loopy = 0; loopy < (beliefs[0].length-1); loopy++) {
	for(int loopx = 0; loopx < (beliefs.length); loopx++) {
	    for(int loopy = 0; loopy < (beliefs[0].length); loopy++) {
		if(beliefs[loopx][loopy] >= HIGH_BELIEF_THRESHOLD) 
		    highCount++;
	    }
	}

	return highCount;
    }

    private double[][] getHighPixels(double[][] beliefs) {
	int highCount = countHighPixels(beliefs);

	if(0 == highCount)
	    return null;

	double[][] pixels = new double[highCount][2];
	
	int pixelCount = 0;
	for(int loopx = 0; loopx < gridSize; loopx++) {
	    for(int loopy = 0; loopy < gridSize; loopy++) {
		if(beliefs[loopx][loopy] >= HIGH_BELIEF_THRESHOLD) {
		    pixels[pixelCount][0] = loopx*mapScale;
		    pixels[pixelCount][1] = loopy*mapScale;
		    pixelCount++;
		}
	    }
	}
	return pixels;
    }
    
    private Clust analyzeCluster(double[][] pixels, Cluster c, double maxMeanDist, DoubleGrid entropyGrid, double[][] beliefs) {

	double[] center = c.getCenter();
	int[] memberIndexes = c.getMemberIndexes();
	double[][] members = new double[memberIndexes.length][2];

	double[] member;
	for(int loopi = 0; loopi < members.length; loopi++) {
	    member = pixels[memberIndexes[loopi]];
	    members[loopi][0] = member[0];
	    members[loopi][1] = member[1];
	}
	Clust clust = new Clust(-1,center[0], center[1], members);

	Machinetta.Debugger.debug("    "+clust.toString(),1,this);

	if(clust.memberDistMax > maxMaxDist) 
	    maxMaxDist = clust.memberDistMax;

// 	clusterCircles.add(new Ellipse2D.Double(center[0], center[1], meanDist, maxDist));

	if(clust.members.length > membersTooManyLimit) {
	    clust.membersTooMany = true;
	}
	if(clust.members.length <= membersTooFewLimit) {
	    clust.membersTooFew = true;
	}
	clust.checkTooFar(membersTooFarLimitSqdMeters);
	clust.entropyNear3(entropyGrid,entropyNearThreshold,entropyNearBoxWidthMeters);
	double area = (clust.members.length * mapScale*mapScale);
	double radius = Math.sqrt(area/Math.PI);
	clust.beliefNear(beliefs,mapScale, HIGH_BELIEF_THRESHOLD, radius);
	return clust;
    }

    private boolean checkClustersTooNear(Cluster[] clusters) {
	for(int loopi = 0; loopi < (clusters.length-1); loopi++) {
	    for(int loopj = loopi+1; loopj < clusters.length; loopj++) {
		double[] center1 = clusters[loopi].getCenter();
		double[] center2 = clusters[loopj].getCenter();
		double xdiff = center1[0] - center2[0];
		double ydiff = center1[1] - center2[1];
		double distSqd = (xdiff * xdiff)+ (ydiff*ydiff);
		if(distSqd < clustersTooNearLimitSqdMeters) {
		    return true;
		}
	    }
	}
	return false;
    }

    public void checkKmeansClusters(DoubleGrid entropyGrid, double[][] beliefs) {
	long startTime;
	long endTime;

	double[][] pixels = getHighPixels(beliefs);
	if(null == pixels) {
	    Machinetta.Debugger.debug("high pixel count is zero, not doing clustering.",1,this);
	    return;
	}

	listOfClusterLists = new ArrayList<ArrayList<Clust>>();

	double bestClusteringProb = 0.0;
	bestClusterList = null;
	bestMaxMeanDist = Double.MAX_VALUE;
	ArrayList<Clust> clusterList;
	for(int loopk = KMEANS_MIN_NUM_CLUSTERS; loopk <= KMEANS_MAX_NUM_CLUSTERS; loopk++) {
	    double goodClusteringProb = 1.0;
	    clusterList = new ArrayList<Clust>();

	    boolean kFailed = false;

	    maxMaxDist = 0.0;
	    maxMeanDist = 0.0;

	    startTime = System.currentTimeMillis();
	    BasicKMeans bkm = new BasicKMeans(pixels, loopk, 1000, System.currentTimeMillis());
	    bkm.run();
	    Cluster[] clusters = bkm.getClusters();
	    endTime = System.currentTimeMillis();

	    Machinetta.Debugger.debug("For "+loopk+" clusters, took "+(endTime - startTime)+" ms, analysis",1,this);
	    double totalBeliefNear = 0.0;
	    for(int loopi = 0; loopi < clusters.length; loopi++) {
		Clust cluster = analyzeCluster(pixels, clusters[loopi], 500, entropyGrid, beliefs);
		clusterList.add(cluster);
		Machinetta.Debugger.debug("      K="+loopk+" cluster "+loopi+" at "+fmt.format(cluster.x)+","+fmt.format(cluster.y)+" tooFar="+cluster.membersTooFar+", entropyNear= "+cluster.entropyNear+"("+fmt2.format(cluster.entropyPercent)+") tooMany="+cluster.membersTooMany+" tooFew="+cluster.membersTooFew,1,this);
 		if(cluster.memberDistMean > maxMeanDist)
 		    maxMeanDist = cluster.memberDistMean;
		totalBeliefNear += cluster.beliefPercent;
// 		if(cluster.membersTooFar) {
// 		    Machinetta.Debugger.debug("  Clusters K= "+loopk+" fails, because members of cluster "+loopi+" are too far away from each other",1,this);
// 		    kFailed = true;
// 		}
	    }
	    double avgBeliefNear = totalBeliefNear/(double)clusters.length;
	    goodClusteringProb *= avgBeliefNear;

	    Machinetta.Debugger.debug("    Clusters K= "+loopk+" avgBeliefNear = "+fmt2.format(avgBeliefNear),1,this);

	    Machinetta.Debugger.debug("    cluster list size "+clusterList.size()+" maxMeanDist "+maxMeanDist,1,this);

	    if(checkClustersTooNear(clusters)) {
		Machinetta.Debugger.debug("    Clusters K= "+loopk+" fails, because clustersTooNear each other",1,this);
		kFailed = true;
		goodClusteringProb *= .1;
	    }

	    Machinetta.Debugger.debug("    Clusters K= "+loopk+" goodClusteringProb = "+fmt2.format(goodClusteringProb),1,this);

	    if(kFailed) 
		Machinetta.Debugger.debug("  Clusters K= "+loopk+" FAILED!",1,this);
	    else
		Machinetta.Debugger.debug("  Clusters K= "+loopk+" SUCCEEDS!",1,this);

	    if(goodClusteringProb > bestClusteringProb) {
		bestClusterList = clusterList;
		bestClusteringProb = goodClusteringProb;
	    }
	}
	if(null == bestClusterList) {
	    Machinetta.Debugger.debug("NULL Best cluster list.",1,this);
	}
	else {
	    Machinetta.Debugger.debug("Best cluster list size "+bestClusterList.size()+" maxMeanDist "+bestMaxMeanDist,1,this);

	    for(int loopi = 0; loopi < bestClusterList.size(); loopi++) {
		Clust c = bestClusterList.get(loopi);
		if(c.membersTooMany)
		    c.failed = true;
		if(c.entropyNear)
		    c.failed = true;
		if(c.membersTooFew)
		    c.failed = true;
 		if(c.clustersTooNear)
 		    c.failed = true;
	    }


	    listOfClusterLists = new ArrayList<ArrayList<Clust>>();
	    listOfClusterLists.add(bestClusterList);
	}
    }

    // Find all contiguous groups of 'high belief' grid cells
    private ArrayList<ArrayList<Point>> clusterAdjacent(double [][] beliefs) {
	double[][] beliefs2 = new double[gridSize][gridSize];
	
	for(int loopx = 0; loopx < gridSize; loopx++) {
	    for(int loopy = 0; loopy < gridSize; loopy++) {
		if(beliefs[loopx][loopy] >= HIGH_BELIEF_THRESHOLD)
		    beliefs2[loopx][loopy] = 1;
		else
		    beliefs2[loopx][loopy] = 0;
	    }
	}
	
	ArrayList<Point> points;
	ArrayList<ArrayList<Point>> pointClusters = new ArrayList<ArrayList<Point>>();
	
	int width = beliefs2.length;
	int height = beliefs2[0].length;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		// If we find an obstacle point in the grid
		if(0 != beliefs2[loopX][loopY]) {
		    points = new ArrayList<Point>();
		    points.add(new Point(loopX, loopY));
		    beliefs2[loopX][loopY] = 0;
		    int loopi = 0;
		    while(loopi < points.size()) {
			Point curp = (Point)points.get(loopi);
			int curx = curp.x;
			int cury = curp.y;

			if(((curx-1) >= 0) && ((cury-1) >= 0) && (beliefs2[curx-1][cury-1] != 0)) {
			    points.add(new Point(curx-1, cury-1));
			    beliefs2[curx-1][cury-1] = 0;
			}
			if(((cury-1) >= 0) && (beliefs2[curx][cury-1] != 0)) {
			    points.add(new Point(curx, cury-1));
			    beliefs2[curx][cury-1] = 0;
			}
			if(((curx+1) < width) && ((cury-1) >= 0) && (beliefs2[curx+1][cury-1] != 0)) {
			    points.add(new Point(curx+1, cury-1));
			    beliefs2[curx+1][cury-1] = 0;
			}
			if(((curx-1) >= 0) && (beliefs2[curx-1][cury] != 0)) {
			    points.add(new Point(curx-1, cury));
			    beliefs2[curx-1][cury] = 0;
			}
			if(((curx+1) < width) && (beliefs2[curx+1][cury] != 0)) {
			    points.add(new Point(curx+1, cury));
			    beliefs2[curx+1][cury] = 0;
			}
			if(((curx-1) >= 0) && ((cury+1) < height) && (beliefs2[curx-1][cury+1] != 0)) {
			    points.add(new Point(curx-1, cury+1));
			    beliefs2[curx-1][cury+1] = 0;
			}
			if(((cury+1) < height) && (beliefs2[curx][cury+1] != 0)) {
			    points.add(new Point(curx, cury+1));
			    beliefs2[curx][cury+1] = 0;
			}
			if(((curx+1) < width) && ((cury+1) < height) && (beliefs2[curx+1][cury+1] != 0)) {
			    points.add(new Point(curx+1, cury+1));
			    beliefs2[curx+1][cury+1] = 0;
			}
			loopi++;
		    }
		    pointClusters.add(points);
		}
	    }
	}	
	return pointClusters;
    }
    
    // @TODO: these aren't really polygons, should call this something else.
    private ArrayList<Clust> convertPolysToClusters(int channel, DoubleGrid entropyGrid, double[][] beliefs, ArrayList<ArrayList<Point>> polygons) {
	ArrayList<Clust> clusterList = new ArrayList<Clust>();
	for(int loopi = 0; loopi < polygons.size(); loopi++) {
	    ArrayList<Point> polygon = polygons.get(loopi);

	    // Create an array of points scaled to the map size
	    double members[][] = new double[polygon.size()][2];
	    for(int loopj = 0; loopj < polygon.size(); loopj++) {
 		members[loopj][0] = polygon.get(loopj).x * mapScale;
 		members[loopj][1] = polygon.get(loopj).y * mapScale;
// 		members[loopj][0] = entropyGrid.toLocalX(polygon.get(loopj).x);
// 		members[loopj][1] = entropyGrid.toLocalY(polygon.get(loopj).y);
	    }

	    // calculate average of all of the points, i.e. the center
	    // of the poly
	    double x = 0.0;
	    double y = 0.0;
	    for(int loopj = 0; loopj < members.length; loopj++) {
		x += members[loopj][0];
		y += members[loopj][1];
	    }
	    x = x/(double)members.length;
	    y = y/(double)members.length;

	    Clust cluster = new Clust(channel, x, y, members);

	    // Check if for members of the polygon being too far from
	    // other members.  (Really what we should probably check
	    // is average distance from the 'center' and perhaps
	    // stddev there of - i.e. how much is the cluster NOT like
	    // a circle.  But this is working for now, so why break
	    // it?)
	    cluster.checkTooFar(membersTooFarLimitSqdMeters);
	    
	    // Check for bits of entropy near the cluster, i.e. is
	    // this cluster surrounded by low entropy and hence pretty
	    // much 'done' or is there still entropy adjacent to the
	    // cluster that implies more scanning/filtering needs to
	    // be done?  (We noticed that the spurious clusters often
	    // had high entropy grid cells nearby or adjacent.)
	    cluster.entropyNear3(entropyGrid,entropyNearThreshold,entropyNearBoxWidthMeters);
	    
	    // Calculate overall area covered by the cluster (i.e. the
	    // area of a grid cell times the number of points)
	    double area = (cluster.members.length * mapScale*mapScale);

	    // calculate overall radius of the area... i.e. how big a
	    // circle would this fill if it WERE a circle.
	    double radius = Math.sqrt(area/Math.PI);
	    
	    // calculate average belief values for circle of specified
	    // radius centered on the cluster.
	    cluster.beliefNear(beliefs,mapScale, HIGH_BELIEF_THRESHOLD, radius);

	    // Check for too many clusters
	    if(cluster.members.length > membersTooManyLimit) {
		cluster.membersTooMany = true;
	    }

	    // Check for too few clusters.
	    if(cluster.members.length <= membersTooFewLimit) {
		cluster.membersTooFew = true;
	    }

	    clusterList.add(cluster);
	}
	return clusterList;
    }

    // Check for clusters that are too close to each other.
    public void checkInterClusterDistance(ArrayList<Clust> clusterList) {
	for(int loopi = 0; loopi < (clusterList.size()-1); loopi++) {
	    Clust c1 = clusterList.get(loopi);
	    for(int loopj = loopi+1; loopj < clusterList.size(); loopj++) {
		Clust c2 = clusterList.get(loopj);
		if(c1.membersTooFew || c2.membersTooFew) {
		    // Make sure really tiny clusters are just ignored,
		    // and don't trip the 'tooNear' flag on other
		    // clusters.
		}
		else if(c1.entropyNear || c2.entropyNear) {
		    // likewise ignore those clusters that have
		    // entropy near them.
		}
		else {
		    if(c1.distSqd(c2) < clustersTooNearLimitSqdMeters) {
			c1.clustersTooNear = true;
			c2.clustersTooNear = true;
		    }
		}
	    }
	}
    }

    // Check if each cluster has failed any of a variety of conditions
    // and hence should be eliminated from consideration.
    public void checkForClusterFailure(ArrayList<Clust> clusterList) {
	for(int loopi = 0; loopi < clusterList.size(); loopi++) {
	    Clust c1 = clusterList.get(loopi);
	    String result="";

	    if(c1.membersTooMany)
		c1.failed = true;
	    if(c1.entropyNear)
		c1.failed = true;
	    if(c1.membersTooFew)
		c1.failed = true;
	    if(c1.clustersTooNear)
		c1.failed = true;

	    if(!c1.failed) {
		result="GOOD";
	    }
	    else {
		result="BAD ";
	    }

	    Machinetta.Debugger.debug(result+" Cluster "+c1.getKey()
				      +" tooNear="+c1.clustersTooNear
				      +" tooFar="+c1.membersTooFar
				      +" entropyNear="+c1.entropyNear+" ("+fmt2.format(c1.entropyPercent)+")"
				      +" tooMany="+c1.membersTooMany
				      +" tooFew="+c1.membersTooFew
				      +" nMembers="+c1.members.length
				      ,1,this);
	}
    }

    public ArrayList<Clust> checkClusters(int channel, DoubleGrid entropyGrid, double[][] beliefs) {
	ArrayList<ArrayList<Point>> polygons = clusterAdjacent(beliefs);
	ArrayList<Clust> clusterList = convertPolysToClusters(channel,entropyGrid, beliefs, polygons);
	checkInterClusterDistance(clusterList);
	Machinetta.Debugger.debug("Found "+clusterList.size()+" clusters.",1,this);
	checkForClusterFailure(clusterList);

	
	// Originally we had returned a list of lists of clusters,
	// because I was trying to use Kmeans which required youy
	// specify how many clusters you expected, so I was trying
	// different numbers and hoping to pick out the 'best' set of
	// clusters.  I'm changing this now to just return a list of
	// clusters but the kmeans code is still around.

//  	if(clusterList.size() > 0)
//  	    listOfClusterLists.add(clusterList);
// 	bestClusterList = clusterList;
	return clusterList;
    }
}
