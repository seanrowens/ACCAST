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
// @version     $Id: ControllerEmpty.java,v 1.3 2008/02/08 02:37:07 owens Exp $ 

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

public class ControllerEmpty implements MouseListener, MouseMotionListener {
    private MapObjectPanel moPanel = null;
    private MapDB mapDB = null;
    private BackgroundConfig config = null;

    private DebugInterface debug = null;

    private void init(MapDB mapDB, BackgroundConfig config, MapObjectPanel moPanel) {
	this.mapDB = mapDB;
	this.config = config;
	this.moPanel = moPanel;
	debug = new DebugFacade(this);
    }

    public ControllerEmpty(MapDB mapDB, BackgroundConfig config, MapObjectPanel moPanel) {
	this.init(mapDB, config, moPanel);
    }

    // implements MouseListener
    public void mousePressed(MouseEvent e) {
	try {
	    debug.debug("mousePressed; # of clicks: "+ e.getClickCount());
	    double x = config.viewPort.destToSourceX(e.getX());
	    double y = config.viewPort.destToSourceY(e.getY());
	}
	catch (Exception e2) {
	    debug.error("mousePressed:exception while processing mousePressed, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

    // implements MouseListener
    public void mouseReleased(MouseEvent e) {
	try {
	    debug.debug("mouseReleased; # of clicks: "+ e.getClickCount());
	    double x = config.viewPort.destToSourceX(e.getX());
	    double y = config.viewPort.destToSourceY(e.getY());
	}
	catch (Exception e2) {
	    debug.error("mouseReleased:exception while processingReleased press, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

    // implements MouseListener
    public void mouseEntered(MouseEvent e) {
	debug.info("Mouse entered");
    }

    // implements MouseListener
    public void mouseExited(MouseEvent e) {
	mapDB.clearHilighted();
	debug.info("Mouse exited");
    }

    // implements MouseListener
    public void mouseClicked(MouseEvent e) {
	try {
	    debug.debug("mouseClicked; # of clicks: "+ e.getClickCount());
	    double x = config.viewPort.destToSourceX(e.getX());
	    double y = config.viewPort.destToSourceY(e.getY());
	}
	catch (Exception e2) {
	    debug.error("mouseClicked:exception while processing mouseClicked, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

    // implements MouseMotionListener
    public void mouseDragged(MouseEvent e) {
	try {
	    debug.debug("mouseDragged; # of clicks: "+ e.getClickCount());
	    double x = config.viewPort.destToSourceX(e.getX());
	    double y = config.viewPort.destToSourceY(e.getY());
	}
	catch (Exception e2) {
	    debug.error("mouseDragged:exception while processing mouseDragged, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

    // implements MouseMotionListener
    public void mouseMoved(MouseEvent e) {
	try {
	    debug.debug("mouseMoved; # of clicks: "+ e.getClickCount());
	    double x = config.viewPort.destToSourceX(e.getX());
	    double y = config.viewPort.destToSourceY(e.getY());
	}
	catch (Exception e2) {
	    debug.error("mouseMoved:exception while processing mouseMoved, ignoring e="+e2);
	    e2.printStackTrace();
	}
    }

}

