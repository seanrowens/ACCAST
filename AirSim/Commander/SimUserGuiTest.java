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
package AirSim.Commander;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.georgeandabe.tigerline.model.TLData;

import AirSim.Environment.Vector3D;
import AirSim.Environment.Assets.ForceId;
import AirSim.Machinetta.Path3D;
import Gui.Background;
import Gui.BackgroundConfig;
import Gui.Contour;
import Gui.DoubleGrid;
import Gui.ForceIds;
import Gui.IntGrid;
import Gui.LatLonUtil;
import Gui.MapDB;
import Gui.MapObject;
import Gui.Obstacles;
import Gui.PaletteTerrainPanel;
import Gui.RepaintTimer;
import Gui.UnitSizes;
import Gui.ViewPort;
import Machinetta.Debugger;

public class SimUserGuiTest extends  javax.swing.JFrame implements WSMObserver {
    private final static DecimalFormat fmt = new DecimalFormat("0.000000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.00");
    private final static DecimalFormat fmt3 = new DecimalFormat("0");

    public static double MAP_LOWER_LEFT_LAT = 30.4096071468;
    public static double MAP_LOWER_LEFT_LON = -97.4604290251;

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

    public static boolean setViewPort = true;
    public static double viewPortX = -100;
    public static double viewPortY = -100;
    public static double viewPortWidth = 5200;
    public static double viewPortHeight = 5200;
    public static boolean soilTypes = false;
    public static boolean showTraces = true;
    public static int contourMultiples = BackgroundConfig.CONTOUR_MULT_100;
    public static boolean gridLinesOneKm = true;
    public static boolean showMapObjectNames = true;



    public SimUserGuiTest() {
	this.baseGridFileName = "../../grd/austin_0_0_50000_50000_050";
// 	this.roadFileNames.add(roadFileName);
        this.repaintIntervalMs = 1000/10;
        Machinetta.Debugger.debug("Creating GUI with grid="+baseGridFileName+" and repaintIntervalMs = repaintIntervalMs", 1, this);
        makeTerrainGui();
    }

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
	//	roadData = Roads.load(roadData, roadFileNames);
	
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


    public void update(StateData sd) {
	String moId = sd.getKey();
	String name;
	if(sd.getPid() != null) {
	    moId= sd.getPid().toString();
	    name = moId;
	}
	else {
	    name = sd.getType()+" "+fmt2.format(sd.getConfidence());
	}
	if(sd.getLocation() == null) {
	    return;
	}

	MapObject mo = mapDB.get(moId);

	if(null == mo) {
	    String moName = name;
	    int unitType = WorldStateMgr.convertType(sd.getType());
	    int mapfid = ForceIds.UNKNOWN;
	    ForceId fid = sd.getForceId();
	    if(fid == ForceId.BLUEFOR)
		mapfid = ForceIds.BLUEFOR;
	    else if(fid == ForceId.OPFOR)
		mapfid = ForceIds.OPFOR;
		
	    mo = new MapObject(moId, mapfid, moName, unitType, UnitSizes.SINGLE, sd.getLocation().x, sd.getLocation().y, 0, 0, 0, 0);
	    mo.setEditable(false);
	    mapDB.add(mo);
	}
	mo.setPos(sd.getLocation().x, sd.getLocation().y, 0);
	//	testCoords(moId,sd.location.x, sd.location.y);
	//	Debugger.debug(1,"updateMapDB: Location for "+moId+" set to "+sd.getLocation());
	mo.setOrientation(sd.getHeadingDegrees());
	if(null != sd.destLocation) {
	    mo.setDest(sd.destLocation.x, sd.destLocation.y,0);
	}

	if(null != sd.plannedPath) {
	    Path3D path = sd.plannedPath.path;
	    Vector3D[] waypoints = path.getWaypointsAry();
	    if(waypoints.length <= 0) {
		return;
	    }
	    mo.clearPlannedPath();
	    for(int loopi = 0; loopi < waypoints.length; loopi++) {
		if(null != waypoints[loopi]) {
		    mo.addPlannedPathPoint((float)waypoints[loopi].x, (float)waypoints[loopi].y);
		}
	    }
	}

	if(sd.checkAssumedDead()) 
	    mo.setBda(mo.BDA_DEAD);
	else 
	    mo.setBda(mo.BDA_ALIVE);

	mapDB.setDirty(true);
    }

    public void remove(ArrayList<StateData> removed) {
	Debugger.debug(1,"expired "+removed.size()+" StateData instances from fuser.");
	for(StateData sd: removed) {
	    String moId = sd.getKey();
	    if(sd.getPid() != null)
		moId= sd.getPid().toString();
	    mapDB.remove(moId);
	}
    }

    private void testCoords(String id, double xMeters, double yMeters) {
	LatitudeLongitude latlon = getLatitudeLongitude(xMeters,yMeters);
	// > upper-left corner: 30.2556 lat, -97.6974 lon
	// > lower-right corner: 30.2027 lat, -97.5873 lon
// 	double northLat = 30.2556;
// 	double southLat = 30.2027;
// 	double westLon = -97.6974;
// 	double eastLon = -97.5873;

// 	From the austin 5k map;
 	double northLat = 30.454672700617905;
 	double southLat = 30.4096071468;
 	double westLon = -97.4604290251;
 	double eastLon = -97.40834918396162;

	// upper-left: lat 30.7672, lon -98.7337
	// lower-right: lat 29.7115, lon 96.5307

	if(latlon.latitude < southLat) {
	    double diffDeg = southLat - latlon.latitude;
	    double diffMeters = LatLonUtil.degreesLatToKm(latlon.latitude, diffDeg) * 1000;
	    Debugger.debug(1,id+" loc "+fmt3.format(xMeters)+", "+fmt3.format(yMeters)+" latlon "+latlon+" lat out by "+fmt.format(diffDeg)+" deg "+fmt3.format(diffMeters)+" m");
	}
	else if (latlon.latitude >  northLat) {
	    double diffDeg = latlon.latitude - northLat;
	    double diffMeters = LatLonUtil.degreesLatToKm(latlon.latitude, diffDeg) * 1000;
	    Debugger.debug(1,id+" loc "+fmt3.format(xMeters)+", "+fmt3.format(yMeters)+" latlon "+latlon+" lat out by "+fmt.format(diffDeg)+" deg "+fmt3.format(diffMeters)+" m");
	}
	if(latlon.longitude > eastLon) {
	    double diffDeg = latlon.longitude - eastLon;
	    double diffMeters = LatLonUtil.degreesLonToKm(latlon.latitude, diffDeg) * 1000;
	    Debugger.debug(1,id+" loc "+fmt3.format(xMeters)+", "+fmt3.format(yMeters)+" latlon "+latlon+" lon out by "+fmt.format(diffDeg)+" deg "+fmt3.format(diffMeters)+" m");
	}
	else if (latlon.longitude <  westLon) {
	    double diffDeg = westLon - latlon.longitude;
	    double diffMeters = LatLonUtil.degreesLonToKm(latlon.latitude, diffDeg) * 1000;
	    Debugger.debug(1,id+" loc "+fmt3.format(xMeters)+", "+fmt3.format(yMeters)+" latlon "+latlon+" lon out by "+fmt.format(diffDeg)+" deg "+fmt3.format(diffMeters)+" m");
	}

    }
    public class LatitudeLongitude
    {
            public double latitude;
            public double longitude;
	public String toString() {
	    return fmt.format(latitude)+", "+fmt.format(longitude);
	}
    }

    public LatitudeLongitude getLatitudeLongitude(double xMeters, double yMeters)
    {
	double localKmX = xMeters / 1000.0;
	double localKmY = yMeters / 1000.0;
	LatitudeLongitude latlon = new LatitudeLongitude();
	latlon.latitude = MAP_LOWER_LEFT_LAT + (float) LatLonUtil.kmToDegreesLat(MAP_LOWER_LEFT_LAT, localKmY);
	latlon.longitude = MAP_LOWER_LEFT_LON + (float) LatLonUtil.kmToDegreesLon(MAP_LOWER_LEFT_LAT, localKmX);
	return latlon;
    }

}
