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
// @version     $Id: TerrainPanel.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

public class TerrainPanel extends JPanel implements Observer {
    // @todo: make this configured dynamically somehow...  really this
    // class is small enough I should just make a version which
    // doesn't create a mapObjectPanel
    public static boolean CREATE_MAP_OBJECT_PANEL = true;

    private DebugInterface debug = null;
    private MapDB mapDB = null;
    private Background background = null;
    private BackgroundConfig config = null;
    private JPanel bottomButtonPanel = null;
    private ConfigPanel configButtonPanel = null;
    private JPanel actionButtonPanel = null;
    private MapObjectPanel rightMapObjectPanel = null;
    private TerrainCanvas leftMapPanel = null;
    private ControllerDefault controller = null;
    public TerrainCanvas getTerrainCanvas() { return leftMapPanel;}
    private JSplitPane topBottomSplit = null;
    private JSplitPane leftRightSplit = null;
    private boolean needToResize = true;

    public void setProbSignificantMin(double value) { leftMapPanel.setProbSignificantMin(value);}

    public void update(Observable  o, Object  arg)
    {
	repaint();
    }

    public TerrainPanel(MapDB mapDB, Background background, BackgroundConfig config, JPanel actionButtonPanel) {
	debug = new DebugFacade(this);
	this.mapDB = mapDB;
	this.background = background;
	this.config = config;

	Border raisedetched, loweredetched, raisedbevel, loweredbevel;
	raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
	loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
	raisedbevel = BorderFactory.createRaisedBevelBorder();
	loweredbevel = BorderFactory.createLoweredBevelBorder();

 	this.setLayout(new BorderLayout());

	JPanel bottomButtonPanel = new JPanel(new BorderLayout());
	actionButtonPanel.setBorder(loweredetched);
	//	actionButtonPanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Actions"));
 	configButtonPanel = new ConfigPanel(config);
	//	configButtonPanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Config"));
	configButtonPanel.setBorder(loweredetched);
	bottomButtonPanel.add(configButtonPanel, BorderLayout.CENTER);
	bottomButtonPanel.add(actionButtonPanel, BorderLayout.SOUTH);

	if(CREATE_MAP_OBJECT_PANEL)
	    rightMapObjectPanel = new MapObjectPanel(mapDB);
	leftMapPanel = new TerrainCanvas(mapDB, config, background);
	controller = new ControllerDefault(mapDB,config,leftMapPanel,rightMapObjectPanel);
	leftMapPanel.addMouseListener(controller);
	leftMapPanel.addMouseMotionListener(controller);

	leftRightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMapPanel, rightMapObjectPanel);
	leftRightSplit.setOneTouchExpandable(true);

	topBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftRightSplit, bottomButtonPanel);
	topBottomSplit.setOneTouchExpandable(true);

	topBottomSplit.setResizeWeight(.15);
	leftRightSplit.setResizeWeight(.9);
	
	add(topBottomSplit, BorderLayout.CENTER);
    }


    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	
	if(needToResize) {
	    needToResize = false;
	    //	    int cwidth = getSize().width - 20;
	    //	    int cheight = getSize.
	    //		Dimension csize = new Dimension(getSize().width - 50, 0);
	    //	    configButtonPanel.setPreferredSize(csize);
	    //	Dimension tsize = new Dimension(0, getSize().height);
	    //	leftMapPanel.setPreferredSize(tsize);
	    //	topBottomSplit.resetToPreferredSizes();
	    //	leftRightSplit.resetToPreferredSizes();
	}
    }

}
