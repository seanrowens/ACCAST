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
// @version     $Id: ControllerNogo.java,v 1.4 2008/02/08 02:37:07 owens Exp $ 

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

public class ControllerNogo implements MouseListener, MouseMotionListener {
    private final static double HILIGHT_RANGE_PIXELS = 50;
    private final static double SELECT_RANGE_PIXELS = 50;

    private TerrainCanvas terrain = null;
    private MapDB mapDB = null;
    private BackgroundConfig config = null;
    private IntGrid grid = null;

    private DebugInterface debug = null;

    private Set<Integer> mapGraphicTypes; 

    private MapObject curSelected = null;
    private int pressx = 0;
    private int pressy = 0;
    private int releasex = 0;
    private int releasey = 0;

    private void init(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, IntGrid grid) {
	this.mapDB = mapDB;
	this.config = config;
	this.terrain = terrain;
	this.grid = grid;
	debug = new DebugFacade(this);
	mapGraphicTypes = new HashSet<Integer>();
	mapGraphicTypes.add(MapObject.TYPE_NOGO_ZONE);
    }

    public ControllerNogo(MapDB mapDB, BackgroundConfig config,  TerrainCanvas terrain, IntGrid grid) {
	this.init(mapDB, config, terrain, grid);
    }

    // implements MouseListener
    public void mousePressed(MouseEvent e) {
	try {
	    debug.debug("mousePressed: # of clicks: "+ e.getClickCount());
	    double localx = config.viewPort.destToSourceX(e.getX());
	    double localy = config.viewPort.destToSourceY(e.getY());
	    pressx = e.getX();
	    pressy = e.getY();
	}
	catch (Exception e2) {
	    debug.error("mousePressed:exception while processing mouse press, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

    // implements MouseListener
    public void mouseReleased(MouseEvent e) {
	//	debug.debug("MouseReleased");
	if(MouseEvent.BUTTON1 != e.getButton()) { 
	    terrain.setSelectionOff();
	    return;
	}
	releasex = e.getX();
	releasey = e.getY();
	int diffx = releasex - pressx;
	int diffy = releasey - pressy;
	if((diffx > -2) & (diffx < 2)) {
	    releasex = releasex - 25;
	    pressx = pressx + 25;
	    debug.debug("diffx="+diffx+", changing releasex to "+releasex+" and pressx to "+pressx);
	}
	if((diffy > -2) & (diffy < 2)) {
	    releasey = releasey - 25;
	    pressy = pressy + 25;
	    debug.debug("diffy="+diffy+", changing releasey to "+releasey+" and pressy to "+pressy);
	}
	debug.debug("diffx="+diffx+", diffy="+diffy);
	
	double localx = config.viewPort.destToSourceX(pressx);
	double localy = config.viewPort.destToSourceY(pressy);
	double localx2 = config.viewPort.destToSourceX(releasex);
	double localy2 = config.viewPort.destToSourceY(releasey);

	if(localx2 > localx) {
	    double temp = localx;
	    localx = localx2;
	    localx2 = temp;
	}
	if(localy2 > localy) {
	    double temp = localy;
	    localy = localy2;
	    localy2 = temp;
	}

	double width = localx - localx2;
	double height = localy - localy2;
	double x = localx2 + width/2;
	double y = localy2 + height/2;

	debug.debug("Creating map graphic at "+x+","+y+" width "+width+" height "+height);
	MapObject newObject = new MapObject(MapObject.createKey(), MapObject.TYPE_NOGO_ZONE, x, y, 0.0, width, height);

	mapDB.add(newObject);
	terrain.setSelectionOff();
    }

    // implements MouseListener
    public void mouseEntered(MouseEvent e) {
	//	debug.info("Mouse entered");
    }

    // implements MouseListener
    public void mouseExited(MouseEvent e) {
	//	debug.info("Mouse exited");
	terrain.setSelectionOff();
    }


    // implements MouseListener
    public void mouseClicked(MouseEvent e) {
	//  	debug.debug("mouseClicked: # of clicks: "+ e.getClickCount());
	try {
	    double localx = config.viewPort.destToSourceX(e.getX());
	    double localy = config.viewPort.destToSourceY(e.getY());
	    if(MouseEvent.BUTTON3 == e.getButton()) {
		MapObject newSelected = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), mapGraphicTypes);
		if((null != newSelected) && (newSelected.isEditable())) {
		    debug.debug("Removing MapObject "+newSelected.name+" id "+newSelected.getKey()+" from map.");
		    mapDB.remove(newSelected);
		    if(null != curSelected)
			curSelected.setSelected(false);
		    curSelected = null;
		}
		mapDB.changed();
		terrain.setSelectionOff();
	    }
	}
	catch (Exception e2) {
	    debug.error("mousePressed:exception while processing mouse press, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

    // implements MouseMotionListener
    public void mouseDragged(MouseEvent e) {
	//	debug.debug("mouseDragged");
	terrain.setSelectionRect(pressx, pressy, e.getX(), e.getY());
    }

    public void mouseMoved(MouseEvent e) {
	//	debug.debug("mouseMoved");
	try {
//	    if(!dragging) {
		double localx = config.viewPort.destToSourceX(e.getX());
		double localy = config.viewPort.destToSourceY(e.getY());
		mapDB.clearHilighted();
		MapObject strikeHilighted = mapDB.findWithin(config.viewPort.destToSourceX(e.getX()),
							     config.viewPort.destToSourceY(e.getY()),
							     config.viewPort.destToSourceWidth(HILIGHT_RANGE_PIXELS), mapGraphicTypes);
		if(null != strikeHilighted) {
		    strikeHilighted.setHilighted(true);
		}
		else {
		    MapObject newHilighted = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(HILIGHT_RANGE_PIXELS), config);
		    if(null != newHilighted) {
			newHilighted.setHilighted(true);
		    }
		}
		mapDB.setDirty(true);
		terrain.setSelectionOff();
//	    }
	}
	catch (Exception e2) {
	    debug.error("mouseClicked:exception while processing mouse click, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }
}
