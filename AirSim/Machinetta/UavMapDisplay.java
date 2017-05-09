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
package AirSim.Machinetta;

import AirSim.Environment.Waypoint;
import Gui.StringUtils;
import Gui.LatLonUtil;
import Util.CSV;
import java.awt.geom.Point2D;

import java.awt.image.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

import javax.imageio.*;

public class UavMapDisplay extends JPanel implements Runnable, MouseInputListener {

    // This is the origin point (lower left corner) of the 1km chunk
    // of the background image that we want to display
    public final static float ORIGIN_LAT = 40.4563444444f;
    public final static float ORIGIN_LON = -79.789486111f;

    public final static double TARGET_MAX_PIXEL_RANGE = 50;


// Image bottom left corner for testing
//    public final static float ORIGIN_LAT = 40.45120555555555f;
//    public final static float ORIGIN_LON = -79.79637222222222f;

    public final static String DEFAULT_BACKGROUND_IMAGE_FILENAME = "/home/owens/gascolahires.jpg";
    //    public final static String DEFAULT_BACKGROUND_IMAGE_FILENAME = "\\home\\owens\\gascolahires.jpg";

    // old
//     public final static double BACK_NORTH_LAT = 40.46586190893674;
//     public final static double BACK_SOUTH_LAT = 40.45120555555555;
//     public final static double BACK_EAST_LON = -79.77922579284821;
//     public final static double BACK_WEST_LON = -79.79637222222222;

// new
    public final static double BACK_NORTH_LAT = 40.465863888888888;
    public final static double BACK_SOUTH_LAT = 40.45120555555555;
    public final static double BACK_EAST_LON = -79.779224999999999;
    public final static double BACK_WEST_LON = -79.79637222222222;

    public final static double BACK_WIDTH_LON = BACK_WEST_LON - BACK_EAST_LON;
    public final static double BACK_HEIGHT_LAT = BACK_NORTH_LAT - BACK_SOUTH_LAT;

    public final static double BACK_WEST_OFF_LON = BACK_WEST_LON - ORIGIN_LON;
    public final static double BACK_EAST_OFF_LON = BACK_EAST_LON - ORIGIN_LON;
    public final static double BACK_NORTH_OFF_LAT = BACK_NORTH_LAT - ORIGIN_LAT;
    public final static double BACK_SOUTH_OFF_LAT = BACK_SOUTH_LAT - ORIGIN_LAT;


    public final static int UAV_OVAL_SIZE = 12;
    public final static int UAV_OVAL_SIZE_HALF = 6;

    public final static double MAP_WIDTH_METERS = 1000;


    HashMap<Integer,UavMapTarget> targetMap = new HashMap<Integer,UavMapTarget>();

    HashMap<Integer, LinkedList<Point2D.Double>> uavLocs = new HashMap<Integer, LinkedList<Point2D.Double>>();

    private int maxUavTrailSize = 4000;
    private String backgroundImageFilename;
    private String serverHostname;
    private int serverPort;
    private String playbackFilename;
    private double speedup;
    
    private UavMapLog uavMapLog = null;
    private String name = "MapDisplay";
    private Thread myThread = null;
    private Socket sock = null;
    private BufferedReader in = null;
    private PrintWriter out = null;


    BufferedImage backgroundSource = null;
    BufferedImage scaledBackground = null;
    int screenWidthCache = -1;
    int screenHeightCache = -1;

    JFrame topFrame;

    public UavMapDisplay(String backgroundImageFilename, String vcBridgeServerHostname, int port, String playbackFilename, double speedup, int maxUavTrailSize) {
	this.backgroundImageFilename = backgroundImageFilename;
	this.serverHostname = vcBridgeServerHostname;
	this.serverPort = port;
	this.playbackFilename = playbackFilename;
	this.speedup = speedup;
	this.maxUavTrailSize = maxUavTrailSize;

	if((null == vcBridgeServerHostname) && (null == playbackFilename)) {
	    throw (new RuntimeException("Either vcBridgeServerHostname or playbackFilename must be non null to run UavMapDisplay."));

	}
	uavMapLog = new UavMapLog();
	
	this.addMouseListener(this);
	this.addMouseMotionListener(this);

	try {
	    backgroundSource = ImageIO.read(new File(backgroundImageFilename));
	} 
	catch(java.io.IOException e) {
	    Machinetta.Debugger.debug(3, "IOException loading background image,  e="+e);
	    e.printStackTrace();
	}

	topFrame = new JFrame("UAV Map Display");
	topFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
	    });

	topFrame.add(this);
	topFrame.setSize(1000,1000);
	topFrame.setLocation(20,20);
	topFrame.show();

	myThread = new Thread(this);
    }

    public void start() {
	myThread.start();
    }

    private void updateTarget(MouseEvent e, UavMapTarget target) {
	target.screenx = e.getX();
	target.screeny = e.getY();

     	target.screenw = getSize().getWidth();
     	target.screenh = getSize().getHeight();
	double wscale = target.screenw/MAP_WIDTH_METERS;
	double hscale = target.screenh/MAP_WIDTH_METERS;
	double scale = wscale;
	if(hscale < scale) 
	    scale = hscale;

	target.localx = target.screenx/scale;
	target.localy = (target.screenh - target.screeny)/scale;

	target.lat = ORIGIN_LAT + LatLonUtil.kmToDegreesLat(ORIGIN_LAT, target.localy/1000.0);
	target.lon = ORIGIN_LON + LatLonUtil.kmToDegreesLat(ORIGIN_LAT, target.localx/1000.0);;
    }

    private UavMapTarget findNearestTarget(double screenx, double screeny, double maxPixelRange) {
	UavMapTarget nearestTarget = null;
	double nearestDist2 = Double.MAX_VALUE;
	double maxPixelRange2 = maxPixelRange*maxPixelRange;
	for(UavMapTarget target: targetMap.values()) {
	    double dist2 = (target.screenx - screenx)*(target.screenx - screenx) 
		+(target.screeny - screeny)*(target.screeny - screeny);
	    if(dist2 > maxPixelRange2)
		continue;
	    if(dist2 < nearestDist2)
		nearestTarget = target;
	}
	return nearestTarget;
    }

    private void setSelectedTarget(double screenx, double screeny, double maxPixelRange) {
	UavMapTarget nearestTarget = null;
	double nearestDist2 = Double.MAX_VALUE;
	double maxPixelRange2 = maxPixelRange*maxPixelRange;
	for(UavMapTarget target: targetMap.values()) {
	    target.setSelected(false);
	    double dist2 = (target.screenx - screenx)*(target.screenx - screenx) 
		+(target.screeny - screeny)*(target.screeny - screeny);
	    if(dist2 > maxPixelRange2)
		continue;
	    if(dist2 < nearestDist2)
		nearestTarget = target;
	}
	if(null != nearestTarget)
	    nearestTarget.setSelected(true);
    }

    private UavMapTarget curTarget = null;
    public void mousePressed(MouseEvent e) {
	curTarget = findNearestTarget(e.getX(), e.getY(), TARGET_MAX_PIXEL_RANGE);
	if(e.getButton() == e.BUTTON3) { 	// BUTTON1, BUTTON2, BUTTON3
	    System.err.println("Deleting click");
	    if(null != curTarget) {
		uavMapLog.click("clickdelete", curTarget);
		targetMap.remove(curTarget.id);
	    }
	}
	else {
	    if(null == curTarget) {
		System.err.println("adding click");
		curTarget = new UavMapTarget();
		updateTarget(e, curTarget);
		targetMap.put(curTarget.id, curTarget);
		uavMapLog.click("clickadd", curTarget);
	    }
	    else {
		System.err.println("moving click");
		updateTarget(e, curTarget);
		uavMapLog.click("clickmove",curTarget);
	    }
	}
    }

    public void mouseReleased(MouseEvent e) {
	if(null != curTarget) {
	    updateTarget(e, curTarget);
	    uavMapLog.click("clickrelease", curTarget);
	    curTarget = null;
	}
    }
    public void mouseEntered(MouseEvent e) {
	//      	Machinetta.Debugger.debug(1,"mouse entered e="+e);
    }
    public void mouseExited(MouseEvent e) {
	//      	Machinetta.Debugger.debug(1,"mouse exited e="+e);
    }
    public void mouseClicked(MouseEvent e) {
	//      	Machinetta.Debugger.debug(1,"mouse clicked e="+e);
    }

    public void mouseDragged(MouseEvent e) {
	if(null !=  curTarget) {
	    updateTarget(e, curTarget);
	    uavMapLog.click("clickdrag",curTarget);
	    Machinetta.Debugger.debug(1,"Updated target to "+curTarget);
	}
	else 
	    Machinetta.Debugger.debug(1,"Dragging, curTarget is null.");
	
    }

    public void mouseMoved(MouseEvent e) {
	setSelectedTarget(e.getX(), e.getY(), TARGET_MAX_PIXEL_RANGE);
    }

    public void updateUav(int uavid, double latitude, double longitude, double altitude, double yaw, double pitch, double roll) {
	double localx = LatLonUtil.degreesLonToKm(ORIGIN_LAT,longitude - ORIGIN_LON) * 1000.0;
	double localy = LatLonUtil.degreesLatToKm(ORIGIN_LAT,latitude - ORIGIN_LAT) * 1000.0;
	Point2D.Double point = new Point2D.Double(localx, localy);
      	Machinetta.Debugger.debug(1,"uavid "+uavid+" lat/lon "+latitude+","+longitude+" local "+localx+","+localy);
	synchronized(uavLocs) {
	    LinkedList<Point2D.Double> l = uavLocs.get(uavid);
	    if(null == l) {
		l = new LinkedList<Point2D.Double>();
	        uavLocs.put(uavid, l);
	    }
	    l.add(point);
	    if(l.size() > maxUavTrailSize)
		l.removeFirst();
	}
	repaint();
	uavMapLog.navData(uavid, latitude, longitude, altitude, localx, localy, yaw, pitch, roll);
    }

    public void paint(Graphics g) {
	long startTime = System.currentTimeMillis();

        Graphics2D g2 = (Graphics2D)g;
	g2.setColor(Color.black);
	g2.clearRect(0,0,(int)getSize().getWidth(),(int)getSize().getHeight());

     	double screenW = getSize().getWidth();
     	double screenH = getSize().getHeight();
     	int iscreenW = (int)screenW;
     	int iscreenH = (int)screenH;
	double wscale = screenW/MAP_WIDTH_METERS;
	double hscale = screenH/MAP_WIDTH_METERS;
	double scale = wscale;
	if(hscale < scale) 
	    scale = hscale;

	if((iscreenW != screenWidthCache)
	   || (iscreenH != screenHeightCache)
	   || (null == scaledBackground)) {
	    
	    screenWidthCache = iscreenW;
	    screenHeightCache = iscreenH;

	    // calc the image corners in local coords relative to the
	    // ORIGIN lat/lon
	    double westLocalX = LatLonUtil.degreesLonToKm(ORIGIN_LAT,BACK_WEST_OFF_LON) * 1000.0;
	    double eastLocalX = LatLonUtil.degreesLonToKm(ORIGIN_LAT,BACK_EAST_OFF_LON) * 1000.0;
	    double northLocalY = LatLonUtil.degreesLatToKm(ORIGIN_LAT,BACK_NORTH_OFF_LAT) * 1000.0;
	    double southLocalY = LatLonUtil.degreesLatToKm(ORIGIN_LAT,BACK_SOUTH_OFF_LAT) * 1000.0;
	    
	    // Now convert the local coords to screen coords... where
	    // we know that the window itself is supposed to show
	    // MAP_WIDTH_METERS by MAP_WIDTH_METERS
	    int westScreenX = (int)(westLocalX * scale);
	    int eastScreenX = (int)(eastLocalX * scale);
	    int northScreenY = iscreenH - (int)((northLocalY * scale));
	    int southScreenY = iscreenH - (int)((southLocalY * scale));

	    // Note, we don't calculate this from screenY values because they're flipped...
	    int widthScreen = (int)((eastLocalX - westLocalX) * scale);
	    int heightScreen = (int)((northLocalY - southLocalY) * scale);

	    Machinetta.Debugger.debug(1, "Background image: local west "+westLocalX+" east "+eastLocalX+" north "+northLocalY+" south "+southLocalY);
	    Machinetta.Debugger.debug(1, "Background image: screen west "+westScreenX+" east "+eastScreenX+" north "+northScreenY+" south "+southScreenY + " width "+widthScreen+" height "+heightScreen);

	    scaledBackground = g2.getDeviceConfiguration().createCompatibleImage(iscreenW, iscreenH);
	    Graphics2D scaledg2 = scaledBackground.createGraphics();
	    scaledg2.drawImage(backgroundSource,westScreenX, northScreenY, widthScreen, heightScreen, null);
	    //scaledg2.drawImage(backgroundSource,0,0, widthScreen, heightScreen, null);
	    scaledg2.dispose();
	}
	
	g2.drawImage(scaledBackground,0,0,null);

        g2.setStroke (new BasicStroke(1));

	synchronized(uavLocs) {
	    for(int uavid: uavLocs.keySet()) {
		LinkedList<Point2D.Double> l = uavLocs.get(uavid);
		int oldx = 0;
		int oldy = 0;
		int x = Integer.MIN_VALUE;
		int y = Integer.MIN_VALUE;
		g2.setColor (Color.blue);
		for(Point2D.Double p: l) {
		    oldx = x;
		    oldy = y;
		    x = (int)(p.x * scale);
		    y = (int)(iscreenH - (p.y * scale));
		    if(oldx != Integer.MIN_VALUE)
			g2.drawLine(oldx,oldy,x,y);
		}
		g2.setColor (Color.red);
		g2.drawOval(x-UAV_OVAL_SIZE_HALF,y-UAV_OVAL_SIZE_HALF, UAV_OVAL_SIZE, UAV_OVAL_SIZE);
		g2.drawString("UAV "+uavid, x+UAV_OVAL_SIZE, y);
		//		Machinetta.Debugger.debug(1,"paint: uavid "+uavid+" at "+x+","+y);
	    }
	}
        
	for(UavMapTarget target: targetMap.values()) {
	    if(target.isSelected())
		g2.setColor (Color.white);
	    else
		g2.setColor (Color.red);
	    int x = (int)(target.localx * scale);
	    int y = (int)(iscreenH - (target.localy * scale));
	    g2.drawLine(x-10,y,x+10,y);
	    g2.drawLine(x,y-10,x,y+10);
	}

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
	//      	Machinetta.Debugger.debug("paint: total="+totalTime,1,this);
    }    

    private  void processInputLine(String line) {
	String[] words = StringUtils.parseList(' ',line);
	if(words.length <= 0)
	    return;
	if(words[0].equalsIgnoreCase("ERROR")) {
	    Machinetta.Debugger.debug(3," Got an error back from VCBridgeServer = "+line);
	    return;
	}
	if(words[0].equalsIgnoreCase("NAVDATA")) {
	    if(words.length < 9) {
		Machinetta.Debugger.debug(3," Got a NAVDATA line from VCBridgeServer, but not enough fields, should be 9, only "+words.length+", original line="+line);
		return;
	    }
	    int uavid = Integer.parseInt(words[2]);
	    double latitude = Double.parseDouble(words[4]);
	    double longitude = Double.parseDouble(words[6]);
	    double altitude =  Double.parseDouble(words[8]);
	    double yaw = 0.0;
	    double pitch = 0.0;
	    double roll = 0.0;
	    if(words.length >= 14) {
		yaw = Double.parseDouble(words[10]);
		pitch = Double.parseDouble(words[12]);
		roll =  Double.parseDouble(words[14]);
	    }
	    
	    updateUav(uavid, latitude, longitude, altitude,yaw,pitch,roll);

	    return;
	}
	Machinetta.Debugger.debug(1,"Unknown line from VCBridgeServer="+line);
    }
    
    private void playbackFile() {
	uavMapLog.playbackNavDataFromFile(this, playbackFilename,speedup);
    }

    private void runFromServer() {
	while(true) {
	    Machinetta.Debugger.debug(1,"Opening connection to "+serverHostname+" port "+serverPort);

	    try {
		sock = new Socket(serverHostname, serverPort);
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out = new PrintWriter(sock.getOutputStream());
	    }
	    catch (IOException e){
		Machinetta.Debugger.debug(1,"        Exception opening connection to "+serverHostname+" port "+serverPort+", will wait 1000ms and try again, exception is;");
		e.printStackTrace(System.err);
		try { Thread.sleep(1000); } catch(Exception e2) {}
		continue;
	    }

	    Machinetta.Debugger.debug(1,"About to start reading/writing to/from socket.");

	    out.println("myname "+name);
	    out.println("uavid all");
	    out.flush();
	    while(true) {
		try {
		    String line = in.readLine();
		    if(null == line) {
			Machinetta.Debugger.debug(3,"Socket to VCBridgeServer has closed on us!");
			break;
		    }
		    processInputLine(line);
		}
		catch (Exception e){
		    Machinetta.Debugger.debug(3,"Exception reading socket from VCBridgeServer, e="+e);
		    e.printStackTrace();
		    break;
		}
	    }


	    try {
		sock.close();
	    }
	    catch (IOException e){
		System.err.println("Exception closing socket.");
		e.printStackTrace(System.err);
	    }

	    try { Thread.sleep(1000); } catch(Exception e) {}
	    // loop back to top of while and try to reopen socket!
	} 
    }

    public void run() {
	if(null == playbackFilename) 
	    runFromServer();
	else
	    playbackFile();
    }

    public static void main(String argv[]) {

	String backgroundImageFilename = DEFAULT_BACKGROUND_IMAGE_FILENAME;
	int vcBridgeServerPort = 54321;
	String vcBridgeServerHostname = "127.0.0.1";
	String playbackFilename = null;
	int maxUavTrailSize = 150;
	double speedup = 1.0;

	for(int loopi=0; loopi < argv.length; loopi++) {
	    if((argv[loopi].equalsIgnoreCase("-background")) || (argv[loopi].equalsIgnoreCase("--background")) && ((loopi+1) < argv.length)) {
		backgroundImageFilename = argv[++loopi];
	    }
	    else if((argv[loopi].equalsIgnoreCase("-vcbridgeserverip")) || (argv[loopi].equalsIgnoreCase("--vcbridgeserverip")) && ((loopi+1) < argv.length)) {
		vcBridgeServerHostname = argv[++loopi];
	    }
	    else if((argv[loopi].equalsIgnoreCase("-vcbridgeserverport")) || (argv[loopi].equalsIgnoreCase("--vcbridgeserverport")) && ((loopi+1) < argv.length)) {
		vcBridgeServerPort = Integer.parseInt(argv[++loopi]);
	    }
	    else if((argv[loopi].equalsIgnoreCase("-maxuavtrailsize")) || (argv[loopi].equalsIgnoreCase("--maxuavtrailsize")) && ((loopi+1) < argv.length)) {
		maxUavTrailSize = Integer.parseInt(argv[++loopi]);
	    }
	    else if((argv[loopi].equalsIgnoreCase("-playback")) || (argv[loopi].equalsIgnoreCase("--playback")) && ((loopi+1) < argv.length)) {
		playbackFilename = argv[++loopi];
	    }
	    else if((argv[loopi].equalsIgnoreCase("-speedup")) || (argv[loopi].equalsIgnoreCase("--speedup")) && ((loopi+1) < argv.length)) {
		speedup = Double.parseDouble(argv[++loopi]);
	    }
	    else { 
		System.err.println("Unknown command line arg["+loopi+"]="+argv[loopi]);
		System.err.println("");
		System.err.println("Usage: UavMapDisplay [-vcbridgeserverip hostname] [-vcbridgeserverport port] [-playback file.log] [-speedup nn.nn]");
		System.err.println("    Option names are case insensitive.");
		System.err.println("    -background specifies the filename of the background image used on");
		System.err.println("        on the GUI and defaults to"+backgroundImageFilename);
		System.err.println("    -vcbridgeserverip specifies the hostname or IP of the Virtual");
		System.err.println("        Cockpit BridgeServer, and defaults to"+vcBridgeServerHostname);
		System.err.println("    -vcbridgeserverport specifies the port number of the Virtual");
		System.err.println("        Cockpit BridgeServer, and defaults to"+vcBridgeServerPort);
		System.err.println("    -playback specifies the name of a log file previously");
		System.err.println("        by UavMapDisplay to play back, and defaults to"+playbackFilename);
		System.err.println("    -speedup is a float specifying the speed at which to playback");
		System.err.println("        logfiles, values > 1.0 are faster than real time, ");
		System.err.println("        values < 1.0 are slower than realtime, it defaults to"+speedup);
		System.err.println("    -maxuavtrailsize is an int specifying the maximum number");
		System.err.println("        of uav locations to keep for displaying each uav trail,");
		System.err.println("        it defaults to "+maxUavTrailSize);

		System.err.println("");
		System.err.println("Despite command line error, trying to run anyway.");
	    }
        }
        
	UavMapDisplay display = null;
	display = new UavMapDisplay(backgroundImageFilename, vcBridgeServerHostname, vcBridgeServerPort, playbackFilename, speedup,maxUavTrailSize);
	display.start();
    }

}
