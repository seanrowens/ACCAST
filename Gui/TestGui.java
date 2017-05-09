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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.georgeandabe.tigerline.model.TLData;

public class TestGui implements ActionListener {
    public final static String ICON_DIR="/afs/cs.cmu.edu/user/owens/camra/terrain_gui_icons";
    // public final static String ICON_DIR="c:/owens/terrain_gui_icons/";
    public final static String ACTION_ONE="Pause";
    public final static String ACTION_TWO="Clear";
    public final static String ACTION_THREE="Generate";
    //    public final static String ACTION_FOUR="Zoom";

    public static int REPAINT_INTERVAL_MS = 1000/20;

    private DebugInterface debug;
    private String baseGridFileName = null;
    private String elevationGridFileName = null;
    private String soilGridFileName = null;
    private String xGridFileName=null;
    private String yGridFileName=null;
    private String zGridFileName=null;
    private IntGrid soilGrid = null;
    private DoubleGrid elevationGrid = null;
    private IntGrid xGrid = null;
    private IntGrid yGrid = null;
    private IntGrid zGrid = null;
    private TLData roadData = null;

    private JFrame topLevelFrame = null;
    private JPanel topPanel = null;
    private JTabbedPane tpane = null;

    private MapDB mapDB = null;
    private Background background = null;
    private BackgroundConfig config = null;
    private JPanel actionButtonPanel = null;
    private JPanel terrainPanel = null;
    private RepaintTimer repaintTimer = null;

    private ArrayList entityList = new ArrayList();

    private HashMap<Integer, ImageIcon> imageryMap = new HashMap<Integer, ImageIcon>();

    public class Entity implements Runnable {
	private Thread myThread;
	private boolean pause = false;
	private MapObject mo;
	private int xstart;
	private int ystart;
	private int xgoal;
	private int ygoal;
	private double xdiff;
	private double ydiff;
	private double orientation = 0;

	public Entity(MapObject mo, int xgoal, int ygoal) {
	    Debug.debug("new mo="+mo.getKey()+" goal="+xgoal+", "+ygoal);
	    myThread = new Thread(this);
	    this.mo = mo;
	    this.xgoal = xgoal;
	    this.ygoal = ygoal;
	    this.xstart = (int)mo.getPosX();
	    this.ystart = (int)mo.getPosY();

	    double vx = xgoal - xstart;
	    double vy = ygoal - ystart;
	    double h = Math.sqrt(vx  * vx + vy * vy);
	    vx = vx/h;
	    vy = vy/h;
	    xdiff = (vx * 100);
	    ydiff = (vy * 100);
	}

	public void start() {
	    myThread.start();
	}

	private void sleepFor(long msecs) {
	    try {
		Thread.sleep(msecs);
	    }
	    catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

	public void pause() {
	    synchronized(this) {
		pause = !pause;
	    }
	}

	public void run() {
	    while(true) {
		sleepFor(50);
		synchronized(this) {
		    if(pause)
			continue;
		}
		double x = mo.getPosX();
		double y = mo.getPosY();
		double z = mo.getPosZ();
		x += xdiff + ((Math.random() * xdiff) - xdiff/2);
		y += ydiff + ((Math.random() * ydiff) - ydiff/2);;
		if(x > soilGrid.getWidthMeters()) {
		    xdiff = - xdiff;
		    x = soilGrid.getWidthMeters() - 1;
		}
		else if(x < 0) {
		    xdiff = - xdiff;
		    x = 0;
		}
		if(y > soilGrid.getHeightMeters()) {
		    ydiff = - ydiff;
		    y = soilGrid.getHeightMeters() - 1;
		}
		else if(y < 0) {
		    ydiff = - ydiff;
		    y = 0;
		}
		mo.setOrientation(x,y);
		mo.setPos(x,y,z);
// 		orientation += 1.0;
// 		if(orientation > 360.0)
// 		    orientation = 0.0;
// 		mo.setOrientation(orientation);
// 		mo.setEllipse(5000, 3000, orientation);

		if(Math.random() < .005)
		    generateDetonations(1);


		mapDB.setDirty(true);
		//		Debug.debug("mo="+mo.getKey()+" set pos to "+(int)x+", "+(int)y);
	    }
	}
    }

    public void loadImagery() {
	String baseDir=ICON_DIR+"/original";
	imageryMap.put(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	imageryMap.put(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	imageryMap.put(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	imageryMap.put(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    public void loadIconsOriginal() {
	String baseDir=ICON_DIR+"/original";
	UnitTypes.setImageIcon(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	UnitTypes.setImageIcon(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	UnitTypes.setImageIcon(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	UnitTypes.setImageIcon(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    public void loadIcons50() {
	String baseDir=ICON_DIR+"/scale50";
	UnitTypes.setImageIcon(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	UnitTypes.setImageIcon(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	UnitTypes.setImageIcon(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	UnitTypes.setImageIcon(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    public void loadIcons25() {
	String baseDir=ICON_DIR;
	UnitTypes.setImageIcon(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	UnitTypes.setImageIcon(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	UnitTypes.setImageIcon(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	UnitTypes.setImageIcon(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    public void addTabLater(JTabbedPane tabPane, String name, JPanel panel, String tip) {
	final String fname = name;
	final JPanel fpanel = panel;
	final String ftip = tip;
	final JTabbedPane fpane = tabPane;
	EventQueue.invokeLater(new Runnable() { public void run()
		{ fpane.addTab(fname, null, fpanel, ftip); }});
    }

    private boolean loadCTDB(String baseGridFileName) {
	debug.debug("Loading CTDBs.");

	baseGridFileName = baseGridFileName;

	if(null == elevationGridFileName)
	    elevationGridFileName = baseGridFileName+"_e.grd";
	if(null == soilGridFileName)
	    soilGridFileName = baseGridFileName+"_s.grd";
	if(null == xGridFileName)
	    xGridFileName = baseGridFileName+"_x.grd";
	if(null == yGridFileName)
	    yGridFileName = baseGridFileName+"_y.grd";
	if(null == zGridFileName)
	    zGridFileName = baseGridFileName+"_z.grd";

	long now = System.currentTimeMillis();
	soilGrid = new IntGrid();
	if(!soilGrid.loadSoilTypeFile(soilGridFileName)) {
	    debug.error("Couldn't load soil grid="+soilGridFileName);
	    return false;
	}
	debug.debug("Loading soil grid elapsed="+(System.currentTimeMillis() - now));
	now = System.currentTimeMillis();
	elevationGrid = new DoubleGrid();
	if(!elevationGrid.loadGridFile(elevationGridFileName)) {
	    debug.error("Couldn't load elevation grid="+elevationGridFileName);
	    return false;
	}
	debug.debug("Loading elevation grid elapsed="+(System.currentTimeMillis() - now));
	return true;
    }

    private JPanel makeTerrainPanel(JFrame myFrame, MapDB mapDB,Background background,BackgroundConfig config, JPanel actionButtonPanel) {
	JPanel tPanel = new PaletteTerrainPanel(myFrame, mapDB, background, config, actionButtonPanel);
	return tPanel;
    }

    public void makeTerrainGui() {
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
	mapDB = new MapDB(soilGrid.getGccToTccTransform(), soilGrid.getTccToGccTransform(), xGrid, yGrid, zGrid);

	background = new Background(elevationGrid, soilGrid, contourPolygonsGrid, obstacleGrid, null, contours,roadData);
	config = new BackgroundConfig(viewPort);
	
	actionButtonPanel = new JPanel();

	JButton actionOneButton = new JButton(ACTION_ONE);
	actionOneButton.setActionCommand(ACTION_ONE);
	actionOneButton.addActionListener(this);
	actionButtonPanel.add(actionOneButton);

	JButton actionTwoButton = new JButton(ACTION_TWO);
	actionTwoButton.setActionCommand(ACTION_TWO);
	actionTwoButton.addActionListener(this);
	actionButtonPanel.add(actionTwoButton);

	JButton actionThreeButton = new JButton(ACTION_THREE);
	actionThreeButton.setActionCommand(ACTION_THREE);
	actionThreeButton.addActionListener(this);
	actionButtonPanel.add(actionThreeButton);

// 	JButton actionFourButton = new JButton(ACTION_FOUR);
// 	actionFourButton.setActionCommand(ACTION_FOUR);
// 	actionFourButton.addActionListener(this);
// 	actionButtonPanel.add(actionFourButton);

	terrainPanel = makeTerrainPanel(topLevelFrame, mapDB, background, config, actionButtonPanel);

	//	terrainPanel = new TerrainPanel(mapDB,background,config, actionButtonPanel);
	repaintTimer = new RepaintTimer(terrainPanel, mapDB, REPAINT_INTERVAL_MS);
	repaintTimer.start();

	addTabLater(tpane, "terrain", terrainPanel, "terrain");
    }

    public void makeTerrainGui2() {
	ViewPort viewPort = new ViewPort(soilGrid);
	mapDB = new MapDB(soilGrid.getGccToTccTransform(), soilGrid.getTccToGccTransform(), xGrid, yGrid, zGrid);

	background = new Background(elevationGrid, soilGrid, null, null, null, null, null);
	config = new BackgroundConfig(viewPort);

	actionButtonPanel = new JPanel();
	terrainPanel = new TerrainPanel(mapDB,background,config, actionButtonPanel);
	repaintTimer = new RepaintTimer(terrainPanel, mapDB, REPAINT_INTERVAL_MS);

	addTabLater(tpane, "terrain", terrainPanel, "terrain");
    }

    public TestGui(String baseGridFileName,ArrayList<String> roads) {
	debug = new DebugFacade(this);

	if(!loadCTDB(baseGridFileName)) {
	    debug.error("Couldn't load soil/elevation/x/y/z files, giving up.");
	    System.exit(1);
	}

	roadData = Roads.load(roadData, roads);

	topLevelFrame = new JFrame("TestGui");
	topLevelFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
	    });
	// For some reason I don't understand, if I just use 0,0 here
	// (the top left of the screen), xwin-32 puts the window
	// slightly off the screen to the top and the left.
	topLevelFrame.setLocation(5,30);
	topLevelFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
	topPanel = new JPanel();
	topPanel.setLayout( new BorderLayout() );
	topLevelFrame.getContentPane().add( topPanel );
	tpane = new JTabbedPane();
	topPanel.add(tpane, BorderLayout.CENTER);

	// Note, DON'T use .pack() - or we undo all the
	// setSize/setLocation crap above.

	makeTerrainGui();

	topLevelFrame.setVisible(true);
	//	loadIcons25();
	loadImagery();
	generate();
    }

    public void generate() {
	generateDetonations(20);
 	generate(5, UnitTypes.ARMOR, ForceIds.BLUEFOR);
//  	generate(5, UnitTypes.LIGHT_INFANTRY, ForceIds.BLUEFOR);
 	generate(5, UnitTypes.ARMOR, ForceIds.OPFOR);
//  	generate(5, UnitTypes.LIGHT_INFANTRY, ForceIds.OPFOR);
//  	generate(5, UnitTypes.AIR_DEFENSE_ARTILLERY, ForceIds.OPFOR);
//  	generate(10, UnitTypes.CIVILIAN_TRUCK, ForceIds.NEUTRAL);
    }

    public void place(int type, int forceid, int x, int y, int xgoal, int ygoal) {
	String moKey = MapObject.createKey();
	String moName = moKey;
	MapObject mo = new MapObject(moKey, forceid, moName, type, UnitSizes.SINGLE, x, y, 0, 0, 0, 0);
	mo.setOrientation(Math.random() * 360);
	mo.setFlash(true);
	if(ForceIds.BLUEFOR != forceid) {
	    mo.setMouseOverImage(imageryMap.get(type).getImage());
	    mo.setMouseOverText(UnitTypes.getName(type)+" : "+moName);
	}
	if(false) {
	if((UnitTypes.ARMOR == type)
	   || (UnitTypes.LIGHT_INFANTRY == type)
	   || (UnitTypes.MECH_INFANTRY == type)
	   || (UnitTypes.LIGHT_FIELD_ARTILLERY == type)
	   || (UnitTypes.MECH_FIELD_ARTILLERY == type)
	   || (UnitTypes.MILITARY_POLICE == type)
	   || (UnitTypes.MISSILE == type)
	   || (UnitTypes.SA_MISSILE == type)
	   || (UnitTypes.SS_MISSILE == type)
	   || (UnitTypes.WASM == type)
	   || (UnitTypes.MUAV == type)
	   || (UnitTypes.MILITARY_TRUCK == type)
	   ) 
	    mo.setThreatToGround(true);

	if((UnitTypes.AIR_DEFENSE_ARTILLERY == type)
	   || (UnitTypes.AIR_FORCES == type)
	   )
	    mo.setThreatToAir(true);
	}

	mapDB.add(mo);

	Entity entity = new Entity(mo, xgoal, ygoal);
	entityList.add(entity);
	entity.start();
	String goalKey = MapObject.createKey();
	MapObject goal = new MapObject(goalKey, MapObject.TYPE_OBJECTIVE, xgoal, ygoal, 0,0,0);
	mapDB.add(goal);
    }

    public void generate(int numOpfor, int type, int forceid) {
	Random rand = new Random();
	
// 	probGrid.computeHighestLowest();
// 	double maxRoll = probGrid.getHighest();
 	double maxRoll = 1.0;

	// leftBorder is set to nonzero to avoid placing
	// enemy units right on top of the starting position
	// of the HMMWVs.  

	//	int leftBorder = 13000;
	int leftBorder = 0;
	int rightBorder = 0;
	int topBorder = 0;
	int bottomBorder = 0;
	int width = (int)soilGrid.getWidthMeters() - leftBorder - rightBorder;
	int height = (int)soilGrid.getHeightMeters() - bottomBorder - topBorder;

	for(int loopi = 0; loopi < numOpfor; loopi++) {
	    while(true) {
		//		try {Thread.sleep(1000);} catch (InterruptedException e) {}

		// maxRoll is set (above) to the highest value in the
		// probability grid.  If we were to pick a random
		// number between 0 and 1, and most of our values in
		// probability grid are very small (which they are
		// likely to be since we normalize which means all
		// values in the grid sum to 1.0), we'd waste a LOT of
		// iterations through here.  So, if we just roll from
		// 0 to maxRoll, the only difference is in the number
		// of calls to the pseudo random number generator.
		// Otherwise the algorithm is the same.
		// double roll = rand.nextDouble() * maxRoll;
		double roll = rand.nextDouble();

		int x = rand.nextInt(width)+leftBorder+soilGrid.getBottomLeftX();
		int y = rand.nextInt(height)+bottomBorder+soilGrid.getBottomLeftY();
		double prob = soilGrid.getValue(soilGrid.toGridX(x), soilGrid.toGridY(y));

		debug.debug("opfor "+loopi+" trying location "+x+","+y+ " rolled "+roll+" prob at that point is "+prob);

		if(roll <= prob) {
		    debug.debug("opfor "+loopi+" creating opfor at location "+x+","+y+ " rolled "+roll+" prob at that point is "+prob);
		    int xgoal = rand.nextInt(width)+leftBorder+soilGrid.getBottomLeftX();
		    int ygoal = rand.nextInt(height)+bottomBorder+soilGrid.getBottomLeftY();

		    place(type, forceid, x, y, xgoal, ygoal);
		    break;
		}
	    }
	}
	
    }

    public void generateDetonations(int num) {
	Random rand = new Random();
	
 	double maxRoll = 1.0;
	// leftBorder is set to nonzero to avoid placing
	// enemy units right on top of the starting position
	// of the HMMWVs.  

	//	int leftBorder = 13000;
	int leftBorder = 0;
	int rightBorder = 0;
	int topBorder = 0;
	int bottomBorder = 0;
	int width = (int)soilGrid.getWidthMeters() - leftBorder - rightBorder;
	int height = (int)soilGrid.getHeightMeters() - bottomBorder - topBorder;

	for(int loopi = 0; loopi < num; loopi++) {
	    while(true) {
		double roll = rand.nextDouble();
		int x = rand.nextInt(width)+leftBorder+soilGrid.getBottomLeftX();
		int y = rand.nextInt(height)+bottomBorder+soilGrid.getBottomLeftY();
		double prob = soilGrid.getValue(soilGrid.toGridX(x), soilGrid.toGridY(y));

		debug.debug("detonation "+loopi+" trying location "+x+","+y+ " rolled "+roll+" prob at that point is "+prob);

		if(roll <= prob) {
		    debug.debug("detonation "+loopi+" creating opfor at location "+x+","+y+ " rolled "+roll+" prob at that point is "+prob);
		    int xgoal = rand.nextInt(width)+leftBorder+soilGrid.getBottomLeftX();
		    int ygoal = rand.nextInt(height)+bottomBorder+soilGrid.getBottomLeftY();

		    MapObject detMo;
		    detMo = new MapObject("detonation"+loopi, MapObject.TYPE_DETONATION, x, y, 0.0, 100.0, 0.0);
		    mapDB.add(detMo);
		    mapDB.setDirty(true);

		    break;
		}
	    }
	}
	
    }

//      private static boolean zoomed = false;
//      // This won't unzoom, just yet.
//      public void setViewPort() {
//  	if(zoomed) {
//  	    config.viewPort.requestSourceView(0.0, 0.0, 50000.0, 50000.0);
//  	    zoomed = false;
//  	}
//  	else {
//  	    config.viewPort.requestSourceView(10000.0, 10000.0, 20000.0, 20000.0);
//  	    zoomed = true;
//  	}
//
// // background.getSourceBottomLeftX(), 
// // 				  background.getSourceBottomLeftY(),
// // 				  background.getSourceWidth(), 
// // 				  background.getSourceHeight());
// // 	config.viewPort.setDest();
//     }

    private void pauseAll() {
	Iterator it = entityList.iterator();
	while(it.hasNext()) {
	    Entity e = (Entity)it.next();
	    e.pause();
	}
    }

    public void actionPerformed(ActionEvent e) {
	try {
	    if (ACTION_ONE.equals(e.getActionCommand())) {
		debug.debug("User clicked "+ACTION_ONE+" button.");
		pauseAll();
	    }
	    else if (ACTION_TWO.equals(e.getActionCommand())) {
		mapDB.clear();
		entityList.clear();
	    }
	    else if (ACTION_THREE.equals(e.getActionCommand())) {
		generate();
	    }
// 	    else if (ACTION_FOUR.equals(e.getActionCommand())) {
// 		setViewPort();
// 	    }
	    else {
		debug.error("Unknown action from swing="+e.getActionCommand()+", ignoring.");
	    }
	}
	catch (Exception e2) {
	    System.err.println("Exception processing swing action="+e.getActionCommand()+", ignoring.");
	    System.err.println("Exception="+e2);
	}
    }

    public static void main(String args[]) {
	String baseGridFileName = null;
	ArrayList<String> roads = new ArrayList<String>();
	
	//	Debug.setOut(System.out);
	for(int loopi = 0; loopi < args.length; loopi++) {
	    if((args[loopi].equalsIgnoreCase("-m") 
		|| args[loopi].equalsIgnoreCase("--map"))
	       && ((loopi+1) < args.length)) {
		baseGridFileName = args[loopi+1];
		loopi++;
	    }
	    else if((args[loopi].equalsIgnoreCase("-r") 
		|| args[loopi].equalsIgnoreCase("--roads"))
	       && ((loopi+1) < args.length)) {
		roads.add(args[loopi+1]);
		loopi++;
	    }
	    else {
		Debug.error("Usage: TestGui --map/-m basegridfilename");
		Debug.error("\ti.e. TestGui --map /usr1/grd/hood/hood_v2.0_062098_0_0_50000_50000_050");
		System.exit(1);
	    }
	}
	
	// Create an instance of the test application
	TestGui foo = new TestGui(baseGridFileName,roads);
    }
}
