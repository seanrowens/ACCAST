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
// @version     $Id: ControllerMultiStrike.java,v 1.6 2008/02/08 02:37:07 owens Exp $ 

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

public class ControllerMultiStrike implements MouseListener, MouseMotionListener {
    private final static double HILIGHT_RANGE_PIXELS = 50;
    private final static double SELECT_RANGE_PIXELS = 50;
    public static int DEFAULT_MAP_OBJECT_TYPE = MapObject.TYPE_STRIKE;
    private MapObjectPanel moPanel = null;
    private MapDB mapDB = null;
    private BackgroundConfig config = null;
    private TerrainCanvas terrain = null;

    private Set<MapObject> selectedSet = new HashSet<MapObject>();
    private MapObject curSelected = null;
    private void setSelected(MapObject newSelected) { 
	curSelected = newSelected;
	if(null != newSelected) {
	    curSelected.setSelected(true);
	    selectedSet.add(newSelected);
	}
	if(null != moPanel) 
	    moPanel.setSelected(curSelected);
	mapDB.setDirty(true);
	terrain.repaint();
    }

    private boolean dragging = false;
    private DebugInterface debug = null;
    private Set<Integer> strikeTypes; 
    private Set<Integer> unitTypes;

    private void init(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, MapObjectPanel moPanel, int defaultMapObjectType) {
	this.mapDB = mapDB;
	this.config = config;
	this.terrain = terrain;
	this.moPanel = moPanel;
	this.DEFAULT_MAP_OBJECT_TYPE = defaultMapObjectType;
	debug = new DebugFacade(this);
	strikeTypes = new HashSet<Integer>();
	strikeTypes.add(MapObject.TYPE_STRIKE);
	unitTypes = new HashSet<Integer>();
	unitTypes.add(MapObject.TYPE_UNIT);
	unitTypes.add(MapObject.TYPE_PICTURE);
    }

    public ControllerMultiStrike(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, MapObjectPanel moPanel) {
	this.init(mapDB, config, terrain, moPanel,MapObject.TYPE_STRIKE);
    }

    // implements MouseListener
    public void mousePressed(MouseEvent e) {
	try {
	    debug.debug("mousePressed: # of clicks: "+ e.getClickCount());
	    double localx = config.viewPort.destToSourceX(e.getX());
	    double localy = config.viewPort.destToSourceY(e.getY());

	    if(MouseEvent.BUTTON1 == e.getButton()) {
		MapObject unitSelected = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), unitTypes);
		MapObject newSelected = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), strikeTypes);

		if(null != newSelected) {
		    if(newSelected != curSelected) {
			setSelected(newSelected);
			debug.debug("mousePressed: Now selected="+curSelected);
		    }
		}
		else {
		    if(unitSelected != null) {
			newSelected = createMapObject(unitSelected.getPosX(), unitSelected.getPosY());
		    }
		    else {
			newSelected = createMapObject(localx, localy);
		    }
		    setSelected(newSelected);
		    debug.debug("mousePressed: Now created="+newSelected);
		}
		mapDB.clearSelected();
		curSelected = newSelected;
		curSelected.setSelected(true);
		mapDB.changed();
		terrain.repaint();
		if(curSelected.isEditable())
		    dragging = true;
	    }
	    else if(MouseEvent.BUTTON3 == e.getButton()) {
		MapObject newSelected = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), strikeTypes);
		if((null != newSelected) && (newSelected.isEditable())) {
		    debug.debug("Removing MapObject "+newSelected.name+" id "+newSelected.getKey()+" from map.");
		    mapDB.remove(newSelected);
		    if(null != curSelected)
			curSelected.setSelected(false);
		    curSelected = null;
		}
		mapDB.changed();
		terrain.repaint();
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
    }

    // implements MouseListener
    public void mouseEntered(MouseEvent e) {
	//	debug.info("Mouse entered");
    }

    // implements MouseListener
    public void mouseExited(MouseEvent e) {
	mapDB.clearHilighted();
	mapDB.setDirty(true);
	terrain.repaint();
	//	debug.info("Mouse exited");
    }

    private MapObject createMapObject(double localx, double localy) {
	localx = ((double)((long)(localx * 10000)))/10000;
	localy = ((double)((long)(localy * 10000)))/10000;
	//	MapObject newObject = new MapObject(MapObject.createKey(), localx, localy, 0.0);
	MapObject newObject = new MapObject(MapObject.createKey(), DEFAULT_MAP_OBJECT_TYPE, localx, localy, 0.0,0,0);
	mapDB.add(newObject);
	terrain.repaint();
	return newObject;
    }

    // implements MouseListener
    public void mouseClicked(MouseEvent e) {
	//  	debug.debug("mouseClicked: # of clicks: "+ e.getClickCount());
    }

    // implements MouseMotionListener
    public void mouseDragged(MouseEvent e) {
	//	debug.debug("mouseDragged");
	if((dragging) && (null != curSelected) && (curSelected.isEditable())) {
	    double x = config.viewPort.destToSourceX(e.getX());
	    double y = config.viewPort.destToSourceY(e.getY());
	    x = (double)((long)(x * 10000))/10000;
	    y = (double)((long)(y * 10000))/10000;
	    curSelected.setPos(x, y, curSelected.posZ);
	    curSelected.setEdited(true);

	    mapDB.fixDis(curSelected);
	    if(null != moPanel)
		moPanel.setSelected(curSelected);
	    mapDB.changed();
	    terrain.repaint();
	}
    }

    // implements MouseMotionListener
    public void mouseMoved(MouseEvent e) {
	//	debug.debug("mouseMoved");
	try {
	    if(!dragging) {
		double localx = config.viewPort.destToSourceX(e.getX());
		double localy = config.viewPort.destToSourceY(e.getY());
		mapDB.clearHilighted();
		MapObject strikeHilighted = mapDB.findWithin(config.viewPort.destToSourceX(e.getX()),
							     config.viewPort.destToSourceY(e.getY()),
							     config.viewPort.destToSourceWidth(HILIGHT_RANGE_PIXELS), strikeTypes);
		if(null != strikeHilighted) {
		    strikeHilighted.setHilighted(true);
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

