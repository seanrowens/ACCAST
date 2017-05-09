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
// @version     $Id: Controller.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;


// @todo: This should probably be an interface that implements
// mouseListener and MouseMotionListener and a getter for
// actionCommand
public class Controller {
    private String actionCommand;
    public String getActionCommand() { return actionCommand; }
    private String statusMsg;
    public String getStatusMsg() { return statusMsg; }
    private String toolTip;
    public String getToolTip() { return toolTip; }
    private MouseListener listener;
    private MouseMotionListener motionListener;
    private DebugInterface debug;

    public Controller(String actionCommand, String statusMsg, String toolTip, MouseListener listener, MouseMotionListener motionListener) {
	this.actionCommand = actionCommand;
	this.statusMsg = statusMsg;
	this.toolTip = toolTip;
	this.listener = listener;
	this.motionListener = motionListener;
	debug = new DebugFacade(this);
    }
    
    public void setActive(boolean active, Component comp) {
	if(active) {
	    debug.debug("Adding controller="+actionCommand);
	    if(null != listener) 
		comp.addMouseListener(listener);
	    if(null != motionListener) 
		comp.addMouseMotionListener(motionListener);
	}
	else {
	    debug.debug("Removing controller="+actionCommand);
	    if(null != listener) 
		comp.removeMouseListener(listener);
	    if(null != motionListener) 
		comp.removeMouseMotionListener(motionListener);
	}
    }
}
