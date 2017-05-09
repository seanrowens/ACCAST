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
// @version     $Id: Contour.java,v 1.4 2008/05/23 07:11:00 owens Exp $ 

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class Contour {
    private DebugInterface debug = null;
    private DoubleGrid elevationGrid = null;
    private ArrayList[] contourLevels = null;
    private ArrayList contourPolygons = null;
    private Image contourCacheImage = null;
    private Image scaledContourCacheImage = null;

    public Contour(DoubleGrid elevationGrid) {
	debug = new DebugFacade(this);
	this.elevationGrid = elevationGrid;
    }
    
    public void compute(double contourStepSize) {
	if(null == elevationGrid)
	    return;

	IntGrid iElevationGrid = new IntGrid(elevationGrid);
	IntGrid contourGrid = new IntGrid(iElevationGrid);
	//	IntGrid contourPolygonsGrid = new IntGrid(iElevationGrid);
	//	contourPolygonsGrid.clear();

	ArrayList contourPolygons = null;

	debug.debug("Grid file="+elevationGrid.toString());
	elevationGrid.computeHighestLowest();
	debug.debug("highest point="+elevationGrid.getHighest()+", lowest="+elevationGrid.getLowest()+", difference="+(elevationGrid.getHighest() - elevationGrid.getLowest()));
	debug.debug("Grid="+elevationGrid.toString());
	contourLevels = new ArrayList[(int)((elevationGrid.getHighest() - elevationGrid.getLowest()) / contourStepSize)+1]  ;

	int contourLevelIndex = 0;
	for(double loopd = elevationGrid.getLowest();
	    loopd < (elevationGrid.getHighest()); 
	    loopd += contourStepSize) {

	    //	    debug.info("        Doing contour for elevation="+loopd);
	    iElevationGrid.cut((int)loopd, IntGrid.PASSABLE, IntGrid.IMPASSABLE, contourGrid);
	    //	    debug.info("                obstacles="+contourGrid.countObstacles());

	    // Original/correct code - at least, correct if we want
	    // contours as lists of separate polygons, one list per
	    // elevation.  If we just want to DRAW contours (by
	    // elevation) quickly then use code below.  
	    //
	    // @todo Hmm, if I support zooming... I may need to go
	    // back to polygons, which are a bit slower...  Also I may
	    // need them in local coords.
	    //
	    // 	    ArrayList polygons = contourIntGrid.convertToPolygons(100);
	    IntGrid outlineGrid = contourGrid.reduceToOutlines();
	    //	    debug.info("                outline obstacles="+outlineGrid.countObstacles());
	    ArrayList polygon = outlineGrid.convertToArrayListOfPoints(100);
	    //	    debug.info("                converted to an arraylist of points, num points="+polygon.size());

	    ArrayList polygons = new ArrayList();
	    polygons.add(polygon);

	    contourLevels[contourLevelIndex] = polygons;
	    //	    debug.info("        Found "+ polygons.size()+" polygons, first polygon number of points="+((ArrayList)polygons.get(0)).size());
// 	    for(int loopi = 0; loopi < polygons.size(); loopi++) {
// 		ArrayList singlePolygon = (ArrayList)polygons.get(loopi);
// 		contourPolygonsGrid.drawArrayListOfPoints(0, singlePolygon);
// 	    }
	    contourLevelIndex++;
	}

    }
    
    public void draw(int contourMultiples, IntGrid outputGrid) {
	if(null == contourPolygons)
	    return;
	if(null == contourLevels) 
	    return;
	for(int loopj = 0; loopj < contourLevels.length; loopj += contourMultiples) {
	    ArrayList polygons = contourLevels[loopj];
	    for(int loopi = 0; loopi < polygons.size(); loopi++) {
		ArrayList singlePolygon = (ArrayList)polygons.get(loopi);
		// @todo this uses grid points.
		outputGrid.drawArrayListOfPoints(0, singlePolygon);
	    }
	}
    }
    

}
