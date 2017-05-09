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
// @version     $Id: ControllerLine.java,v 1.5 2008/02/08 02:37:07 owens Exp $ 

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

public class ControllerLine implements MouseListener, MouseMotionListener {
    private final static double HILIGHT_RANGE_PIXELS = 50;
    private final static double SELECT_RANGE_PIXELS = 50;

    public static int DEFAULT_MAP_OBJECT_TYPE = MapObject.TYPE_LINE;
    private MapObjectPanel moPanel = null;
    private MapDB mapDB = null;
    private BackgroundConfig config = null;
    private TerrainCanvas terrain = null;

    private MapObject curSelected = null;
    private MapObject curHilighted = null;

    private boolean dragging = false;
    private boolean drawing = false;

    private DebugInterface debug = null;
    private Set<Integer> lineTypes; 
    private Set<Integer> unitTypes;

    private void init(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, MapObjectPanel moPanel, int defaultMapObjectType) {
	this.mapDB = mapDB;
	this.config = config;
	this.terrain = terrain;
	this.moPanel = moPanel;
	this.DEFAULT_MAP_OBJECT_TYPE = defaultMapObjectType;
	debug = new DebugFacade(this);
	lineTypes = new HashSet<Integer>();
	lineTypes.add(MapObject.TYPE_LINE);
	unitTypes = new HashSet<Integer>();
	unitTypes.add(MapObject.TYPE_UNIT);
	unitTypes.add(MapObject.TYPE_PICTURE);
    }

    public ControllerLine(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, MapObjectPanel moPanel) {
	this.init(mapDB, config, terrain, moPanel, MapObject.TYPE_LINE);
    }

    public ControllerLine(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, MapObjectPanel moPanel, int defaultMapObjectType) {
	this.init(mapDB, config, terrain, moPanel, defaultMapObjectType);
    }

    // implements MouseListener
    public void mousePressed(MouseEvent e) {
	debug.debug("Mouse pressed; # of clicks: "+ e.getClickCount());
	double x = config.viewPort.destToSourceX(e.getX());
	double y = config.viewPort.destToSourceY(e.getY());
	
	try {

	    MapObject newSelected = mapDB.findWithin(x, y, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), lineTypes);
		//	    MapObject newSelected = mapDB.findWithin(x, y, SELECT_RANGE, config);
	    if(null != newSelected) {
		if(newSelected != curSelected) {
		    if(null != curSelected)
			curSelected.setSelected(false);
		    newSelected.setSelected(true);
		    curSelected = newSelected;
		    debug.debug("Now selected="+curSelected);
		    if(null != moPanel) 
			moPanel.setSelected(curSelected);
		    mapDB.changed();
		    terrain.repaint();
		}
		dragging = true;
	    }
	    else {
		//	    debug.debug("Creating line object at "+e.getX()+", "+e.getY());
		newSelected = createMapObject(e.getX(), e.getY(), x, y);
		newSelected.setType(DEFAULT_MAP_OBJECT_TYPE);
		newSelected.addLinePoint((float)x, (float)y);
		// @TODO: Redundant, consolidate with code above.
		if(newSelected != curSelected) {
		    if(null != curSelected)
			curSelected.setSelected(false);
		    newSelected.setSelected(true);
		    curSelected = newSelected;
		    debug.debug("Now selected="+curSelected);
		    if(null != moPanel)
			moPanel.setSelected(curSelected);
		    mapDB.changed();
		    terrain.repaint();
		}
		drawing = true;
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
	if(dragging)
	    dragging = false;
	if(drawing)
	    drawing = false;
    }

    // implements MouseListener
    public void mouseEntered(MouseEvent e) {
	//	debug.info("Mouse entered");
    }

    // implements MouseListener
    public void mouseExited(MouseEvent e) {
	//	debug.info("Mouse exited");
	mapDB.changed();
	terrain.repaint();
	curHilighted = null;
    }

    private MapObject createMapObject(int mapx, int mapy, double x, double y) {
	long longX = (long)(x*10000);
	long longY = (long)(y*10000);
	double x2 = ((double)longX)/10000;
	double y2 = ((double)longY)/10000;
	x = ((double)((long)(x * 10000)))/10000;
	y = ((double)((long)(y * 10000)))/10000;
	//	debug.debug("mouseClicked:x="+x+", y="+y+", longX="+longX+", longY="+longY+", x2="+x2+", y2="+y2);
	//	debug.debug("Creating object at screen coords=("+mapx+", "+mapy+"), map coords=("+x+", "+y+"), scale="+config.viewPort.getScale());
	MapObject newObject = new MapObject(MapObject.createKey(), x, y, 0.0);
	mapDB.add(newObject);
	terrain.repaint();
	return newObject;
    }

    // implements MouseListener
    public void mouseClicked(MouseEvent e) {
	//	debug.debug("MouseClicked");
	double x = config.viewPort.destToSourceX(e.getX());
	double y = config.viewPort.destToSourceY(e.getY());

	try {
	    MapObject newSelected = null;
	    if(MouseEvent.BUTTON1 == e.getButton()) {
		newSelected = mapDB.findWithin(x, y, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), config);
		if(null == newSelected) {
		    newSelected = createMapObject(e.getX(), e.getY(), x, y);
		}
	    }
	    else if(MouseEvent.BUTTON3 == e.getButton()) {
		if((null != curSelected) && (curSelected.isEditable())) {
		    debug.debug("Removing MapObject "+curSelected.name+" id "+curSelected.getKey()+" from map.");
		    mapDB.remove(curSelected);
		    newSelected = null;
		    terrain.repaint();
		}
		// Don't really need this - mapDB.remove calls changed()
		//	    config.changed();
	    }

	    if(newSelected != curSelected) {
		if(null != curSelected)
		    curSelected.setSelected(false);
		if(null != newSelected)
		    newSelected.setSelected(true);
		curSelected = newSelected;
		debug.debug("Now selected="+curSelected);
		if(null != moPanel)
		    moPanel.setSelected(curSelected);
		mapDB.changed();
		terrain.repaint();
	    }
	}
	catch (Exception e2) {
	    debug.error("mouseClicked:exception while processing mouse click, ignoring e="+e2);
	    e2.printStackTrace();
	}

    }

    // implements MouseMotionListener
    public void mouseDragged(MouseEvent e) {
	//	debug.debug("MouseDragged");
	if((dragging || drawing) && (null != curSelected) && (curSelected.isEditable())) {
	    double x = config.viewPort.destToSourceX(e.getX());
	    double y = config.viewPort.destToSourceY(e.getY());
	    x = (double)((long)(x * 10000))/10000;
	    y = (double)((long)(y * 10000))/10000;
	    if(drawing && (MapObject.TYPE_LINE == curSelected.getType())) {
		//debug.debug("MouseDragged adding point to object "+curSelected.getKey()+" type "+curSelected.getType()+" at "+x+", "+y+" to line ");
		curSelected.addLinePoint((float)x, (float) y);
		debug.debug("lineObject.addLinePoint("+x+"f,"+""+y+"f);");
	    }
	    else if(dragging) {
		curSelected.setPos(x, y, curSelected.posZ);
		curSelected.setEdited(true);
	    }

	    mapDB.fixDis(curSelected);
	    if(null != moPanel)
		moPanel.setSelected(curSelected);
	    mapDB.changed();
	    terrain.repaint();
	}
    }

    // implements MouseMotionListener
    public void mouseMoved(MouseEvent e) {
	//	debug.debug("MouseMoved");
	try {
	    if(!dragging) {
		double localx = config.viewPort.destToSourceX(e.getX());
		double localy = config.viewPort.destToSourceY(e.getY());
		mapDB.clearHilighted();
		MapObject lineHilighted = mapDB.findWithin(config.viewPort.destToSourceX(e.getX()),
							   config.viewPort.destToSourceY(e.getY()),
							   config.viewPort.destToSourceWidth(HILIGHT_RANGE_PIXELS), lineTypes);
		if(null != lineHilighted) {
		    lineHilighted.setHilighted(true);
		}
		else {
		    MapObject newHilighted = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), config);
		    if(null != newHilighted) {
			newHilighted.setHilighted(true);
		    }
		}
		mapDB.setDirty(true);
		terrain.repaint();
	    }
	}
	catch (Exception e2) {
	    debug.error("mouseClicked:exception while processing mouse click, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }
}

