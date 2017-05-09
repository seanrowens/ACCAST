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
 * SimOperatorGUI.java
 *
 * Created on March 20, 2006, 9:44 AM
 */

package AirSim.Commander;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.georgeandabe.tigerline.model.TLData;

import AirSim.Environment.Area;
import AirSim.Machinetta.Point2D;
import AirSim.Machinetta.CostMaps.BBFTabPanel;
import AirSim.Machinetta.CostMaps.Clust;
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
import Gui.ViewPort;
import Machinetta.Debugger;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefType.Belief;

/**
 * GUI for the SimOperator.
 * @author  owens
 */
public class SimOperatorGUI extends javax.swing.JFrame implements ActionListener  {
    public final static String ACTION_STRIKE="Strike";
    public final static String ACTION_SIMULSTRIKE="SimulStrike";
    public final static String ACTION_BDASTRIKE="BDAStrike";
    public final static String ACTION_PATROL="Patrol";
    public final static String ACTION_NOGO="Nogo";
    public final static String ACTION_MOVE="Move";
    public final static String ACTION_PICTURE="Order Picture";
    public final static String ACTION_CLUSTER="Scan Clusters";
    
    private final static String STRIKE_TOOLTIP = "Send pending strikes to planner";
    private final static String SIMULSTRIKE_TOOLTIP = "Send pending strikes to planner as simultaneous strike";
    private final static String BDASTRIKE_TOOLTIP = "Send pending strikes to planner with Battle Damage Assement requested";
    private final static String PATROL_TOOLTIP = "Send pending patrol zones to planner";
    private final static String NOGO_TOOLTIP = "Send pending NOGO zones to planner";
    private final static String MOVE_TOOLTIP = "Order UGV to move to opposing Flag";
    private final static String PICTURE_TOOLTIP = "Send pending Picture Orders to planner";
    private final static String CLUSTER_TOOLTIP = "Create and send Picture Orders for Clusters";
    
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
    
    private String baseGridFileName = null;
    private ArrayList<String> roadFileNames = new ArrayList<String>();
    
    private int repaintIntervalMs = 1000/1;

    private String elevationGridFileName = null;
    private String soilGridFileName = null;
    private IntGrid soilGrid = null;
    private DoubleGrid elevationGrid = null;
    private TLData roadData = null;
    
    private JFrame topLevelFrame = null;
    private Background background = null;
    private BackgroundConfig config = null;
    private JPanel actionButtonPanel = null;
    private MapDB mapDB = null;
    public MapDB getMapDB() { return mapDB; }
    private MapUtil mapUtil = null;
    public MapUtil getMapUtil() { return mapUtil; }
    private RepaintTimer repaintTimer = null;
    private PaletteTerrainPanel terrainPanel = null;
    
    private BBFTabPanel bbfBeliefTabPanel = null;
    private BBFTabPanel bbfEntropyTabPanel = null;

    private ArrayList<PlanAgent> planAgents = new ArrayList<PlanAgent>();
    
    private ArrayList<Clust> bestClusterList = null;
    public void setBestClusterList(ArrayList<Clust> value) { bestClusterList = value; }

    private final static double TARGET_UPDATES_SINCE_LAST_SEEN_LIMIT = 10;
    private final static double TARGET_UPDATES_SINCE_LAST_MOVE_LIMIT = 3;
    private static double TARGET_MOVE_LIMIT_METERS = 50;
    private static double TARGET_MOVE_LIMIT_SQD_METERS = TARGET_MOVE_LIMIT_METERS * TARGET_MOVE_LIMIT_METERS;

    private JButton clusterButton;

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
    
    private void addFlags(MapDB mapDB) {
        MapObject mo = null;
        mo = new MapObject("opforflag", ForceIds.OPFOR, "OpFor Flag", mo.TYPE_OBJECTIVE,
                47500, 47500, 0.0, 5000, 5000);
        mapDB.add(mo);
        mo = new MapObject("blueforflag", ForceIds.BLUEFOR, "BlueFor Flag", mo.TYPE_OBJECTIVE,
                2500, 2500, 0.0, 5000, 5000);
        mapDB.add(mo);
        mapDB.setDirty(true);
    }
    
    public void makeTerrainGui() {
        loadCTDB();
	roadData = Roads.load(roadData, roadFileNames);
	
        JPanel topPanel = null;
        
        topLevelFrame = new JFrame("                                             Sim Operator GUI");
        topLevelFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        // For some reason I don't understand, if I just use 0,0 here
        // (the top left of the screen), xwin-32 puts the window
        // slightly off the screen to the top and the left.
	//	int bayesPanelWidth = (int)((UAVRI.MAP_WIDTH_METERS/(UAVRI.BBF_GRID_SCALE_FACTOR*2)))-100;
	int bayesPanelWidth = 0;
        topLevelFrame.setLocation(5+bayesPanelWidth,30);
         //	topLevelFrame.setLocation(0,0);
 	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	topLevelFrame.setSize(screenSize.width-bayesPanelWidth,screenSize.height);
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
        mapDB = new MapDB(soilGrid.getGccToTccTransform(), soilGrid.getTccToGccTransform(), null, null, null);
        mapUtil = new MapUtil(mapDB, "/usr0/sanjaya/terrain_gui_icons");
        //	addFlags(mapDB);
        
        background = new Background(elevationGrid, soilGrid, contourPolygonsGrid, obstacleGrid, null, contours, roadData);
        config = new BackgroundConfig(viewPort);
        
        actionButtonPanel = new JPanel();
        
        JButton strikeButton = new JButton(ACTION_STRIKE);
        strikeButton.setActionCommand(ACTION_STRIKE);
        strikeButton.setToolTipText(STRIKE_TOOLTIP);
        strikeButton.addActionListener(this);
        actionButtonPanel.add(strikeButton);
        
        JButton simulStrikeButton = new JButton(ACTION_SIMULSTRIKE);
        simulStrikeButton.setActionCommand(ACTION_SIMULSTRIKE);
        simulStrikeButton.setToolTipText(SIMULSTRIKE_TOOLTIP);
        simulStrikeButton.addActionListener(this);
        actionButtonPanel.add(simulStrikeButton);
        
        JButton bdaStrikeButton = new JButton(ACTION_BDASTRIKE);
        bdaStrikeButton.setActionCommand(ACTION_BDASTRIKE);
        bdaStrikeButton.setToolTipText(BDASTRIKE_TOOLTIP);
        bdaStrikeButton.addActionListener(this);
        actionButtonPanel.add(bdaStrikeButton);
        
        JButton patrolButton = new JButton(ACTION_PATROL);
        patrolButton.setActionCommand(ACTION_PATROL);
        patrolButton.setToolTipText(PATROL_TOOLTIP);
        patrolButton.addActionListener(this);
        actionButtonPanel.add(patrolButton);
        
        JButton nogoButton = new JButton(ACTION_NOGO);
        nogoButton.setActionCommand(ACTION_NOGO);
        nogoButton.setToolTipText(NOGO_TOOLTIP);
        nogoButton.addActionListener(this);
        actionButtonPanel.add(nogoButton);
        
        JButton moveButton = new JButton(ACTION_MOVE);
        moveButton.setActionCommand(ACTION_MOVE);
        moveButton.setToolTipText(MOVE_TOOLTIP);
        moveButton.addActionListener(this);
        actionButtonPanel.add(moveButton);
        
        JButton pictureButton = new JButton(ACTION_PICTURE);
        pictureButton.setActionCommand(ACTION_PICTURE);
        pictureButton.setToolTipText(PICTURE_TOOLTIP);
        pictureButton.addActionListener(this);
        actionButtonPanel.add(pictureButton);
        
        clusterButton = new JButton(ACTION_CLUSTER);
	clusterButton.setEnabled(false);
        clusterButton.setActionCommand(ACTION_CLUSTER);
        clusterButton.setToolTipText(CLUSTER_TOOLTIP);
        clusterButton.addActionListener(this);
        actionButtonPanel.add(clusterButton);
        
        //	PaletteTerrainPanel.CLEAN_MODE = true;
        terrainPanel = new PaletteTerrainPanel(this, mapDB,background,config, actionButtonPanel);
        
        if(setViewPort)
            config.viewPort.requestSourceView(viewPortX,viewPortY,viewPortWidth,viewPortHeight);
        config.soilTypes = false;
        config.showTraces = showTraces;
        config.contourMultiples = contourMultiples;
        config.gridLinesOneKm = gridLinesOneKm;
        config.showMapObjectNames = showMapObjectNames;
        
        repaintTimer = new RepaintTimer(terrainPanel, mapDB, repaintIntervalMs);
        repaintTimer.start();
        
        topPanel.add(terrainPanel);
        // Note, DON'T use .pack() - or we undo all the
        // setSize/setLocation crap above.
        
        Machinetta.Debugger.debug("Setting top level frame visible.", 0, this);
        topLevelFrame.setVisible(true);
	topLevelFrame.toBack();
    }
    
    public void addBelief(Belief belief) {
        mapUtil.addBelief(belief);
    }
          
    private void clickStrike(String action) {
        ArrayList<MapObject> strikeList = new ArrayList<MapObject>();
        MapObject[] mapObjects = mapDB.getMapObjects();
        if(mapObjects == null)
            return;
        for(int loopi = 0; loopi < mapObjects.length; loopi++) {
            MapObject mo = mapObjects[loopi];
            if(null == mo)
                continue;
            if(!mo.isEditable())
                continue;
            if(mo.getType() != mo.TYPE_STRIKE)
                continue;
            mo.setTasked(true);
            mo.setSelected(false);
            mo.setEditable(false);
            strikeList.add(mo);
        }
        if (ACTION_STRIKE.equals(action)) {
            for(MapObject mo: strikeList) {
                Point2D strikePoint = new Point2D((int)mo.getPosX(),(int)mo.getPosY());
                PlanAgent pAgent = (new PlanCreatorInterface()).attackFromAir(strikePoint);
                planAgents.add(pAgent);
                Debugger.debug("Created attackFromAir plan for point="+strikePoint+", planAgent="+pAgent, 1, this);
            }
        } else if (ACTION_SIMULSTRIKE.equals(action) || ACTION_BDASTRIKE.equals(action)) {
            String pointString="";
            ArrayList<Point2D> strikePointList = new ArrayList<Point2D>();
            for(MapObject mo: strikeList) {
                pointString += "("+mo.getPosX()+","+mo.getPosY()+"), ";
                Point2D strikePoint = new Point2D((int)mo.getPosX(),(int)mo.getPosY());
                strikePointList.add(strikePoint);
            }
            if (ACTION_SIMULSTRIKE.equals(action)) {
                PlanAgent pAgent = (new PlanCreatorInterface()).simultaneousAttackFromAir(strikePointList);
                planAgents.add(pAgent);
                Debugger.debug("Created simultaneousAttackFromAir plan for points="+pointString+", planAgent="+pAgent, 1, this);
            }
            // @todo: SRO Thu Apr 27 22:55:52 EDT 2006 - need to add
            // simul+bda strike plan creation and buttons.
            else if(ACTION_BDASTRIKE.equals(action)) {
                PlanAgent pAgent = (new PlanCreatorInterface()).attackFromAirWithBDA(strikePointList);
                planAgents.add(pAgent);
                Debugger.debug("Created attackFromAirWithBDA plan for points="+pointString+", planAgent="+pAgent, 1, this);
            }
        }
    }
    
    private void clickPatrol() {
        MapObject[] mapObjects = mapDB.getMapObjects();
        if(mapObjects == null)
            return;
        for(int loopi = 0; loopi < mapObjects.length; loopi++) {
            MapObject mo = mapObjects[loopi];
            if(null == mo)
                continue;
            if(!mo.isEditable())
                continue;
            if(mo.getType() != mo.TYPE_PATROL_ZONE)
                continue;
            mo.setEditable(false);
            
            double centerx = mo.getPosX();
            double centery = mo.getPosY();
            double sizex = mo.getSizeX();
            double sizey = mo.getSizeY();
            int rectX = (int)(centerx - sizex/2);
            int rectY = (int)(centery - sizey/2);
            int rectWidth = (int)sizex;
            int rectHeight = (int)sizey;
            Rectangle rect = new Rectangle(rectX, rectY, rectWidth, rectHeight);
            PlanAgent pAgent = (new PlanCreatorInterface()).patrol(rect);
            planAgents.add(pAgent);
            Debugger.debug("Created patrol plan for area="+rect+", planAgent="+pAgent, 1, this);
        }
    }
    
    private void clickNogo() {
        MapObject[] mapObjects = mapDB.getMapObjects();
        if(mapObjects == null)
            return;
        for(int loopi = 0; loopi < mapObjects.length; loopi++) {
            MapObject mo = mapObjects[loopi];
            if(null == mo)
                continue;
            if(!mo.isEditable())
                continue;
            if(mo.getType() != mo.TYPE_NOGO_ZONE)
                continue;
            mo.setEditable(false);
            
            double centerx = mo.getPosX();
            double centery = mo.getPosY();
            double sizex = mo.getSizeX();
            double sizey = mo.getSizeY();
            double x1 = centerx - sizex/2;
            double y1 = centery - sizey/2;
            double x2 = centerx + sizex/2;
            double y2 = centery + sizey/2;
            Area area = new Area(x1, y1, x2, y2);
            (new PlanCreatorInterface()).nogo(area);
            Debugger.debug("Created nogo plan for area="+area,1,this);
        }
    }
    
    private void clickMove() {
        MapObject[] mapObjects = mapDB.getMapObjects();
        if(mapObjects == null)
            return;
        for(int loopi = 0; loopi < mapObjects.length; loopi++) {
            MapObject mo = mapObjects[loopi];
            if(null == mo)
                continue;
            if(!mo.isEditable())
                continue;
            if(mo.getType() != mo.TYPE_OBJECTIVE)
                continue;
            mo.setEditable(false);
        }
    }
    
    private void clickPicture() {
        ArrayList<MapObject> pictureOrderList = new ArrayList<MapObject>();
        MapObject[] mapObjects = mapDB.getMapObjects();
        if(mapObjects == null)
            return;
        for(int loopi = 0; loopi < mapObjects.length; loopi++) {
            MapObject mo = mapObjects[loopi];
            if(null == mo)
                continue;
            if(!mo.isEditable())
                continue;
            if(mo.getType() != mo.TYPE_PICTURE_ORDER)
                continue;
            mo.setTasked(true);
            mo.setSelected(false);
            mo.setEditable(false);
            pictureOrderList.add(mo);
        }
        for(MapObject mo: pictureOrderList) {
            java.awt.Point pictureOrderPoint = new java.awt.Point((int)mo.getPosX(),(int)mo.getPosY());
            PlanAgent pAgent = (new PlanCreatorInterface()).EOSenseLocation(pictureOrderPoint);
            planAgents.add(pAgent);
            Debugger.debug("Created EOSenseLocation (picture order) plan for point="+pictureOrderPoint+", planAgent="+pAgent, 1, this);
        }
    }

    private void clickCluster() {
        if(bestClusterList == null)
            return;

        for(int loopi = 0; loopi < bestClusterList.size(); loopi++) {
	    Clust cluster = bestClusterList.get(loopi);

	    MapObject mo = new MapObject("Cluster."+(loopi+1)+" "+" picture order", MapObject.TYPE_PICTURE_ORDER, cluster.x, cluster.y, 0, cluster.memberDistMean,0);
	    mapDB.add(mo);
            mo.setTasked(true);
            mo.setSelected(false);
            mo.setEditable(false);

            java.awt.Point pictureOrderPoint = new java.awt.Point((int)cluster.x,(int)cluster.y);
            PlanAgent pAgent = (new PlanCreatorInterface()).EOSenseLocation(pictureOrderPoint);
            planAgents.add(pAgent);
            Debugger.debug("Created EOSenseLocation (picture order) plan for cluster at= "+pictureOrderPoint+", planAgent="+pAgent, 1, this);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            String action = e.getActionCommand();
            if (ACTION_STRIKE.equals(action)) {
                clickStrike(action);
            } else if (ACTION_SIMULSTRIKE.equals(action)) {
                clickStrike(action);
            } else if (ACTION_BDASTRIKE.equals(action)) {
                clickStrike(action);
            } else if (ACTION_PATROL.equals(action)) {
                clickPatrol();
            } else if (ACTION_NOGO.equals(action)) {
                clickNogo();
            } else if (ACTION_MOVE.equals(action)) {
                clickMove();
            } else if (ACTION_PICTURE.equals(action)) {
                clickPicture();
            } else if (ACTION_CLUSTER.equals(action)) {
                clickCluster();
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
    
    /** Creates new form SimOperatorGUI */
    public SimOperatorGUI(String baseGridFileName, String roadFileName, int repaintIntervalMs) {
        this.baseGridFileName = baseGridFileName;
	this.roadFileNames.add(roadFileName);
        this.repaintIntervalMs = repaintIntervalMs;
        Machinetta.Debugger.debug("Creating GUI with grid="+baseGridFileName+" and repaintIntervalMs = repaintIntervalMs", 1, this);
        makeTerrainGui();
    }
    
    public void task(ClusterTarget target) {
	target.tasked = true;
	Clust cluster = target.cluster;
	MapObject mo = new MapObject(target.key1+" "+" picture order", MapObject.TYPE_PICTURE_ORDER, cluster.x, cluster.y, 0, cluster.memberDistMean,0);
	mapDB.add(mo);
	mo.setTasked(true);
	mo.setSelected(false);
	mo.setEditable(false);
	java.awt.Point pictureOrderPoint = new java.awt.Point((int)cluster.x,(int)cluster.y);
	PlanAgent pAgent = (new PlanCreatorInterface()).EOSenseLocation(pictureOrderPoint);
	planAgents.add(pAgent);
	Debugger.debug("        Created EOSenseLocation (picture order) plan for cluster at= "+pictureOrderPoint+", planAgent="+pAgent, 1, this);
    }

}
