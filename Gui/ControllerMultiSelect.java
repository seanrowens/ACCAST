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
// @version     $Id: ControllerMultiSelect.java,v 1.5 2008/02/08 02:37:07 owens Exp $ 

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

// SRO Wed Apr 19 03:44:13 EDT 2006
// 
// From what I can tell the microsoft convention is that click (or
// click+drag) deselects whatever is currently selected, and selects
// whatever you click over.  shift-click leaves the current selection
// and adds what ever you clicked over. Alt-shift-click leaves the
// current selection alone except it also removes whatever you clicked
// over.  Modifier keys (alt, shift, ctrl) take effect on the pressing
// of the key, not on the release.  (Ctrl-click does something funky
// like selecting the entire sentence/line or whatever, while
// ctrl-shift-click expands the current selection to the nearest word
// boundary and then adds whatever you clicked over.  For our
// purposes, I guess I should make ctrl-shift-click be the same as
// shift click.)
public class ControllerMultiSelect implements MouseListener, MouseMotionListener {
    private final static double HILIGHT_RANGE_PIXELS = 50;
    private final static double SELECT_RANGE_PIXELS = 50;

    private TerrainCanvas terrain = null;
    private MapDB mapDB = null;
    private BackgroundConfig config = null;
    private IntGrid grid = null;

    private DebugInterface debug = null;

    private int pressx = 0;
    private int pressy = 0;
    private int releasex = 0;
    private int releasey = 0;

    private Set<Integer> unitTypes;

    private HashMap<String, MapObject> selected = new HashMap<String, MapObject>();
    private boolean dragging = false;
    private boolean removing = false;

    private void init(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, IntGrid grid) {
	this.mapDB = mapDB;
	this.config = config;
	this.terrain = terrain;
	this.grid = grid;
	debug = new DebugFacade(this);
	unitTypes = new HashSet<Integer>();
	unitTypes.add(MapObject.TYPE_UNIT);
	unitTypes.add(MapObject.TYPE_PICTURE);
    }

    public ControllerMultiSelect(MapDB mapDB, BackgroundConfig config,  TerrainCanvas terrain, IntGrid grid) {
	this.init(mapDB, config, terrain, grid);
    }

    // implements MouseListener
    public void mousePressed(MouseEvent e) {
	// "The button mask returned by InputEvent.getModifiers()
	// reflects only the button that changed state, not the
	// current state of all buttons."
	try {
	    debug.debug("mousePressed: # of clicks: "+ e.getClickCount());

	    dragging = true;

	    int modifiers = e.getModifiersEx();
	    debug.debug("mousePressed: modifiers="+modifiers);
	    if(0 == (modifiers & e.SHIFT_DOWN_MASK)) {
		mapDB.clearSelected();
		terrain.repaint();
		selected.clear();
		removing = false;
	    }
	    else {
		if(0 != (modifiers & e.ALT_DOWN_MASK))
		    removing = true;
		else if(0 != (modifiers & e.ALT_MASK))
		    removing = true;
		else
		    removing = false;
	    }

	    debug.debug("mousePressed: removing="+removing);

	    pressx = e.getX();
	    pressy = e.getY();
	    double localx = config.viewPort.destToSourceX(e.getX());
	    double localy = config.viewPort.destToSourceY(e.getY());
	    MapObject newSelected = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), config);
	    if(null != newSelected) {
		selected.put(newSelected.getKey(), newSelected);
		newSelected.setSelected(true);
		mapDB.setDirty(true);
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
	if(MouseEvent.BUTTON1 != e.getButton()) { 
	    return;
	}
	dragging = false;

	releasex = e.getX();
	releasey = e.getY();
	int diffx = releasex - pressx;
	int diffy = releasey - pressy;
	terrain.setSelectionOff();
	
	double localx = config.viewPort.destToSourceX(pressx);
	double localy = config.viewPort.destToSourceY(pressy);
	double localx2 = config.viewPort.destToSourceX(releasex);
	double localy2 = config.viewPort.destToSourceY(releasey);

	ArrayList<MapObject> foundList = mapDB.findWithin(localx, localy, localx2, localy2, unitTypes);
	if(null == foundList)
	    return;
	if(removing) {
	    mapDB.setSelected(foundList, false);
	    terrain.repaint();
	    for(MapObject mo: foundList) {
		selected.remove(mo.getKey());
	    }
	}
	else {
	    mapDB.setSelected(foundList, true);
	    terrain.repaint();
	    for(MapObject mo: foundList) {
		selected.put(mo.getKey(), mo);
	    }
	}
	
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
    }

    // implements MouseMotionListener
    public void mouseDragged(MouseEvent e) {
	//	debug.debug("mouseDragged");
	terrain.setSelectionRect(pressx, pressy, e.getX(), e.getY());
	double localx = config.viewPort.destToSourceX(pressx);
	double localy = config.viewPort.destToSourceY(pressy);
	double localx2 = config.viewPort.destToSourceX(e.getX());
	double localy2 = config.viewPort.destToSourceY(e.getY());
	ArrayList<MapObject> foundList = mapDB.findWithin(localx, localy, localx2, localy2, unitTypes);
	debug.debug("found "+foundList.size()+" objects inside box from "+localx+", "+localy+" to "+localx2+","+localy2);
	mapDB.clearHilighted();
	mapDB.setHilighted(foundList, true);
	terrain.repaint();
    }

    // implements MouseMotionListener
    public void mouseMoved(MouseEvent e) {
// 	debug.debug("mouseMoved");
	// @todo: hilight
	try {
	    if(!dragging) {
		double localx = config.viewPort.destToSourceX(e.getX());
		double localy = config.viewPort.destToSourceY(e.getY());
		mapDB.clearHilighted();
		MapObject newHilighted = mapDB.findWithin(localx, localy, config.viewPort.destToSourceWidth(SELECT_RANGE_PIXELS), config);
		if(null != newHilighted) {
		    newHilighted.setHilighted(true);
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
