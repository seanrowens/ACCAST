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
/*
 * MainFrameGUI.java
 *
 * Created on Tue Apr 11 15:42:32 EDT 2006
 */

package AirSim.Environment.GUI;

import AirSim.Environment.*;
import AirSim.Environment.Assets.*;
import AirSim.Environment.Assets.Sensors.*;
import AirSim.Environment.Assets.Tasks.*;
import Machinetta.Communication.UDPCommon;
import AirSim.Environment.Buildings.*;

import Gui.*;
//import com.sun.org.apache.bcel.internal.generic.ExceptionThrower;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.text.DecimalFormat;





public class MainFrameGUI {
    private final static boolean SHOW_BEAM_CHECKBOX = false;
    private final static boolean SHOW_EMITTER_CHECKBOX = false;
    private final static boolean SHOW_DESTS_CHECKBOX = false;
    private final static boolean SHOW_CONTROLLER_BUTTONS = false;
    public final static String ICON_DIR="/usr0/sanjaya/terrain_gui_icons";

    public static boolean setViewPort = false;
    public static double viewPortX = 0;
    public static double viewPortY = 0;
    public static double viewPortWidth = 0;
    public static double viewPortHeight = 0;
    public static boolean soilTypes = false;
    public static boolean showTraces = true;
    public static int contourMultiples = BackgroundConfig.CONTOUR_MULT_100;
    public static boolean gridLinesOneKm = true;
    public static boolean showMapObjectNames = true;
    public static boolean showMapObjectTypes = false;
    public static boolean markXplaneAsset = false;

    private MainFrame mainframe;
    private int repaintIntervalMs;

    private JFrame topLevelFrame = null;
    private Gui.Background background = null;
    private BackgroundConfig config = null;
    private JPanel actionButtonPanel = null;
    private MapDB mapDB = null;
    private PaletteTerrainPanel terrainPanel = null;
    private RepaintTimer repaintTimer = null;
    private JButton SCButton = null;
    private JToggleButton runC=null;
    private JSlider speedC = null;
    private JLabel speedLabel = null;
    private JLabel actualSpeedLabel = null;
    
    private DecimalFormat fmt = new DecimalFormat("  0.00");
    // NOTE: Only call from inside swing thread.
    private void setSpeedLabel() {
	double perSec = 1000.0 / (double)MainFrame.UPDATE_RATE;
	speedLabel.setText(fmt.format(perSec)+" steps/sec");
    }

    
    public void setActualSpeedLabel(double actualStepsPerSecond) {
	final double factualStepsPerSecond = actualStepsPerSecond;
	try {
	    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			actualSpeedLabel.setText(fmt.format(factualStepsPerSecond)+" actual steps/sec");
		    }
		});
	} catch (Exception e) {
	    Machinetta.Debugger.debug(1,"setActualSpeedLabel: exception e="+e);
	    e.printStackTrace();
	}
    }

    // Various flags for changing how things are displayed
    public static boolean showDest = false;
    public static boolean showEmitters = false;
    public static boolean showBeams = false;
    public static boolean showRoads = true;

    private Rectangle opforFlag = null;
    private Rectangle blueforFlag = null;

    private int curSCNum = 0;
    
    // Access to what is going on - singleton
    Env env = new Env();

    public MainFrameGUI(MainFrame mainframe, int repaintIntervalMs, Rectangle opforFlag, Rectangle blueforFlag) {
	Machinetta.Debugger.debug("Constructing GUI",1, this);
	this.mainframe = mainframe;
	this.repaintIntervalMs = repaintIntervalMs;
	this.opforFlag = opforFlag;
	this.blueforFlag = blueforFlag;
	makeTerrainGui();
	repaintTimer.start();
	Machinetta.Debugger.debug("Done constructing GUI",1, this);
    }

    private void loadIconsOriginal() {
	String baseDir=ICON_DIR+"/original";
	UnitTypes.setImageIcon(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	UnitTypes.setImageIcon(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	UnitTypes.setImageIcon(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	UnitTypes.setImageIcon(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    private void loadIcons50() {
	String baseDir=ICON_DIR+"/scale50";
	UnitTypes.setImageIcon(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	UnitTypes.setImageIcon(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	UnitTypes.setImageIcon(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	UnitTypes.setImageIcon(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    private void loadIcons25() {
	String baseDir=ICON_DIR;
	UnitTypes.setImageIcon(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	UnitTypes.setImageIcon(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	UnitTypes.setImageIcon(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	UnitTypes.setImageIcon(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }
   
    private void setSCButtonText() {
	String nextScenarioName=null;
	if(this.curSCNum < env.getScenarios().size()) {
	    String nextScenario = env.getScenario(this.curSCNum);
	    //            Machinetta.Debugger.debug(1, "Setting button text for Cur Scenario Str: " + nextScenario);
	    
	    StringTokenizer tok2 = new StringTokenizer(nextScenario, "\n");
	    if(tok2.hasMoreTokens()) {
		String line = tok2.nextToken();
		if(line.startsWith("*SCENARIO ")) {
		    nextScenarioName = line.substring(10);
		    Machinetta.Debugger.debug(1, "cur scenario name="+nextScenarioName);
		}
	    }
	}
	if(null != nextScenarioName) 
	    SCButton.setText(nextScenarioName);
	else 
	    SCButton.setEnabled(false);
    }

    private void SCButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if(this.curSCNum < env.getScenarios().size()) {
            String scenarioStr = env.getScenario(this.curSCNum);
        
            //Machinetta.Debugger.debug(1, "Cur Scenario Str: " + scenarioStr);
            StringTokenizer tok = new StringTokenizer(scenarioStr, "\n");            
            while(tok.hasMoreTokens()) {
                String line = (tok.hasMoreTokens()? tok.nextToken(): "###");
                env.process(line);
            }
            
            this.curSCNum++;
	    setSCButtonText();
        } else {
            Machinetta.Debugger.debug(1, "ERROR: No more scenarios!!!!!!!!");
        }
        
    }
        
    private void RunFastButtonActionPerformed(java.awt.event.ActionEvent evt) {
	if(!MainFrame.FAST_UPDATE_ON) {
	    Machinetta.Debugger.debug(1, "FAST_UPDATE: User clicked Run Fast button!");
	    MainFrame.FAST_UPDATE_ON = true;
	    MainFrame.OLD_UPDATE_RATE = MainFrame.UPDATE_RATE;
	    MainFrame.FAST_UPDATE_RATE = MainFrame.FAST_UPDATE_RATE;
	}
    }
        
    public void unpause() {
	try {
	    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			runC.setSelected(!MainFrame.paused);
		    }
		});
	} catch (Exception e) {
	    System.err.println("unpause invokeandwait didn't successfully complete");
	}
    }

    public void makeTerrainGui() {
	//	loadIcons25();
        JPanel topPanel = null;

	topLevelFrame = new JFrame("Sanjaya Ground Truth GUI");
	topLevelFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {mainframe.shutdown("Window closed.");}
	    });
	// For some reason I don't understand, if I just use 0,0 here
	// (the top left of the screen), xwin-32 puts the window
	// slightly off the screen to the top and the left.
 	topLevelFrame.setLocation(5,30);
 	//	topLevelFrame.setLocation(0,0);
 	topLevelFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
	// For when we're using a laptop with dual GUIs
	// 	topLevelFrame.setLocation(1280,30);
	// 	topLevelFrame.setSize(1280,1024);
	topPanel = new JPanel();
	topPanel.setLayout( new BorderLayout() );
	topLevelFrame.getContentPane().add( topPanel );
       
        // ----------------------------------------------------------------------
        // Build the obstacle grids - slope and soil type based.
        // ----------------------------------------------------------------------
        Obstacles obstacles = null;
        IntGrid obstacleGrid = null;
        obstacles = new Obstacles(env.getElevationGrid(), env.getSoilGrid());
        obstacleGrid = obstacles.getObstacleGrid();
        
        // ----------------------------------------------------------------------
        // Build the contour lines.
        // ----------------------------------------------------------------------
        IntGrid contourPolygonsGrid = null;
        Contour contours = null;
        contourPolygonsGrid = new IntGrid(obstacleGrid);
        contourPolygonsGrid.clear();
        contours = new Contour(env.getElevationGrid());
        contours.compute(25.0);
        
	ViewPort viewPort = new ViewPort(env.getSoilGrid());
        mapDB = new MapDB(env.getSoilGrid().getGccToTccTransform(), env.getSoilGrid().getTccToGccTransform(), env.getXGrid(), env.getYGrid(), env.getZGrid());
        
        //background = new Background(env.getElevationGrid(), env.getSoilGrid(), contourPolygonsGrid, obstacleGrid, null, contours);
        background = new Gui.Background(env.getElevationGrid(), env.getSoilGrid(), contourPolygonsGrid, obstacleGrid, null, contours, env.getRoadData());
        config = new BackgroundConfig(viewPort);

        actionButtonPanel = new JPanel();
        // Using variable doesn't work
	speedC = new JSlider(1, MainFrame.MAX_STEPS_PER_SECOUND, MainFrame.DEFAULT_SPEED_SLIDER_VAL);
	speedLabel = new JLabel();
	setSpeedLabel();
	actualSpeedLabel = new JLabel();

	runC = new JToggleButton("Run", !MainFrame.paused);
        final JCheckBox showDestsC = new JCheckBox("Show Dests", showDest);
        final JCheckBox showBeamsC = new JCheckBox("Show Beams", showBeams);
        final JCheckBox showEmittersC = new JCheckBox("Show Emitters", showEmitters);
	SCButton = new JButton();
        final JButton RunFastButton = new JButton();
        
	setSCButtonText();
        actionButtonPanel.add(SCButton);
	SCButton.setSize(200,30);
        SCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SCButtonActionPerformed(evt);
            }
        });
        
//         actionButtonPanel.add(RunFastButton);
        RunFastButton.setText("Run Fast Until Event");
        RunFastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunFastButtonActionPerformed(evt);
            }
        });
        
        actionButtonPanel.add(runC);
        runC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MainFrame.paused = !runC.isSelected();
		Machinetta.Debugger.debug("Simulation 'paused' set to "+MainFrame.paused,1, this);		
            }
        });
        actionButtonPanel.add(speedC);
        speedC.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
		mainframe.setUpdateRate(speedC.getValue());
		setSpeedLabel();
            }
        });
        actionButtonPanel.add(speedLabel);
        actionButtonPanel.add(actualSpeedLabel);
        
	if(SHOW_DESTS_CHECKBOX) {
	actionButtonPanel.add(showDestsC);
        showDestsC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDest = showDestsC.isSelected();
            }
        });
	}
        
	if(SHOW_BEAM_CHECKBOX) {
        actionButtonPanel.add(showBeamsC);
        showBeamsC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showBeams = showBeamsC.isSelected();
            }
        });
	}
        
	if(SHOW_EMITTER_CHECKBOX) {
        actionButtonPanel.add(showEmittersC);
	showEmittersC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showEmitters = showEmittersC.isSelected();
            }
        });
	}
        
        PaletteTerrainPanel.CLEAN_MODE = true;	
	PaletteTerrainPanel.SHOW_CONTROLLERS = true;
	terrainPanel = new PaletteTerrainPanel(topLevelFrame, mapDB,background,config, actionButtonPanel);
	repaintTimer = new RepaintTimer(terrainPanel, mapDB, repaintIntervalMs);
        
	if(setViewPort)
	    config.viewPort.requestSourceView(viewPortX,viewPortY,viewPortWidth,viewPortHeight);
 	config.soilTypes = false;
 	config.showTraces = showTraces;
 	config.contourMultiples = contourMultiples;
	config.gridLinesOneKm = gridLinesOneKm;
	config.showMapObjectNames = showMapObjectNames;
	config.showMapObjectTypes = showMapObjectTypes;
        config.showRoads = showRoads;

        //terrainPanel.add(drawingPanel);
        
        topPanel.add(terrainPanel);
        // Note, DON'T use .pack() - or we undo all the
        // setSize/setLocation crap above.
        
        topLevelFrame.setVisible(true);
    }
    
    HashMap<String,MapObject> detMap = new HashMap<String,MapObject>();
    private void drawAsset(Asset asset) {
	if(!asset.isVisibleOnGT()) return;
            
	String id = asset.getID();

	// @TODO: we really should break out all of the asset types,
	// unit types, forceid stuff into a separate source tree.
	int forceid = 0;
	if(asset.getForceId() == ForceId.BLUEFOR)
	    forceid = ForceIds.BLUEFOR;
	else if(asset.getForceId() == ForceId.OPFOR)
	    forceid = ForceIds.OPFOR;
	else if(asset.getForceId() == ForceId.NEUTRAL)
	    forceid = ForceIds.NEUTRAL;
	else
	    forceid = ForceIds.UNKNOWN;

	int bda = MapObject.BDA_ALIVE;
	if (asset.state == State.LIVE)
	    bda = MapObject.BDA_ALIVE;
	else
	    bda = MapObject.BDA_DEAD;

	double posx = asset.location.x;
	double posy = asset.location.y;

	// draw any detonations
 	if(asset.isDetonated()) {
 	    String detId = id+".detonation";
 	    MapObject detMo = detMap.get(detId);
 	    if(null == detMo) {
 		detMo = new MapObject(detId, MapObject.TYPE_DETONATION, posx, posy, 0.0, 100.0, 0.0);
                detMo.setName("");
 		mapDB.add(detMo);
 		mapDB.setDirty(true);
		detMap.put(detId, detMo);
 	    }            
 	}

	// draw any direct fire lines
 	if(!(State.DESTROYED == asset.state) &&  asset.isDirectFiring()) {
 	    String dfId = id+".directfire";
 	    MapObject dfMo = mapDB.get(dfId);
 	    if(null == dfMo) {
 		dfMo = new MapObject(dfId, MapObject.TYPE_LINE, posx, posy, 0.0, 0.0, 0.0);
		dfMo.setName("");
		dfMo.addLinePoint(posx, posy);
		Vector3D dfTarget = asset.getDirectFireTarget();
		dfMo.addLinePoint(dfTarget.x, dfTarget.y);
		double dist = asset.location.toVectorLength(dfTarget);
		Machinetta.Debugger.debug(1,"asset "+asset.getID()+" direct firing at target asset "+asset.getDirectFireTargetAsset().getID()+" at range of "+fmt.format(dist));
 		mapDB.add(dfMo);
 		mapDB.setDirty(true);
 	    }
	    asset.setDirectFiring(false);
 	}
	else { 
 	    String dfId = id+".directfire";
	    mapDB.remove(dfId);
	}

	
	MapObject mo = mapDB.get(id);
	if(null == mo) {

	    // Get the unit type.
	    String name = id;
	    int unitType = UnitTypes.ARMOR;
	    if(asset instanceof A10) {
		unitType = UnitTypes.AIR_FORCES;
		name = "A10 "+name;
	    } else if(asset instanceof C130) {
		unitType = UnitTypes.AIR_FORCES;
		name = "C130 "+name;
	    } else if(asset instanceof F14) {
		unitType = UnitTypes.AIR_FORCES;
		name = "F14 "+name;
	    } else if(asset instanceof F15) {
		unitType = UnitTypes.AIR_FORCES;
		name = "F15 "+name;
	    } else if(asset instanceof F16) {
		unitType = UnitTypes.AIR_FORCES;
		name = "F16 "+name;
	    } else if(asset instanceof F18) {
		unitType = UnitTypes.AIR_FORCES;
		name = "F18 "+name;
	    } else if(asset instanceof M1A1Tank) {
		unitType = UnitTypes.ARMOR;
		name = "M1A1 "+name;
	    } else if(asset instanceof M2Tank) {
		unitType = UnitTypes.ARMOR;
		name = "M2 "+name;
	    } else if(asset instanceof SA9) {
		unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
		name = "M2 "+name;
	    } else if(asset instanceof Hummer) {
		unitType = UnitTypes.MECH_INFANTRY;
		name = "HMV "+name;
	    } else if(asset instanceof Truck) {
		unitType = UnitTypes.CIVILIAN_TRUCK;
		name = "Truck "+name;
		forceid = ForceIds.NEUTRAL;
	    } else if(asset instanceof SAMissile) {
		unitType = UnitTypes.SA_MISSILE;
		name = "SAM "+name;
	    } else if(asset instanceof ASMissile) {
		unitType = UnitTypes.SA_MISSILE;	// Don't have a mapobject type AS_MISSILE, so use SA_MISSLE
		name = "ASM "+name;
	    } else if(asset instanceof SSMissile) {
		unitType = UnitTypes.SS_MISSILE;
		name = "SSM "+name;
	    } else if(asset instanceof SmallUAV) {
		unitType = UnitTypes.MUAV;
		name = "MUAV "+name;
	    } else if(asset instanceof Civilian){
		unitType = UnitTypes.CIVILIAN;
		name ="CIV "+name;
	    } else if(asset instanceof FAARP){
		unitType = UnitTypes.MILITARY_TRUCK;
		name ="FAR "+name;
	    } else if(asset instanceof Infantry){
		unitType = UnitTypes.LIGHT_INFANTRY;
		name ="INF "+name;
	    } else if(asset instanceof WASM)
		unitType = UnitTypes.WASM;
	    else if(asset instanceof Emitter){
		unitType = UnitTypes.EMITTER;
		name ="RFEmit"+name;
	    } else if(asset instanceof UnattendedGroundSensor) {
		unitType = UnitTypes.SENSOR;
		name ="SNSR "+name;
	    }
            /* Note: Do not put descendant of Tank/MannedAircraft
             * after this else statement or they will never be
             * reached.
             **/
	    else if(asset instanceof Tank)
		unitType = UnitTypes.ARMOR;
	    else if(asset instanceof MannedAircraft)
		unitType = UnitTypes.AIR_FORCES;
            /* Note: Do not put descendant of GroundVehicle/Aircraft
             * after this else statement or they will never be
             * reached.
             **/
            else if(asset instanceof GroundVehicle)
		unitType = UnitTypes.ARMOR;
	    else if(asset instanceof Aircraft)
		unitType = UnitTypes.AIR_FORCES;
            
            else
		unitType = UnitTypes.UNKNOWN;
                
	    //	    Machinetta.Debugger.debug(1,"Asset "+asset.getID()+" of class "+asset.getClass().getName()+" is drawn as a UnitType "+unitType+" with name "+name);

	    mo = new MapObject(id, forceid, id, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0);
	    //		    System.err.println("DEBUG:SEAN: New mapobject: "+mo.toString());

	    if(asset instanceof MapObjectDrawable) 
		mo.setDrawable((MapObjectDrawable)asset);
	    mapDB.add(mo);
	    mapDB.setDirty(true);
	}
	//		System.err.println("DEBUG:SEAN: Updating mapobject: "+mo.toString());
            
            
	//	if(asset.getBlueForce())
	mo.setTasked(asset.isTasked());
	if(asset.tasks.size() > 0 && showDest) {
	    Task task = asset.tasks.getFirst();
	    if(task instanceof Move) {
		Move move = (Move)task;
		mo.setDestColor(Color.black);
		mo.setDest(move.getDestinationX(), move.getDestinationY(), move.getDestinationZ());
	    } else if(task instanceof Attack) {
		Attack attack = (Attack)task;
		mo.setDestColor(Color.red);
		mo.setDest(attack.getDestinationX(), attack.getDestinationY(), attack.getDestinationZ());
	    } else if(task instanceof Patrol) {
		Patrol patrol = (Patrol)task;
		// 			mo.setDestColor(Color.blue);
		// 			mo.setDest(patrol.getXCenter(), patrol.getYCenter(), 0);
	    }
	} else {
	    mo.clearDest();
	}
            
	if(asset.isCollision()) {
	    mo.setFlash(true, false,0,0);
	    asset.setCollision(false);
	}

	mo.setBda(bda);
	if(mo.BDA_DEAD == bda)
	    mo.clearDest();
	mo.setPos(posx, posy, 0);
	if (asset.heading != null)
	    mo.setOrientation(asset.heading.angle());

	if(markXplaneAsset) {
	    if(Env.XPLANE_ASSET_VIEW == asset) {
		mo.setXplane(true);
	    } else {
		mo.setXplane(false);
	    }
	}
	
	mapDB.setDirty(true);
    }
    
    private void drawBuilding(Building b) {
        String id = b.getID();
        int forceid = ForceIds.NEUTRAL;

	int bda = MapObject.BDA_ALIVE;
	if (b.getState() == State.LIVE)
	    bda = MapObject.BDA_ALIVE;
	else
	    bda = MapObject.BDA_DEAD;
	double posx = b.getLocation().x;
	double posy = b.getLocation().y;
            
	MapObject mo = mapDB.get(id);
	if(null == mo) {
	    String name = id;
	    int unitType = UnitTypes.BUILDING;
	    if(b instanceof Terminal) {
		unitType = UnitTypes.TERMINAL;
		name = "TM "+name;
	    }
            else
		unitType = UnitTypes.BUILDING;
                
	    mo = new MapObject(id, forceid, id, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0, b.getWidth(), b.getHeight());
	    //		    System.err.println("DEBUG:SEAN: New mapobject: "+mo.toString());

	    if(b instanceof Pavement) {
		Machinetta.Debugger.debug(1, "drawing pavement "+id);
		Pavement p = (Pavement)b;
		mo.setPoly(p.getPoly());
		mo.setType(mo.TYPE_POLY);
		mo.setMapGraphicLineColor(Color.BLACK);
		mo.setMapGraphicColor(Color.LIGHT_GRAY);
		mo.setMapGraphicFillOn(false);
	    }

	    mapDB.add(mo);
	    mapDB.setDirty(true);
	}
	//		System.err.println("DEBUG:SEAN: Updating mapobject: "+mo.toString());
            
            
	mo.setBda(bda);
	if(mo.BDA_DEAD == bda)
	    mo.clearDest();
	mo.setPos(posx, posy, 0);

	mapDB.setDirty(true);
    }
        
    public void updateMapDB() {
	// Draw tanks
	int x1 = env.getSoilGrid().getBottomLeftX();
	int y1 = env.getSoilGrid().getBottomLeftY();
	int z1 = 0;
	int x2 = x1 + (int)env.getSoilGrid().getWidthMeters();
	int y2 = y1 + (int)env.getSoilGrid().getHeightMeters();
	int z2 = 100000;
            
	// Step one - get everything in mapDB and check to see if
	// anything is missing from Env - if so then remove it from
	// mapDB.
	MapObject moAry[] = mapDB.getMapObjects();
	for(int loopi = 0; loopi < moAry.length; loopi++) {
	    MapObject mo = moAry[loopi];
	    if(mo != null) {
		String id = mo.getKey();
		if(id.equals("opforflag"))
		    continue;
		if(id.equals("blueforflag"))
		    continue;
		if(mo.getType() == mo.TYPE_DETONATION && !mo.isAnimationDone())
                    continue;
                
		// @TODO: untangle this re: assets vs buildings and
		// figure out how to do this right without _expecting_
		// to get exceptions every time we update.
                Asset asset = null;
                Building building = null;
                try {
                    asset = env.getAssetByID(id);
                } catch(ClassCastException e) {
                    building = env.getBuildingByID(id);
                }
                
                if(null == asset && null == building)
		    mapDB.remove(mo);
                if(asset != null) {
		    // NOTE: the original if statement did not display
		    // assets inside the the building.  Since this is
		    // ground truth, I'm changing it so assets in the
		    // building still show up on the GUI.

		    //                    if(asset.getMountingAsset() != null || asset.getContainingBuilding() != null)
                    if(asset.getMountingAsset() != null)
                        mapDB.remove(mo);
                }
		if(mo.isEdited()) {
		    mo.setEdited(false);
		    asset.location.x = mo.getPosX();
		    asset.location.y = mo.getPosY();
		    asset.location.z = mo.getPosZ();
		}
		if(null != asset){
		    if(asset instanceof Emitter){
			if(((Emitter)asset).isDetected()){
			    mo.setDetected(true);
			}
		    }
		}
	    }
	}
            
	// Make sure our flags (if set) are in the mapdb.
	if(null != opforFlag) {
	    MapObject mo = mapDB.get("opforflag");
	    if(null == mo) {
		int width = opforFlag.width;
		int height = opforFlag.height;
		int posx = opforFlag.x+width/2;
		int posy = opforFlag.y+height/2;
		mo = new MapObject("opforflag", ForceIds.OPFOR, "OpFor Flag", mo.TYPE_OBJECTIVE, 
				   posx, posy, 0.0, width, height);
		mapDB.add(mo);
		mapDB.setDirty(true);
	    }
	}
	if(null != blueforFlag) {
	    MapObject mo = mapDB.get("blueforflag");
	    if(null == mo) {
		int width = blueforFlag.width;
		int height = blueforFlag.height;
		int posx = blueforFlag.x+width/2;
		int posy = blueforFlag.y+height/2;
		mo = new MapObject("blueforflag", ForceIds.BLUEFOR, "BlueFor Flag", mo.TYPE_OBJECTIVE, 
				   posx, posy, 0.0, width, height);
		mapDB.add(mo);
		mapDB.setDirty(true);
	    }
	}

	// Next step - get every asset and draw it by calling
	// drawAsset, and then get every building and draw it by
	// calling drawBuilding.
	try {
	    for (ListIterator li = env.getAllAssets().listIterator();
		 li.hasNext(); ) {
		Asset a = (Asset)li.next();
		if(a.getMountingAsset() == null && a.getContainingBuilding() == null)
		    drawAsset(a);
	    }
        
	    for (ListIterator li = env.getAllBuildings().listIterator();
		 li.hasNext(); ) {
		Building b = (Building)li.next();
		drawBuilding(b);
	    }
	}
	catch (java.util.ConcurrentModificationException e) {
	    // @TODO: Fix this right - the SCButtonActionPerformed is
	    // parsing some delayed parts of the Env.txt file which end up
	    // creating assets while this loop is running, causing the
	    // exception;
	    //
	    // Exception in thread "Thread-1" java.util.ConcurrentModificationException
	    // 	at java.util.AbstractList$Itr.checkForComodification(AbstractList.java:372)
	    // 	at java.util.AbstractList$Itr.next(AbstractList.java:343)
	    // 	at AirSim.Environment.GUI.MainFrameGUI.updateMapDB(MainFrameGUI.java:588)
	    // 	at AirSim.Environment.GUI.MainFrame$3.run(MainFrame.java:475)
	    //
	    // The proper fix is to make sure this doesn't happen,
	    // preferably by instead setting some flag from the button,
	    // and checking that flag in the same thread that calls this
	    // method, and doing the work there, so it can't happen at the
	    // same time.  That or put locks around everything in Env
	    // which would probably slow everythign down a fair bit.
	    //
	    // Instead I'm going to cheat for now and just catch the
	    // exception and ignore it.
	}
// 	int[] sentMsgs = Asset.getSentMsgCounts();
// 	if(null != sentMsgs) {
// 	    TerrainCanvas terrain = terrainPanel.getTerrainCanvas();
// 	    terrain.setSentMsgs(sentMsgs[1]);
//             int failedMessages = 0;
//             for (int i = 2; i <= UDPCommon.MSG_SENDCOUNT_LIMIT; i++) {
//                 failedMessages += sentMsgs[i];
//             }
// 	    terrain.setFailedMsgs(failedMessages);
// 	    Machinetta.Debugger.debug("Setting TerrainCanvas sentMsgs="+sentMsgs[0]+", failedMsgs="+failedMessages,0,this);
// 	}
	long totalAckReqSent = Asset.getTotalAckReqSent();
	if(-1 != totalAckReqSent) {
	    long totalResentAtLeastOnce = Asset.getTotalResentAtLeastOnce();
	    long totalFailed = Asset.getTotalFailed();
	    TerrainCanvas terrain = terrainPanel.getTerrainCanvas();
	    terrain.setSentMsgs(totalAckReqSent);
	    terrain.setResentMsgs(totalResentAtLeastOnce);
	    terrain.setFailedMsgs(totalFailed);
	    Machinetta.Debugger.debug("Setting TerrainCanvas sentMsgs="+totalAckReqSent+", resentMsgs="+totalResentAtLeastOnce+", failedMsgs="+totalFailed,0,this);
	}

    }

    public void snapshot(String filename) {
	updateMapDB();
	topLevelFrame.setVisible(true);

	// Pause so the screen can refresh.
	try {Thread.sleep(1000);} catch (InterruptedException e) {}
            
	Snapshot snapshot = new Snapshot(1000,1000);
	Machinetta.Debugger.debug("Snapshotting to "+filename,3,this);
	snapshot.snap(filename, terrainPanel.getTerrainCanvas());
    }

    public void buildEmitterOverlay() {
	ArrayList<DoubleGrid> overlayList = new	ArrayList<DoubleGrid>();
	DoubleGrid sigGrid = new DoubleGrid(env.getSoilGrid(), false);
	double[][] signals = RSSISensor.computeSignalGridVerySlowly(sigGrid.getWidth());
	for(int loopy = 0; loopy < sigGrid.getHeight(); loopy++) {
	    for(int loopx = 0; loopx < sigGrid.getWidth(); loopx++) {
		sigGrid.setValue(loopx, loopy, signals[loopx][loopy]);
	    }
	}
	background.setOverlayList(overlayList);
    }
}
