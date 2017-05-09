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
// @version     $Id: ButtonManager.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

// This class manages which Buttons are selected, basically its a
// souped up radiobutton group.  It allows us to have radio buttons,
// and have a parellel set of menu bar radio items, and popup menu
// radio items, etc.
//
// Note that this class expects that any two buttons (i.e. a radio
// button and a menu item) that reference the same selection will have
// the same actioncommand string.  I.e. if we want to have a button
// that turns on 'multi-strike', and also have a menubar menuitem that
// turns on multistrike, and also have a popup menu item that turns on
// menu strike, we must have THREE buttons (note that menuitems are
// descendants of abstractbutton) and they must all have the exact
// same actioncommand text.

public class ButtonManager {
    ArrayList<AbstractButton> buttonList = new ArrayList<AbstractButton>();
    
    public ButtonManager() {
    }
    
    public void addButton(AbstractButton button) {
	buttonList.add(button);
    }
    
    public void setSelected(String actionCommand) {
	for(AbstractButton button: buttonList) {
	    if(actionCommand.equals(button.getActionCommand())) {
		button.setSelected(true);
	    }
	    else {
		button.setSelected(false);
	    }
	}
    }
    
}
