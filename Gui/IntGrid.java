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

import java.awt.Canvas;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// This is the basic "grid of ints" class.  We're going to use this
// (or a grid of floats, or of ints, or some kinda fixed point floats)
// to implement all of our terrain grids.
public class IntGrid extends Grid {
    public double getCellSizeMeters() { return gridCellSize;}
    public double getGridCellSizeMeters() { return gridCellSize;}

    public int values[][] = null;
    private int highest= -1000000000; // Integer.MIN_VALUE;
    public int getHighest() { return highest;}
    private int lowest = 1000000000; // Integer.MAX_VALUE;
    public int getLowest() { return lowest;}

    public void computeHighestLowest() {
	//	debug.debug("Recomputing highest and lowest.");
	highest= -1000000000;
	lowest = 1000000000;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(values[loopY][loopX] > highest) {
		    if(highest != PASSABLE)
			highest = values[loopY][loopX];
		}
		if(values[loopY][loopX] < lowest) {
		    lowest = values[loopY][loopX];
		}
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
	IntGrid grid = (IntGrid)otherGrid;
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

    public int getValue(int gridx, int gridy) {
	if((gridx < 0) || (gridy < 0) || (gridx >= width) || (gridy >= height)) 
	    return IMPASSABLE;
	return values[gridy][gridx];
    }
    
    public synchronized void setValue(int gridx, int gridy, int value) {
	if(insideGrid(gridx,gridy)) {
	    values[gridy][gridx] = value;
	    if(values[gridy][gridx] > highest)
		highest = values[gridy][gridx];
	    if(values[gridy][gridx] < lowest)
		lowest = values[gridy][gridx];
	    changed();
	}
    }

    public synchronized void setValue(int gridx, int gridy, double value) {
	if(insideGrid(gridx,gridy)) {
	    values[gridy][gridx] = (int)value;
	    if(values[gridy][gridx] > highest)
		highest = values[gridy][gridx];
	    if(values[gridy][gridx] < lowest)
		lowest = values[gridy][gridx];
	    changed();
	}
    }

    public void fastSetValue(int gridx, int gridy, int value) {
	if(insideGrid(gridx,gridy)) {
	    values[gridy][gridx] = value;
	    if(values[gridy][gridx] > highest)
		highest = values[gridy][gridx];
	    if(values[gridy][gridx] < lowest)
		lowest = values[gridy][gridx];
	}
    }

    public void fastSetValue(int gridx, int gridy, double value) {
	if(insideGrid(gridx,gridy)) {
	    values[gridy][gridx] = (int)value;
	    if(values[gridy][gridx] > highest)
		highest = values[gridy][gridx];
	    if(values[gridy][gridx] < lowest)
		lowest = values[gridy][gridx];
	}
    }

    public void setFootPrint(int gridX, int gridY, int setValues[][]) {
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

    public void addFootPrint(int gridX, int gridY, int setValues[][]) {
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

    public synchronized void setAllValues(int newValue) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		values[loopY][loopX] = newValue;
	    }
	}
	highest = newValue;
	lowest = newValue;
	changed();
    }

    public void clear(int value) {
	setAllValues(value);
	changed();
    }

    public void clear() {
	clear(PASSABLE);
    }

    public void add(int gridx, int gridy, double value) {
	if(insideGrid(gridx,gridy)) {
	    values[gridy][gridx] += (int)value;
	}
    }

    public void add(int gridx, int gridy, int value) {
	if(insideGrid(gridx,gridy)) {
	    values[gridy][gridx] += value;
	}
    }

    public void ceiling(int ceiling) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(values[loopY][loopX] > ceiling)
		    values[loopY][loopX] = ceiling;
	    }
	}	
    }

    public void multiply(int factor) {
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
		values[loopY][loopX] = (int)Math.exp(-values[loopY][loopX]);
	    }
	}	
    }

    public void setBorder(int borderWidth, int value) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(
		   (loopY < borderWidth)
		   || (loopY > (width - borderWidth))
		   || (loopX < borderWidth)
		   || (loopX > (height - borderWidth))
		   )
		    values[loopY][loopX] = value;
	    }
	}	
    }

    public void replace(int oldValue, int newValue) {
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		if(oldValue == values[loopY][loopX])
		    values[loopY][loopX] = newValue;
	    }
	}	
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
		System.err.println("DEBUG:IntGrid:"+loopX+"\t=\t"+histo[loopX]);
	}
    }

    public int[] histogram(int numSlots) {
	int histo[]  = new int[numSlots];
	for(int loopi = 0; loopi < numSlots; loopi++)
	    histo[loopi] = 0;
	computeHighestLowest();
	//	double increment = (highest - lowest) / numSlots;
	for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		int index = (values[loopY][loopX] - lowest);
		if(index > (numSlots - 1)) 
		    histo[numSlots-1]++;
		else if(index < 0)
		    histo[0]++;
		else
		    histo[index]++;
	    }
	}
	return histo;
    }

    public void cube() {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = values[loopY][loopX] * values[loopY][loopX] * values[loopY][loopX];
	    }
	}
    }

    public IntGrid() {
	debug = new DebugFacade(this);
    }
    
    private void init(IntGrid grid, boolean copyOldToNew) {
	debug = new DebugFacade(this);
	copyHeaders(grid);
	values = new int[height][width];
	if(copyOldToNew) {
	    for(int loopY = 0; loopY < height; loopY++) {
		for(int loopX = 0; loopX < width; loopX++) {
		    values[loopY][loopX] = grid.values[loopY][loopX];
		}
	    }
	}
    }

    public IntGrid(IntGrid grid, boolean copyOldToNew) {
	init(grid, copyOldToNew);
    }

    public IntGrid(IntGrid grid, int reduceBy) {
	debug = new DebugFacade(this);
	copyHeaders(grid);
	int oldHeight = height;
	int oldWidth = width;
	height /= reduceBy;
	width /= reduceBy;
	gridCellSize *= reduceBy;
	values = new int[height+1][width+1];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = 0;
	    }
	}

	for(int loopY = 0; loopY < oldHeight; loopY++) {
	    for(int loopX = 0; loopX < oldWidth; loopX++) {
		values[loopY/reduceBy][loopX/reduceBy] += grid.values[loopY][loopX];
	    }
	}
    }

    public IntGrid(IntGrid grid, int x, int y, int width, int height) {
	debug = new DebugFacade(this);
	copyHeaders(grid);
	if((x+width) > grid.width)
	    width = grid.width - x;
	if((y+height) > grid.height)
	    height = grid.height - y;
	if(x < 0) {
	    x = 0;
	    width = width + x;
	}
	if(y < 0) {
	    y = 0;
	    height = height + x;
	}

	debug.info("For grid of "+grid.width+", "+grid.height+", creating subsection grid of size "+width+", "+height+" at "+x+", "+y+" to "+(x+width)+", "+(y+height));  
	values = new int[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = grid.values[y+loopY][x+loopX];
	    }
	}
	
	computeHighestLowest();
	debug.info("Done creating subsection grid of size "+width+", "+height+" at "+x+", "+y);  
    }


    // Copy constructor 
    public IntGrid(IntGrid grid) {
	init(grid, true);
    }

    public IntGrid(DoubleGrid grid) {
	debug = new DebugFacade(this);
	copyHeaders(grid);
	
	values = new int[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = (int)grid.values[loopY][loopX];
		if(values[loopY][loopX] > highest)
		    highest = values[loopY][loopX];
		if(values[loopY][loopX] < lowest)
		    lowest = values[loopY][loopX];
	    }
	}
    }

    public IntGrid(int height, int width, int gridCellSize, int bottomLeftX, int bottomLeftY, double widthMeters, double heightMeters, double southLat, double northLat, double westLong, double eastLong) {
	this.width = width;
	this.height = height;
	this.gridCellSize = gridCellSize;
	this.bottomLeftX = bottomLeftX;
	this.bottomLeftY = bottomLeftY;
	this.widthMeters = widthMeters;
	this.heightMeters = heightMeters;
	this.southLat = southLat;
	this.northLat = northLat;
	this.westLong = westLong;
	this.eastLong = eastLong;
	validData = true;

	debug = new DebugFacade(this);

	values = new int[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = PASSABLE;
	    }
	}
    }

    public IntGrid(int height, int width) {
	this.width = width;
	this.height = height;
	this.gridCellSize = gridCellSize;
	this.bottomLeftX = bottomLeftX;
	this.bottomLeftY = bottomLeftY;
	this.widthMeters = widthMeters;
	this.heightMeters = heightMeters;
	this.southLat = southLat;
	this.northLat = northLat;
	this.westLong = westLong;
	this.eastLong = eastLong;
	validData = true;

	debug = new DebugFacade(this);

	values = new int[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] = PASSABLE;
	    }
	}
    }

    public synchronized void toDoubleGrid(DoubleGrid resultGrid) {
        for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		resultGrid.values[loopY][loopX] = (double)values[loopY][loopX];
	    }
	}
	resultGrid.changed();
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
	values = new int[height][width];
	highest= Integer.MIN_VALUE;
	lowest = Integer.MAX_VALUE;
    }

    protected void parseAndLoadGridFileLine(int loopY, String line) {
	int[] parsedLine = new int[width];
	//parsedLine = StringUtils.parseIntList(width, line);
	parsedLine = StringUtils.parseIntList(width, line);

	// Copy the parsed ints into the final location.
	for(int loopX = 0; loopX < width; loopX++) {
	    if(parsedLine[loopX] > highest)
		highest = parsedLine[loopX];
	    if(parsedLine[loopX] < lowest)
		lowest = parsedLine[loopX];
	    values[loopY][loopX] = parsedLine[loopX];
	}
    }

    public boolean loadSoilTypeFile(String fileName) {
	return loadGridFile(fileName);
    }

    protected String gridFileLineToString(int loopY) {
	StringBuffer buf = new StringBuffer(1000);
	buf.append(values[loopY][0]);
	for(int loopX = 1; loopX < width; loopX++) {
	    buf.append(" ").append(values[loopY][loopX]);
	}
	return buf.toString();
    }

    public IntGrid subsample(int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
	return null;
    }

    public int countGreaterThan(int value) {
	int count = 0;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(values[loopY][loopX] > value) 
		    count++;
	    }	
	}
	return count;
    }

    // Count up all !PASSABLE grid cells.  Used mostly for debugging.
    public int countObstacles() {
	int count = 0;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(PASSABLE != values[loopY][loopX]) 
		    count++;
	    }	
	}
	return count;
    }
    
    // Given a cutPoint value, set all values less than or equal to
    // cut to low, and all values higher than cut to high.
    public void cut(int cut, int low, int high, IntGrid resultGrid) {
        for(int loopY = 0; loopY < height; loopY++) {  
            for(int loopX = 0; loopX < width; loopX++) {  
		resultGrid.values[loopY][loopX] = (values[loopY][loopX] <= cut) ? low : high;
	    }
	}
	resultGrid.changed();
    }

    public IntGrid reduceSinglePoints() {
	IntGrid newGrid = new IntGrid(this);
	for(int loopY = 1; loopY < (height - 1); loopY++) {
	    for(int loopX = 1; loopX < (width - 1); loopX++) {
		if((PASSABLE != values[loopY][loopX])
		   && (
		       (PASSABLE == values[loopY- 1][loopX - 1])
		       && (PASSABLE == values[loopY - 1][loopX])
		       && (PASSABLE == values[loopY - 1][loopX + 1])
		       && (PASSABLE == values[loopY][loopX - 1])
		       && (PASSABLE == values[loopY][loopX + 1])
		       && (PASSABLE == values[loopY + 1][loopX - 1])
		       && (PASSABLE == values[loopY + 1][loopX])
		       && (PASSABLE == values[loopY + 1][loopX + 1])
		       )
		) {
		    newGrid.values[loopY][loopX] = PASSABLE;
		}
	    }
	}
	return newGrid;
    }

    // Basically, clear (set to PASSABLE) every IMPASSABLE grid spot
    // that is surrounded on all sides by !PASSABLE grid spots.  This
    // has the effect of reducing contiguous areas of !PASSABLE grid
    // spots into outlines.  (I.e. !PASSABLE grid spots inside the
    // contiguous area are set to PASSABLE.)  This method only finds
    // outlines for PASSABLE and !PASSABLE.  See below for a modified
    // routine that takes a unit size parameter.
    public IntGrid reduceToOutlines() {
	IntGrid newGrid = new IntGrid(this);
	for(int loopY = 1; loopY < (height - 1); loopY++) {
	    for(int loopX = 1; loopX < (width - 1); loopX++) {
		if((PASSABLE != values[loopY][loopX])
		   && (
		       (PASSABLE != values[loopY- 1][loopX - 1])
		       && (PASSABLE != values[loopY - 1][loopX])
		       && (PASSABLE != values[loopY - 1][loopX + 1])
		       && (PASSABLE != values[loopY][loopX - 1])
		       && (PASSABLE != values[loopY][loopX + 1])
		       && (PASSABLE != values[loopY + 1][loopX - 1])
		       && (PASSABLE != values[loopY + 1][loopX])
		       && (PASSABLE != values[loopY + 1][loopX + 1])
		       )
		) {
		    newGrid.values[loopY][loopX] = PASSABLE;
		}
	    }
	}
	return newGrid;
    }

    // Like reduceToOutlines above, but this routine takes a unitSize
    // parameter.
    public IntGrid reduceToOutlines(int maxCellValue, int newCellValue) {
	IntGrid newGrid = new IntGrid(this);
	for(int loopY = 1; loopY < (height - 1); loopY++) {
	    for(int loopX = 1; loopX < (width - 1); loopX++) {
		// If this grid point is an obstacle for this unit size
		if(maxCellValue >= values[loopY][loopX]) {

		    // If a cell is surrounded on all sides by
		    // values >= maxCellValue, then set it to newCellValue;
		    if((maxCellValue >= values[loopY- 1][loopX - 1])
		       && (maxCellValue >= values[loopY - 1][loopX])
		       && (maxCellValue >= values[loopY - 1][loopX + 1])
		       && (maxCellValue >= values[loopY][loopX - 1])
		       && (maxCellValue >= values[loopY][loopX + 1])
		       && (maxCellValue >= values[loopY + 1][loopX - 1])
		       && (maxCellValue >= values[loopY + 1][loopX])
		       && (maxCellValue >= values[loopY + 1][loopX + 1])
		       ) {
			newGrid.values[loopY][loopX] = newCellValue;
		    }
		}
	    }
	}
	return newGrid;
    }

    // Perform the an approximation of a Minkowsky sum on each grid
    // cell, using a 'radius' of n meters.  Note, I think we can
    // compute distancegrid once, and use that for every sized unit.
    // But haven't done that yet.
    public IntGrid robinkowskySumCircleApproximation(double radius) {
	int radiusInGridCells = (int)(radius/gridCellSize);
	IntGrid newGrid = new IntGrid(this);
	int y;
	int x;
	int radiusInGridCellsSquared = radiusInGridCells*radiusInGridCells;
	int oldCounter = 0;

	int[][] circlePoints = new int[(radiusInGridCells*2)+1][(radiusInGridCells*2)+1];
	for(int loopi = -radiusInGridCells; loopi < radiusInGridCells; loopi++) {
	    for(int loopj = -radiusInGridCells; loopj < radiusInGridCells; loopj++) {
		int distSquared = (loopi*loopi) + (loopj*loopj);
		if(distSquared < radiusInGridCellsSquared ) {
		    circlePoints[loopi+radiusInGridCells][loopj+radiusInGridCells] = 1;
		}
	    }
	}

 	for(int loopY = 0; loopY < height; loopY++) {
 	    for(int loopX = 0; loopX < width; loopX++) {
 		if(IMPASSABLE == values[loopY][loopX]) {
		    oldCounter++;
 		    for(int loopi = -radiusInGridCells; loopi < radiusInGridCells; loopi++) {
 			for(int loopj = -radiusInGridCells; loopj < radiusInGridCells; loopj++) {
			    if(1 == circlePoints[loopi+radiusInGridCells][loopj+radiusInGridCells]) {
				y = loopY + loopi;
				x = loopX + loopj;
				if((y >= 0)  && (y < height)  && (x >= 0) && (x < width)) {
				    if(newGrid.values[y][x] > radiusInGridCells) {
					newGrid.values[y][x] = radiusInGridCells;
				    }
				}
 			    }
 			}
 		    }
 		}
 	    }
 	}

	debug.info("oldCounter="+oldCounter);
	return newGrid;
    }

    // Add each of the values from another grid into this one.
    public void add(IntGrid grid) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		values[loopY][loopX] += grid.values[loopY][loopX];
	    }
	}	
    }


    // 'or' two grids together, return result.  No longer really used,
    // might be handy later.
    public IntGrid or(IntGrid grid) {
	IntGrid newGrid = new IntGrid(this);
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		newGrid.values[loopY][loopX] |= grid.values[loopY][loopX];
	    }
	}	
	return newGrid;
    }

    // Copy this.values into 'result', except for cells in 'grid' that
    // are set to IMPASSABLE.
    public void remove(IntGrid grid, IntGrid result) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == grid.values[loopY][loopX])
		    result.values[loopY][loopX] = PASSABLE;
		else
		    result.values[loopY][loopX] = values[loopY][loopX];
	    }
	}	
    }

    // Given another grid, merge the two, return result. ('merge' is
    // kind of like an OR operation, but it always takes the lowest
    // value for a grid space. I.e. if one grid says a 'platoon' can
    // fit in a space, and the other grid says a 'company' can fit in
    // the space, then we set the value for that space to 'platoon'.)
    public IntGrid merge(IntGrid grid) {
	IntGrid newGrid = new IntGrid(this);
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(newGrid.values[loopY][loopX] > grid.values[loopY][loopX]) {
		    newGrid.values[loopY][loopX] = grid.values[loopY][loopX];
		    if(newGrid.values[loopY][loopX] > newGrid.highest)
			newGrid.highest = newGrid.values[loopY][loopX];
		    if(newGrid.values[loopY][loopX] < newGrid.lowest)
			newGrid.lowest = newGrid.values[loopY][loopX];
		}
	    }
	}	
	return newGrid;
    }

    // Just like merge, but we're merging INTO the values in 'this'
    // instead of into a new grid that we'll return.
    public void copyInto(IntGrid grid) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(grid.values[loopY][loopX] > values[loopY][loopX]) {
		    values[loopY][loopX] = grid.values[loopY][loopX];
		    if(values[loopY][loopX] > highest)
			highest = values[loopY][loopX];
		    if(values[loopY][loopX] < lowest)
			lowest = values[loopY][loopX];
		}
	    }
	}	
	changed();
    }

    // Checks for various soil types that we've decided are
    // impassable, sets those values to IMPASSABLE.  
    // 
    // @todo Need to make this a version that takes some kinda
    // parameters as to whether shallow water is an obstacle, and
    // whether trees are obstacles.
    public void mergeWaterNogoSoilType(IntGrid soilGrid, IntGrid result) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(SoilTypes.DEEP_WATER == soilGrid.values[loopY][loopX])
		    result.values[loopY][loopX] = IMPASSABLE;
 		else if(SoilTypes.SHALLOW_WATER == soilGrid.values[loopY][loopX])
 		    result.values[loopY][loopX] = IMPASSABLE;
// 		else if(SoilTypes.CANOPY_FOREST == soilGrid.values[loopY][loopX])
// 		    result.values[loopY][loopX] = IMPASSABLE;
		else if(SoilTypes.SWAMP == soilGrid.values[loopY][loopX])
		    result.values[loopY][loopX] = IMPASSABLE;
		else if(SoilTypes.NO_GO == soilGrid.values[loopY][loopX])
		    result.values[loopY][loopX] = IMPASSABLE;
		else
		    result.values[loopY][loopX] = values[loopY][loopX];
	    }
	}
    }

    public int removeRegion(int unitSize, int startgridx, int startgridy, IntGrid destGrid) {
	ArrayList points = new ArrayList();
	points.add(new Point(startgridx, startgridy));
	destGrid.values[startgridy][startgridx] = values[startgridy][startgridx];
	values[startgridy][startgridx] = PASSABLE;

	animationCounter = 0;
	int curx = 0;
	int cury = 0;
	int loopi = 0;
	while(loopi < points.size()) {
	    Point curp = (Point)points.get(loopi);
	    curx = curp.x;
	    cury = curp.y;

	    if(animated) animate(1);
		
	    if(((cury-1) >= 0) && (values[cury-1][curx] < unitSize)) {
		points.add(new Point( curx,cury-1));
		destGrid.values[cury-1][curx] = values[cury-1][curx];
		values[cury-1][curx] = PASSABLE;
	    }
	    if(((curx+1) < width) && (values[cury][curx+1] < unitSize)) {
		points.add(new Point( curx+1,cury));
		destGrid.values[cury][curx+1] = values[cury][curx+1];
		values[cury][curx+1] = PASSABLE;
	    }
	    if(((cury+1) < height) && (values[cury+1][curx] < unitSize)) {
		points.add(new Point( curx,cury+1));
		destGrid.values[cury+1][curx] = values[cury+1][curx];
		values[cury+1][curx] = PASSABLE;
	    }
	    if(((curx-1) >= 0) && (values[cury][curx-1] < unitSize)) {
		points.add(new Point( curx-1,cury));
		destGrid.values[cury][curx-1] = values[cury][curx-1];
		values[cury][curx-1] = PASSABLE;
	    }
	    if(((cury-1) >= 0) && ((curx+1) < width) && (values[cury-1][curx+1] < unitSize)) {
		points.add(new Point( curx+1,cury-1));
		destGrid.values[cury-1][curx+1] = values[cury-1][curx+1];
		values[cury-1][curx+1] = PASSABLE;
	    }
	    if(((cury+1) < height) && ((curx+1) < width) && (values[cury+1][curx+1] < unitSize)) {
		points.add(new Point( curx+1,cury+1));
		destGrid.values[cury+1][curx+1] = values[cury+1][curx+1];
		values[cury+1][curx+1] = PASSABLE;
	    }
	    if(((cury+1) < height) && ((curx-1) >= 0) && (values[cury+1][curx-1] < unitSize)) {
		points.add(new Point( curx-1,cury+1));
		destGrid.values[cury+1][curx-1] = values[cury+1][curx-1];
		values[cury+1][curx-1] = PASSABLE;
	    }
	    if(((cury-1) >= 0) && ((curx-1) >= 0) && (values[cury-1][curx-1] < unitSize)) {
		points.add(new Point( curx-1,cury-1));
		destGrid.values[cury-1][curx-1] = values[cury-1][curx-1];
		values[cury-1][curx-1] = PASSABLE;
	    }
	    loopi++;
	}
	changed();
	return points.size();
    }

    public synchronized void drawArrayListOfPoints(int drawValue, ArrayList gridPoints) {
	Point point = null;
	for(int loopi = 0; loopi < gridPoints.size(); loopi++) {
	    point = (Point) gridPoints.get(loopi);
	    values[point.y][point.x] = drawValue;
	    if(animated) animate(1);
	}
	changed();
    }
    
    public Polygon makePoly(ArrayList gridPoints) {
	Point p = null;
	Polygon poly = new Polygon();
	for(int loopj = 0; loopj < gridPoints.size(); loopj++) {
	    p = (Point)gridPoints.get(loopj);
	    poly.addPoint((int)p.getX(), (int)p.getY());
	}
	p = (Point)gridPoints.get(0);
	poly.addPoint((int)p.getX(), (int)p.getY());
	return poly;
    }

    public int areaContainedByPoly(Polygon gridPoly) {
	int totalArea = 0;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(gridPoly.contains(loopX, loopY))
		    totalArea += (gridCellSize * gridCellSize);
	    }
	}
	return totalArea;
    }

    public int areaContainedByArrayListOfPoints(ArrayList gridPoints) {
	Polygon poly = makePoly(gridPoints);
	return areaContainedByPoly(poly);
    }

    public void drawLine(int drawValue, int gridx, int gridy, int gridx2, int gridy2) {
	double distx = gridx2 - gridx;
	double disty = gridy2 - gridy;
	for(double loopd = 0; loopd < 1.0; loopd += .001) {
	    values[(int)(gridy+(loopd * disty))][(int)(gridx+ (loopd * distx))] = drawValue;
	}
    }

    public synchronized void drawLinesArrayListOfPoints(int drawValue, ArrayList gridPoints) {
	Point point = null;
	Point point2 = null;
	for(int loopi = 0; loopi < gridPoints.size()-1; loopi++) {
	    point = (Point) gridPoints.get(loopi);
	    point2 =(Point) gridPoints.get(loopi+1);
	    drawLine(drawValue, point.x, point.y, point2.x, point2.y);
	    if(animated) animate(1);
	}
	point = (Point)gridPoints.get(gridPoints.size() - 1);
	point2 = (Point)gridPoints.get(0);
	drawLine(drawValue, point.x, point.y, point2.x, point2.y);
	changed();
    }

    public synchronized void drawLinesListOfArrayListOfPoints(int drawValue, java.util.List list) {
	Iterator iter = list.iterator();
	while(iter.hasNext()) {
	    ArrayList poly = (ArrayList)iter.next();
	    drawLinesArrayListOfPoints(drawValue, poly);
	}
    }

    public synchronized ArrayList convertToArrayListOfPoints(double unitSize) {
	ArrayList returnList = new ArrayList();
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(values[loopY][loopX] < unitSize/gridCellSize) {
		    returnList.add(new Point(loopX, loopY));
		}
	    }
	}
	return returnList;
    }

    public Point[] convertToArrayOfPoints(double unitSize) {
	ArrayList points = convertToArrayListOfPoints(unitSize);
	Point[] returnArray = new Point[1];
	returnArray = (Point[])points.toArray(returnArray);
	return returnArray;
    }

    public boolean removeNextRegion(int unitSize, IntGrid destGrid) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		// If we find an obstacle point in the grid
		if(values[loopY][loopX] < unitSize) {
		    int regionSize = removeRegion(unitSize, loopX, loopY, destGrid);
		    destGrid.changed();
		    changed();
		    return true;
		}
	    }
	}	
	return false;
    }

    // Returns an ArrayList of ArrayLists, each ArrayList represents
    // an outline of an obstacle polygon.
    //
    // This version of 'convert to polygons', instead of working on
    // outlines of polygons, works on regions.  Somewhat better.
    //
    // Step 1, find a region, and 'flood fill' it to find all of its
    // parts, copying into an empty IntGrid the same size as this, and
    // removing from the original grid.
    //
    // Step 2, convert the new IntGrid, with only one region, to an
    // outline.
    //
    // Step 3, convert the outline to a single polygon.
    public ArrayList convertToPolygons(double unitSizeMeters) {
	IntGrid copy = new IntGrid(this);
	int unitSizeGridCells = (int) (unitSizeMeters/gridCellSize);
	if(unitSizeGridCells <= 0) {
	    debug.warn("convertToPolygons: unitSizeMeters="+unitSizeMeters+", gridCellSize="+gridCellSize+", so looking for looking for values of < "+unitSizeGridCells+" - is this really what you expected?");
	}
	//	debug.debug("convertToPolygons: unitSizeMeters="+unitSizeMeters+", gridCellSize="+gridCellSize+", looking for values of < "+unitSizeGridCells);
	IntGrid region = new IntGrid(this);
	ArrayList points = null;
	ArrayList polygons = new ArrayList();

	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		// If we find an obstacle point in the grid
		if(copy.values[loopY][loopX] < unitSizeGridCells) {
		    //		    debug.info("Found a region at "+loopY+", "+loopX);
		    region.clear();
		    int regionSize = copy.removeRegion(unitSizeGridCells, loopX, loopY, region);
  		    IntGrid outlineRegion = region.reduceToOutlines(unitSizeGridCells, PASSABLE);
		    //		    Debug.info("After reduceToOutlines, outlineRegion.countObstacles() = "+outlineRegion.countObstacles());
		    points = outlineRegion.convertToArrayListOfPoints(unitSizeGridCells);
 		    polygons.add(points);
		    //		    debug.info("Region size was "+regionSize+" region outline obstacles = "+region.countObstacles());
		}
		else {
		    //		    values[loopY][loopX] = PASSABLE;
		}
	    }
	}	
	changed();
	//	debug.info("Done converting to polygons, found "+polygons.size()+" polygons.");
	return polygons;
    }

    public ArrayList convertToFilledPolygons(double unitSizeMeters) {
	IntGrid copy = new IntGrid(this);
	int unitSizeGridCells = (int) (unitSizeMeters/gridCellSize);
	if(unitSizeGridCells <= 0) {
	    debug.warn("convertToPolygons: unitSizeMeters="+unitSizeMeters+", gridCellSize="+gridCellSize+", so looking for looking for values of < "+unitSizeGridCells+" - is this really what you expected?");
	}
	IntGrid region = new IntGrid(this);
	ArrayList points = null;
	ArrayList polygons = new ArrayList();

	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		// If we find an obstacle point in the grid
		if(copy.values[loopY][loopX] < unitSizeGridCells) {
		    region.clear();
		    int regionSize = copy.removeRegion(unitSizeGridCells, loopX, loopY, region);
		    points = region.convertToArrayListOfPoints(unitSizeGridCells);
 		    polygons.add(points);
		}
		else {
		    //		    values[loopY][loopX] = PASSABLE;
		}
	    }
	}	
	changed();
	//	debug.info("Done converting to polygons, found "+polygons.size()+" polygons.");
	return polygons;
    }


    // This function is to use to test against the faster versions.
    // Speed is sacrificed in the interest of clarity and correctness.
    public void computeDistanceGridCorrect(IntGrid result) {
	int maxDist = (int)Math.sqrt(((height * height) + (width * width)));
	
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		result.values[loopY][loopX] = maxDist;
	    }
	}

	boolean obstaclesExist = false;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == values[loopY][loopX]) {
		    obstaclesExist = true;
		    break;
		}
	    }
	}

	if(!obstaclesExist) {
	    debug.debug("There are no obstacles to compute distance grid with, returning an array of all cells set to maxDist (maxDist="+maxDist+" is the distance between the top left corner and the bottom right corner.)");
	    return;
	}

	int[][] distanceCache = new int[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		distanceCache[loopY][loopX] = -1;
	    }
	}	

	// For each individual spot in the grid
	boolean spotDone = false;
	int nextDebugPrint = height/10;
	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY > nextDebugPrint) {
		debug.info("processing y="+loopY+" ("+(((double)(loopY - 1)/(double)height)*100)+" percent)");
		nextDebugPrint += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		
		if(IMPASSABLE == values[loopY][loopX]) {
		    result.values[loopY][loopX] = 0;
		    continue;
		}
 		int minDist = maxDist;
 		for(int loopY2 = 0; loopY2 < height; loopY2++) {
 		    for(int loopX2 = 0; loopX2 < width; loopX2++) {
 			if(IMPASSABLE == values[loopY2][loopX2]) {
 			    int distance = 0;
 			    int xdist = loopX2 - loopX;
 			    int ydist = loopY2 - loopY;
			    // for the cache, since xdist and ydist
			    // will both be squared, sign doesn't
			    // matter.
			    if(xdist < 0)
				xdist = -1 * xdist;
			    if(ydist < 0)
				ydist = -1 * ydist;
			    // To improve cache performance - take
			    // advantage of symmetry.
			    if(xdist > ydist) {
				int temp = ydist;
				ydist = xdist;
				xdist = temp;
			    }
			    if(-1 == distanceCache[ydist][xdist]) {
				distanceCache[ydist][xdist] = (int)Math.sqrt((xdist * xdist)+(ydist * ydist));
			    }
			    distance = distanceCache[ydist][xdist];

 			    if(distance < minDist) {
 				minDist = distance;
 			    }
 			}
 		    }
 		} // while(!spotDone)

 		result.values[loopY][loopX] = minDist;
	    }
	}
	debug.info("Done generating distance grid.");
    }

    private int getDistance(int[][] distanceCache, int xdist, int ydist) {
	int distance =  0;
	// for the cache, since xdist and ydist
	// will both be squared, sign doesn't
	// matter.
	if(xdist < 0)
	    xdist = -1 * xdist;
	if(ydist < 0)
	    ydist = -1 * ydist;
	// To improve cache performance - take
	// advantage of symmetry.
	if(xdist > ydist) {
	    int temp = ydist;
	    ydist = xdist;
	    xdist = temp;
	}
	if(-1 == distanceCache[ydist][xdist]) {
	    distanceCache[ydist][xdist] = (int)Math.sqrt((xdist * xdist)+(ydist * ydist));
	}
	distance = distanceCache[ydist][xdist];
	return distance;
    }

    public void computeDistanceGrid(IntGrid result) {
	long startTime = 0;
	long endTime = 0;
	startTime = System.currentTimeMillis();
	int maxDist = (int)Math.sqrt(((height * height) + (width * width)));
	
	debug.debug("computeDistanceGrid:");
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		result.values[loopY][loopX] = maxDist;
	    }
	}

	debug.debug("computeDistanceGrid:checking for obstacles");
	boolean obstaclesExist = false;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == values[loopY][loopX]) {
		    obstaclesExist = true;
		    break;
		}
	    }
	}
	debug.debug("computeDistanceGrid: checking for obstacles");
	if(!obstaclesExist) {
	    debug.debug("There are no obstacles to compute distance grid with, returning an array of all cells set to maxDist (maxDist="+maxDist+" is the distance between the top left corner and the bottom right corner.)");
	    return;
	}

	debug.debug("computeDistanceGrid: big loop");
	// For each individual spot in the grid
	boolean spotDone = false;
	int counter = height/10;
	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY > counter) {
		debug.info("computeDistanceGrid: processing y="+loopY+" ("+(((double)(loopY - 1)/(double)height)*100)+" percent) elapsed="+(System.currentTimeMillis() - startTime));
		counter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		
		if(IMPASSABLE == values[loopY][loopX]) {
		    result.values[loopY][loopX] = 0;
		    continue;
		}

		double radius = 1.0;
 		int leftX = loopX;
 		int rightX = loopX;
 		int topY = loopY;
 		int bottomY = loopY;
 		spotDone = false; 
 		int minDist = maxDist;
 		while(radius < minDist) {
		    if(radius > 200.0) {
			radius += 200.0;
			leftX -= 200;
			rightX += 200;
			topY -= 200;
			bottomY += 200;
		    }
		    else if(radius > 100.0) {
			radius += 100.0;
			leftX -= 100;
			rightX += 100;
			topY -= 100;
			bottomY += 100;
		    }
		    else if(radius > 50.0) {
			radius += 50.0;
			leftX -= 50;
			rightX += 50;
			topY -= 50;
			bottomY += 50;
		    }
		    else if(radius > 10.0) {
			radius += 10.0;
			leftX -= 10;
			rightX += 10;
			topY -= 10;
			bottomY += 10;
		    }
		    else {
			radius += 1.0;
			leftX -= 1;
			rightX += 1;
			topY -= 1;
			bottomY += 1;
		    }
 		    if((leftX < 0) 
 		       && (rightX >= width)
 		       && (topY < 0)
 		       && (bottomY >= height)) {
			break;
 		    }

 		    if(leftX < 0) 
 			leftX = 0;
 		    if(rightX >= width)
 			rightX = width - 1;
 		    if(topY < 0)
 			topY = 0;
 		    if(bottomY >= height)
 			bottomY = height - 1;

		    double radiusSquared = radius * radius;
		    
 		    for(int loopY2 = topY; loopY2 <= bottomY; loopY2++) {
 			for(int loopX2 = leftX; loopX2 <= rightX; loopX2++) {
 			    if(IMPASSABLE == values[loopY2][loopX2]) {
				int xdist = loopX2 - loopX;
				int ydist = loopY2 - loopY;
				int distSquared = (xdist *xdist) + (ydist * ydist);
				if(distSquared > radiusSquared)
				    continue;
				int distance = (int)Math.sqrt((xdist * xdist)+(ydist * ydist));
  				if(distance < minDist) {
 				    minDist = distance;
  				}
 			    }
 			}
 		    }
 		}
 		result.values[loopY][loopX] = minDist;
	    }
	}
	endTime = System.currentTimeMillis();
	debug.info("Done generating distance grid, elapsed="+(endTime - startTime));
    }

    public void computeDistanceGrid2(IntGrid result) {
	long startTime = 0;
	long endTime = 0;
	startTime = System.currentTimeMillis();
	int maxDist = (int)Math.sqrt(((height * height) + (width * width)));
	
	debug.debug("computeDistanceGrid2:");
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		result.values[loopY][loopX] = maxDist;
	    }
	}
	
	boolean[][] flags = new boolean[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		flags[loopY][loopX] = false;
	    }
	}

	debug.debug("computeDistanceGrid2:checking for obstacles");
	boolean obstaclesExist = false;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == values[loopY][loopX]) {
		    obstaclesExist = true;
		    break;
		}
	    }
	}
	if(!obstaclesExist) {
	    debug.debug("There are no obstacles to compute distance grid with, returning an array of all cells set to maxDist (maxDist="+maxDist+" is the distance between the top left corner and the bottom right corner.)");
	    return;
	}
	debug.debug("computeDistanceGrid2: done checking for obstacles");

	debug.debug("computeDistanceGrid2:initializing distance");
	int[][] distanceCache = new int[height][width];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		distanceCache[loopY][loopX] = -1;
	    }
	}	

	debug.debug("computeDistanceGrid2: big loop");
	// For each individual spot in the grid
	boolean spotDone = false;
	int counter = height/10;
	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY > counter) {
		debug.info("processing y="+loopY+" ("+(((double)(loopY - 1)/(double)height)*100)+" percent)");
		counter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		
		if(IMPASSABLE == values[loopY][loopX]) {
		    result.values[loopY][loopX] = 0;
		    continue;
		}

		for(int loopFlagY = 0; loopFlagY < height; loopFlagY++) {
		    for(int loopFlagX = 0; loopFlagX < width; loopFlagX++) {
			flags[loopFlagY][loopFlagX] = false;
		    }
		}
		double radius = 1.0;
 		int leftX = loopX;
 		int rightX = loopX;
 		int topY = loopY;
 		int bottomY = loopY;
 		spotDone = false; 
 		int minDist = maxDist;
 		while(radius < minDist) {
		    if(radius > 200.0) {
			radius += 200.0;
			leftX -= 200;
			rightX += 200;
			topY -= 200;
			bottomY += 200;
		    }
		    else if(radius > 100.0) {
			radius += 100.0;
			leftX -= 100;
			rightX += 100;
			topY -= 100;
			bottomY += 100;
		    }
		    else if(radius > 50.0) {
			radius += 50.0;
			leftX -= 50;
			rightX += 50;
			topY -= 50;
			bottomY += 50;
		    }
		    else if(radius > 10.0) {
			radius += 10.0;
			leftX -= 10;
			rightX += 10;
			topY -= 10;
			bottomY += 10;
		    }
		    else {
			radius += 1.0;
			leftX -= 1;
			rightX += 1;
			topY -= 1;
			bottomY += 1;
		    }
 		    if((leftX < 0) 
 		       && (rightX >= width)
 		       && (topY < 0)
 		       && (bottomY >= height)) {
			break;
 		    }

 		    if(leftX < 0) 
 			leftX = 0;
 		    if(rightX >= width)
 			rightX = width - 1;
 		    if(topY < 0)
 			topY = 0;
 		    if(bottomY >= height)
 			bottomY = height - 1;

		    double radiusSquared = radius * radius;
		    
 		    for(int loopY2 = topY; loopY2 <= bottomY; loopY2++) {
 			for(int loopX2 = leftX; loopX2 <= rightX; loopX2++) {
 			    if(IMPASSABLE == values[loopY2][loopX2]) {
				if(flags[loopY2][loopX2])
				    continue;
				flags[loopY2][loopX2] = true;
				int xdist = loopX2 - loopX;
				int ydist = loopY2 - loopY;
				int distSquared = (xdist *xdist) + (ydist * ydist);
				if(distSquared > radiusSquared)
				    continue;
				int distance = (int)Math.sqrt((xdist * xdist)+(ydist * ydist));
  				if(distance < minDist) {
 				    minDist = distance;
  				}
 			    }
 			}
 		    }
 		}
 		result.values[loopY][loopX] = minDist;
	    }
	}
	endTime = System.currentTimeMillis();
	debug.info("Done generating distance grid, elapsed="+(endTime - startTime));
    }

    public void computeDistanceGrid3(IntGrid result) {
	int maxDist = (int)Math.sqrt(((height * height) + (width * width)));
	debug.debug("computeDistanceGrid3:");
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		result.values[loopY][loopX] = maxDist;
	    }
	}
	debug.debug("computeDistanceGrid3:checking for obstacles");
	boolean obstaclesExist = false;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == values[loopY][loopX]) {
		    obstaclesExist = true;
		    break;
		}
	    }
	}
	if(!obstaclesExist) {
	    debug.debug("computeDistanceGrid3: There are no obstacles to compute distance grid with, returning an array of all cells set to maxDist (maxDist="+maxDist+" is the distance between the top left corner and the bottom right corner.)");
	    return;
	}
	debug.debug("computeDistanceGrid3: done checking for obstacles");

	long startTime = 0;
	long endTime = 0;
	startTime = System.currentTimeMillis();

	Map distMap = new HashMap(height * width);
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(loopY > loopX) 
		    distMap.put((loopY+"."+loopX).intern(), new Distance(loopX, loopY));
		else
		    distMap.put((loopX+"."+loopY).intern(), new Distance(loopY, loopX));
	    }
	}
	ArrayList distList = new ArrayList(distMap.values());
	Collections.sort(distList);
	int[] distx = new int[distList.size()];
	int[] disty = new int[distList.size()];
	int[] distd = new int[distList.size()];
	Distance distObj = null;
	for(int loopi = 0; loopi < distList.size(); loopi++) {
	    distObj = (Distance)distList.get(loopi);
	    debug.debug("computeDistanceGrid3: dist "+loopi+" = "+distObj.x+", "+distObj.y+" dist="+distObj.distance);
	    distx[loopi] = distObj.x;
	    disty[loopi] = distObj.y;
	    distd[loopi] = distObj.distance;
	}
	endTime = System.currentTimeMillis();
	debug.info("computeDistanceGrid3: elapsed time to generate distance list="+(endTime - startTime));
	
	debug.debug("computeDistanceGrid3: big loop");
	// For each individual spot in the grid
	boolean spotDone = false;
	int counter = height/10;
	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY > counter) {
		debug.info("computeDistanceGrid3: processing y="+loopY+" ("+(((double)(loopY - 1)/(double)height)*100)+" percent) elapsed="+(System.currentTimeMillis() - startTime));
		counter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		
		if(IMPASSABLE == values[loopY][loopX]) {
		    result.values[loopY][loopX] = 0;
		    continue;
		}

		int x1 = 0;
		int y1 = 0;
		for(int loopi = 0; loopi < distd.length; loopi++) {
		    x1 = loopX + distx[loopi];
		    y1 = loopY + disty[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }
			
		    x1 = loopX + disty[loopi];
		    y1 = loopY + distx[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }

		    x1 = loopX - distx[loopi];
		    y1 = loopY - disty[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }
			
		    x1 = loopX - disty[loopi];
		    y1 = loopY - distx[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }
			
		    x1 = loopX - distx[loopi];
		    y1 = loopY + disty[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }
			
		    x1 = loopX - disty[loopi];
		    y1 = loopY + distx[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }
			
		    x1 = loopX + distx[loopi];
		    y1 = loopY - disty[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }
			
		    x1 = loopX + disty[loopi];
		    y1 = loopY - distx[loopi];

		    if((x1 >= 0) && (x1 < width) && (y1 >= 0) && (y1 < height)) {
			if(IMPASSABLE == values[y1][x1]) {
			    result.values[loopY][loopX] = distd[loopi];
			    break;
			}
		    }
		}
	    }
	}
	endTime = System.currentTimeMillis();
	debug.info("computeDistanceGrid3:Done generating distance grid, elapsed="+(endTime - startTime));
    }

    public void computeDistanceGrid4(IntGrid result) {
	long startTime = 0;
	long endTime = 0;
	startTime = System.currentTimeMillis();
	int maxDist = (int)Math.sqrt(((height * height) + (width * width)));
	
	debug.debug("computeDistanceGrid4:");
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		result.values[loopY][loopX] = maxDist;
	    }
	}

	debug.debug("computeDistanceGrid4:checking for obstacles");
	boolean obstaclesExist = false;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == values[loopY][loopX]) {
		    obstaclesExist = true;
		    break;
		}
	    }
	}
	if(!obstaclesExist) {
	    debug.debug("There are no obstacles to compute distance grid with, returning an array of all cells set to maxDist (maxDist="+maxDist+" is the distance between the top left corner and the bottom right corner.)");
	    return;
	}

	int bucketSize = 20;
	int flagHeight = (height/bucketSize)+1;
	int flagWidth = (width/bucketSize)+1;
	boolean[][] flags = new boolean[flagHeight][flagWidth];
	for(int loopY = 0; loopY < flagHeight; loopY++) {
	    for(int loopX = 0; loopX < flagWidth; loopX++) {
		flags[loopY][loopX] = false;
	    }
	}
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == values[loopY][loopX]) {
		    flags[loopY/bucketSize][loopX/bucketSize] = true;
		}
	    }
	}


	debug.debug("computeDistanceGrid4: big loop");
	// For each individual spot in the grid
	boolean spotDone = false;
	int counter = height/10;
	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY > counter) {
		debug.info("computeDistanceGrid4: processing y="+loopY+" ("+(((double)(loopY - 1)/(double)height)*100)+" percent) elapsed="+(System.currentTimeMillis() - startTime));
		counter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		
		if(IMPASSABLE == values[loopY][loopX]) {
		    result.values[loopY][loopX] = 0;
		    continue;
		}

		double radius = 1.0;
 		int leftX = loopX - 1;
 		int rightX = loopX + 1;
 		int topY = loopY - 1;
 		int bottomY = loopY + 1;
 		spotDone = false; 
 		int minDist = maxDist;

 		while((radius < minDist) && radius < bucketSize) {
		    radius += 1.0;
		    leftX -= 1;
		    rightX += 1;
		    topY -= 1;
		    bottomY += 1;

 		    if(leftX < 0) 
 			leftX = 0;
 		    if(rightX >= width)
 			rightX = width - 1;
 		    if(topY < 0)
 			topY = 0;
 		    if(bottomY >= height)
 			bottomY = height - 1;

		    double radiusSquared = radius * radius;
		    
 		    for(int loopY2 = topY; loopY2 <= bottomY; loopY2++) {
 			for(int loopX2 = leftX; loopX2 <= rightX; loopX2++) {
 			    if(IMPASSABLE == values[loopY2][loopX2]) {
				int xdist = loopX2 - loopX;
				int ydist = loopY2 - loopY;
				int distSquared = (xdist *xdist) + (ydist * ydist);
				if(distSquared > radiusSquared)
				    continue;
				int distance = (int)Math.sqrt(distSquared);
  				if(distance < minDist) {
 				    minDist = distance;
  				}
 			    }
 			}
 		    }
 		}


		while(radius < minDist) {
		    if(radius > bucketSize*10) {
			radius += bucketSize*10;
			leftX -= bucketSize*10;
			rightX += bucketSize*10;
			topY -= bucketSize*10;
			bottomY += bucketSize*10;
		    }
		    else if(radius > bucketSize*5) {
			radius += bucketSize*5;
			leftX -= bucketSize*5;
			rightX += bucketSize*5;
			topY -= bucketSize*5;
			bottomY += bucketSize*5;
		    }
		    else {
			radius += bucketSize;
			leftX -= bucketSize;
			rightX += bucketSize;
			topY -= bucketSize;
			bottomY += bucketSize;
		    }
 		    if((leftX < 0) 
 		       && (rightX >= width)
 		       && (topY < 0)
 		       && (bottomY >= height)) {
			break;
 		    }

 		    if(leftX < 0) 
 			leftX = 0;
 		    if(rightX >= width)
 			rightX = width - 1;
 		    if(topY < 0)
 			topY = 0;
 		    if(bottomY >= height)
 			bottomY = height - 1;

		    double radiusSquared = radius * radius;
		    
 		    for(int bucketY = topY; bucketY <= bottomY; bucketY += bucketSize) {
 			for(int bucketX = leftX; bucketX <= rightX; bucketX += bucketSize) {
			    if(!flags[bucketY/bucketSize][bucketX/bucketSize])
				continue;
			    
			    for(int loopY2 = bucketY; loopY2 <= bucketY + bucketSize; loopY2++) {
				for(int loopX2 = bucketX; loopX2 <= bucketX + bucketSize; loopX2++) {
				    if((loopX2 < 0) || (loopY2 < 0) || (loopX2 >= width) || (loopY2 >= height)) 
					continue;
				    				    
				    if(IMPASSABLE == values[loopY2][loopX2]) {
					int xdist = loopX2 - loopX;
					int ydist = loopY2 - loopY;
					int distSquared = (xdist *xdist) + (ydist * ydist);
					if(distSquared > radiusSquared)
					    continue;
					int distance = (int)Math.sqrt((xdist * xdist)+(ydist * ydist));
					if(distance < minDist) {
					    minDist = distance;
					}
				    }
				}
			    }
			}
 		    }
 		}
 		result.values[loopY][loopX] = minDist;
	    }
	}
	endTime = System.currentTimeMillis();
	debug.info("Done generating distance grid, elapsed="+(endTime - startTime));
    }

    // This is currently the best method to use to draw an image.  The
    // code that calls this looks like;
    //
    // 	    ImageProducer imageProducer = grid.drawImageProducer(img, colorRGBs);
    // 	    if(imageProducer != null) {
    // 		Debug.info("IntPanel:Creating image...");
    // 		Image tempImage = createImage(imageProducer);
    // 		Graphics ig = img.createGraphics();
    // 		ig.drawImage(tempImage,0,0,null);
    // 		endTime = System.currentTimeMillis();
    // 		//		Debug.info("Time to create image="+(endTime - startTime));
    // 	    }
    // 	    else {
    // 		Debug.info("IntGrid.drawImageProducer returned null, not drawing image.  Maybe the IntGrid instance doesn't have validData set to true?");
    // 	    }
    //
    // Unfortunately I could not just place the above code into
    // IntGrid, because you need 'createImage(ImageProducer)' which is
    // only available on subclasses of Component, such as JPanel.
    //
    // I'll admit this is kinda gross, I probably should go through
    // the pain and suffering of learning how to use Rasters in
    // BufferedImages.  But I don't have time for that right now, and
    // this method is fast enough for our purposes.  (It is a buttload
    // faster than BufferedImage.setRGB, even the version of setRGB
    // that takes arrays, though that may be due to ColorModel
    // issues.)
    public synchronized MemoryImageSource drawImageProducer(BufferedImage img, int[] colorRGBs ) {
	if(!validData) {
	    debug.error("drawImageProducer: Trying to draw non valid data, not drawing anything.");
	    return null;
	}

	int paintingCounter = 0;
	int value = 0;
	int rgbValues[] = new int[height * width];

	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY == paintingCounter) {
		paintingCounter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		value = values[loopY][loopX];
		if(value < 0) 
		    value = 0;
		else if (value >= colorRGBs.length) 
		    value = colorRGBs.length -1;
		rgbValues[(loopY * width) + loopX] = colorRGBs[value];
	    }
	}
	MemoryImageSource imageSource =  new MemoryImageSource(width, height, rgbValues, 0, width);
	return imageSource;
    }

    public synchronized Image drawImage(int[] colorRGBs ) {
	//	if(!validData)
	//	    return null;
	if(null == colorRGBs) {
	    debug.error("drawImage can't draw the grid with a null color RGB array.");
	    return null;
	}

	int paintingCounter = 0;
	int value = 0;
	int rgbValues[] = new int[height * width];

	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY == paintingCounter) {
		paintingCounter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {
		value = values[loopY][loopX];
		if(value < 0) 
		    value = 0;
		else if (value >= colorRGBs.length) 
		    value = colorRGBs.length -1;
		rgbValues[(loopY * width) + loopX] = colorRGBs[value];
	    }
	}
	MemoryImageSource foo =  new MemoryImageSource(width, height, rgbValues, 0, width);
	Canvas canvas = new Canvas();
	Image tempImage = canvas.createImage(foo);
	
	return tempImage; 
    }

    public synchronized Image drawImage(int[] colorRGBs, ViewPort viewPort) {
	//	if(!validData)
	//	    return null;
	if(null == colorRGBs) {
	    debug.error("drawImage can't draw the grid with a null color RGB array.");
	    return null;
	}

	// @todo rename these destHeight, destWidth
	int dHeight = (int)viewPort.getDestHeight();
	int dWidth = (int)viewPort.getDestWidth();
	int rgbValues[] = new int[dHeight * dWidth];

	int localSrcX = 0;
	int localSrcY = 0;
	int paintingCounter = 0;
	int value = 0;

	int backgroundRGB = SoilTypes.DEFAULT_SOIL_COLOR.getRGB();
	for(int loopY = 0; loopY < dHeight; loopY++) {
	    if(loopY == paintingCounter) {
		//		debug.debug("Redrawing is "+(loopY*100)/height+"% complete."); 
		paintingCounter += dHeight/10;
	    }
	    for(int loopX = 0; loopX < dWidth; loopX++) {
		localSrcX = (int)viewPort.destToSourceX(loopX);
		localSrcY = (int)viewPort.destToSourceY(loopY);
		int gridX = toGridX(localSrcX);
		int gridY = toGridY(localSrcY);

		if((gridX < 0) || (gridX >= width) || (gridY < 0) || (gridY >= height))
		    rgbValues[(loopY * dWidth) + loopX] = backgroundRGB;
		else {
		    value = values[gridY][gridX];
		    if(value < 0) 
			value = 0;
		    else if (value >= colorRGBs.length) 
			value = colorRGBs.length -1;
		    rgbValues[(loopY * dWidth) + loopX] = colorRGBs[value];
		}
	    }
	}
	MemoryImageSource imageSource =  new MemoryImageSource(dWidth, dHeight, rgbValues, 0, dWidth);

	if(null == imageSource) {
	    debug.error("Couldn't create image producer.");
	}

	Canvas canvas = new Canvas();
	Image tempImage = canvas.createImage(imageSource);
	return tempImage; 
    }

    // Draw the grid as a soil image - i.e. use grid values as indexes
    // into a color array.  Really this is misnamed and is more
    // general than that.
    public synchronized Image drawSoilImage(int[] colorRGBs) {
	//	if(!validData)
	//	    return null;

	int paintingCounter = 0;
	int value = 0;
	int rgbValues[] = new int[height * width];

	for(int loopY = 0; loopY < height; loopY++) {
	    if(loopY == paintingCounter) {
		//		debug.debug("Redrawing is "+(loopY*100)/height+"% complete."); 
		paintingCounter += height/10;
	    }
	    for(int loopX = 0; loopX < width; loopX++) {

		value = values[loopY][loopX];
 		if(value < 0) 
 		    value = 0;
 		else if (value >= colorRGBs.length) 
 		    value = colorRGBs.length -1;
 		rgbValues[(loopY * width) + loopX] = colorRGBs[value];
	    }
	}
	MemoryImageSource imageSource =  new MemoryImageSource(width, height, rgbValues, 0, width);
	
	if(null == imageSource) {
	    debug.error("Couldn't create image producer.");
	}

	Canvas canvas = new Canvas();
	Image tempImage = canvas.createImage(imageSource);
	return tempImage;
    }

    // Draw the grid as a soil image - i.e. use grid values as indexes
    // into a color array.  Really this is misnamed and is more
    // general than that.
    public synchronized Image drawSoilImage(int[] colorRGBs, ViewPort viewPort) {
	//	if(!validData)
	//	    return null;
	if(null == colorRGBs) {
	    debug.error("drawImage can't draw the grid with a null color RGB array.");
	    return null;
	}

	// @todo rename these destHeight, destWidth
	int dHeight = (int)viewPort.getDestHeight();
	int dWidth = (int)viewPort.getDestWidth();
	int rgbValues[] = new int[dHeight * dWidth];

	int localSrcX = 0;
	int localSrcY = 0;
	int paintingCounter = 0;
	int value = 0;

	int backgroundRGB = SoilTypes.DEFAULT_SOIL_COLOR.getRGB();
	for(int loopY = 0; loopY < dHeight; loopY++) {
	    if(loopY == paintingCounter) {
		//		debug.debug("Redrawing is "+(loopY*100)/height+"% complete."); 
		paintingCounter += dHeight/10;
	    }
	    for(int loopX = 0; loopX < dWidth; loopX++) {
		localSrcX = (int)viewPort.destToSourceX(loopX);
		localSrcY = (int)viewPort.destToSourceY(loopY);
		int gridX = toGridX(localSrcX);
		int gridY = toGridY(localSrcY);

		if((gridX < 0) || (gridX >= width) || (gridY < 0) || (gridY >= height))
		    rgbValues[(loopY * dWidth) + loopX] = backgroundRGB;
		else {
		    value = values[gridY][gridX];
		    if(value < 0) 
			value = 0;
		    else if (value >= colorRGBs.length) 
			value = colorRGBs.length -1;
		    rgbValues[(loopY * dWidth) + loopX] = colorRGBs[value];
		}
	    }
	}
	MemoryImageSource imageSource =  new MemoryImageSource(dWidth, dHeight, rgbValues, 0, dWidth);

	if(null == imageSource) {
	    debug.error("Couldn't create image producer.");
	}

	Canvas canvas = new Canvas();
	Image tempImage = canvas.createImage(imageSource);
	return tempImage;
    }

    // ----------------------------------------------------------------------
    //
    // Obsolete or experimental code. 
    //
    // ----------------------------------------------------------------------

    // Trying to fix roads... 
    public void extendRoads(int distance) {
	int x1;
	int y1;
	int x2;
	int y2;
	int x1a;
	int y1a;
	int x2a;
	int y2a;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {

		x1 = loopX - 1;
		y1 = loopY;
		x2 = loopX + 1;
		y2 = loopY;

		if((SoilTypes.ASPHALT == getValue(x1,y1)) && (SoilTypes.ASPHALT == getValue(x2,y2)))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX;
		y1 = loopY - 1;
		x2 = loopX;
		y2 = loopY + 1;
		if((SoilTypes.ASPHALT == getValue(x1,y1)) && (SoilTypes.ASPHALT == getValue(x2,y2)))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX - 1;
		y1 = loopY - 1;
		x2 = loopX + 1;
		y2 = loopY + 1;
		if((SoilTypes.ASPHALT == getValue(x1,y1)) && (SoilTypes.ASPHALT == getValue(x2,y2)))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX + 1;
		y1 = loopY - 1;
		x2 = loopX - 1;
		y2 = loopY + 1;
		if((SoilTypes.ASPHALT == getValue(x1,y1)) && (SoilTypes.ASPHALT == getValue(x2,y2)))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX - 1;
		y1 = loopY;
		x2 = loopX + 1;
		y2 = loopY;
		x1a = loopX - 2;
		y1a = loopY;
		x2a = loopX + 2;
		y2a = loopY;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX;
		y1 = loopY - 1;
		x2 = loopX;
		y2 = loopY + 1;
		x1a = loopX;
		y1a = loopY - 2;
		x2a = loopX;
		y2a = loopY + 2;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX - 1;
		y1 = loopY - 1;
		x2 = loopX + 1;
		y2 = loopY + 1;
		x1a = loopX - 2;
		y1a = loopY - 2;
		x2a = loopX + 2;
		y2a = loopY + 2;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX + 1;
		y1 = loopY - 1;
		x2 = loopX - 1;
		y2 = loopY + 1;
		x1a = loopX + 2;
		y1a = loopY - 2;
		x2a = loopX - 2;
		y2a = loopY + 2;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX - 2;
		y1 = loopY - 1;
		x1a = loopX - 1;
		y1a = loopY - 1;
		x2 = loopX + 1;
		y2 = loopY;
		x2a = loopX +2;
		y2a = loopY + 1;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);
		
		x1 = loopX - 2;
		y1 = loopY + 1;
		x1a = loopX - 1;
		y1a = loopY;
		x2 = loopX + 1;
		y2 = loopY - 1;
		x2a = loopX + 2;
		y2a = loopY - 1;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX - 2;
		y1 = loopY + 1;
		x1a = loopX - 1;
		y1a = loopY + 1;
		x2 = loopX + 1;
		y2 = loopY;
		x2a = loopX +2;
		y2a = loopY - 1;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);
		
		x1 = loopX - 1;
		y1 = loopY - 1;
		x1a = loopX - 1;
		y1a = loopY - 2;
		x2 = loopX;
		y2 = loopY + 1;
		x2a = loopX + 1;
		y2a = loopY + 2;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX + 1;
		y1 = loopY - 1;
		x1a = loopX + 1;
		y1a = loopY - 2;
		x2 = loopX;
		y2 = loopY + 1;
		x2a = loopX - 1;
		y2a = loopY + 2;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX - 1;
		y1 = loopY + 1;
		x1a = loopX - 1;
		y1a = loopY + 2;
		x2 = loopX;
		y2 = loopY - 1;
		x2a = loopX + 1;
		y2a = loopY - 2;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);

		x1 = loopX + 1;
		y1 = loopY + 1;
		x1a = loopX + 1;
		y1a = loopY + 2;
		x2 = loopX;
		y2 = loopY - 1;
		x2a = loopX - 1;
		y2a = loopY - 2;
		if(((SoilTypes.ASPHALT == getValue(x1,y1)) || (SoilTypes.ASPHALT == getValue(x1a,y1a)))
		   && ((SoilTypes.ASPHALT == getValue(x2,y2)) || (SoilTypes.ASPHALT == getValue(x2a,y2a))))
		    setValue(loopX, loopY, SoilTypes.ASPHALT);


	    }
	}
    }

    // OLD - doesn't perform as well as the new one - on the other
    // hand, the old one puts the points on the perimeter of the
    // polyon in better order.  Hopefully we'll be able to combine
    // this functionality into the new one.
    //
    // Returns an ArrayList of java.awt.Polygons representing
    // obstacles.  Should only use this after reducing a grid to
    // outlines.
    //
    // @deprecated
    public ArrayList oldConvertToPolygons(int unitSize) {
	// IntGrid copy = new IntGrid(this);
	IntGrid copy = this;
	ArrayList polygons = new ArrayList();
	ArrayList poly = null;
	
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		// If we find an obstacle point in the grid
		if(copy.values[loopY][loopX] < unitSize) {
		    int curx = loopX;
		    int cury = loopY;
		    while(true) {
			poly = new ArrayList();
			poly.add(new Point(curx, cury));
			copy.values[cury][curx] = unitSize + 5;
			if(((cury-1) >= 0) && (copy.values[cury-1][curx] < unitSize)) {
			    cury--; 
			}
			else if(((curx+1) < width) && (copy.values[cury][curx+1] < unitSize)) {
			    curx++;
			}
			else if(((cury+1) < height) && (copy.values[cury+1][curx] < unitSize)) {
			    cury++;
			}
			else if(((curx-1) >= 0) && (copy.values[cury][curx-1] < unitSize)) {
			    curx--;
			}
			else if(((cury-1) >= 0) && ((curx+1) < width) && (copy.values[cury-1][curx+1] < unitSize)) {
			    cury--; 
			    curx++;
			}
			else if(((cury+1) < height) && ((curx+1) < width) && (copy.values[cury+1][curx+1] < unitSize)) {
			    cury++;
			    curx++;
			}
			else if(((cury+1) < height) && ((curx-1) >= 0) && (copy.values[cury+1][curx-1] < unitSize)) {
			    cury++;
			    curx--;
			}
			else if(((cury-1) >= 0) && ((curx-1) >= 0) && (copy.values[cury-1][curx-1] < unitSize)) {
			    cury--;
			    curx--;
			}
			else {
			    break;
			}
		    }
		    polygons.add(poly);
		    poly = null;
		    //		    return polygons;
		}
		else {
		    //		    copy.values[loopY][loopX] = unitSize + 5;
		}
	    }
	}	

	return polygons;
    }

    // unitsFootPrint is an array of values that represents basically
    // a bitmap drawn with square approximations of "units in some
    // formation".  Each cell in the footprint should be the same size
    // as a cell in the intgrid.  The footprint array is as the
    // largest unit formation (tank company in wedge formation,
    // infantry company, etc) we want to work with.  On it are 'drawn'
    // concentric squares representing different unit footprints, with
    // the larger units having higher numbers, and the smaller units
    // having lower numbers.
    //
    // Here's an example.  Assume first, for the sake of the example
    // only (I have no idea if this is accurate), that we're dealing
    // with four unit formations, in order from largest to smallest;
    //
    // 4 tank company in wedge formation (1000m x 1000m)
    // 3 infantry company in foo formation (500m x 500m) 
    // 2 tank platoon in wedge formation (250m x 250m) 
    // 1 infantry platoon in foo formation (100m x 100m) 
    // 
    // If our grid is 50 meter squares, and a tank company in wedge
    // formation requires a total square of 1000 meters on a size,
    // 1000 over 50 means that we have an array of 20 by 20 ints.
    //
    // First, all of those ints are set to 4 to represent "Tank
    // company in wedge formation."  Next, we 'draw' over the array an
    // infantry company, by putting 3s into the array locations
    // matching a 500m x 500m square centered in the array.  Then we
    // draw 2s, then 1s.  Make sense?
    //
    // When the grid is done running the 'sums', the result will be a
    // grid of values representing the different unit sizes that can
    // fit into each cell.  If a cell has a 4 in it, then a tank
    // company can be placed with its center in that position.  The
    // different tanks of the company may be placed at nearby cells
    // with lower values, but the center is in the '4' cell.
    //
    // @deprecated
    public IntGrid robinkowskySumFootprint(int[][] unitsFootPrint) {
	IntGrid newGrid = new IntGrid(this);
	int y;
	int x;
	int unitsFootPrintHeight = unitsFootPrint.length;
	int unitsFootPrintWidth = unitsFootPrint[0].length;

	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(IMPASSABLE == values[loopY][loopX]) {
		    for(int loopi = 0; loopi < unitsFootPrintHeight; loopi++) {
			for(int loopj = 0; loopj < unitsFootPrintWidth; loopj++) {
			    y = loopY + (loopi - (unitsFootPrintHeight/2));
			    x = loopX + (loopj - (unitsFootPrintWidth/2));
			    if((y >= 0)  && (y < height)  && (x >= 0) && (x < width)) {
				// Note, we are only 'writing' the
				// footprint cell value in if the
				// current cell value is not already
				// smaller than the one we are copying
				// in, i.e. if we've already found an
				// obstacle that prevents placing an
				// infantry company there, then we
				// don't overwrite that cell with the
				// number representing an infantry
				// company, or a tank company.
				if((newGrid.values[y][x] > unitsFootPrint[loopi][loopj])) {
				    newGrid.values[y][x] = unitsFootPrint[loopi][loopj];
				}
				if(newGrid.values[y][x] > newGrid.highest)
				    newGrid.highest = newGrid.values[y][x];
				if(newGrid.values[y][x] < newGrid.lowest)
				    newGrid.lowest = newGrid.values[y][x];

			    }
			}
		    }
		}
	    }
	}
	return newGrid;
    }

    // Experimental - not quite sure what I was up to here, but I
    // think it involved finding the highest points/ridge lines in a
    // 'surface'.  I think I was feeding distance grids into this,
    // trying to basically figure out the voronoi diagram in a faster
    // manner.
    public void computeRidges(IntGrid result) {

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

    // I have no memory of why this is here.  Its building a hashmap
    // where the keys are every possible two adjacent values.
    public HashMap buildAdjacencyMap() {
	HashMap map = new HashMap();
	for(int loopY = 1; loopY < (height - 1); loopY++) {
	    for(int loopX = 1; loopX < (width - 1); loopX++) {
		map.put(values[loopY- 1][loopX - 1]+"|"+values[loopY][loopX],null);
		map.put(values[loopY - 1][loopX]+"|"+values[loopY][loopX],null);
		map.put(values[loopY - 1][loopX + 1]+"|"+values[loopY][loopX],null);
		map.put(values[loopY][loopX - 1]+"|"+values[loopY][loopX],null);
		map.put(values[loopY][loopX + 1]+"|"+values[loopY][loopX],null);
		map.put(values[loopY + 1][loopX - 1]+"|"+values[loopY][loopX],null);
		map.put(values[loopY + 1][loopX]+"|"+values[loopY][loopX],null);
		map.put(values[loopY + 1][loopX + 1]+"|"+values[loopY][loopX],null);
	    }
	}	
	return map;
    }

    // Smoothing kernel.  Experimental?  Nothing seems to use it.
    public void smooth(IntGrid result) {
	for(int loopX = 1; loopX < (width - 1); loopX++) {
	    for(int loopY = 1; loopY < (height - 1); loopY++) {
		if(0 != values[loopY][loopX]) {
		    result.values[loopY][loopX] = values[loopY][loopX];
		}
		else {
		    result.values[loopY][loopX] = 
			((values[loopY-1][loopX-1]
			+ values[loopY][loopX-1]
			+ values[loopY+1][loopX-1]
			+ values[loopY-1][loopX]
			+ values[loopY+1][loopX]
			+ values[loopY-1][loopX+1]
			+ values[loopY][loopX+1]
			 + values[loopY+1][loopX+1])/8);
		}
	    }
	}
    }

    
}
