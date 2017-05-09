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
 * DynaZonesGUI.java
 *
 * Created on Mon Jul 23 18:27:53 EDT 2007
 */

package AirSim.Commander;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.georgeandabe.tigerline.model.TLData;

import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.PlannedPath;
import AirSim.Machinetta.Beliefs.FlyZone;
import AirSim.Machinetta.Beliefs.UAVLocation;
import Gui.Background;
import Gui.BackgroundConfig;
import Gui.Contour;
import Gui.DoubleGrid;
import Gui.ForceIds;
import Gui.IntGrid;
import Gui.MapDB;
import Gui.MapObject;
import Gui.Obstacles;
import Gui.PaletteTerrainPanel;
import Gui.RepaintTimer;
import Gui.Roads;
import Gui.UnitSizes;
import Gui.UnitTypes;
import Gui.ViewPort;
import Machinetta.Debugger;
import Machinetta.Coordination.MACoordination;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefID;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author  owens
 */
public class DynaZonesGUI extends javax.swing.JFrame implements ActionListener, StateChangeListener {
    public final static String ACTION_AUTO="Auto";
    public final static String ACTION_APPROVE="Approve";
    public final static String ACTION_DENY="SimulStrike";

    private final static String TOOLTIP_AUTO = "Activate automatic mode - no user response required";
    private final static String TOOLTIP_APPROVE = "Approve requested fly zone";
    private final static String TOOLTIP_DENY = "Deny requested fly zone";

//     public static boolean setViewPort = false;
//     public static double viewPortX = 0;
//     public static double viewPortY = 0;
//     public static double viewPortWidth = 0;
//     public static double viewPortHeight = 0;
//     public static boolean soilTypes = false;
//     public static boolean showTraces = true;
//     public static int contourMultiples = BackgroundConfig.CONTOUR_MULT_100;
//     public static boolean gridLinesOneKm = true;
//     public static boolean showMapObjectNames = true;
    
    private String baseGridFileName = null;
    private ArrayList<String> roadFileNames = new ArrayList<String>();
    
    private int repaintIntervalMs = 1000/10;
    private String elevationGridFileName = null;
    private String soilGridFileName = null;
    private IntGrid soilGrid = null;
    private DoubleGrid elevationGrid = null;
    private TLData roadData = null;
    
    private JFrame topLevelFrame = null;
    private JFrame zoomFrame = null;
    private Background background = null;
    private Background zoomBackground = null;
    private BackgroundConfig config = null;
    private BackgroundConfig zoomConfig = null;
    private JPanel actionButtonPanel = null;
    private JPanel zoomActionButtonPanel = null;
    private MapDB mapDB = null;
    private MapUtil mapUtil = null;
    private RepaintTimer repaintTimer = null;
    private RepaintTimer zoomRepaintTimer = null;
    private PaletteTerrainPanel terrainPanel = null;
    private PaletteTerrainPanel zoomPanel = null;
    
    private ArrayList<PlanAgent> planAgents = new ArrayList<PlanAgent>();
    
    private HashMap<ProxyID, Long> lastLocationTime = new HashMap<ProxyID, Long>();
    private HashSet<BeliefID> seenPaths = new HashSet<BeliefID>();
    private HashMap<ProxyID,PlannedPath> assetPathMap = new HashMap<ProxyID,PlannedPath>();
    private HashMap<ProxyID,PlannedPath> unusedPathMap = new HashMap<ProxyID,PlannedPath>();
    private HashMap<ProxyID,FlyZone> flyZoneMap = new HashMap<ProxyID,FlyZone>();
    private ProxyState state=null;

    // @TODO: there are similar methods to this everywhere that I
    // create a terrain GUI, it should probably be moved to a utility
    // function in Gui package.
    private void loadCTDB() {
        Machinetta.Debugger.debug("Loading CTDBs.", 0, this);
                
        if(null == elevationGridFileName)
            elevationGridFileName = baseGridFileName+"_e.grd";
        if(null == soilGridFileName)
            soilGridFileName = baseGridFileName+"_s.grd";
        
        soilGrid = new IntGrid();
        soilGrid.loadSoilTypeFile(soilGridFileName);
        elevationGrid = new DoubleGrid();
        elevationGrid.loadGridFile(elevationGridFileName);
    }
    
    public void makeTerrainGui() {
        loadCTDB();
	roadData = Roads.load(roadData, roadFileNames);

        JPanel topPanel = null;
        
 	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        topLevelFrame = new JFrame("                                             TrafficController");
        topLevelFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        // For some reason I don't understand, if I just use 0,0 here
        // (the top left of the screen), xwin-32 puts the window
        // slightly off the screen to the top and the left.
	topLevelFrame.setLocation(5,30);
	topLevelFrame.setSize(screenSize.width,screenSize.height);
// 	topLevelFrame.setLocation(1280,30);
// 	topLevelFrame.setSize(1280,1024);
        topPanel = new JPanel();
        topPanel.setLayout( new BorderLayout() );
        topLevelFrame.getContentPane().add( topPanel );

        zoomFrame = new JFrame("                                      Zoomed TrafficController");
        zoomFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        // For some reason I don't understand, if I just use 0,0 here
        // (the top left of the screen), xwin-32 puts the window
        // slightly off the screen to the top and the left.
	zoomFrame.setLocation(5,30);
	zoomFrame.setSize(screenSize.width,screenSize.height);
// 	zoomFrame.setLocation(1280,30);
// 	zoomFrame.setSize(1280,1024);
        JPanel zoomTopPanel = new JPanel();
        zoomTopPanel.setLayout( new BorderLayout() );
        zoomFrame.getContentPane().add( zoomTopPanel );
        
        
        // ----------------------------------------------------------------------
        // Build the obstacle grids - slope and soil type based.
        // ----------------------------------------------------------------------
        Obstacles obstacles = null;
        IntGrid obstacleGrid = null;
        obstacles = new Obstacles(elevationGrid, soilGrid);
        obstacleGrid = obstacles.getObstacleGrid();
        
        // ----------------------------------------------------------------------
        // Build the contour lines.
        // ----------------------------------------------------------------------
        IntGrid contourPolygonsGrid = null;
        Contour contours = null;
        contourPolygonsGrid = new IntGrid(obstacleGrid);
        contourPolygonsGrid.clear();
        contours = new Contour(elevationGrid);
        contours.compute(25.0);
        
        ViewPort viewPort = new ViewPort(soilGrid);
        ViewPort zoomViewPort = new ViewPort(soilGrid);
        mapDB = new MapDB(soilGrid.getGccToTccTransform(), soilGrid.getTccToGccTransform(), null, null, null);
        mapUtil = new MapUtil(mapDB, "/usr0/sanjaya/terrain_gui_icons");
        
        background = new Background(elevationGrid, soilGrid, contourPolygonsGrid, obstacleGrid, null, contours, roadData);
        zoomBackground = new Background(elevationGrid, soilGrid, contourPolygonsGrid, obstacleGrid, null, contours, roadData);
        config = new BackgroundConfig(viewPort);
        zoomConfig = new BackgroundConfig(zoomViewPort);
        
        actionButtonPanel = new JPanel();
        zoomActionButtonPanel = new JPanel();

        JButton autoButton = new JButton(ACTION_AUTO);
        autoButton.setActionCommand(ACTION_AUTO);
        autoButton.setToolTipText(TOOLTIP_AUTO);
        autoButton.addActionListener(this);
        actionButtonPanel.add(autoButton);

	autoButton.setSelected(true);

        JButton approveButton = new JButton(ACTION_APPROVE);
        approveButton.setActionCommand(ACTION_APPROVE);
        approveButton.setToolTipText(TOOLTIP_APPROVE);
        approveButton.addActionListener(this);
        actionButtonPanel.add(approveButton);

	approveButton.setEnabled(false);

        JButton denyButton = new JButton(ACTION_DENY);
        denyButton.setActionCommand(ACTION_DENY);
        denyButton.setToolTipText(TOOLTIP_DENY);
        denyButton.addActionListener(this);
        actionButtonPanel.add(denyButton);

	denyButton.setEnabled(false);

        
	PaletteTerrainPanel.SHOW_CONTROLLERS=false;
        terrainPanel = new PaletteTerrainPanel(this, mapDB,background,config, actionButtonPanel);

        zoomPanel = new PaletteTerrainPanel(this, mapDB,zoomBackground,zoomConfig,zoomActionButtonPanel);
	terrainPanel.addTwoPanelZoomController(zoomConfig.viewPort, 10000, 10000);

        if(GUIConfig.SET_VIEWPORT)
            config.viewPort.requestSourceView(GUIConfig.GUI_VIEWPORT_X,
					      GUIConfig.GUI_VIEWPORT_Y,
					      GUIConfig.GUI_VIEWPORT_WIDTH,
					      GUIConfig.GUI_VIEWPORT_HEIGHT);
        config.soilTypes = GUIConfig.GUI_SOIL_TYPES;
        config.showTraces = GUIConfig.GUI_SHOW_TRACES;
        config.contourMultiples = GUIConfig.GUI_CONTOUR_MULTIPLES;
        config.gridLinesOneKm = GUIConfig.GUI_GRID_LINES_ONE_KM;
        config.showMapObjectNames = GUIConfig.GUI_SHOW_MAP_OBJECT_NAMES;
        
        repaintTimer = new RepaintTimer(terrainPanel, mapDB, repaintIntervalMs);
        repaintTimer.start();

        zoomRepaintTimer = new RepaintTimer(zoomPanel, mapDB, repaintIntervalMs);
        zoomRepaintTimer.start();
        
        topPanel.add(terrainPanel);
        zoomTopPanel.add(zoomPanel);
        // Note, DON'T use .pack() - or we undo all the
        // setSize/setLocation crap above.
        
        Machinetta.Debugger.debug("Setting top level frame visible.", 0, this);
        topLevelFrame.setVisible(true);
	topLevelFrame.toBack();

        zoomFrame.setVisible(true);
	zoomFrame.toBack();
    }
    
    public void addBelief(Belief belief) {
	long startTime = System.currentTimeMillis();
	try {
	    startTime = System.currentTimeMillis();
	    if (belief instanceof UAVLocation) {
		//		Debugger.debug("addBelief: Got UAVLocation",1,this);
		addUAVLocation((UAVLocation)belief);
	    }
	    else if(belief instanceof PlannedPath) {
		Debugger.debug("addBelief: Got PlannedPath",1,this);
		addPlannedPath((PlannedPath)belief);
	    }
	    else if(belief instanceof AirSim.Machinetta.Beliefs.FlyZone) {
		Debugger.debug("addBelief: Got FlyZone",1,this);
		addFlyZone((FlyZone)belief);
	    }
	    else if(belief instanceof Machinetta.State.BeliefType.RAPBelief) {
		Debugger.debug("stateChange:RAPBelief, ignoring", 0, this);
	    }
	    else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief) {
		Debugger.debug("stateChange:TeamPlanBelief, ignoring", 0, this);
	    }
	    else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief) {
		Debugger.debug("stateChange:RoleAllocationBelief, ignoring", 0, this);
	    }
	    else if(belief instanceof AirSim.Machinetta.Beliefs.RSSIReading) {
		Debugger.debug("stateChange:RSSIReading ignoring", 0, this);
	    }
	    else {
		Debugger.debug("stateChange:Unknown class "+belief.getClass().getName()+" for new belief="+belief.toString(), 3, this);
	    }
	}
	catch(Exception e) {
	    Debugger.debug("        Exception processing new belief, e"+e,4,this);
	    e.printStackTrace();
	}
	long elapsed = (System.currentTimeMillis() - startTime);
	if(elapsed > 10)
	    Debugger.debug("        processing new belief, elapsed time="+elapsed,1,this);
    }

    public void addUAVLocation(UAVLocation location) {
	ProxyID pid = location.id;	
	Long lastTimeL = lastLocationTime.get(pid);
	long lastTime = 0;
	if(null != lastTimeL)
	    lastTime = lastTimeL.longValue();
	if(lastTime > location.getTime()) {
	    //	    Debugger.debug("stateChange:UAVLocation:Old UAVLocation - ignoring (last location time="+lastTime+"), id='"+pid+"', time='"+location.getTime()+"', lat,lon="+location.latitude+","+location.longtitude, 1, this);
	} 
	else {
	    //	    Debugger.debug("stateChange:UAVLocation:New UAVLocation (last location time="+lastTime+"), id='"+pid+"', time='"+location.getTime()+"', lat,lon="+location.latitude+","+location.longtitude, 1, this);
	    lastLocationTime.put(pid, location.getTime());
	    double posx = location.longtitude;
	    double posy = location.latitude;
	    double heading = location.heading;

	    String moId = pid.toString();
	    MapObject mo = mapDB.get(moId);
	    if(null == mo) {
		String moName = moId;
		int unitType = UnitTypes.MUAV;
		mo = new MapObject(moId, ForceIds.BLUEFOR, moName, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0);
		mo.setEditable(false);
		mapDB.add(mo);
	    }
	    mo.setPos(posx, posy, 0);
	    mo.setOrientation(heading);
//	    Location destLoc = destMap.get(moId);
// 	    if(null != destLoc) {
// 		mo.setDest(destLoc.getX(), destLoc.getY(),0);
// 	    }
	    PlannedPath path = unusedPathMap.get(pid);
	    if(null != path)
		addPlannedPath(path);
	    mapDB.setDirty(true);
	}
    }

    public void addPlannedPath(PlannedPath plannedPath) {
	BeliefID ppID = plannedPath.getID();
	if(seenPaths.contains(ppID)) {
	    Debugger.debug("addPlannedPath: ignoring repeat of known planned path id="+plannedPath.getID(),0,this);
	    return;
	}

	Debugger.debug("stateChange:plannedPath="+(Belief)plannedPath, 0, this);
	Path3D path = plannedPath.path;
	ProxyID assetID = path.getAssetID();

	if(null == assetID) {
	    Debugger.debug("addPlannedPath: null assetID in path, can't do anything useful to display this path - fix whatever generates this path to include the assetID",1,this);
	    return;
	}

	String assetIDString = assetID.toString();
	MapObject mo = mapDB.get(assetIDString);
	if(null == mo) { 
	    Debugger.debug("addPlannedPath: Couldn't find existing map object for assetid = "+assetIDString,1,this);
	    PlannedPath oldPath = unusedPathMap.get(assetID);
	    if(null == oldPath)
		unusedPathMap.put(assetID,plannedPath);
	    else if(oldPath.time < plannedPath.time)
		unusedPathMap.put(assetID,plannedPath);
	    return;
	}

	PlannedPath oldPath = assetPathMap.get(assetID);
	if(null != oldPath) 
	    seenPaths.remove(oldPath.getID());
	seenPaths.add(ppID);
	unusedPathMap.remove(assetID);	// In case it came through before, and was saved here.
	assetPathMap.put(assetID,plannedPath);

	Debugger.debug("addPlannedPath: Found existing map object for assetid = "+assetIDString+", new path="+path.toString() ,1,this);
	Vector3D[] waypoints = path.getWaypointsAry();
	if(waypoints.length <= 0) {
	    Debugger.debug("addPlannedPath: zero waypoints in planned path",0,this);
	    return;
	}
	Debugger.debug("addPlannedPath: "+waypoints.length+" waypoints in planned path",0,this);
	mo.clearPlannedPath();
	for(int loopi = 0; loopi < waypoints.length; loopi++) {
	    if(null != waypoints[loopi]) {
		mo.addPlannedPathPoint((float)waypoints[loopi].x, (float)waypoints[loopi].y);
	    }
	}
	if(null != plannedPath.conflicted) {
	    Debugger.debug("addPlannedPath: received plannedPath conflict notification for path owner "+assetID,0,this);
	    mo.setPlannedPathConflict(true);
	}
	else {
	    mo.setPlannedPathConflict(false);
	}
    }

    public boolean checkForFlyZoneConflicts(FlyZone fz) {
	Iterator<FlyZone> it = flyZoneMap.values().iterator();
	while(it.hasNext()) {
	    FlyZone oldZone = it.next();
	    if(fz.pid.equals(oldZone.pid))
		continue;
	    if(fz.intersects(oldZone)) {
		Debugger.debug("checkForFlyZoneConflicts:FlyZones intersect: old "+oldZone+" new "+fz, 1, this);
		return true;
	    }
	}
	return false;
    }

    public void addFlyZone(FlyZone fz) {
	Debugger.debug("addFlyZone:FlyZone request="+fz, 1, this);

	boolean conflict = checkForFlyZoneConflicts(fz);

	if(conflict) {
	    fz.approved = false;
	}
	else {
	    fz.approved = true;
	}

	FlyZone reply = new FlyZone(fz);
	InformationAgent agent = new InformationAgent(reply, reply.pid);
	Debugger.debug("addFlyZone:FlyZone request id="+fz.getID(), 1, this);
	Debugger.debug("addFlyZone:FlyZone reply id  ="+reply.getID(), 1, this);

	MACoordination.addAgent(agent);
	agent.stateChanged();	    // Let it act
	Debugger.debug("addFlyZone: Done sending reply="+fz, 1, this);

	double x = (fz.longtitude1+fz.longtitude2)/2;
	double y = (fz.latitude1+fz.latitude2)/2;
	double z = (fz.altitude1+fz.altitude2)/2;
	double width = fz.longtitude2 - fz.longtitude1;
	double length = fz.latitude2 - fz.latitude1;
	
	String flyzoneIDString = "FlyZone."+fz.pid.toString();
	MapObject mo = mapDB.get(flyzoneIDString);
	if(null == mo) {
	    String moName = "FlyZone."+fz.pid.toString()+" "+fz.altitude1+"m to "+fz.altitude2+"m";
	    mo = new MapObject(flyzoneIDString, MapObject.TYPE_MAP_GRAPHIC, x, y, z, width, length);
	    //	    mo.setName(moName);
	    mo.setEditable(false);
	    mapDB.add(mo);
	}
	else {
	    mo.setPos(x,y,z);	
	    mo.setSizeX(width);
	    mo.setSizeY(length);
	}
	if(conflict) {
	    mo.setMapGraphicColor(Color.red);
	    Debugger.debug("addFlyZone: Setting zone color to RED for "+mo.getName(),1,this);
	}
	else {
	    mo.setMapGraphicColor(Color.blue);
	    Debugger.debug("addFlyZone: Setting zone color to BLUE for "+mo.getName(),1,this);
	}

	mapDB.setDirty(true);

	flyZoneMap.put(fz.pid, fz);
    }

    private void clickApprove(String action) {
	Debugger.debug("User clicked Approve.", 1, this);
    }

    private void clickDeny(String action) {
	Debugger.debug("User clicked Deny.", 1, this);
    }

    private void clickAuto(String action) {
	Debugger.debug("User clicked Auto.", 1, this);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String action = e.getActionCommand();
            if (ACTION_APPROVE.equals(action)) {
                clickApprove(action);
            } else if (ACTION_DENY.equals(action)) {
                clickDeny(action);
            } else if (ACTION_AUTO.equals(action)) {
                clickAuto(action);
            } else {
                Debugger.debug("Unknown action from swing="+action+", ignoring.", 3, this);
            }
        } catch (Exception e2) {
            Debugger.debug("ERROR: Exception processing swing action, ignoring.",4,this);
            Debugger.debug("ERROR: action="+e.getActionCommand()+", exception="+e2,4,this);
            System.err.println("ERROR: Exception processing swing action, ignoring.");
            System.err.println("ERROR: action="+e.getActionCommand()+", exception="+e2);
        }
    }
    
    public void stateChanged(BeliefID[] b) {
	for (BeliefID bel: b) {
	    Belief belief = state.getBelief(bel);
	    try {
		addBelief(belief);
	    } catch (Exception e) {
		Debugger.debug("stateChange:Exception processing changed belief='"+belief+"', e="+e,5, this);
		e.printStackTrace();
	    }
                    
	}
    }

    /** Creates new form DynaZonesGUI */
    public DynaZonesGUI(String baseGridFileName, String roadFileName, int repaintIntervalMs) {
        this.baseGridFileName = baseGridFileName;
	this.roadFileNames.add(roadFileName);
        this.repaintIntervalMs = repaintIntervalMs;
        Machinetta.Debugger.debug("Creating GUI with grid="+baseGridFileName+" and repaintIntervalMs = repaintIntervalMs", 1, this);
        makeTerrainGui();

	state = new ProxyState();
        state.addChangeListener(this);
    }
    
}
