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
// @version     $Id: PaletteTerrainPanel.java,v 1.5 2007/12/12 19:46:36 junyounk Exp $ 

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class PaletteTerrainPanel extends JPanel {
    public static boolean CLEAN_MODE = true;
    public static boolean SHOW_CONTROLLERS = true;

    private final static String DELETE_AC = "Delete";
    private final static String MODE_MENU_AC = "Mode";
    private final static String DEFAULT_AC = "Default";
    private final static String MULTISTRIKE_AC = "MultiStrike";
    private final static String PICTUREORDER_AC = "PictureOrder";
    private final static String SOIL_AC = "Soil Painter";
    private final static String EMPTY_AC = "Empty";
    private final static String ZOOM_AC = "Zoom";
    private final static String MULTISELECT_AC = "Select Units";
    private final static String MAPGRAPHIC_AC = "Map Graphics";
    private final static String PATROL_AC = "Patrol Zones";
    private final static String NOGO_AC = "Nogo Zones";
    private final static String ROUTE_AC = "Routes";

    private final static String DEFAULT_MSG = "DEFAULT MODE: Left click and drag to create lines.  Right click to delete.";
    private final static String MULTISTRIKE_MSG = "MULTISTRIKE MODE: Left click to place or select strike targets, left click and drag to move, right click to delete.";  
    private final static String PICTUREORDER_MSG = "PICTURE ORDER MODE: Left click to place or select picture locations, left click and drag to move, right click to delete.";  
    private final static String ZOOM_MSG = "ZOOM MODE: Left click and drag to select an area to zoom on.  Middle click to zoom out.  Right click to reset zoom to entire map.";
    private final static String SOIL_MSG = "SOIL PAINT MODE: Left click and drag to paint asphalt on map.";
    private final static String EMPTY_MSG = "EMPTY MODE: This mode is for development testing and should not be used.";
    private final static String MULTISELECT_MSG = "SELECT UNITS MODE: Left click (or left click and drag) to select new, shift-left click to add to selection, alt-shift-left click to remove from selection.";
    private final static String MAPGRAPHIC_MSG = "MAPGRAPHIC MODE: Left click and drag to draw a box, rigth click to remove.";
    private final static String PATROL_MSG = "PATROL ZONES MODE: Left click and drag to draw a box, rigth click to remove.";
    private final static String NOGO_MSG = "NOGO ZONES MODE: Left click and drag to draw a box, rigth click to remove.";
    private final static String ROUTE_MSG = "ROUTES MODE: Left click and drag to draw a route line, rigth click to remove.";

    private final static String DEFAULT_TOOLTIP = "Set mode to DEFAULT - draw and delete lines.";
    private final static String MULTISTRIKE_TOOLTIP = "Set mode to MULTISTRIKE - place multiple strike targets on the map and then execute strikes by clicking Strike button.";
    private final static String PICTUREORDER_TOOLTIP = "Set mode to PICTUREORDER - place multiple picture orders on the map and then task them by clicking Order Picture button.";
    private final static String ZOOM_TOOLTIP = "Set mode to ZOOM - select regions of the terrain to zoom in on.";
    private final static String SOIL_TOOLTIP = "Set mode to SOIL PAINT - update soil types in the soil grid.";
    private final static String EMPTY_TOOLTIP = "Set mode to EMPTY - this mode is for development testing and should not be used.";
    private final static String MULTISELECT_TOOLTIP = "Set mode to SELECT UNITS - select one or more units.";
    private final static String MAPGRAPHIC_TOOLTIP = "Set mode to MAP GRAPHICS - draw boxes on the map for patrol, nogo regions, etc.";
    private final static String PATROL_TOOLTIP = "Set mode to PATROL ZONES - draw boxes on the map to specify regions for UAVs to patrol";
    private final static String NOGO_TOOLTIP = "Set mode to NOGO ZONES - draw boxes on the map to specify regions for UAVs to avoid";
    private final static String ROUTE_TOOLTIP = "Set mode to ROUTES - draw route lines on the map to specify routes";

    private DebugInterface debug = null;

    private JFrame myFrame;
    private MapDB mapDB = null;
    private Background background = null;
    private BackgroundConfig config = null;
    private IntGrid soilGrid = null;
    
    private JPanel actionButtonPanel = null;

    private TerrainCanvas terrainCanvas = null;
    public TerrainCanvas getTerrainCanvas() { return terrainCanvas;}
    private JPanel bottomButtonPanel = null;
    private ConfigPanel configButtonPanel = null;
    private ControllerManager controllerMan;
    private ButtonManager buttonMan;
    private JLabel statusMsgLabel;
    private JPanel statusMsgPanel;
    private PalettePanel palettePanel;
    private ControllerDefault ctrlDefault;
    private ControllerMultiStrike ctrlMultiStrike;
    private ControllerCreateMapObject ctrlOrderPicture;
    private ControllerSoil ctrlSoil;
    private ControllerEmpty ctrlEmpty;
    private ControllerZoom ctrlZoom;
    private ControllerMultiSelect ctrlMultiSelect;
    private ControllerMapGraphic ctrlMapGraphic;
    private ControllerPatrol ctrlPatrol;
    private ControllerNogo ctrlNogo;
    private ControllerLine ctrlRoute;
    private JSplitPane topBottomSplit = null;
    private JMenuBar menuBar;
    private JMenu controllerMenu;
    private JPopupMenu mainPopup;
    private JPopupMenu controllerPopup;
    private JPanel drawingPanel;

    public class RadioAction extends AbstractAction {
        public RadioAction(String text, ImageIcon icon,
                          String desc, Integer mnemonic) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
	    debug.debug("Action performed ="+e.toString());
	    controllerMan.setActive(e.getActionCommand());
	    buttonMan.setSelected(e.getActionCommand());
        }
    }

    private AbstractButton makeMenuItem(String s, int mnemonic, boolean selected) {
	JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(s);
	menuItem.setMnemonic(mnemonic);
	menuItem.setActionCommand(s);
	menuItem.setSelected(selected);
	buttonMan.addButton(menuItem);
	menuItem.addActionListener(new RadioAction(s, null, "", new Integer(mnemonic)));
	//	menuItem.addActionListener(radioAction);
	return menuItem;
    }

    private AbstractButton makeMainMenuItem(String s, int mnemonic) {
	JMenuItem menuItem = new JMenuItem(s);
	menuItem.setMnemonic(mnemonic);
	menuItem.setActionCommand(s);
	menuItem.addActionListener(new RadioAction(s, null, "", new Integer(mnemonic)));
	return menuItem;
    }

    public void addTwoPanelZoomController(ViewPort zoomViewPort, double width, double height) {
	ControllerTwoPanelZoom twoPanelZoom = new ControllerTwoPanelZoom(mapDB, config, terrainCanvas, null, zoomViewPort, width, height);
	Controller twoPanelZoomC = new Controller("TWOPANELZOOM", "", "", twoPanelZoom, twoPanelZoom);
	controllerMan.addController(twoPanelZoomC);
	controllerMan.setActive("TWOPANELZOOM");
    }

    public PaletteTerrainPanel(JFrame myFrame, MapDB mapDB, Background background, BackgroundConfig config, JPanel actionButtonPanel) {
	this.myFrame = myFrame;
	this.mapDB = mapDB;
	this.background = background;
	this.config = config;
	this.actionButtonPanel = actionButtonPanel;
        //this.drawingPanel = drawingPanel;
	
	this.soilGrid = background.getSoilGrid();
	
	debug = new DebugFacade(this);

 	this.setLayout(new BorderLayout());
	Border loweredetched;
	loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

	JPanel bottomButtonPanel = new JPanel(new BorderLayout());
	terrainCanvas = new TerrainCanvas(mapDB, config, background);
	config.viewPort.requestSourceViewFitToScreen(soilGrid);
	
	statusMsgLabel = new JLabel("Status Bar");
	controllerMan = new ControllerManager(terrainCanvas, statusMsgLabel);
	buttonMan = new ButtonManager();

	JPanel paletteAndStatus = new JPanel();
 	paletteAndStatus.setLayout(new BorderLayout());
	statusMsgPanel = new JPanel();
	statusMsgPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	statusMsgPanel.add(statusMsgLabel);

	palettePanel = new PalettePanel(buttonMan, controllerMan);
	palettePanel.setBorder(loweredetched);

	if(SHOW_CONTROLLERS) {
	    paletteAndStatus.add(statusMsgPanel, BorderLayout.NORTH);
	    paletteAndStatus.add(palettePanel, BorderLayout.SOUTH);
	}

	if(!CLEAN_MODE) {
	    ctrlDefault = new ControllerDefault(mapDB,config,terrainCanvas,null,MapObject.TYPE_LINE);
	    Controller defC = new Controller(DEFAULT_AC, DEFAULT_MSG, DEFAULT_TOOLTIP, ctrlDefault, ctrlDefault);
	    palettePanel.addController(false, DEFAULT_AC, null, "Default Controller", KeyEvent.VK_D,DEFAULT_AC, defC);
	}

	ctrlMultiStrike = new ControllerMultiStrike(mapDB,config,terrainCanvas,null);
	Controller multistrikeC = new Controller(MULTISTRIKE_AC, MULTISTRIKE_MSG, MULTISTRIKE_TOOLTIP, ctrlMultiStrike, ctrlMultiStrike);
	palettePanel.addController(false, MULTISTRIKE_AC, null, "Multi Strike Controller", KeyEvent.VK_M,MULTISTRIKE_AC, multistrikeC);

	ctrlOrderPicture = new ControllerCreateMapObject(mapDB,config,terrainCanvas,null, MapObject.TYPE_PICTURE_ORDER, null);
	Controller orderPictureC = new Controller(PICTUREORDER_AC, PICTUREORDER_MSG, PICTUREORDER_TOOLTIP, ctrlOrderPicture, ctrlOrderPicture);
	palettePanel.addController(false, PICTUREORDER_AC, null, "Order Picture Controller", KeyEvent.VK_M,PICTUREORDER_AC, orderPictureC);

	if(!CLEAN_MODE) {
	    ctrlSoil = new ControllerSoil(config, background, soilGrid);
	    Controller soilC = new Controller(SOIL_AC, SOIL_MSG, SOIL_TOOLTIP, ctrlSoil, ctrlSoil);
	    palettePanel.addController(false, SOIL_AC, null, "Soil Paint Controller", KeyEvent.VK_S,SOIL_AC, soilC);
	}

	if(!CLEAN_MODE) {
	    ctrlEmpty = new ControllerEmpty(mapDB,config,null);
	    Controller emptyC = new Controller(EMPTY_AC, EMPTY_MSG, EMPTY_TOOLTIP, ctrlEmpty, ctrlEmpty);
	    palettePanel.addController(false, EMPTY_AC, null, "Empty Controller", KeyEvent.VK_E,EMPTY_AC, emptyC);
	}

	ctrlZoom = new ControllerZoom(mapDB,config, terrainCanvas, soilGrid);
	Controller zoomC = new Controller(ZOOM_AC, ZOOM_MSG, ZOOM_TOOLTIP, ctrlZoom, ctrlZoom);
	palettePanel.addController(false, ZOOM_AC, null, "Zoom", KeyEvent.VK_E,ZOOM_AC, zoomC);

	ctrlMultiSelect = new ControllerMultiSelect(mapDB,config, terrainCanvas, soilGrid);
	Controller multiSelectC = new Controller(MULTISELECT_AC, MULTISELECT_MSG, MULTISELECT_TOOLTIP, ctrlMultiSelect, ctrlMultiSelect);
	palettePanel.addController(true, MULTISELECT_AC, null, "MultiSelect", KeyEvent.VK_E,MULTISELECT_AC, multiSelectC);

	if(!CLEAN_MODE) {
	    ctrlMapGraphic = new ControllerMapGraphic(mapDB,config, terrainCanvas, soilGrid);
	    Controller mapGraphicC = new Controller(MAPGRAPHIC_AC, MAPGRAPHIC_MSG, MAPGRAPHIC_TOOLTIP, ctrlMapGraphic, ctrlMapGraphic);
	    palettePanel.addController(false, MAPGRAPHIC_AC, null, "MapGraphic", KeyEvent.VK_E,MAPGRAPHIC_AC, mapGraphicC);
	}

	ctrlPatrol = new ControllerPatrol(mapDB,config, terrainCanvas, soilGrid);
	Controller patrolC = new Controller(PATROL_AC, PATROL_MSG, PATROL_TOOLTIP, ctrlPatrol, ctrlPatrol);
	palettePanel.addController(false, PATROL_AC, null, "Patrol", KeyEvent.VK_E,PATROL_AC, patrolC);

	ctrlNogo = new ControllerNogo(mapDB,config, terrainCanvas, soilGrid);
	Controller nogoC = new Controller(NOGO_AC, NOGO_MSG, NOGO_TOOLTIP, ctrlNogo, ctrlNogo);
	palettePanel.addController(false, NOGO_AC, null, "Nogo", KeyEvent.VK_E,NOGO_AC, nogoC);

	ctrlRoute = new ControllerLine(mapDB,config,terrainCanvas,null,MapObject.TYPE_LINE);
	Controller routeC = new Controller(ROUTE_AC, ROUTE_MSG, ROUTE_TOOLTIP, ctrlRoute, ctrlRoute);
//	palettePanel.addController(false, ROUTE_AC, null, "Route Controller", KeyEvent.VK_D,ROUTE_AC, routeC);

	actionButtonPanel.setBorder(loweredetched);
 	configButtonPanel = new ConfigPanel(config);
	configButtonPanel.setBorder(loweredetched);
	bottomButtonPanel.add(paletteAndStatus, BorderLayout.NORTH);
	bottomButtonPanel.add(actionButtonPanel, BorderLayout.CENTER);
	bottomButtonPanel.add(configButtonPanel, BorderLayout.SOUTH);

	topBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, terrainCanvas, bottomButtonPanel);
	topBottomSplit.setOneTouchExpandable(true);
	topBottomSplit.setResizeWeight(1.0);
	
	add(topBottomSplit, BorderLayout.CENTER);

	if(SHOW_CONTROLLERS) {
	    menuBar = new JMenuBar();
	    controllerMenu = new JMenu("Controller");
	    if(!CLEAN_MODE) {
		controllerMenu.add(makeMenuItem(DEFAULT_AC, KeyEvent.VK_B, false));
	    }
	    controllerMenu.add(makeMenuItem(MULTISTRIKE_AC, KeyEvent.VK_C, false));
	    controllerMenu.add(makeMenuItem(PICTUREORDER_AC, KeyEvent.VK_C, false));
	    if(!CLEAN_MODE) {
		controllerMenu.add(makeMenuItem(SOIL_AC, KeyEvent.VK_C, false));
	    }
	    if(!CLEAN_MODE) {
		controllerMenu.add(makeMenuItem(EMPTY_AC, KeyEvent.VK_E, false));
	    }
	    controllerMenu.add(makeMenuItem(ZOOM_AC, KeyEvent.VK_Z, false));
	    controllerMenu.add(makeMenuItem(MULTISELECT_AC, KeyEvent.VK_M, true));
	    if(!CLEAN_MODE) {
		controllerMenu.add(makeMenuItem(MAPGRAPHIC_AC, KeyEvent.VK_G, false));
	    }
	    controllerMenu.add(makeMenuItem(PATROL_AC, KeyEvent.VK_P, false));
	    controllerMenu.add(makeMenuItem(NOGO_AC, KeyEvent.VK_N, false));
	    controllerMenu.add(makeMenuItem(ROUTE_AC, KeyEvent.VK_R, false));
	    menuBar.add(controllerMenu);
	    myFrame.setJMenuBar(menuBar);
	}

	controllerPopup = new JPopupMenu();
	if(!CLEAN_MODE) {
	    controllerPopup.add(makeMenuItem(DEFAULT_AC, KeyEvent.VK_B, false));
	}
	controllerPopup.add(makeMenuItem(MULTISTRIKE_AC, KeyEvent.VK_C, false));
	controllerPopup.add(makeMenuItem(PICTUREORDER_AC, KeyEvent.VK_C, false));
	if(!CLEAN_MODE) {
	    controllerPopup.add(makeMenuItem(SOIL_AC, KeyEvent.VK_C, false));
	}
	if(!CLEAN_MODE) {
	    controllerPopup.add(makeMenuItem(EMPTY_AC, KeyEvent.VK_E, false));
	}
	controllerPopup.add(makeMenuItem(ZOOM_AC, KeyEvent.VK_Z, false));
	controllerPopup.add(makeMenuItem(MULTISELECT_AC, KeyEvent.VK_M, true));
	if(!CLEAN_MODE) {
	    controllerPopup.add(makeMenuItem(MAPGRAPHIC_AC, KeyEvent.VK_G, false));
	}
	controllerPopup.add(makeMenuItem(PATROL_AC, KeyEvent.VK_G, false));
	controllerPopup.add(makeMenuItem(NOGO_AC, KeyEvent.VK_G, false));
	controllerPopup.add(makeMenuItem(ROUTE_AC, KeyEvent.VK_R, false));

	mainPopup = new JPopupMenu();
// 	mainPopup.add(makeMainMenuItem(DELETE_AC, KeyEvent.VK_D));
	mainPopup.add(controllerPopup);

	terrainCanvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if(e.isPopupTrigger()) {
                    mainPopup.show(terrainCanvas, e.getX()-40, e.getY()-10);
                }
            }
            public void mouseReleased(MouseEvent e) {
                if(e.isPopupTrigger()) {
                    mainPopup.show(terrainCanvas, e.getX()-40, e.getY()-10);
                }
            }
        });

	ctrlZoom.setDisplayDetail();
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
    }

}
