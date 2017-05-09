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
// @version     $Id: ControllerTwoPanelZoom.java,v 1.2 2008/02/08 02:37:07 owens Exp $ 

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

public class ControllerTwoPanelZoom implements MouseListener, MouseMotionListener {
    private final static boolean ZOOM_ON_MOVE=false;

    private final static double HILIGHT_RANGE_PIXELS = 50;
    private final static double SELECT_RANGE_PIXELS = 50;

    private final static String ZOOM_BOX_ID = "zoomBox";
    private final static String TENTATIVE_ZOOM_BOX_ID = "tentativeZoomBox";

    private MapObjectPanel moPanel = null;
    private MapDB mapDB = null;
    private BackgroundConfig config = null;
    private TerrainCanvas terrain = null;

    private ViewPort zoomViewPort;
    private double zoomWidth;
    private double zoomHeight;

    private MapObject zoomBox = null;
    private MapObject tentativeZoomBox = null;

    private MapObject curSelected = null;
    private MapObject curHilighted = null;

    private boolean dragging = false;
    private boolean drawing = false;

    private DebugInterface debug = null;

    public ControllerTwoPanelZoom(MapDB mapDB, BackgroundConfig config, TerrainCanvas terrain, MapObjectPanel moPanel, ViewPort zoomViewPort, double zoomWidth, double zoomHeight) {
	this.mapDB = mapDB;
	this.config = config;
	this.terrain = terrain;
	this.moPanel = moPanel;
	this.zoomViewPort = zoomViewPort;
	this.zoomWidth = zoomWidth;
	this.zoomHeight = zoomHeight;
	debug = new DebugFacade(this);

	zoomBox = new MapObject(ZOOM_BOX_ID, MapObject.TYPE_MAP_GRAPHIC, 0, 0, 0, zoomWidth, zoomHeight);
	zoomBox.setName("");
	zoomBox.setEditable(false);
	zoomBox.setMapGraphicColor(new Color(0,0,0,0));
	zoomBox.setMapGraphicFillOn(false);
	//	zoomBox.setMapGraphicLineColor(new Color(0,0,0,0));

	mapDB.add(zoomBox);
	
	tentativeZoomBox = new MapObject(TENTATIVE_ZOOM_BOX_ID, MapObject.TYPE_MAP_GRAPHIC, 0, 0, 0, zoomWidth, zoomHeight);
	tentativeZoomBox.setName("");
	tentativeZoomBox.setEditable(false);
	tentativeZoomBox.setMapGraphicColor(new Color(0,0,0,0));
	tentativeZoomBox.setMapGraphicFillOn(false);
	tentativeZoomBox.setMapGraphicLineColor(Color.red);

	mapDB.add(tentativeZoomBox);

    }


    // implements MouseListener
    public void mousePressed(MouseEvent e) {
	debug.debug("Mouse pressed; # of clicks: "+ e.getClickCount());
	double x = config.viewPort.destToSourceX(e.getX());
	double y = config.viewPort.destToSourceY(e.getY());
	zoomBox.setPos(x,y,0);
	x = x - zoomWidth/2;
	y = y - zoomHeight/2;
	zoomViewPort.requestSourceView(x,y,x+zoomWidth, y+zoomHeight);
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

    // implements MouseListener
    public void mouseClicked(MouseEvent e) {
    }

    // implements MouseMotionListener
    public void mouseDragged(MouseEvent e) {
	double x = config.viewPort.destToSourceX(e.getX());
	double y = config.viewPort.destToSourceY(e.getY());
	tentativeZoomBox.setPos(x,y,0);
	zoomBox.setPos(x,y,0);
	x = x - zoomWidth/2;
	y = y - zoomHeight/2;
	zoomViewPort.requestSourceView(x,y,x+zoomWidth, y+zoomHeight);
    }

    // implements MouseMotionListener
    public void mouseMoved(MouseEvent e) {
	double x = config.viewPort.destToSourceX(e.getX());
	double y = config.viewPort.destToSourceY(e.getY());
	tentativeZoomBox.setPos(x,y,0);
	if(ZOOM_ON_MOVE) {
	    zoomBox.setPos(x,y,0);
	    x = x - zoomWidth/2;
	    y = y - zoomHeight/2;
	    zoomViewPort.requestSourceView(x,y,x+zoomWidth, y+zoomHeight);
	}
    }
}

