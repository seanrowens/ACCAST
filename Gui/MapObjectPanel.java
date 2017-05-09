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
// @version     $Id: MapObjectPanel.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;
import java.lang.NumberFormatException;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class MapObjectPanel extends JPanel implements Observer, ActionListener {
    private DebugInterface debug = null;
    private MapDB mapDB = null;
    private MapObject selectedObject = null;

    private BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
    //    private SpringLayout layout = new SpringLayout();
    //    private GridBagLayout layout = new GridBagLayout();
    //    private FlowLayout layout = new FlowLayout();
    private JTextField name = new JTextField(8);
    private JComboBox objectType = new JComboBox();
    private JComboBox unitType = new JComboBox();
    private JComboBox unitForceId = new JComboBox();
    private JComboBox unitSize = new JComboBox();
    private JTextField posX = new JTextField(4);
    private JTextField posY = new JTextField(4);
    private JTextField posZ = new JTextField(4);
    private JTextField sizeY = new JTextField(4);
    private JTextField sizeX = new JTextField(4);
    private JTextField orientation = new JTextField(4);
    private JTextField velX = new JTextField(4);
    private JTextField velY = new JTextField(4);
    private JTextField velZ = new JTextField(4);
    private JTabbedPane tabPane = new JTabbedPane();
    private JTextArea comments = new JTextArea(20,4);
    private PieChart pieChart = new PieChart();
    

    public MapObjectPanel(MapDB mapDB) {
	debug = new DebugFacade(this);
	this.mapDB = mapDB;
	
	mapDB.addObserver(this);

	this.setLayout(layout);
	this.add(new JLabel("Name"));
	this.add(name);
	for(int loopi = 0; loopi < MapObject.typeNames.length; loopi++) {
	    objectType.addItem(MapObject.typeNames[loopi]);
	}
	this.add(new JLabel("Object Type"));
	this.add(objectType);

	for(int loopi = 0; loopi < UnitTypes.names.length; loopi++) {
	    unitType.addItem(UnitTypes.names[loopi]);
	}
	this.add(new JLabel("Unit Type"));
	this.add(unitType);

	for(int loopi = 0; loopi < ForceIds.names.length; loopi++) {
	    unitForceId.addItem(ForceIds.names[loopi]);
	}
	this.add(new JLabel("Force ID"));
	this.add(unitForceId);

	for(int loopi = 0; loopi < UnitSizes.names.length; loopi++) {
	    unitSize.addItem(UnitSizes.names[loopi]);
	}
	this.add(new JLabel("Unit Size"));
	this.add(unitSize);
	this.add(new JLabel("Position"));
	this.add(posX);
	posX.setHorizontalAlignment(JTextField.RIGHT);
	this.add(posY);
	posY.setHorizontalAlignment(JTextField.RIGHT);
	this.add(posZ);
	this.add(new JLabel("Size"));	
	this.add(sizeX);
	sizeX.setHorizontalAlignment(JTextField.RIGHT);
	this.add(sizeY);
	sizeY.setHorizontalAlignment(JTextField.RIGHT);
	this.add(new JLabel("Orientation (degrees)"));	
	orientation.setHorizontalAlignment(JTextField.RIGHT);
	this.add(orientation);
	this.add(new JLabel("Velocity"));
	posZ.setHorizontalAlignment(JTextField.RIGHT);
	this.add(velX);
	velX.setHorizontalAlignment(JTextField.RIGHT);
	this.add(velY);
	velY.setHorizontalAlignment(JTextField.RIGHT);
	this.add(velZ);
	velZ.setHorizontalAlignment(JTextField.RIGHT);
	this.add(tabPane);
	//	this.add(new JLabel("Comments"));
        pieChart.width = 300;
//         pieChart.addValue("M1A1", 6.1);
//         pieChart.addValue("T80", 15.1);
//         pieChart.addValue("T72", 10.1);
//         pieChart.addValue("HMMMV", 8.0);
//         pieChart.addValue("M1A1", 40.0);
//         pieChart.addValue("M2", 20.6);

	tabPane.add("Comments", comments);
	tabPane.add("Classification", pieChart);
	
	
 	name.addActionListener(this);
 	objectType.addActionListener(this);
 	unitType.addActionListener(this);
 	unitForceId.addActionListener(this);
 	unitSize.addActionListener(this);
 	posX.addActionListener(this);
 	posY.addActionListener(this);
 	posZ.addActionListener(this);
	sizeX.addActionListener(this);
 	sizeY.addActionListener(this);
 	orientation.addActionListener(this);
 	velX.addActionListener(this);
 	velY.addActionListener(this);
 	velZ.addActionListener(this);
	setSelected(null);
    }

    public void update(Observable  o, Object  arg) {
	// TODO: Tue Sep 20 16:57:58 EDT 2005 SRO
	//
	// Not worth doing right now, but eventually should have the
	// 'classifactions' piechart tab disappear if the MapObject is
	// not of type TYPE_UNIT, or reappear if it is.
	if(null != selectedObject) {
	    pieChart.clear();
	    //	    Debug.debug("SEAN: cleared piechart");
	    Iterator iter = selectedObject.getUnitClassificationsKeysIterator();
	    while(iter.hasNext()) {
		String key = (String) iter.next();
		//		Debug.debug("SEAN: Adding key "+key+" prob "+selectedObject.getClassificationProb(key)+" to piechart.");
		pieChart.addValue(key, selectedObject.getClassificationProb(key));
	    }
	    //	    Debug.debug("SEAN: Done adding to piechart");
	}
	//	repaint();
    }

    public void actionPerformed(ActionEvent e){
	//	debug.info("Got an action event="+e.toString());
	if(null == selectedObject) 
	    return;
	Object source = e.getSource();

	if(source == name) {
	    selectedObject.name = name.getText();
	}
	else if(source == comments) {
	    selectedObject.comments = comments.getText();
	}
	else if(source == objectType) {
	    selectedObject.setType(objectType.getSelectedIndex());
	}
	else if(source == unitType) {
	    selectedObject.unitType = unitType.getSelectedIndex();
	}
	else if(source == unitForceId) {
	    selectedObject.setForceId(unitForceId.getSelectedIndex());
	}
	else if(source == unitSize) {
	    selectedObject.unitSize = unitSize.getSelectedIndex();
	}
	else if(source == posX) {
	    try {
		selectedObject.posX = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == posY) {
	    try {
		selectedObject.posY = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == posZ) {
	    try {
		selectedObject.posZ = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == sizeX) {
	    try {
		selectedObject.sizeX = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == sizeY) {
	    try {
		selectedObject.sizeY = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == orientation) {
	    try {
		selectedObject.setOrientation(Double.parseDouble(((JTextField)source).getText()));
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == velX) {
	    try {
		selectedObject.velX = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == velY) {
	    try {
		selectedObject.velY = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else if(source == velZ) {
	    try {
		selectedObject.velZ = Double.parseDouble(((JTextField)source).getText());
	    }
	    catch(NumberFormatException ex) {
		debug.debug("Unable to double parse field value="+((JTextField)source).getText());
		((JTextField)source).setText("");
	    }
	}
	else {
	    debug.debug("Got an ActionEvent in actionPerformed, but can't find the source, event="+e.toString());
	}
	setSelected(selectedObject);
	mapDB.changed();
    }

    public void setSelected(MapObject selected) {
	selectedObject = selected;
	if(selected != null) {
	    synchronized(selectedObject) {
		name.setText(selectedObject.name);
		name.setEditable(true);
		objectType.setSelectedIndex(selectedObject.getType());
		objectType.setEnabled(true);
		if(selectedObject.isUnit()) {
		    unitType.setSelectedIndex(selectedObject.unitType);
		    unitForceId.setSelectedIndex(selectedObject.getForceId());
		    unitSize.setSelectedIndex(selectedObject.unitSize);
		    unitType.setEnabled(true);
		    unitForceId.setEnabled(true);
		    unitSize.setEnabled(true);
		}
		else if(selectedObject.hasForceId()) {
		    unitType.setSelectedIndex(UnitTypes.UNKNOWN);
		    unitForceId.setSelectedIndex(selectedObject.getForceId());
		    unitSize.setSelectedIndex(UnitSizes.UNKNOWN);
		    unitType.setEnabled(false);
		    unitForceId.setEnabled(true);
		    unitSize.setEnabled(false);
		}
		else {
		    unitType.setSelectedIndex(UnitTypes.UNKNOWN);
		    unitForceId.setSelectedIndex(ForceIds.UNKNOWN);
		    unitSize.setSelectedIndex(UnitSizes.UNKNOWN);
		    unitType.setEnabled(false);
		    unitForceId.setEnabled(false);
		    unitSize.setEnabled(false);
		}
		posX.setText(Double.toString(selectedObject.posX));
		posY.setText(Double.toString(selectedObject.posY));
		posZ.setText(Double.toString(selectedObject.posZ));
		sizeX.setText(Double.toString(selectedObject.sizeX));
		sizeY.setText(Double.toString(selectedObject.sizeY));
		orientation.setText(Double.toString(selectedObject.getOrientation()));
		velX.setText(Double.toString(selectedObject.velX));
		velY.setText(Double.toString(selectedObject.velY));
		velZ.setText(Double.toString(selectedObject.velZ));
		comments.setText(selectedObject.comments);

		objectType.setEditable(false);
		unitType.setEditable(false);
		unitForceId.setEditable(false);
		unitSize.setEditable(false);
		posX.setEditable(true);
		posY.setEditable(true);
		posZ.setEditable(true);
		sizeX.setEditable(true);
		sizeY.setEditable(true);
		orientation.setEditable(true);
		if(MapObject.TYPE_UNIT == selectedObject.getType()) {
		    velX.setEditable(true); 
		    velY.setEditable(true); 
		    velZ.setEditable(true); 
		}
		else {
		    velX.setEditable(false); 
		    velY.setEditable(false); 
		    velZ.setEditable(false); 
		}
		comments.setEditable(true);
	    }
	}
	else {
	    name.setText("");
	    name.setEditable(false);
	    objectType.setSelectedIndex(0);
	    objectType.setEditable(false);
	    objectType.setEnabled(false);
	    unitType.setSelectedIndex(0);
	    unitType.setEditable(false);
	    unitType.setEnabled(false);
	    unitForceId.setSelectedIndex(0);
	    unitForceId.setEditable(false);
	    unitForceId.setEnabled(false);
	    unitSize.setSelectedIndex(0);
	    unitSize.setEditable(false);
	    unitSize.setEnabled(false);
	    posX.setText("");
	    posX.setEditable(false);
	    posY.setText("");
	    posY.setEditable(false);
	    posZ.setText("");
	    posZ.setEditable(false);
	    sizeX.setText("");
	    sizeX.setEditable(false);
	    sizeY.setText("");
	    sizeY.setEditable(false);
	    orientation.setText("");
	    orientation.setEditable(false);
	    velX.setText("");
	    velX.setEditable(false);
	    velY.setText("");
	    velY.setEditable(false);
	    velZ.setText("");
	    velZ.setEditable(false);
	    comments.setText("");
	    comments.setEditable(false);
	}
	repaint();
    }
   
//     public void paintComponent(Graphics g) {
//     }
    
    
}
