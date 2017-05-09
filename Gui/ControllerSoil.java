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
// @version     $Id: ControllerSoil.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

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

public class ControllerSoil implements MouseListener, MouseMotionListener {
    public static int DEFAULT_MAP_SOIL_TYPE = SoilTypes.ASPHALT;

    private int currentSoilType = DEFAULT_MAP_SOIL_TYPE;
    private BackgroundConfig config = null;
    private Background background = null;
    private IntGrid soilGrid;
    
    private int lastGridX = 0;
    private int lastGridY = 0;

    private DebugInterface debug = null;

    private void init(BackgroundConfig config,  Background background, IntGrid soilGrid) {
	this.config = config;
	this.background = background;
	this.soilGrid = soilGrid;
	debug = new DebugFacade(this);
    }

    public ControllerSoil(BackgroundConfig config, Background background, IntGrid soilGrid) {
	this.init(config,background, soilGrid);
    }

    // implements MouseListener
    public void mousePressed(MouseEvent e) {
	try {
	    debug.debug("mousePressed: # of clicks: "+ e.getClickCount());
	    int localx = (int)config.viewPort.destToSourceX(e.getX());
	    int localy = (int)config.viewPort.destToSourceY(e.getY());
	    int gridx = soilGrid.toGridX(localx);
	    int gridy = soilGrid.toGridY(localy);
	    lastGridX = gridx;
	    lastGridY = gridy;
	    
	    if(MouseEvent.BUTTON1 == e.getButton()) {
		debug.debug("Setting "+localx+","+localy+" (grid "+gridx+", "+gridy+") to soil type="+currentSoilType);
		soilGrid.setValue(gridx, gridy, currentSoilType);
		background.emptyCache();
	    }
	    else if(MouseEvent.BUTTON2 == e.getButton()) {
		debug.debug("Setting "+localx+","+localy+" (grid "+gridx+", "+gridy+") to soil type="+currentSoilType);
		currentSoilType = soilGrid.getValue(localx, localy);
	    }
	    else if(MouseEvent.BUTTON3 == e.getButton()) {
		soilGrid.saveGridFile("/usr1/logs/testpalette/newsoil.grd");
	    }
	}
	catch (Exception e2) {
	    debug.error("mousePressed:exception while processing mouse press, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

    // implements MouseListener
    public void mouseReleased(MouseEvent e) {
	//	debug.debug("MouseReleased");
    }

    // implements MouseListener
    public void mouseEntered(MouseEvent e) {
	//	debug.info("Mouse entered");
    }

    // implements MouseListener
    public void mouseExited(MouseEvent e) {
	//	debug.info("Mouse exited");
    }

    // implements MouseListener
    public void mouseClicked(MouseEvent e) {
	//  	debug.debug("mouseClicked: # of clicks: "+ e.getClickCount());
    }

    // implements MouseMotionListener
    public void mouseDragged(MouseEvent e) {
	debug.debug("mouseDragged");
	int localx = (int)config.viewPort.destToSourceX(e.getX());
	int localy = (int)config.viewPort.destToSourceY(e.getY());
	int gridx = soilGrid.toGridX(localx);
	int gridy = soilGrid.toGridY(localy);

	//	if(MouseEvent.BUTTON1 == e.getButton()) {

	double xdiff = gridx - lastGridX;
	double ydiff = gridy - lastGridY;
	double maxdiff = (xdiff > ydiff) ? xdiff : ydiff;
	for(double loopd = 0.0; loopd < 1.0; loopd += .001) {
	    int gridx2 = (int)(lastGridX + (loopd * xdiff));
	    int gridy2 = (int)(lastGridY + (loopd * ydiff));
	    debug.debug("Setting "+localx+","+localy+" (grid "+gridx2+", "+gridy2+") to soil type="+currentSoilType);
	    soilGrid.setValue(gridx2, gridy2, currentSoilType);
	}
	
	    background.emptyCache();
	    lastGridX = gridx;
	    lastGridY = gridy;
// 	}
// 	else if(MouseEvent.BUTTON2 == e.getButton()) {
// 	    currentSoilType = soilGrid.getValue(localx, localy);
// 	}
    }

    // implements MouseMotionListener
    public void mouseMoved(MouseEvent e) {
	//	debug.debug("mouseMoved");
    }
}
