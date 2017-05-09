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
// @version     $Id: ControllerManager.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;


// This class manages which Controllers (basically which
// MouseListener/MouseMotionListeners) are listening to the associated
// Component(s).
public class ControllerManager {
    private DebugInterface debug;
    private ArrayList<Controller> controllerList = new ArrayList<Controller>();
    private ArrayList<Component> componentList = new ArrayList<Component>();
    private JLabel statusBarLabel = null;
   
    public ControllerManager(Component c, JLabel statusBarLabel) {
	componentList.add(c);
	this.statusBarLabel = statusBarLabel;
	debug = new DebugFacade(this);
    }
    
    public void addController(String actionCommand, String statusMsg, MouseListener listener, MouseMotionListener motionListener) {
	debug.debug("Adding controller="+actionCommand);
	controllerList.add(new Controller(actionCommand, statusMsg, statusMsg, listener, motionListener));
    }

    public void addController(Controller controller) {
	debug.debug("Adding controller="+controller.getActionCommand());
	controllerList.add(controller);
    }
    
    public void setActive(String actionCommand) {
	for(Controller controller: controllerList) {
	    debug.debug("Comparing action="+actionCommand+" against controller action="+controller.getActionCommand());
	    if(actionCommand.equals(controller.getActionCommand())) {
		for(Component comp: componentList) {
		    debug.debug("\tsetting active for component="+comp+" controller action="+controller.getActionCommand());
		    controller.setActive(true, comp);
		}
		if(null != statusBarLabel) {
		    final Controller fController = controller;
		    debug.debug("Setting status bar msg to "+fController.getStatusMsg());
		    EventQueue.invokeLater(new Runnable() { 
			    public void run() { 
				statusBarLabel.setText(fController.getStatusMsg());
				statusBarLabel.repaint();
			    }	
			}
					   );	    
		}
		else {
		    debug.debug("null statusBarLabel, not setting status bar msg to "+controller.getStatusMsg());
		}
	    }
	    else {
		for(Component comp: componentList) {
		    debug.debug("\tsetting inactive for compoent="+comp+" controller action="+controller.getActionCommand());
		    controller.setActive(false, comp);
		}
	    }
	}
    }
    
}
