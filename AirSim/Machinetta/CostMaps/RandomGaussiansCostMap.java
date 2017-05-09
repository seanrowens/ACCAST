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
 * RandomGaussiansCostMap.java
 *
 * Created on July 13, 2006, 5:44 PM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.UAVRI;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class RandomGaussiansCostMap extends MixGaussiansCostMap implements CostMap, Dynamic {
    private final static int ENTIRE_MAP_GAUSSIAN_COUNT = 1;
    private int mapBotLeftX;
    private int mapBotLeftY;
    private int mapWidth;
    private int mapHeight;
    private boolean regenerateOnRebuild;
    private double amplitudeMultiplier = 500;
    private double divisorMultiplier = 100;
    private boolean smallMoves = false;
    private double smallMovesRange;
    private boolean clusters = false;
    private double clustersRange;
    private int clustersCount;

    private int mapTopRightX;
    private int mapTopRightY;

    public static Random rand = new Random();

    /**
     * Class constructor creates a new instance of
     * RandomGaussiansCostMap.  This cost map randomly places either a
     * single cost/reward gaussian, or a cluster of them, on the
     * costmap, based on parameters.  Gaussians are located randomly,
     * with an amplitude (how high the center is) randomly generated
     * between 0 and 1, multiplied by the amplitudeMultiplier, and a
     * divisor (how sharply it spreads out) randomly generated between
     * 0 and 1, multiplied by the divisorMultiplier.  Clusters are
     * several guassians located close by each other.  This costmap
     * implements the Dynamic interface, which means rebuild() is
     * called before planning.  This is to avoid unnecessary and
     * possibly costly rebuilding between plans.
     * 
     * @param	mapBotLeftX	The left (west) boundary of the map
     * @param	mapBotLeftY	The bottom (south) boundary of the map
     * @param	mapWidth	The width (east to west) of the map
     * @param	mapHeight	The height (north to south) of the map
     * @param	regenerateOnRebuild	if set, regenerate map on rebuild, otherwise do nothing on rebuild
     * @param	amplitudeMultiplier	scales the amplitude of the gaussians 
     * @param	divisorMultiplier	scales the divisor of the gaussians 
     * @param	smallMoves	when rebuilding, moves each gaussian randomly a 'small' distance.
     * @param	smallMovesRange	range of small random move for rebuilding
     * @param	clusters	place a cluster of random gaussians rather than a single gaussian.
     * @param	clustersRange	how far apart members of a cluster of gaussians may be.
     * @param	clustersCount	how many gaussians are in a cluster.
     */
    public RandomGaussiansCostMap(int mapBotLeftX, int mapBotLeftY, int mapWidth, int mapHeight, boolean regenerateOnRebuild, double amplitudeMultiplier, double divisorMultiplier, boolean smallMoves, double smallMovesRange, boolean clusters, double clustersRange, int clustersCount) {
	this.mapBotLeftX = mapBotLeftX;
	this.mapBotLeftY = mapBotLeftY;
	this.mapWidth = mapWidth;
	this.mapHeight = mapHeight;
	this.regenerateOnRebuild = regenerateOnRebuild;
	this.amplitudeMultiplier = amplitudeMultiplier;
	this.divisorMultiplier = divisorMultiplier;
	this.smallMoves = smallMoves;
	this.smallMovesRange = smallMovesRange;
	this.clusters = clusters;
	this.clustersRange = clustersRange;
	this.clustersCount = clustersCount;

	mapTopRightX = mapBotLeftX + mapWidth;
	mapTopRightY = mapBotLeftY + mapHeight;
	rebuild();
    }

    private void setAmplitudeAndDivisor(Gaussian g) {
	//	g.amplitude= (rand.nextDouble() - 0.5) * amplitudeMultiplier;
	g.amplitude= 0 - ((rand.nextDouble()+.1) * amplitudeMultiplier);
	g.divisor = (rand.nextInt(10) + 5) * divisorMultiplier;
    }

    private void keepInMap(Gaussian g) {
	if(g.x < mapBotLeftX) 
	    g.x = mapBotLeftX;
	if(g.x > mapTopRightX) 
	    g.x = mapTopRightX;
	if(g.y < mapBotLeftY) 
	    g.y = mapBotLeftY;
	if(g.y > mapTopRightY) 
	    g.y = mapTopRightY;
    }

    private void rebuildEntireMap() {
	gaussians.clear();
	for(int loopi = 0; loopi < ENTIRE_MAP_GAUSSIAN_COUNT; loopi++) {
	    Gaussian g = new Gaussian();
	    g.x = mapBotLeftX + rand.nextInt((int)mapWidth);
	    g.y = mapBotLeftY + rand.nextInt((int)mapHeight);
	    setAmplitudeAndDivisor(g);
	    Machinetta.Debugger.debug("Rebuilding random gaussian map, new gaussian at "+g.x+", "+g.y+" amplitude="+g.amplitude+" divisor="+g.divisor, 1, this);
	    addGaussian(g.x,g.y,g.amplitude,g.divisor);
	}
    }

    private void rebuildSmallMoves() {
	Gaussian g;
	if(gaussians.size() > 0) 
	    g = gaussians.get(0);
	else {
	    g = new Gaussian();
	    g.x = mapBotLeftX + mapWidth/2;
	    g.y = mapBotLeftY + mapHeight/2;
	}
	g.x = g.x + rand.nextInt((int)smallMovesRange) - (int)(smallMovesRange/2);
	g.y = g.y + rand.nextInt((int)smallMovesRange) - (int)(smallMovesRange/2);
	keepInMap(g);
	setAmplitudeAndDivisor(g);
	gaussians.clear();
	addGaussian(g.x,g.y,g.amplitude,g.divisor);
    }

    private void rebuildClusters() {
	gaussians.clear();
	Gaussian g1 = new Gaussian();
	g1.x = mapBotLeftX + rand.nextInt((int)mapWidth);
	g1.y = mapBotLeftY + rand.nextInt((int)mapHeight);
	setAmplitudeAndDivisor(g1);
	addGaussian(g1.x,g1.y,g1.amplitude,g1.divisor);

	for(int loopi = 1; loopi < clustersCount; loopi++) {
	    Gaussian g2 = new Gaussian();
	    g2.x = g1.x + rand.nextInt((int)clustersRange) - (int)(clustersRange/2);
	    g2.y = g1.y + rand.nextInt((int)clustersRange) - (int)(clustersRange/2);
	    keepInMap(g2);
	    setAmplitudeAndDivisor(g2);
	    addGaussian(g2.x,g2.y,g2.amplitude,g2.divisor);
	}
    }

    public void rebuild() {
	if(!regenerateOnRebuild)
	    return;

	if(!smallMoves && !clusters)
	    rebuildEntireMap();
	else if(smallMoves)
	    rebuildSmallMoves();
	else
	    rebuildClusters();
    }

}
