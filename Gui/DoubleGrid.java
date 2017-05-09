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
package Gui;

// @author      Sean R. Owens
// @version     $Id: DoubleGrid.java,v 1.7 2006/12/19 00:46:34 pscerri Exp $ 

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;

// This is the basic "grid of doubles" class.  We're going to use this
// (or a grid of floats, or of ints, or some kinda fixed point floats)
// to implement all of our terrain grids.
public class DoubleGrid extends Grid {
    public double getCellSizeMeters() { return gridCellSize;}
    public double getGridCellSizeMeters() { return gridCellSize;}

    protected double values[][] = null;
    private double highest= -1000000000000.0;// Double.MIN_VALUE;
    public double getHighest() { return highest;}
    private double lowest = 1000000000000.0; // Double.MAX_VALUE;
    public double getLowest() { return lowest;}

    public void computeHighestLowest() {
	debug.debug("Recomputing highest and lowest.");
	highest= -1000000000.0;
	lowest = 1000000000.0;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(values[loopY][loopX] > highest)
		    highest = values[loopY][loopX];
		if(values[loopY][loopX] < lowest)
		    lowest = values[loopY][loopX];
	    }
	}
    }

    public ArrayList findHighestPoints(int numPoints) {
	ArrayList found = new ArrayList();
	Point least = null;
	least = new Point(0, 0);
	found.add(least);

	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		System.err.println("HighestPoints:"+values[loopY][loopX]);
		if(values[loopY][loopX] > values[least.y][least.x]) {
		    if(found.size() < numPoints) {
			found.add(new Point(loopX, loopY));
		    }
		    else {
			least.y = loopY;
			least.x = loopX;
		    }
		    for(int loopi = 0; loopi < found.size(); loopi++) {
			Point point = (Point)found.get(loopi);
			if(values[point.y][point.x] < values[least.y][least.x])
			    least = point;
		    }
		}
	    }
	}
	for(int loopi = 0; loopi < found.size(); loopi++) {
	    Point point = (Point)found.get(loopi);
	    System.err.println("HighestPointsFound:"+values[point.y][point.x]);
	}
	return found;
    }

    public boolean equals(Object otherGrid) {
	DoubleGrid grid = (DoubleGrid)otherGrid;
	if(grid == this)
	    return true;
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(values[loopY][loopX] != grid.values[loopY][loopX])
		    return false;
	    }
	}
	return true;
    }

    public double getValue(int x, int y) {
	if((x < 0) || (y < 0) || (x >= width) || (y >= height)) 
	    return IMPASSABLE;
	return values[y][x];
    }

    public synchronized void setValue(int x, int y, int value) {
	if(insideGrid(x,y)) {
	    values[y][x] = value;
	    if(values[y][x] > highest)
		highest = values[y][x];
	    if(values[y][x] < lowest)
		lowest = values[y][x];
	    changed();
	}
    }

    public synchronized void setValue(int x, int y, double value) {
	if(insideGrid(x,y)) {
	    values[y][x] = value;
	    if(values[y][x] > highest)
		highest = values[y][x];
	    if(values[y][x] < lowest)
		lowest = values[y][x];
	    changed();
	}
    }

    public void fastSetValue(int x, int y, int value) {
	if(insideGrid(x,y)) {
	    values[y][x] = value;
	    if(values[y][x] > highest)
		highest = values[y][x];
	    if(values[y][x] < lowest)
		lowest = values[y][x];
	}
    }

    public void fastSetValue(int x, int y, double value) {
	if(insideGrid(x,y)) {
            values[y][x] = value;
	    if(values[y][x] > highest)
		highest = values[y][x];
	    if(values[y][x] < lowest)
		lowest = values[y][x];
	}
    }

    public void setValues(int gridX, int gridY, double setValues[][]) {
	int newValWidth = setValues.length;
	int newValHeight = setValues[0].length;
	int gridleftx = gridX;
	int gridrightx = gridX + newValWidth;
	int gridboty = gridY;
	int gridtopy = gridY + newValHeight;

	if(gridleftx < 0) {
	    gridleftx = 0;
	}
	if(gridrightx >= width) {
	    gridrightx = width-1;
	}
	if(gridboty < 0) {
	    gridboty = 0;
	}
	if(gridtopy >= height) {
	    gridtopy = height-1;
	}

	int loopY = 0;
	int loopX = 0;
	int indY = 0;
	int indX = 0;
	for(loopY = gridboty, indY = 0; loopY <= gridtopy; loopY++, indY++) {
	    for( loopX = gridleftx, indX = 0; loopX <= gridrightx; loopX++, indX++) {
		values[loopY][loopX] = setValues[indX][newValHeight - 1 - indY];
	    }
	}
    }

    public void setFootPrint(int gridX, int gridY, double setValues[][]) {
	int gridleftx = gridX - setValues.length/2;
	int gridrightx = gridX + setValues.length/2 - 1;
	int gridboty = gridY - setValues[0].length/2;
	int gridtopy = gridY + setValues[0].length/2 - 1;
	// TODO: SRO Wed Jul 19 17:29:57 EDT 2006 - when we're too
	// near the edge here, we mess up the foot print - when we set
	// gridleftx or gridrightx to the grid limits, we forget to similarly
	// change the index into setValues.

	if(gridleftx < 0) {
	    gridleftx = 0;
	}
	if(gridrightx >= width) {
	    gridrightx = width-1;
	}
	if(gridboty < 0) {
	    gridboty = 0;
	}
	if(gridtopy >= height) {
	    gridtopy = height-1;
	}

	//	System.err.println("setValues.length="+setValues.length+", setValues[0].length="+setValues[0].length+", left="+gridleftx+", right="+gridrightx+", bot="+gridboty+", top="+gridtopy);

	int loopY = 0;
	int loopX = 0;
	int indY = 0;
	int indX = 0;
	for(loopY = gridboty, indY = 0; loopY <= gridtopy; loopY++, indY++) {
	    for( loopX = gridleftx, indX = 0; loopX <= gridrightx; loopX++, indX++) {
		values[loopY][loopX] = setValues[indX][indY];
	    }
	}
    }

    public void addFootPrint(int gridX, int gridY, double setValues[][]) {
	int gridleftx = gridX - setValues.length/2;
	int gridrightx = gridX + setValues.length/2 - 1;
	int gridboty = gridY - setValues[0].length/2;
	int gridtopy = gridY + setValues[0].length/2 - 1;
	// TODO: SRO Wed Jul 19 17:29:57 EDT 2006 - when we're too
	// near the edge here, we mess up the foot print - when we set
	// gridleftx or gridrightx to the grid limits, we forget to similarly
	// change the index into setValues.

	int indleftx = 0;
	int indtopy = 0;

	if(gridleftx < 0) {
	    indleftx += -gridleftx;
	    gridleftx = 0;
	}
	if(gridrightx >= width) {
	    gridrightx = width-1;
	}
	if(gridboty < 0) {
	    indtopy += -gridboty;
	    gridboty = 0;
	}
	if(gridtopy >= height) {
	    gridtopy = height-1;
	}

	//	System.err.println("setValues.length="+setValues.length+", setValues[0].length="+setValues[0].length+", left="+gridleftx+", right="+gridrightx+", bot="+gridboty+", top="+gridtopy);

	int loopY = 0;
	int loopX = 0;
	int indY = 0;
	int indX = 0;
	for(loopY = gridboty, indY = 0; loopY <= gridtopy; loopY++, indY++) {
	    for( loopX = gridleftx, indX = 0; loopX <= gridrightx; loopX++, indX++) {
		values[loopY][loopX] += setValues[indX][indY];
	    }
	}
    }

    public synchronized void setAllValues(double newValue) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] = newValue;
	    }
	}
	highest = newValue;
	lowest = newValue;
	changed();
    }

    public void clear(double value) {
	setAllValues(value);
	changed();
    }

    public void clear() {
	clear(PASSABLE);
    }

    public void add(int x, int y, double value) {
	if(insideGrid(x,y)) {
	    values[y][x] += value;
	}
    }

    public void add(int x, int y, int value) {
	if(insideGrid(x,y)) {
	    values[y][x] += value;
	}
    }

    public void ceiling(double ceiling) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(values[loopY][loopX] > ceiling)
		    values[loopY][loopX] = ceiling;
	    }
	}	
    }

    public void add(double value) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] += value;
	    }
	}	
    }

    public void multiply(double factor) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] *= factor;
	    }
	}	
    }

    public void invert() { 
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(values[loopY][loopX] > 0) {
		    values[loopY][loopX] = 1/values[loopY][loopX];
		}
	    }
	}	
    }

    public void square() {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] = values[loopY][loopX]*values[loopY][loopX];
	    }
	}	
    }

    public void invExp() {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] = Math.exp(-values[loopY][loopX]);
	    }
	}	
    }

    public void negate() {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] = -values[loopY][loopX];
	    }
	}	
    }

    // TODO: SRO Tue Aug  1 23:19:27 EDT 2006
    //
    // Crap, I think I reversed things here... the larger the distance
    // from the average, the LOWER the entropy.  The closer the
    // higher.  Got to fix this, and at the same time fix the
    // ParticleFilterCostMap.getCost method to take this new value.
    public void fakeEntropy() {
	double gridAvg = average();
	
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] = gridAvg - values[loopY][loopX];
		if(values[loopY][loopX] < 0)
		    values[loopY][loopX] *= -1;
	    }
	}
	computeHighestLowest();
	divide(getHighest());
    }

    public void print() {
	for(int loopY = 0; loopY < height; loopY++) {  
	    System.err.print(loopY+"| ");
            for(int loopX = 0; loopX < width; loopX++) {  
		System.err.print(values[loopY][loopX]+",");
	    }
	    System.err.println();
	}	
    }

    public void histogram() {
	int histo[]  = new int[1001];
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(values[loopY][loopX] < 1000)
		    histo[(int)values[loopY][loopX]]++;
		else
		    histo[1000]++;
	    }
	}
	for(int loopX = 0; loopX < 1001; loopX++) {  
	    if(0 != histo[loopX]) 
		System.err.println(loopX+"\t=\t"+histo[loopX]);
	}
    }

    public void abs() {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(values[loopY][loopX] < 0) 
		    values[loopY][loopX] = values[loopY][loopX] * -1;
	    }
	}
    }

    // This is just scaling/shearing everything so max is topEnd and min is
    // 0.0.
    public void scaleTo(double topEnd) {
 	computeHighestLowest();
 	double range = highest - lowest;
	if(0.0 == range)
	    return;

 	debug.debug("BEFORE NORMALIZE highest="+highest+", lowest="+lowest+", range="+range);
 	for(int loopY = 0; loopY < height; loopY++) {  
             for(int loopX = 0; loopX < width; loopX++) {  
 		values[loopY][loopX] = ((values[loopY][loopX] - lowest)/range) * topEnd;
 	    }
 	}
 	computeHighestLowest();
	range = highest - lowest;
 	debug.debug("AFTER NORMALIZE highest="+highest+", lowest="+lowest+", range="+range);
// 	Normalizer normalizer = new Normalizer();
// 	normalizer.normaliseAscending2(width, height, values);
    }
    
    // @todo: This really shouldn't be called normalize since its not
    // really normalizing, it is just scaling everything so max is topEnd
    // and min is 0.0.
    public void normalize(double topEnd) {
	debug.error("Don't use normalize(double), use scaleTo(double).  See comments for normalize for why.");
	scaleTo(topEnd);
    }

    // This is the real normalize method - this normalizes the values
    // of the grid so that they sum to 1.0.
    public void normalize() {
	double sum = 0.0;
 	for(int loopY = 0; loopY < height; loopY++) {  
             for(int loopX = 0; loopX < width; loopX++) {  
		 sum = sum + values[loopY][loopX];
 	    }
 	}
	if(sum != 0.0) {
	    for(int loopY = 0; loopY < height; loopY++) {  
		for(int loopX = 0; loopX < width; loopX++) {  
		    values[loopY][loopX] = values[loopY][loopX]/sum;
		}
	    }
	}
    }

    // @todo: This really shouldn't be called normalize since its not
    // really normalizing.  Its just scaling everything based on the
    // distance from source and sink.  (For that matter its not what
    // mathematicians normally call a 'saddle' either.)
    public void normalizeSaddle(int gridSourceX, int gridSourceY, int gridSinkX, int gridSinkY) {
 	computeHighestLowest();
 	double range = highest - lowest;
	if(0.0 == range) {
	    debug.debug("normalizeSaddle: range is 0.0, not doing anything, returning early.");
	    return;
	}
	
	double distSourceToSink = Math.sqrt(((gridSourceX - gridSinkX)*(gridSourceX - gridSinkX)) +
					    ((gridSourceY - gridSinkY)*(gridSourceY - gridSinkY)));

 	debug.debug("normalizeSaddle: highest="+highest+", lowest="+lowest+", range="+range);
 	debug.debug("normalizeSaddle: grid coords for source="+gridSourceX+","+gridSourceY+" sink="+gridSinkX+","+gridSinkY);
 	for(int loopY = 0; loopY < height; loopY++) {  
             for(int loopX = 0; loopX < width; loopX++) {  
		 double sourceRadius = Math.sqrt(((gridSourceX - loopX)*(gridSourceX - loopX)) +
						 ((gridSourceY - loopY)*(gridSourceY - loopY)));
		 double sinkRadius = Math.sqrt(((gridSinkX - loopX)*(gridSinkX - loopX)) +
					       ((gridSinkY - loopY)*(gridSinkY - loopY)));
		 if(sinkRadius < sourceRadius) 
		     values[loopY][loopX] = values[loopY][loopX] * sinkRadius;
		 else 
		     values[loopY][loopX] = values[loopY][loopX] * sourceRadius;
	     }
 	}
 	computeHighestLowest();
	range = highest - lowest;
 	debug.debug("normalizeSaddle: done, highest="+highest+", lowest="+lowest+", range="+range);
// 	Normalizer normalizer = new Normalizer();
// 	normalizer.normaliseAscending2(width, height, values);
    }
    
    public DoubleGrid() {
	debug = new DebugFacade(this);
    }

    public DoubleGrid(DoubleGrid grid) {
	debug = new DebugFacade(this);
	copyHeaders(grid);
	
	values = new double[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = grid.values[loopY][loopX];
	    }
	}


    }

    public void copy(IntGrid grid, boolean copy) {
	copyHeaders(grid);

	values = new double[height][width];
	if(copy) {
	    for(int loopY = 0; loopY < height; loopY++) {
		for(int loopX = 0; loopX < width; loopX++) {
		    values[loopY][loopX] = grid.values[loopY][loopX];
		    if(values[loopY][loopX] > highest)
			highest = values[loopY][loopX];
		    if(values[loopY][loopX] < lowest)
			lowest = values[loopY][loopX];
		}
	    }
	}
    }

    public void copy(DoubleGrid grid, boolean copy) {
	copyHeaders(grid);

	values = new double[height][width];
	if(copy) {
	    for(int loopY = 0; loopY < height; loopY++) {
		for(int loopX = 0; loopX < width; loopX++) {
		    values[loopY][loopX] = grid.values[loopY][loopX];
		    if(values[loopY][loopX] > highest)
			highest = values[loopY][loopX];
		    if(values[loopY][loopX] < lowest)
			lowest = values[loopY][loopX];
		}
	    }
	}
    }


    public DoubleGrid(IntGrid grid, boolean copy) {
	debug = new DebugFacade(this);
	copy(grid, copy);
    }

    public DoubleGrid(IntGrid grid) {
	this(grid, true);
    }
    
    // deprecated
    public DoubleGrid(int height, int width) {
	debug = new DebugFacade(this);
	this.height = height;
	this.width = width;
	values = new double[height][width];	
	validData = true;
	clear();
    }

    // deprecated
    public DoubleGrid(int height, int width, int gridCellSize) {
	debug = new DebugFacade(this);
	this.height = height;
	this.width = width;
	this.gridCellSize = gridCellSize;
	this.widthMeters = width * gridCellSize;
	this.heightMeters = height * gridCellSize;
	values = new double[height][width];	
	validData = true;
	clear();
    }

    public DoubleGrid(DoubleGrid grid, int x, int y, int width, int height) {
	debug = new DebugFacade(this);
	copyHeaders(grid);
	
	debug.info("Creating subsection grid of size "+width+", "+height+" at "+x+", "+y);  
	values = new double[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = grid.values[y+loopY][x+loopX];
	    }
	}
	
	computeHighestLowest();
	debug.info("Done creating subsection grid of size "+width+", "+height+" at "+x+", "+y);  
    }

    protected void allocateValues(int height, int width) {
	// Throw away old values array BEFORE we allocate the new
	// values array.  This is kinda wierd, it may not actually
	// help, but common sense says that the allocation happens
	// in two steps, first the new array is allocated, then it
	// is assigned to 'values', after which values is freed
	// and eventually garbage collected.
	values = null;
	System.gc();
	values = new double[height][width];
	highest= Double.MIN_VALUE;
	lowest = Double.MAX_VALUE;
    }

    protected void parseAndLoadGridFileLine(int loopY, String line) {
	double[] parsedLine = new double[width];
	//parsedLine = StringUtils.parseDoubleList(width, line);
	StringUtils.parseDoubleListFast(width, parsedLine, line);

	// Copy the parsed doubles into the final location.
	for(int loopX = 0; loopX < width; loopX++) {
	    if(parsedLine[loopX] > highest)
		highest = parsedLine[loopX];
	    if(parsedLine[loopX] < lowest)
		lowest = parsedLine[loopX];
	    values[loopY][loopX] = parsedLine[loopX];
	}
    }

    protected String gridFileLineToString(int loopY) {
	StringBuffer buf = new StringBuffer(1000);
	buf.append(values[loopY][0]);
	for(int loopX = 1; loopX < width; loopX++) {
	    buf.append(" ").append(values[loopY][loopX]);
	}
	return buf.toString();
    }

    public synchronized void toIntGrid(IntGrid resultGrid) {
        for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		resultGrid.values[loopY][loopX] = (int)values[loopY][loopX];
	    }
	}
	resultGrid.changed();
    }

    public synchronized void computeElevationObstacle(IntGrid result) {
	double maxSlope = .3826834319 * gridCellSize;
	computeElevationObstacle(maxSlope, result);
    }

    public synchronized void computeElevationObstacle(double maxSlope, IntGrid result) {
        double slope1, slope2, slope3, slope4, avgSlope;  

        for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		result.values[loopY][loopX] = PASSABLE;
	    }
	}

        for(int loopY = 1; loopY < (height - 1); loopY++) {  
            for(int loopX = 1; loopX < (width - 1); loopX++) {  
                slope1 = values[loopY - 1][loopX] - values[loopY][loopX];  
                slope2 = values[loopY + 1][loopX] - values[loopY][loopX];  
                slope3 = values[loopY][loopX - 1] - values[loopY][loopX];  
                slope4 = values[loopY][loopX + 1] - values[loopY][loopX];  
                if(slope1<0) 
                    slope1 = -slope1; 
                if(slope2<0) 
                    slope2 = -slope2; 
                if(slope3<0) 
                    slope3 = -slope3; 
                if(slope4<0) 
                    slope4 = -slope4; 
                avgSlope = (slope1 + slope2 + slope3 + slope4)/4; 
                if(avgSlope > maxSlope) 
                    result.values[loopY][loopX] = IMPASSABLE; 
                else 
                    result.values[loopY][loopX] = PASSABLE; 
            }   
        }  
    }

    public synchronized void averageSlopes(DoubleGrid resultGrid) {
        double slope1, slope2, slope3, slope4, avgSlope;  

	resultGrid.clear();
	
        for(int loopY = 1; loopY < (height - 1); loopY++) {  
            for(int loopX = 1; loopX < (width - 1); loopX++) {  
                slope1 = values[loopY - 1][loopX] - values[loopY][loopX];  
                slope2 = values[loopY + 1][loopX] - values[loopY][loopX];  
                slope3 = values[loopY][loopX - 1] - values[loopY][loopX];  
                slope4 = values[loopY][loopX + 1] - values[loopY][loopX];  
                if(slope1<0) 
                    slope1 = -slope1; 
                if(slope2<0) 
                    slope2 = -slope2; 
                if(slope3<0) 
                    slope3 = -slope3; 
                if(slope4<0) 
                    slope4 = -slope4; 
                avgSlope = (slope1 + slope2 + slope3 + slope4)/4; 
		resultGrid.values[loopY][loopX] = avgSlope;
		if(avgSlope > resultGrid.highest)
		    resultGrid.highest = avgSlope;
		if(avgSlope < resultGrid.lowest)
		    resultGrid.lowest = avgSlope;
            }   
        }  
    }
    
    public void divide(double divisor) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = values[loopY][loopX]/divisor;
	    }
	}	
    }

    public double sum() {
	double sum = 0;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		sum += values[loopY][loopX];
	    }
	}	
	return sum;
    }

    public double average() {
	return(sum()/(width*height));
    }


    public void entropy() {
	double entropy=0.0;
	double lastp=Double.MIN_VALUE;
	double log2 = Math.log(2);
	int saveCounter = 0;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(values[loopY][loopX] == lastp) {
		    values[loopY][loopX] = entropy;
		    saveCounter++;
		}
		else {
		    double p = values[loopY][loopX];
		    lastp = p;
		    double pp = 1 - values[loopY][loopX];
		    double logp = Math.log(p);
		    double logpp = Math.log(pp);
		    entropy = -1 * ( (logp*p) + (logpp*pp) )/log2;
		    values[loopY][loopX] =  entropy;
		}
	    }
	}
	debug.info("Saved "+saveCounter+" calculations of entropy out of "+(width *height)+" (%"+(((double)saveCounter)/((double)(width*height))));	
    }

    // Given a cutPoint value, set all values less than or equal to
    // cut to low, and all values higher than cut to high.
    public void cut(double cut, double low, double high, DoubleGrid resultGrid) {
        for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		resultGrid.values[loopY][loopX] = (values[loopY][loopX] <= cut) ? low : high;
	    }
	}
	resultGrid.changed();
    }

    // Add each of the values from another grid into this one.
    public void add(DoubleGrid grid) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] += grid.values[loopY][loopX];
	    }
	}	
    }

    public synchronized void contour(double elevation, DoubleGrid resultGrid) {
        for(int loopY = 1; loopY < (height - 1); loopY++) {  
            for(int loopX = 1; loopX < (width - 1); loopX++) {  
		if((values[loopY][loopX] >= elevation)

		   && ((values[loopY-1][loopX-1] < elevation)
		       || (values[loopY-1][loopX] < elevation)
		       || (values[loopY-1][loopX+1] < elevation)
		       || (values[loopY-1][loopX] < elevation)
		       || (values[loopY+1][loopX] < elevation)
		       || (values[loopY+1][loopX-1] < elevation)
		       || (values[loopY+1][loopX] < elevation)
		       || (values[loopY+1][loopX+1] < elevation))) {
		    resultGrid.values[loopY][loopX] = 1;
		}
		else {
		    resultGrid.values[loopY][loopX] = 0;
		}
	    }
	}
    }
    
    public void computeDistanceGrid(DoubleGrid result) {
	double maxDist = Math.sqrt((double)((height * height) + (width * width)));
	double distance = maxDist;
	
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		result.values[loopY][loopX] = distance;
	    }
	}

	boolean spotDone = false;
	// Now the hard way.
	for(int loopY = 0; loopY < height; loopY++) {
	    //	    debug.info("processing row="+loopY);
	    for(int loopX = 0; loopX < width; loopX++) {
		if(0 == result.values[loopY][loopX]) 
		    continue;
		if(IMPASSABLE == values[loopY][loopX]) {
		    result.values[loopY][loopX] = 0;
		    continue;
		}
		    
		int maxBoxRadius = (int)((((float)result.values[loopY][loopX])/2) + 1);
		spotDone = false; 
		for(int loopBoxRadius = 1; (!spotDone) && (loopBoxRadius < maxBoxRadius); loopBoxRadius++) {
		    for(int loopi = 0; (!spotDone) && (loopi < loopBoxRadius); loopi++) {

			if(((loopY - loopi) >= 0)
			   && ((loopX - loopBoxRadius) >= 0)
			   && (IMPASSABLE == values[loopY - loopi][loopX - loopBoxRadius])
			   ) {
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}
			else if(((loopY + loopi) < height) 
				&& ((loopX - loopBoxRadius) >= 0)
				&& (IMPASSABLE == values[loopY + loopi][loopX - loopBoxRadius])
				) {
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}
			else if(((loopY - loopi) >= 0) 
				&& ((loopX + loopBoxRadius) < width)
				&& (IMPASSABLE == values[loopY - loopi][loopX + loopBoxRadius])
				) {
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}
			else if(((loopY + loopi) < height) 
				&& ((loopX + loopBoxRadius) < width)
				&& (IMPASSABLE == values[loopY + loopi][loopX + loopBoxRadius]) 
				) {
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}
			else if(((loopY - loopBoxRadius) >= 0) 
				&& ((loopX - loopi) >= 0)
				&& (IMPASSABLE == values[loopY - loopBoxRadius][loopX - loopi])
				) {
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}
			else if(((loopY + loopBoxRadius) < height) 
				&& ((loopX - loopi) >= 0) 
				&& (IMPASSABLE == values[loopY + loopBoxRadius][loopX - loopi]) 
				){
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}
			else if(((loopY - loopBoxRadius) >= 0) 
				&& ((loopX + loopi) < width)
				&& (IMPASSABLE == values[loopY - loopBoxRadius][loopX + loopi])
				) {
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}
			else if(((loopY + loopBoxRadius) < height)
				&& ((loopX + loopi) < width)  
				&&( IMPASSABLE == values[loopY + loopBoxRadius][loopX + loopi])
				) {
			    distance = Math.sqrt((loopi * loopi) + (loopBoxRadius * loopBoxRadius));
			    if(distance <= result.values[loopY][loopX]) {
				spotDone = true;
				result.values[loopY][loopX] = distance;
			    }
			}

		    }
		}
	    }
	}

    }

    // Experimental - not quite sure what I was up to here, but I
    // think it involved finding the highest points/ridge lines in a
    // 'surface'.  I think I was feeding distance grids into this,
    // trying to basically figure out the voronoi diagram in a faster
    // manner.
    public void computeRidges(DoubleGrid result) {

	for(int loopY = 1; loopY < (height - 1); loopY++) {
	    for(int loopX = 1; loopX < (width - 1); loopX++) {
		if((values[loopY][loopX] > values[loopY - 1][loopX])
		   && (values[loopY][loopX] > values[loopY + 1][loopX]))
		    result.values[loopY][loopX] = highest+10;
		else if((values[loopY][loopX] > values[loopY][loopX - 1])
			&& (values[loopY][loopX] > values[loopY][loopX + 1]))
		    result.values[loopY][loopX] = highest+10;
		else if((values[loopY][loopX] > values[loopY -1][loopX - 1])
			&& (values[loopY][loopX] > values[loopY + 1][loopX + 1]))
		    result.values[loopY][loopX] = highest+10;
		else if((values[loopY][loopX] > values[loopY + 1][loopX - 1])
			&& (values[loopY][loopX] > values[loopY - 1][loopX + 1]))
		    result.values[loopY][loopX] = highest+10;
		else
		    result.values[loopY][loopX] = values[loopY][loopX];
	    }
	}
	
    }

    public synchronized MemoryImageSource drawImageProducer(Graphics panelG, BufferedImage img, boolean showElevationWithHue, boolean showElevationWithBrightness, double defaultHue, double defaultSaturation, double defaultBrightness, double contourFactor, double contourGranularity) {
	if(!validData) {
	    Debug.error("DoubleGrid:                Trying to draw invalid data.");
	    return null;
	}
	else
	    debug.debug("                Drawing double grid.");

	int paintingCounter = 0;

	double elevation = 0.0;
	double remainder = 0.0;
	double aboveLowest = 0.0;
	double percent = 0.0;

	int blackRGB = Color.black.getRGB();
	int colorRGBs[] = new int[100];
	for(int loopi = 0; loopi < colorRGBs.length; loopi++) {
	    if(showElevationWithHue) {
		colorRGBs[loopi] = (Color.getHSBColor(((float)loopi)/colorRGBs.length, (float)defaultSaturation, (float)defaultBrightness)).getRGB();
	    }
	    else if(showElevationWithBrightness) {
		colorRGBs[loopi] = (Color.getHSBColor((float)defaultHue, (float)defaultSaturation, ((float)loopi)/colorRGBs.length)).getRGB();
	    }
	}

	int rgbValues[] = new int[height * width];

	debug.debug("Redrawing terrain.  highest="+highest+", lowest="+lowest+", range="+(highest - lowest));
	Graphics2D g = img.createGraphics();
 
	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY == paintingCounter) {
		//		debug.debug("Redrawing is "+(loopY*100)/height+"% complete."); 
		paintingCounter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		elevation = values[loopY][loopX];
		aboveLowest = elevation - lowest;
		percent = aboveLowest / ( highest - lowest);
		remainder = elevation % contourFactor;

		if(remainder < contourGranularity) {
		    rgbValues[(loopY * width) + loopX] = blackRGB;
		    //		    img.setRGB(loopX, loopY, blackRGB);
		}
		else {
		    if(percent >= 1.0)
			percent = 0.99;
		    rgbValues[(loopY * width) + loopX] = colorRGBs[((int)(percent*100))];
		    //		    img.setRGB(loopX, loopY, colorRGBs[((int)(percent*100))]);
		}
	    }
	}
	debug.debug("    Creating imageProducer");
	MemoryImageSource foo =  new MemoryImageSource(width, height, rgbValues, 0, width);
	debug.debug("    Done creating imageProducer.");
	return foo;
    }

    public synchronized Image drawImage(Graphics panelG, boolean showElevationWithHue, boolean showElevationWithBrightness, double defaultHue, double defaultSaturation, double defaultBrightness, double contourFactor, double contourGranularity) {
	if(!validData) {
	    Debug.info("DoubleGrid:                Trying to draw invalid data.");
	    return null;
	}
	else
	    debug.debug("                Drawing double grid.");

	int paintingCounter = 0;

	double elevation = 0.0;
	double remainder = 0.0;
	double aboveLowest = 0.0;
	double percent = 0.0;

	int blackRGB = Color.black.getRGB();
	int colorRGBs[] = new int[100];
	for(int loopi = 0; loopi < colorRGBs.length; loopi++) {
	    if(showElevationWithHue) {
		colorRGBs[loopi] = (Color.getHSBColor(((float)loopi)/colorRGBs.length, (float)defaultSaturation, (float)defaultBrightness)).getRGB();
	    }
	    else if(showElevationWithBrightness) {
		colorRGBs[loopi] = (Color.getHSBColor((float)defaultHue, (float)defaultSaturation, ((float)loopi)/colorRGBs.length)).getRGB();
	    }
	}

	int rgbValues[] = new int[height * width];

	debug.debug("                Redrawing terrain.  highest="+highest+", lowest="+lowest+", range="+(highest - lowest));
 
	computeHighestLowest();

	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY == paintingCounter) {
		//		debug.debug("                Redrawing is "+(loopY*100)/height+"% complete."); 
		paintingCounter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		elevation = values[loopY][loopX];
		aboveLowest = elevation - lowest;
		percent = aboveLowest / ( highest - lowest);
		remainder = elevation % contourFactor;

		// TODO: SRO Fri Feb 11 17:37:26 EST 2005 - the four
		// lines of code below add the 'contour' to the image.
		// Don't want it for now.  Perhaps should get rid of
		// it entirely.  Not sure yet so leaving it here.
		//
// 		if(remainder < contourGranularity) {
// 		    rgbValues[(loopY * width) + loopX] = blackRGB;
// 		}
// 		else
 {
		    if(showElevationWithHue || showElevationWithBrightness) {
			if(percent >= 1.0)
			    percent = 0.99;
			rgbValues[(loopY * width) + loopX] = colorRGBs[(int)(percent*100)];
		    }
		    else {
			if(elevation >= 2000.0)
			    elevation = 2000.0;
			// rgbValues[(loopY * width) + loopX] = colorRGBs[((int)(elevation*100))];
			if(elevation <= 500) { 
			    rgbValues[(loopY * width) + loopX] = Color.black.getRGB();
			}
			else if(elevation < 700) {
			    rgbValues[(loopY * width) + loopX] = Color.darkGray.getRGB();
			}
			else if (elevation < 900) {
			    rgbValues[(loopY * width) + loopX] = Color.gray.getRGB();
			}
			else if (elevation < 1100) {
			    rgbValues[(loopY * width) + loopX] = Color.lightGray.getRGB();
			}
			else if (elevation < 1300) {
			    rgbValues[(loopY * width) + loopX] = Color.red.getRGB();
			}
			else if (elevation < 1500) {
			    rgbValues[(loopY * width) + loopX] = Color.orange.getRGB();
			}
			else if (elevation < 1700) {
			    rgbValues[(loopY * width) + loopX] = Color.yellow.getRGB();
			}
			else {
			    rgbValues[(loopY * width) + loopX] = Color.white.getRGB();
			}
		    }
		}
	    }
	}
	MemoryImageSource foo =  new MemoryImageSource(width, height, rgbValues, 0, width);
	Canvas canvas = new Canvas();
	Image tempImage = canvas.createImage(foo);
	
	return tempImage;
    }
    
    // TODO: SRO Thu Jul 20 20:30:34 EDT 2006
    //
    // Someday I need to refactor the draw functions quite a bit.
    // Among other things I want to replace these with a version that
    // takes a color array, simplify the heck out of it, then create
    // various facade methods that generate the color array including
    // one that mimics these original methods.  And I have NO IDEA why
    // panelG is a method param - nothing seems to use it.
    public synchronized Image drawImage(Graphics panelG, boolean showElevationWithHue, boolean showElevationWithBrightness, double defaultHue, double defaultSaturation, double defaultBrightness, double contourFactor, double contourGranularity, int alpha) {
	if(!validData) {
	    Debug.info("DoubleGrid:                Trying to draw invalid data.");
	    return null;
	}
	else
	    debug.debug("                Drawing double grid.");

	int paintingCounter = 0;

	double elevation = 0.0;
	double remainder = 0.0;
	double aboveLowest = 0.0;
	double percent = 0.0;

	int blackRGB = Color.black.getRGB();
	int colorRGBs[] = new int[100];
	for(int loopi = 0; loopi < colorRGBs.length; loopi++) {
	    
	    Color color = null;
	    if(showElevationWithHue) {
		color = (Color.getHSBColor(((float)loopi)/colorRGBs.length, (float)defaultSaturation, (float)defaultBrightness));
	    }
	    else if(showElevationWithBrightness) {
		color = (Color.getHSBColor((float)defaultHue, (float)defaultSaturation, ((float)loopi)/colorRGBs.length));
	    }
	    if(loopi >= 5)
		color = new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha);
	    else
		color = new Color(color.getRed(),color.getGreen(),color.getBlue(),0);
	    colorRGBs[loopi] = color.getRGB();
	}

	int rgbValues[] = new int[height * width];

	debug.debug("                Redrawing terrain.  highest="+highest+", lowest="+lowest+", range="+(highest - lowest));
 
	computeHighestLowest();

	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY == paintingCounter) {
		//		debug.debug("                Redrawing is "+(loopY*100)/height+"% complete."); 
		paintingCounter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		elevation = values[loopY][loopX];
		aboveLowest = elevation - lowest;
		percent = aboveLowest / ( highest - lowest);
		remainder = elevation % contourFactor;

		// TODO: SRO Fri Feb 11 17:37:26 EST 2005 - the four
		// lines of code below add the 'contour' to the image.
		// Don't want it for now.  Perhaps should get rid of
		// it entirely.  Not sure yet so leaving it here.
		//
// 		if(remainder < contourGranularity) {
// 		    rgbValues[(loopY * width) + loopX] = blackRGB;
// 		}
// 		else
 {
		    if(showElevationWithHue || showElevationWithBrightness) {
			if(percent >= 1.0)
			    percent = 0.99;
			rgbValues[(loopY * width) + loopX] = colorRGBs[(int)(percent*100)];
		    }
		    else {
			if(elevation >= 2000.0)
			    elevation = 2000.0;
			// rgbValues[(loopY * width) + loopX] = colorRGBs[((int)(elevation*100))];
			if(elevation <= 500) { 
			    rgbValues[(loopY * width) + loopX] = Color.black.getRGB();
			}
			else if(elevation < 700) {
			    rgbValues[(loopY * width) + loopX] = Color.darkGray.getRGB();
			}
			else if (elevation < 900) {
			    rgbValues[(loopY * width) + loopX] = Color.gray.getRGB();
			}
			else if (elevation < 1100) {
			    rgbValues[(loopY * width) + loopX] = Color.lightGray.getRGB();
			}
			else if (elevation < 1300) {
			    rgbValues[(loopY * width) + loopX] = Color.red.getRGB();
			}
			else if (elevation < 1500) {
			    rgbValues[(loopY * width) + loopX] = Color.orange.getRGB();
			}
			else if (elevation < 1700) {
			    rgbValues[(loopY * width) + loopX] = Color.yellow.getRGB();
			}
			else {
			    rgbValues[(loopY * width) + loopX] = Color.white.getRGB();
			}
		    }
		}
	    }
	}
	MemoryImageSource foo =  new MemoryImageSource(width, height, rgbValues, 0, width);
	Canvas canvas = new Canvas();
	Image tempImage = canvas.createImage(foo);
	
	return tempImage;
    }

}
