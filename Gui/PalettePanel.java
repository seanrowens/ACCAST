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
// @version     $Id: PalettePanel.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;


// This class holds a list of actions.  When a button is clicked or a
// menu item is selected, the action goes here, and this class then
// disables 

public class PalettePanel extends JPanel {
    private DebugInterface debug;
    private ButtonManager buttonManager;
    private ControllerManager controllerManager;

    public class RadioAction extends AbstractAction {
        public RadioAction(String text, ImageIcon icon,
                          String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
	    debug.debug("Action performed="+e.getActionCommand()+", action="+e.toString());
	    controllerManager.setActive(e.getActionCommand());
	    buttonManager.setSelected(e.getActionCommand());
        }
    }

    public PalettePanel(ButtonManager buttonManager, ControllerManager controllerManager) {
	super();
	this.buttonManager = buttonManager;
	this.controllerManager = controllerManager;
	debug = new DebugFacade(this);
    }

    // @todo: should use actions for creating the button.
    public void addController(boolean selected, String buttonText, ImageIcon icon, String buttonDesc, int mnemonic, String actionCommand, Controller controller) {
	JToggleButton button = new JToggleButton(actionCommand);
	button.setMnemonic(mnemonic);
	button.setActionCommand(actionCommand);
	button.setSelected(selected);
	// Note, add to buttonManager before adding listener
	buttonManager.addButton(button);
	controllerManager.addController(controller);
	button.addActionListener(new RadioAction(buttonText, icon, buttonDesc, new Integer(mnemonic)));
	if(null != controller.getToolTip())
	    button.setToolTipText(controller.getToolTip());
	add(button);
	if(selected) {
	    buttonManager.setSelected(actionCommand);
	    controllerManager.setActive(actionCommand);
	}
    }
    
}
