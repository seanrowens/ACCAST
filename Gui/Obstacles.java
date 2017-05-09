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
// @version     $Id: Obstacles.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;


public class Obstacles {
    private DebugInterface debug = null;
    private DoubleGrid elevationGrid = null;
    private IntGrid soilGrid = null;

    private DoubleGrid slopeGrid = null;
    public DoubleGrid getSlopeGrid() { return slopeGrid; }
    private IntGrid slopeObstacleGrid = null;
    public IntGrid getSlopeObstacleGrid() { return slopeObstacleGrid; }
    private IntGrid obstacleGrid = null;
    public IntGrid getObstacleGrid() { return obstacleGrid; }
    private IntGrid outlineGrid = null;
    public IntGrid getOutlineGrid() { return outlineGrid; }
    
    public Obstacles(DoubleGrid elevationGrid, IntGrid soilGrid) {
	this.debug = new DebugFacade(this);
	this.elevationGrid = elevationGrid;
	this.soilGrid = soilGrid;
	compute();
    }

    private void compute() { 
	// ----------------------------------------------------------------------
	// Build the obstacle grids - slope and soil type based.
	// ----------------------------------------------------------------------
	debug.info("Generating slope grid.");
	slopeGrid = new DoubleGrid(elevationGrid.getHeight(), elevationGrid.getWidth());
 	elevationGrid.averageSlopes(slopeGrid);

	debug.info("Generating obstacle grid.");
	slopeObstacleGrid = new IntGrid(elevationGrid);
	elevationGrid.computeElevationObstacle(slopeObstacleGrid);

	debug.info("        Width, heighit of slope obstacleGrid:" + slopeObstacleGrid.getWidth() +", " +slopeObstacleGrid.getHeight());
	debug.info("        Number of obstacle cells in slope obstacleGrid: "+ slopeObstacleGrid.countObstacles());
	debug.info("Done generating obstacle grid.");
	debug.info("Merging nogo soil types.");

	obstacleGrid = new IntGrid(slopeObstacleGrid, false);
	slopeObstacleGrid.mergeWaterNogoSoilType(soilGrid, obstacleGrid);
	debug.info("        Number of obstacle cells in merged obstacleGrid: "+ obstacleGrid.countObstacles());

	outlineGrid = obstacleGrid.reduceToOutlines().reduceSinglePoints();
	debug.info("        Number of obstacle cells in merged obstacleGrid outline: "+ outlineGrid.countObstacles());
    }
}
