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
// @version     $Id: MapObject.java,v 1.27 2009/03/29 04:51:40 owens Exp $ 

// @TODO: This entire class should be refactored, it's way too
// complicated and messy.  Among other things we should probably
// factor out the drawing code somehow.  There's a lot of fiddly
// little code to draw various shapes that should be replaced and a
// lot of the scaling crap should be replaced by affine transforms.

import java.awt.*;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

// @todo: SRO Fri Apr 14 21:31:39 EDT 2006
//
// This class really should be refactored, broken up into several
// classes.
public class MapObject {
    public final static Color COLOR_ONE = new Color(0,0,0);
    public final static Color COLOR_TWO = new Color(64,64,64);
    public final static Color COLOR_THREE = new Color(0,128,0);
    public final static Color COLOR_FOUR = new Color(192,192,192);
    public final static Color COLOR_FIVE = new Color(255,255,255);

    public final static int FILL_ALPHA_LEVEL = 31;
    public final static int TRACE_ALPHA_LEVEL = 31;

    public final static boolean DRAW_MOUSE_OVER_NAME = true;
    public final static boolean DRAW_MOUSE_OVER_TYPE = true;
    public final static boolean DRAW_MOUSE_OVER_PICTURE = true;
    public final static boolean DRAW_MOUSE_OVER_TEXT = true;
    public final static boolean DRAW_THREAT_TRIANGLES = true;
    public final static boolean DRAW_TASK_CIRCLES = false;
    
    public final static int LOWEST_LAYER_INCLUSIVE = 0;
    public final static int HIGHEST_LAYER_INCLUSIVE = 4;
    
    public final static int DEFAULT_TRACE_LINE_LENGTH = 350000;
    public final static int SBOX_W = 30;
    public final static int SBOX_H = 24;
    public final static int SBOX_W_HALF = SBOX_W/2;
    public final static int SBOX_H_HALF = SBOX_H/2;
    public final static int SBOX_W_THIRD = SBOX_W/3;
    public final static int SBOX_H_THIRD = SBOX_H/3;
    public final static int SBOX_W_FOURTH = SBOX_W/4;
    public final static int SBOX_H_FOURTH = SBOX_H/4;
    public final static int SBOX_W_FIFTH = SBOX_W/5;
    public final static int SBOX_H_FIFTH = SBOX_H/5;
    public final static int SBOX_W_SIXTH = SBOX_W/6;
    public final static int SBOX_H_SIXTH = SBOX_H/6;
    public final static int SBOX_W_TENTH = SBOX_W/10;
    public final static int SBOX_H_TENTH = SBOX_H/10;
    public final static int SENSOR_CIRCLE_DIAMETER = 15;
    public final static int IMAGE_BOX_WIDTH = 15;
    public final static int IMAGE_BOX_HEIGHT = 12;
    public final static int IMAGE_CIRCLE_DIAMETER = 8;
    public final static int FLASH_BOX_WIDTH = 38;
    public final static int FLASH_BOX_HEIGHT = 32;
    public final static int SYMBOL_ORIENTATION_LINE_SIZE=50;
    public final static int TRACE_MIN_DIST = 10;
    public final static	int TRACE_MIN_DIST_SQD = TRACE_MIN_DIST * TRACE_MIN_DIST;
    public final static int ROAD_WIDTH = 10000;
    public final static int RAIL_WIDTH = 10000;
    
    public int buildingWidth = 0;
    public int buildingHeight = 0;
    
    public final static long DETONATION_DURATION = 1000;
    public final static long FLASH_INTERVAL_INIT = 50;
    public final static long FLASH_INTERVAL_INC = 50;
    public final static long FLASH_INTERVAL_MAX = 1000;
    public final static long FLASH_OFF_INTERVAL = 100;
    public final static long FLASH_DURATION = 10000;

    public final static Color HILIGHTED_BACK_COLOR = Color.yellow;
    public final static Color HILIGHTED_SHADOW_COLOR = Color.yellow;
    public final static Color SELECTED_BACK_COLOR = Color.white;
    public final static Color SELECTED_SHADOW_COLOR = Color.white;
    public final static Color DEAD_BACK_COLOR = Color.black;

    public final static Color DEST_DEFAULT_COLOR = Color.black;

    public final static Color MOUSE_OVER_PICTURE_FRAME_COLOR = Color.black;

    public final static Color MAP_AREA_DEFAULT_COLOR = Color.red;
    public final static Color MAP_AREA_BOUNDS_BOX_COLOR = Color.black;

    // transparency really slows down the redrawing over X
    //    public final static Color DETONATION_COLOR = new Color(255,0,0,127);
    public final static Color DETONATION_COLOR = Color.red;
    public final static Color FLASH_COLOR = Color.red;
    
    public final static Color CLEARING_BASE = Color.blue;
    public final static Color CLEARING_TRANS = new Color(((CLEARING_BASE.getRGB()  & 0x00FFFFFF) | 0x80000000), true);
    
    public final static Color CORRIDOR_BASE = Color.yellow;
    public final static Color CORRIDOR_TRANS = new Color(((CORRIDOR_BASE.getRGB()  & 0x00FFFFFF) | 0x80000000), true);
    
    public final static Color OP_BASE = Color.white;
    public final static Color OP_TRANS = Color.white;
    //    public final static Color OP_TRANS = new Color(((OP_BASE.getRGB()  & 0x00FFFFFF) | 0x80000000), true);
    
    public final static Color SHORTEST_PATH_TRANS = Color.green;
    public final static Color SECOND_SHORTEST_PATH_TRANS = Color.white;
//     public final static Color SHORTEST_PATH_TRANS = new Color(((Color.green.getRGB()  & 0x00FFFFFF) | 0x80000000), true);
//     public final static Color SECOND_SHORTEST_PATH_TRANS = new Color(((Color.white.getRGB()  & 0x00FFFFFF) | 0x80000000), true);
    
    public final static Color ENGAGEMENT_AREA_TRANS = new Color(((Color.red.getRGB()  & 0x00FFFFFF) | 0xC0000000), true);
    
    private long timeCreated = System.currentTimeMillis();
    
    private MapObjectDrawable drawable = null;
    public void setDrawable(MapObjectDrawable drawable){this.drawable = drawable;}
    
    private Color nogoZoneColor =  Color.red;
    public void setNogoZoneColor(Color color) { nogoZoneColor = color; }
    
    private Color patrolZoneColor =  Color.green;
    public void setPatrolColor(Color color) { patrolZoneColor = color; }
    
    private Color mapGraphicColor =  Color.blue;
    public void setMapGraphicColor(Color color) { mapGraphicColor = color; }

    private Color mapGraphicLineColor =  Color.black;
    public void setMapGraphicLineColor(Color color) { mapGraphicLineColor = color; }

    private int mapGraphicAlpha = FILL_ALPHA_LEVEL;
    public void setMapGraphicAlpha(int value) { mapGraphicAlpha = value; }

    private boolean mapGraphicFillOn = true;
    public void setMapGraphicFillOn(boolean value) { mapGraphicFillOn = value; }

    private boolean flagAnimationDone = false;
    public boolean isAnimationDone() { return flagAnimationDone; }
    
    private class DrawColors {
	Color fore;
	Color back;
	Color shadow;
	Color text;
	public DrawColors(Color f, Color b, Color s, Color t) {
	    fore = f;
	    back = b;
	    shadow = s;
	    text = t;
	}
	public DrawColors(DrawColors c) {
	    fore = c.fore;
	    back = c.back;
	    shadow = c.shadow;
	    text = c.text;
	}
	public void hilighted() {
	    fore = back;
	    back = HILIGHTED_BACK_COLOR;
	    shadow = HILIGHTED_SHADOW_COLOR;
	}
	public void selected() {
	    fore = back;
	    back = SELECTED_BACK_COLOR;
	    shadow = SELECTED_SHADOW_COLOR;
	}
	public void dead() {
	    fore = back;
	    back = DEAD_BACK_COLOR;
	}
    }
    
    private final DrawColors COLORS_UNKNOWN = new DrawColors(Color.black, ForceIds.COLOR_UNKNOWN, ForceIds.COLOR_UNKNOWN, Color.black);
    private final DrawColors COLORS_OPFOR = new DrawColors(Color.black, ForceIds.COLOR_OPFOR, ForceIds.COLOR_OPFOR, Color.black);
    private final DrawColors COLORS_BLUEFOR = new DrawColors(Color.black, ForceIds.COLOR_BLUEFOR, ForceIds.COLOR_BLUEFOR, Color.black);
    // Draw WASM's some odd color - wtf?  purple?
    //    private final DrawColors COLORS_WASM = new DrawColors(Color.black, RGB.blueforWasm, RGB.blueforWasm, Color.black);
    // Draw WASM's blue
    private final DrawColors COLORS_WASM = new DrawColors(Color.black, Color.blue, Color.white, Color.black);
    // Draw WASM's red
    //    private final DrawColors COLORS_WASM = new DrawColors(Color.black, Color.red, Color.white, Color.black);
    private final DrawColors COLORS_NEUTRAL = new DrawColors(Color.black, ForceIds.COLOR_NEUTRAL, ForceIds.COLOR_NEUTRAL, Color.black);
    private final DrawColors COLORS_DEFAULT = new DrawColors(Color.red, Color.white, Color.white, Color.black);
    private final DrawColors COLORS_STRIKE = new DrawColors(Color.green, Color.white, Color.white, Color.black);
    private final DrawColors COLORS_STRIKE_TASKED = new DrawColors(Color.red, Color.white, Color.white, Color.black);
    private final DrawColors COLORS_PICTURE_ORDER = new DrawColors(Color.green, Color.darkGray, Color.white, Color.black);
    private final DrawColors COLORS_PICTURE_ORDER_TASKED = new DrawColors(Color.red, Color.darkGray, Color.white, Color.black);
    
    private final Color ROAD_COLOR = Color.green;
    private final Color RAIL_COLOR = Color.red;
    private final Color TRACE_COLOR = COLOR_THREE;
    // private final Color TRACE_COLOR = Color.yellow;
    private final Color PLANNEDPATH_COLOR = COLOR_ONE;
    private final Color PLANNEDPATH_CONFLICT_COLOR = Color.red;
    private final Color ELLIPSE_COLOR = Color.green;
    
    public final static float MISSILE_STROKE = 2.0f;
    public final static float LINE_STROKE = 1.5f;
    public final static float TRACE_STROKE = 3.0f;
    public final static float UAV_TRACE_STROKE = 120.0f;
    public final static float PLANNEDPATH_STROKE = 4.0f;
    public final static BasicStroke UAV_TRACE_STROKE_OBJ = new BasicStroke(UAV_TRACE_STROKE, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
    public final static float FLASH_STROKE = 2.0f;
    public final static float POLY_STROKE = 1.0f;
    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_SCAN = 1;
    public final static int TYPE_ASSEMBLY_AREA = 2;
    public final static int TYPE_OBJECTIVE = 3;
    public final static int TYPE_EA_CANDIDATE = 4;
    public final static int TYPE_EA_INFERENCED = 5;
    public final static int TYPE_UNIT = 6;
    public final static int TYPE_NAI = 7;
    public final static int TYPE_CHECKPOINT = 8;
    public final static int TYPE_OBSERVATION_POST = 9;
    public final static int TYPE_MINEFIELD = 10;
    public final static int TYPE_ANTITANK_DITCH = 11;
    public final static int TYPE_POLITICAL_OBSTACLE = 12;
    public final static int TYPE_WEATHER_OBSTACLE = 13;
    public final static int TYPE_GENERIC_OBSTACLE = 14;
    public final static int TYPE_REMOVE_OBSTACLE = 15;
    public final static int TYPE_MAP_GRAPHIC = 16;
    public final static int TYPE_PATROL_ZONE = 17;
    public final static int TYPE_CLEARING = 18;
    public final static int TYPE_CORRIDOR = 19;
    public final static int TYPE_AA_PRIMARY = 20;
    public final static int TYPE_AA_SECONDARY = 21;
    public final static int TYPE_LINE = 22;
    public final static int TYPE_STRIKE = 23;
    public final static int TYPE_CHOKEPOINT = 24;
    public final static int TYPE_ROAD = 25;
    public final static int TYPE_RAIL = 26;
    public final static int TYPE_DETONATION = 27;
    public final static int TYPE_NOGO_ZONE = 28;
    public final static int TYPE_PICTURE = 29;
    public final static int TYPE_PICTURE_ORDER = 30;
    public final static int TYPE_POLY = 31;
    public final static int MAX_TYPE_VALUE = 31;
    
    public final static String typeNames[] = {"Unknown", "Scan", "Assembly Area", "Objective", "EA Candidate", "EA Inferenced", "Unit", "Named Area Of Interest", "Checkpoint", "Observation Post", "Minefield", "AT Ditch", "Political Obstacle", "Weather Obstacle", "Generic Obstacle", "Remove Obstacle", "MapGraphic", "Patrol Zone", "Clearing", "Corridor", "Avenue of Approach Primary", "Avenue of Approach Seconday", "Line",  "Strike", "Chokepoint", "Road", "Railway", "Detonation", "Nogo Zone","Image","Pic Order", "Polygon", "Unknown1","Unknown2"};
    public final static String typeAbbrs[] = {"UNK", "SCAN", "AA", "OBJ", "EA CAND.", "EA INF.", "UNIT", "NAI", "CP", "OP", "MINE", "ATDITCH", "POBST", "WOBST", "OBST", "ROBST", "GRPH", "PTRL", "CLEARING", "CORRIDOR", "AA PRIMARY", "AA SECONDAY", "L", "STRIKE", "CHOKEPOINT", "ROAD", "RAIL", "DET", "NOGO","IMG","Pic Order","POLY","Unknown1","Unknown2"};
    
    private boolean flash = false;
    private boolean flashOn = false;
    private long lastFlashSwitch;
    private long timeStartedFlashing;
    private long flashInterval;
    private boolean flashSizeSpecified = false;
    private double flashWidth=0;
    private double flashHeight=0;
    public void setFlash(boolean value, boolean sizeSpecified, double width, double height) {
        //	Debug.debug(this, "MapObject.setFlash: Setting flash="+value);
        this.flash = value;
        if(flash) {
            this.flash = true;
            this.flashOn = true;
            timeStartedFlashing = System.currentTimeMillis();
            lastFlashSwitch = timeStartedFlashing;
            flashInterval = FLASH_INTERVAL_INIT;
        } else {
            this.flash = false;
            this.flashOn = false;
            timeStartedFlashing = 0;
            flashInterval = 0;
        }
        flashWidth = width;
        flashHeight = height;
        flashSizeSpecified = sizeSpecified;
    }
    public void setFlash(boolean value) {
        setFlash(value, false, 0.0, 0.0);
    }
    public void setFlash(boolean value, double width, double height) {
        setFlash(value, true, width, height);
    }
    
    private boolean detected = false;
    public void setDetected(boolean detected){ this.detected = detected; }
    
    private boolean selected = false;
    public boolean isSelected() { return selected;}
    public void setSelected(boolean selected) { this.selected = selected;}
    
    private boolean hilighted = false;
    public boolean isHilighted() { return hilighted;}
    public void setHilighted(boolean hilighted) { this.hilighted = hilighted;}
    
    private boolean editable = true;
    public boolean isEditable() { return editable;}
    public void setEditable(boolean editable) { this.editable = editable;}
    
    private boolean edited = false;
    public boolean isEdited() { return edited;}
    public void setEdited(boolean edited) { this.edited = edited;}
    
    private boolean tasked = false;
    public boolean isTasked() { return tasked;}
    public void setTasked(boolean tasked) { this.tasked = tasked;}
    
    private boolean finished = false;
    public boolean isFinished() { return finished;}
    public void setFinished(boolean finished) { this.finished = finished;}

    private boolean xplane = false;
    public boolean isXplane() { return xplane;}
    public void setXplane(boolean xplane) { 
        Debug.debug(this, "MapObject.setXplane: key="+key+" xplane set to "+xplane);
	this.xplane = xplane;
    }

    private int type = TYPE_UNKNOWN;
    public int getType() { return type;}
    public void setType(int type) {
        this.type = type;
        if(TYPE_STRIKE == type) {
            if((sizeX <= 0) && (sizeY <= 0)) {
                sizeX = 5000;
            }
        }
    }
    public String getTypeName() { return typeNames[type]; }
    
    public boolean isUnit() {
        return TYPE_UNIT == type;
    }
    
    public int getLayer() {
	if((TYPE_POLY == type))
	    return 0;
        if((TYPE_ROAD == type) || (TYPE_RAIL == type))
            return 0;
        else if((TYPE_MINEFIELD == type)
        || (TYPE_ANTITANK_DITCH == type)
        || (TYPE_POLITICAL_OBSTACLE == type)
        || (TYPE_GENERIC_OBSTACLE == type)
        || (TYPE_WEATHER_OBSTACLE == type)
        || (TYPE_REMOVE_OBSTACLE == type)
        || (TYPE_PATROL_ZONE == type)
        || (TYPE_NOGO_ZONE == type)
        || ((TYPE_UNIT == type) && (unitType == UnitTypes.SENSOR))
        || ((TYPE_UNIT == type) && (unitType == UnitTypes.BUILDING))
        || ((TYPE_UNIT == type) && (unitType == UnitTypes.TERMINAL))
        )
            return 1;
        else if(type == TYPE_UNIT && !UnitTypes.isAir(unitType))
            return 2;
        else if(type == TYPE_UNIT && UnitTypes.isAir(unitType))
            return 3;
        else
            return 4;
    }
    
    public boolean displayOnTop() { return UnitTypes.isAir(unitType);}
    public boolean displayOnBottom() { return !UnitTypes.isAir(unitType);}
    
    public boolean hasForceId() {
        if((TYPE_POLITICAL_OBSTACLE == type)
        || (TYPE_GENERIC_OBSTACLE == type)
        || (TYPE_OBJECTIVE == type)
        || (TYPE_ASSEMBLY_AREA == type)
        || (TYPE_OBSERVATION_POST == type)
        || (TYPE_MINEFIELD == type)
        || (TYPE_ANTITANK_DITCH == type)
        || (TYPE_ROAD == type)
        || (TYPE_RAIL == type)
        )
            return true;
        else
            return false;
    }
    
    public boolean alwaysDisplayable(int type) {
        if((TYPE_UNKNOWN == type)
        || (TYPE_SCAN == type)
        || (TYPE_ASSEMBLY_AREA == type)
        || (TYPE_OBJECTIVE == type)
        || (TYPE_POLITICAL_OBSTACLE == type)
        || (TYPE_WEATHER_OBSTACLE == type)
        || (TYPE_GENERIC_OBSTACLE == type)
        || (TYPE_REMOVE_OBSTACLE == type)
        || (TYPE_LINE == type)
        || (TYPE_STRIKE == type)
        || (TYPE_ROAD == type)
        || (TYPE_RAIL == type)
        || (TYPE_CHOKEPOINT == type))
            return true;
        else
            return false;
    }
    
    // TODO: This should be reversed, so to speak.  That is, it is
    // used by MapDB findWithin to find if things are click-on-able
    // (selectable?) and it should be a separate method, and things
    // should be selectable as long as they don't have a config flag
    // that is set off.
    public boolean isDisplayable(BackgroundConfig config) {
        if (alwaysDisplayable(type)
        || (config.showUnits && (TYPE_UNIT == type))
        || (config.showNai && (TYPE_NAI == type))
        || (config.showMinefields && (TYPE_MINEFIELD == type))
        || (config.showMinefields && (TYPE_ANTITANK_DITCH == type))
        || (config.showCheckpoints && (TYPE_CHECKPOINT == type))
        || (config.showObservationPosts && (TYPE_OBSERVATION_POST == type))
        || (config.showClearings && (TYPE_CLEARING == type))
        || (config.showCorridors && (TYPE_CORRIDOR == type))
        || (config.showAAPrimary && (TYPE_AA_PRIMARY == type))
        || (config.showAASecondary && (TYPE_AA_SECONDARY == type))
        || (config.showCandidateEngagementAreas && (TYPE_EA_CANDIDATE == type))
        || (config.showInferencedEngagementAreas && (TYPE_EA_INFERENCED == type))
        || (config.showRoads && (TYPE_ROAD == type))
        || (config.showRailways && (TYPE_RAIL == type))
        || (config.showRailways && (TYPE_PICTURE == type))
        )
            return true;
        else
            return false;
    }
    
    private java.util.List linePoints = new ArrayList();
    public void addLinePoint(float x, float y) {
        // TODO: Should we check to make sure we're TYPE_LINE?
        Point2D.Float p = new Point2D.Float(x, y);
        linePoints.add(p);
    }
    public void addLinePoint(double x, double y) {
        // TODO: Should we check to make sure we're TYPE_LINE?
	addLinePoint((float)x, (float)y);
    }
    public Iterator getLinePointIterator() {
        return linePoints.iterator();
    }
    public int getLinePointSize() {
        return linePoints.size();
    }

    private boolean plannedPathConflict = false;
    public void setPlannedPathConflict(boolean value) { plannedPathConflict = value;}

    private java.util.List<Point> plannedPathPoints = new ArrayList<Point>();
    public void addPlannedPathPoint(float x, float y) {
	Point p = new Point((int)x, (int)y);
	plannedPathPoints.add(p);
    }
    public Iterator getPlannedPathPointIterator() {
	return plannedPathPoints.iterator();
    }
    public int getPlannedPathPointSize() {
	return plannedPathPoints.size();
    }
    public void clearPlannedPath() {
	plannedPathPoints.clear();
    }

    private java.util.List<Point> tracePoints = new ArrayList<Point>();
    public void addTracePoint(float x, float y) {
	Point p = new Point((int)x, (int)y);
	synchronized(tracePoints) {
	    tracePoints.add(p);
	}
    }
    public Iterator getTracePointIterator() {
        return tracePoints.iterator();
    }
    public int getTracePointSize() {
	synchronized(tracePoints) {
	    return tracePoints.size();
	}
    }
    public void addTrace(int x, int y) {
	synchronized(tracePoints) {
	    if(tracePoints.size() <= 0) {
		Point p = new Point((int)x, (int)y);
		tracePoints.add(p);
		//	    Debug.debug(this, "MapObject.addTrace:Added first trace at "+x+", "+y);
		return;
	    }
	    Point lastPoint = (Point)tracePoints.get(tracePoints.size() - 1);
	    int xdiff = lastPoint.x - x;
	    int ydiff = lastPoint.y - y;
	    int dsqd = (xdiff * xdiff) + (ydiff * ydiff);
	    if(dsqd > TRACE_MIN_DIST_SQD) {
		Point p = new Point((int)x, (int)y);
		tracePoints.add(p);
		//	    Debug.debug(this, "MapObject.addTrace:Added "+tracePoints.size()+" trace at "+x+", "+y);
	    }
	    else {
		//	    Debug.debug(this, "MapObject.addTrace:DIDN'T add "+(tracePoints.size() + 1)+" trace at "+x+", "+y);
	    }
	}
    }
    
    private static int keySequence = 1;
    public static String createKey() { return Integer.toString(keySequence++); }
    
    private String key = null;
    public String getKey() { return key;}
    public void setKey(String key) { this.key = key;}
    public boolean equals( Object anObject ) { return anObject.equals(key); }
    public int hashCode() { return key.hashCode(); }
    
    private int forceId = ForceIds.UNKNOWN;
    public int getForceId() { return forceId;}
    public void setForceId(int forceId) { this.forceId = forceId;}
    
    public int unitSize = UnitSizes.UNKNOWN;
    public int getUnitSize() { return unitSize;}
    public void setUnitSize(int unitSize) { this.unitSize = unitSize; }
    
    // @todo: can add animation easily later if we have an array of
    // icons and step through them... or would an animated GIF do
    // it?  hmmm.
    //
    // imageIcon is set by setUnitType().
    private ImageIcon imageIcon = null;
    
    private Image mouseOverImage = null;
    public void setMouseOverImage(Image image) { mouseOverImage = image;}
    
    private String mouseOverText = null;
    public void setMouseOverText(String text) { mouseOverText = text;}
    
    private boolean threatToAir = false;
    public void setThreatToAir(boolean value) { threatToAir = value;}
    private boolean threatToGround = false;
    public void setThreatToGround(boolean value) { threatToGround = value;}
    
    private boolean ellipseFlag = false;
    private double ellipseHeight = -1;
    private double ellipseWidth = -1;
    private double ellipseOrientation = -1;
    public void setEllipse(double width, double height, double orientation) {
        ellipseFlag = true;
        ellipseHeight = height;
        ellipseWidth = width;
        ellipseOrientation = orientation;
    }
    public void clearEllipse() { ellipseFlag = false; }
    
    public int unitType = UnitTypes.UNKNOWN;
    public int getUnitType() { return unitType;}
    public void setUnitType(int unitType) {
        this.unitType = unitType;
        imageIcon = UnitTypes.getImageIcon(unitType);
        Debug.debug(this, "MapObject.setUnitType: key="+key+" type set to "+type+" ("+UnitTypes.getName(unitType)+")");
    }
    
    private HashMap unitClassifications = new HashMap();
    public Iterator getUnitClassificationsKeysIterator() {
        return unitClassifications.keySet().iterator();
    }
    public double getClassificationProb(String key) {
        Double d = (Double)unitClassifications.get(key);
        if(null != d)
            return d.doubleValue();
        else
            return 0.0;
    }
    public void addClassification(String classificationName, double prob) {
        unitClassifications.put(classificationName, new Double(prob));
    }
    
    private double probSignificant = 100.0;
    public void setProbSignificant(double value) { probSignificant = value; }
    public double getProbSignificant() { return probSignificant; }
    
    public final static int BDA_DEAD = 0;
    public final static int BDA_ALIVE = 1;
    public int bda = BDA_ALIVE;
    public int getBda() { return bda;}
    public void setBda(int value) { this.bda = value; }
    
    public double disX = 0.0;
    public double disY = 0.0;
    public double disZ = 0.0;
    public double getDisX() { return disX; }
    public double getDisY() { return disY; }
    public double getDisZ() { return disZ; }
    public void setDis(double x, double y, double z) {
        disX = x;
        disY = y;
        disZ = z;
    }
    
    public double posX = 0.0;
    public double posY = 0.0;
    public double posZ = 0.0;
    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public double getPosZ() { return posZ; }
    public void setPos(double x, double y, double z) {
        if(TYPE_LINE == type) {
            double xdiff = posX - x;
            double ydiff = posY - y;
            Iterator iter = linePoints.iterator();
            while(iter.hasNext()) {
                Point2D.Float p = (Point2D.Float)iter.next();
                p.setLocation(p.getX()-xdiff,p.getY()-ydiff);
            }
        }
        posX = x;
        posY = y;
        posZ = z;
        //	if((TYPE_UNIT == type) && !UnitTypes.isAir(unitType)) {
        if(TYPE_UNIT == type) {
            addTrace((int)x,(int)y);
        }
    }
    public double velX = 0.0;
    public double velY = 0.0;
    public double velZ = 0.0;
    public double getVelX() { return velX; }
    public double getVelY() { return velY; }
    public double getVelZ() { return velZ; }
    public void setVel(double x, double y, double z) {
        velX = x;
        velY = y;
        velZ = z;
    }

    private Color destColor = DEST_DEFAULT_COLOR;
    public void setDestColor(Color c) { destColor = c; }
    public boolean destSet = false;
    public double destX = 0.0;
    public double destY = 0.0;
    public double destZ = 0.0;
    public double getDestX() { return destX; }
    public double getDestY() { return destY; }
    public double getDestZ() { return destZ; }
    public void clearDest() { destSet = false; }
    public void setDest(double x, double y, double z) {
        destX = x;
        destY = y;
        destZ = z;
        destSet = true;
    }
    
    public double sizeX = 0.0;
    public double getSizeX() { return sizeX;}
    public void setSizeX(double value) { sizeX = value;}
    public double sizeY = 0.0;
    public double getSizeY() { return sizeY;}
    public void setSizeY(double value) { sizeY = value;}
    private boolean orientationSet = false;
    private double orientation = 0.0;
    public double getOrientation() { return orientation;}
    public void setOrientation(double value) { orientation = value; orientationSet = true;}
    public void setOrientation(double x, double y) {
        double xdiff = x - posX;
        double ydiff = y - posY;
        double length = Math.sqrt(xdiff * xdiff + ydiff * ydiff);
        xdiff = xdiff/length;
        ydiff = ydiff/length;
        double ret = 0.0;
        
        double angleRad = 0.0;
        if (xdiff == 0.0 && ydiff == 0.0) {
            angleRad = 0.0;
        } else if (xdiff >= 0 && ydiff >= 0) {
            angleRad = Math.atan(ydiff/xdiff);
        } else if (xdiff <= 0 && ydiff >= 0) {
            angleRad = Math.PI + Math.atan(ydiff/xdiff);
        } else if (xdiff >= 0 && ydiff <= 0) {
            angleRad = Math.atan(ydiff/xdiff);
        } else if (xdiff <= 0 && ydiff <= 0) {
            angleRad = -Math.PI + Math.atan(ydiff/xdiff);
        }
        orientation = Math.toDegrees(angleRad);
        orientationSet = true;
    }
    
    public String name = "Unknown";
    public String getName() { return name;}
    public void setName(String value) { this.name = value;}
    public String comments = "";
    public void setComments(String value) { comments = value; }
    
    public Area area = null;
    
    private Point[] poly = null;
    public Point[] getPoly() { return poly; }
    public void setPoly(Point[] value) { poly = value;}

    // TODO: SRO Thu Jul 28 17:33:23 EDT 2005
    //
    // Way too many constructors.  Need to reduce these as much as
    // possible, possibly replace some of them with intelligently
    // named factory methods.
    //
    //     public MapObject(int type, String key, Area area)
    //     public MapObject(String key, double posX, double posY, double posZ)
    //     public MapObject(String key, int type, double posX, double posY, double posZ, double sizeX, double sizeY)
    //     public MapObject(String key, int forceId, String name, double posX, double posY, double posZ)
    //     public MapObject(String key, int forceId, String name, double posX, double posY, double posZ, double velX, double velY, double velZ)
    //     public MapObject(String key, int forceId, String name, int type, double posX, double posY, double posZ, double velX, double velY, double velZ)
    //     public MapObject(String key, int forceId, String name, int type, double posX, double posY, double posZ, double sizeX, double sizeY)
    //     public MapObject(String key, int forceId, String name, int unitType, int unitSize, double posX, double posY, double posZ, double velX, double velY, double velZ)
    //     public MapObject(String key, int forceId, String name, int type, int unitType, int unitSize, double posX, double posY, double posZ, double velX, double velY, double velZ)
    //     public MapObject(String key, int forceId, String name, int type, int unitType, int unitSize, double posX, double posY, double posZ, double velX, double velY, double velZ, double orientation)
    //    public MapObject(String key, int forceId, String name, int type, double posX, double posY, double posZ, double sizeX, double sizeY, double orientation, boolean foo)
    
    public MapObject(int type, String key, Area area) {
        this.type = type;
        this.key = key;
        this.name = key;
        Rectangle bounds = area.getBounds();
        int boundsx = (int)(bounds.x + (bounds.width/2));
        int boundsy = (int)(bounds.y + (bounds.height/2));
        this.posX = boundsx;
        this.posY = boundsy;
        this.area = area;
        //	Debug.debug(this, "MapObject: New map object, type="+type+", area="+area+", key="+key+", name="+name);
    }
    
    public MapObject(String key, double posX, double posY, double posZ) {
        this.key = key;
        this.name = key;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.type = TYPE_STRIKE;
    }
    
    public MapObject(String key, int type, double posX, double posY, double posZ, double sizeX, double sizeY) {
        this.key = key;
        this.type = type;
        this.name = key;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
    
    public MapObject(String key, int forceId, String name, double posX, double posY, double posZ) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }
    
    public MapObject(String key, int forceId, String name, double posX, double posY, double posZ, double velX, double velY, double velZ) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }
    
    public MapObject(String key, int forceId, String name, int type, double posX, double posY, double posZ, double velX, double velY, double velZ) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }
    
    public MapObject(String key, int forceId, String name, int type, double posX, double posY, double posZ, double sizeX, double sizeY) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
    
    public MapObject(String key, int forceId, String name, int unitType, int unitSize, double posX, double posY, double posZ, double velX, double velY, double velZ) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.type = TYPE_UNIT;
        //	Debug.debug(this, "MapObject: type="+getTypeName());
        setUnitType(unitType);
        this.unitSize = unitSize;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }
    
    public MapObject(String key, int forceId, String name, int unitType, int unitSize, double posX, double posY, double posZ, double velX, double velY, double velZ, int buildingWidth, int buildingHeight) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.type = TYPE_UNIT;
        //	Debug.debug(this, "MapObject: type="+getTypeName());
        setUnitType(unitType);
        this.unitSize = unitSize;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.buildingWidth = buildingWidth;
        this.buildingHeight = buildingHeight;
        
    }
        
    public MapObject(String key, int forceId, String name, int type, int unitType, int unitSize, double posX, double posY, double posZ, double velX, double velY, double velZ) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.type = type;
        //	Debug.debug(this, "MapObject: type="+getTypeName());
        setUnitType(unitType);
        this.unitSize = unitSize;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }
    
    public MapObject(String key, int forceId, String name, int type, int unitType, int unitSize, double posX, double posY, double posZ, double velX, double velY, double velZ, double orientation) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.type = type;
        //	Debug.debug(this, "MapObject: type="+getTypeName());
        setUnitType(unitType);
        this.unitSize = unitSize;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.orientation = orientation;
    }
    
    public MapObject(String key, int forceId, String name, int type, double posX, double posY, double posZ, double sizeX, double sizeY, double orientation, boolean foo) {
        this.key = key;
        this.forceId = forceId;
        this.name = name;
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.orientation = orientation;
    }
    
    public String getTypeAbbr() {
        if((type < TYPE_UNKNOWN) || (type >MAX_TYPE_VALUE))
            return "BAD TYPE";
        else if(TYPE_UNIT == type)
            return UnitTypes.abbrs[unitType]+" "+UnitSizes.abbrs[unitSize];
        else
            return typeAbbrs[type];
    }
    
    public String getLabel() {
        if((type < TYPE_UNKNOWN) || (type >MAX_TYPE_VALUE))
            return "BAD LABEL";
        else if(TYPE_UNIT == type)
            return UnitTypes.abbrs[unitType]+" "+UnitSizes.abbrs[unitSize]+" "+ name;
        else if(TYPE_CLEARING == type)
            return name;
        else if(TYPE_CORRIDOR == type)
            return name;
        else if(TYPE_OBSERVATION_POST == type)
            return name;
        else
            return typeAbbrs[type] + " " + name;
    }
    
    private void drawCross(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
        g.drawLine(x-8, y, x+8, y);
        g.drawLine(x, y-8, x, y+8);
    }
    
    private void drawMissileAsCross(Graphics g, int x, int y, Color lineColor, Color fillColor, double scale) {
        g.setColor(lineColor);
        ((Graphics2D)g).setStroke(new BasicStroke(MISSILE_STROKE));
        g.drawLine((int)(x-(4*scale)), y, (int)(x+(4*scale)), y);
	g.drawLine(x, (int)(y-(4*scale)), x, (int)(y+(4*scale)));
    }

    private void drawMissileAsWedge(Graphics g, int x, int y, Color lineColor, Color fillColor, double scale) {
	double radians = (-orientation)*(Math.PI/180);
	double radians2 = (-orientation - 90 )*(Math.PI/180);
	int offa = (int)(Math.cos(radians)*11*scale);
	int offb = (int)(Math.sin(radians)*11*scale);
	int offc = (int)(Math.cos(radians2)*5*scale);
	int offd = (int)(Math.sin(radians2)*3*scale);
	int[] xpoints = new int[3];
	int[] ypoints = new int[3];
	xpoints[0] = x+offa;
	ypoints[0] = y+offb;
	xpoints[1] = x-offa+offc;
	ypoints[1] = y-offb+offd;
	xpoints[2] = x-offa-offc;
	ypoints[2] = y-offb-offd;
	g.setColor(fillColor);
	g.fillPolygon(xpoints, ypoints, 3);
	g.setColor(lineColor);
	g.drawLine(x + offa, y + offb, x - offa+offc, y - offb+offd);
	g.drawLine(x + offa, y + offb, x - offa-offc, y - offb-offd);
	g.drawLine(x - offa-offc, y - offb-offd, x - offa+offc, y - offb+offd);
    }
    

    private void drawMissile(Graphics g, int x, int y, Color lineColor, Color fillColor) {
	drawMissileAsWedge(g,x,y,lineColor,fillColor,1.0);
    }
    
    private void drawSAMissile(Graphics g, int x, int y, Color lineColor, Color fillColor) {
	drawMissileAsWedge(g,x,y,lineColor,fillColor,1.0);
    }
    
    private void drawSSMissile(Graphics g, int x, int y, Color lineColor, Color fillColor) {
	drawMissileAsWedge(g,x,y,lineColor,fillColor,1.0);
    }
    
    private void drawWASM(Graphics g, int x, int y, Color lineColor, Color fillColor) {
        
	if(DRAW_TASK_CIRCLES) {
	    if(isTasked()) {
		if(isFinished()) 
		    g.setColor(fillColor);
		else
		    g.setColor(lineColor);
		g.drawOval(x - 10, y - 10, (int)(10*2), (int)(10*2));
	    }
	}
        
// 	g.drawLine(x-6, y, x+6, y);
// 	g.drawLine(x, y-6, x, y+6);
        
// 	double radians = (-orientation)*(Math.PI/180);
// 	double radians2 = (-orientation - 90 )*(Math.PI/180);
// 	int offa = (int)(Math.cos(radians)*10);
// 	int offb = (int)(Math.sin(radians)*10);
// 	int offc = (int)(Math.cos(radians2)*5);
// 	int offd = (int)(Math.sin(radians2)*5);
//  	g.drawLine(x + offa/2, y + offb/2, x - offa, y - offb);
//  	g.drawLine(x + offc, y + offd, x - offc, y - offd);
        
        double scale = 2.0;
        
        double radians = (-orientation)*(Math.PI/180);
        double radians2 = (-orientation - 90 )*(Math.PI/180);
        int offa = (int)(Math.cos(radians)*14*scale);
        int offb = (int)(Math.sin(radians)*14*scale);
        int offc = (int)(Math.cos(radians2)*7*scale);
        int offd = (int)(Math.sin(radians2)*5*scale);
        int[] xpoints = new int[3];
        int[] ypoints = new int[3];
        xpoints[0] = x+offa;
        ypoints[0] = y+offb;
        xpoints[1] = x-offa+offc;
        ypoints[1] = y-offb+offd;
        xpoints[2] = x-offa-offc;
        ypoints[2] = y-offb-offd;
        g.setColor(fillColor);
        g.fillPolygon(xpoints, ypoints, 3);
        g.setColor(lineColor);
        g.drawLine(x + offa, y + offb, x - offa+offc, y - offb+offd);
        g.drawLine(x + offa, y + offb, x - offa-offc, y - offb-offd);
        g.drawLine(x - offa-offc, y - offb-offd, x - offa+offc, y - offb+offd);
	if(xplane) {
	    drawXplane(g,x,y);
	}
    }
    
    private void drawMUAV(Graphics g, int x, int y, Color lineColor, Color fillColor) {
        if(DRAW_TASK_CIRCLES) {
            if(isTasked()) {
                g.setColor(lineColor);
                g.drawOval(x - 10, y - 10, (int)(10*2), (int)(10*2));
            }
        }
// 	g.drawLine(x-6, y, x+6, y);
// 	g.drawLine(x, y-6, x, y+6);
        
// 	double radians = (-orientation)*(Math.PI/180);
// 	double radians2 = (-orientation - 90 )*(Math.PI/180);
// 	int offa = (int)(Math.cos(radians)*10);
// 	int offb = (int)(Math.sin(radians)*10);
// 	int offc = (int)(Math.cos(radians2)*5);
// 	int offd = (int)(Math.sin(radians2)*5);
//  	g.drawLine(x + offa/2, y + offb/2, x - offa, y - offb);
//  	g.drawLine(x + offc, y + offd, x - offc, y - offd);
        
        double radians = (-orientation)*(Math.PI/180);
        double radians2 = (-orientation - 90 )*(Math.PI/180);
        double scale = 2.0;
        int offa = (int)(Math.cos(radians)*11*scale);
        int offb = (int)(Math.sin(radians)*11*scale);
        int offc = (int)(Math.cos(radians2)*5*scale);
        int offd = (int)(Math.sin(radians2)*3*scale);
        int[] xpoints = new int[3];
        int[] ypoints = new int[3];
        xpoints[0] = x+offa;
        ypoints[0] = y+offb;
        xpoints[1] = x-offa+offc;
        ypoints[1] = y-offb+offd;
        xpoints[2] = x-offa-offc;
        ypoints[2] = y-offb-offd;
        g.setColor(fillColor);
        g.fillPolygon(xpoints, ypoints, 3);
        g.setColor(lineColor);
        g.drawLine(x + offa, y + offb, x - offa+offc, y - offb+offd);
        g.drawLine(x + offa, y + offb, x - offa-offc, y - offb-offd);
        g.drawLine(x - offa-offc, y - offb-offd, x - offa+offc, y - offb+offd);

	if(xplane) {
	    drawXplane(g,x,y);
	}
    }
    
    private void drawSensor(Graphics g, int x, int y, DrawColors colors, ViewPort viewPort, boolean drawTrace, boolean selected, boolean hilighted) {
        g.setColor(colors.back);
        g.fillOval(x - (int)SENSOR_CIRCLE_DIAMETER/2, y - (int)SENSOR_CIRCLE_DIAMETER/2,
                (int)(SENSOR_CIRCLE_DIAMETER), (int)(SENSOR_CIRCLE_DIAMETER));
        g.setColor(colors.fore);
        g.drawOval(x - (int)SENSOR_CIRCLE_DIAMETER/2, y - (int)SENSOR_CIRCLE_DIAMETER/2,
                (int)(SENSOR_CIRCLE_DIAMETER), (int)(SENSOR_CIRCLE_DIAMETER));
        drawMouseOver(g, x, y,colors, selected, hilighted);
        //	drawUnit(g, x, y, colors, viewPort, drawTrace, selected, hilighted);
    }
    
    private void drawPictureOrder(Graphics g, int x, int y, DrawColors colors, ViewPort viewPort, boolean selected, boolean hilighted) {
        drawPicture(g,x,y,colors,viewPort,selected,hilighted);
    }
    
    private void drawPicture(Graphics g, int x, int y, DrawColors colors, ViewPort viewPort, boolean selected, boolean hilighted) {
        g.setColor(colors.back);
        g.fillRect(x-(IMAGE_BOX_WIDTH/2), y-(IMAGE_BOX_HEIGHT/2),
                IMAGE_BOX_WIDTH, IMAGE_BOX_HEIGHT);
        g.setColor(colors.fore);
        g.drawRect(x-(IMAGE_BOX_WIDTH/2), y-(IMAGE_BOX_HEIGHT/2),
                IMAGE_BOX_WIDTH, IMAGE_BOX_HEIGHT);
        g.drawOval(x-IMAGE_CIRCLE_DIAMETER/2, y - IMAGE_CIRCLE_DIAMETER/2,
                (IMAGE_CIRCLE_DIAMETER), (IMAGE_CIRCLE_DIAMETER));
        drawMouseOver(g, x, y,colors, selected, hilighted);
    }
    
    private void drawUnitBox(Graphics g, int x, int y, Color foreColor, Color backColor) {
        g.setColor(backColor);
        g.fillRect(x-(SBOX_W_HALF), y-(SBOX_H_HALF),
                SBOX_W, SBOX_H);
        g.setColor(foreColor);
        g.drawRect(x-(SBOX_W_HALF), y-(SBOX_H_HALF),
                SBOX_W, SBOX_H);
        
        if(DRAW_THREAT_TRIANGLES) {
            if(threatToAir) {
                Polygon poly = new Polygon();
                poly.addPoint(x-(SBOX_W_HALF), y-(SBOX_H_HALF));
                poly.addPoint(x, y-(SBOX_H_HALF)-(SBOX_H_THIRD));
                poly.addPoint(x+(SBOX_W_HALF), y-(SBOX_H_HALF));
                g.fillPolygon(poly);
            }
            if(threatToGround) {
                Polygon poly = new Polygon();
                poly.addPoint(x+(SBOX_W_HALF), y-(SBOX_H_HALF));
                poly.addPoint(x+(SBOX_W_HALF)+(SBOX_W_THIRD), y);
                poly.addPoint(x+(SBOX_W_HALF), y+(SBOX_H_HALF));
                g.fillPolygon(poly);
            }
        }
    }
    
    private void drawBDBox(Graphics g, int x, int y, Color foreColor, Color backColor,ViewPort viewPort) {
        //Machinetta.Debugger.debug(1, " in drawBDBox!!!!!!!!!!!!!!!!!!!1");
        
        Polygon bdPoly = new Polygon();
	
	int bw = (int)(buildingWidth*viewPort.getScale());
	int bh = (int)(buildingHeight*viewPort.getScale());
	// This was done before we were called;
	// 	x = (int)viewPort.sourceToDestX(x);
	// 	y = (int)viewPort.sourceToDestY(y);

        bdPoly.addPoint(x-(bw/2), y-(bh/2));
        bdPoly.addPoint(x-(bw/2), y+(bh/2));
        bdPoly.addPoint(x-(bw/4), y+(bh/2));
        bdPoly.addPoint(x-(bw/4), y+(5*bh/6));
        bdPoly.addPoint(x+(bw/4), y+(5*bh/6));
        bdPoly.addPoint(x+(bw/4), y+(bh/2));
        bdPoly.addPoint(x+(bw/2), y+(bh/2));
        bdPoly.addPoint(x+(bw/2), y-(bh/2));

        g.setColor(backColor);
        g.fillPolygon(bdPoly);
        
        g.setColor(foreColor);
        g.drawPolygon(bdPoly);
        g.drawRect(x-(bw/3), y-(bh/10), 2*bw/3, 2*bh/5);
        
        g.drawOval((int)(x-(bw/2.5)), (int)(y-(bh/2.5)), bh/7, bh/7);
        g.drawOval((int)(x-(bw/3.5)), (int)(y-(bh/2.5)), bh/7, bh/7);
        g.drawOval((int)(x+(bw/3.5)), (int)(y-(bh/2.5)), bh/7, bh/7);
        g.drawOval((int)(x+(bw/2.5)), (int)(y-(bh/2.5)), bh/7, bh/7);
    }
        
    private void drawUnitSymbolArmor(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
        g.drawOval(x - SBOX_W_THIRD, y - SBOX_H_THIRD,
                (int)(SBOX_W/1.5), (int)(SBOX_H/1.5));
    }
    private void drawUnitSymbolCivilian(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
        g.drawOval(x - SBOX_W_TENTH, y - SBOX_H_THIRD,
		   (int)(SBOX_W_FIFTH), (int)(SBOX_H_FIFTH));
        g.drawLine(x-SBOX_W_FIFTH, y, x+SBOX_W_FIFTH, y);	// arms
        g.drawLine(x, y-SBOX_H_FIFTH, x, y+SBOX_H_FIFTH);	// body
	//        g.drawLine(x+SBOX_H_FIFTH, y+SBOX_H_FIFTH, x-SBOX_H_FIFTH, y+SBOX_H_FIFTH);	// body
	g.drawLine(x, y+SBOX_H_FIFTH, x-SBOX_H_FIFTH, y+SBOX_H_THIRD);	
	g.drawLine(x, y+SBOX_H_FIFTH, x+SBOX_H_FIFTH, y+SBOX_H_THIRD);	
    }
    private void drawDeadUnitBox(Graphics g, int x, int y, Color foreColor, Color backColor) {
        g.setColor(backColor);
        g.fillRect(x-(SBOX_W_FOURTH), y-(SBOX_H_FOURTH),
                SBOX_W_HALF, SBOX_H_HALF);
        g.setColor(foreColor);
        g.drawRect(x-(SBOX_W_FOURTH), y-(SBOX_H_FOURTH),
                SBOX_W_HALF, SBOX_H_HALF);
    }
    private void drawDeadUnit(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
        g.drawLine((int)(x-SBOX_W_TENTH), (int)(y-SBOX_H_TENTH), (int)(x+SBOX_W_TENTH), (int)(y+SBOX_H_TENTH));
        g.drawLine((int)(x-SBOX_W_TENTH), (int)(y+SBOX_H_TENTH), (int)(x+SBOX_W_TENTH), (int)(y-SBOX_H_TENTH));
    }
    //@TODO: need to update this symbol with real military truck.
    // Currently we are using civilian symbol, and this is wrong.
    private void drawUnitSymbolMilitaryTruck(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
	// X for infantry
        g.drawLine(x-(SBOX_W_HALF), y-(SBOX_H_HALF),
                x+(SBOX_W_HALF), y+(SBOX_H_HALF));
        g.drawLine(x-(SBOX_W_HALF), y+(SBOX_H_HALF),
                x+(SBOX_W_HALF), y-(SBOX_H_HALF));
	// Line down the middle for motorized
        g.drawLine(x, y-(SBOX_H_HALF),
                x, y+(SBOX_H_HALF));
    }
    private void drawUnitSymbolAir(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
        g.drawOval(x - SBOX_W_THIRD, y - SBOX_H_SIXTH,
                (int)(SBOX_W_THIRD), (int)(SBOX_W_THIRD));
        g.drawOval(x, y - SBOX_H_SIXTH,
                (int)(SBOX_W_THIRD), (int)(SBOX_W_THIRD));
    }
    private void drawUnitSymbolLightInf(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
	// X for infantry
        g.drawLine(x-(SBOX_W_HALF), y-(SBOX_H_HALF),
                x+(SBOX_W_HALF), y+(SBOX_H_HALF));
        g.drawLine(x-(SBOX_W_HALF), y+(SBOX_H_HALF),
                x+(SBOX_W_HALF), y-(SBOX_H_HALF));
    }
    private void drawUnitSymbolMechInf(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
	// Oval for treads... for mech infantry fighting vehicles
        g.drawOval(x - SBOX_W_THIRD, y - SBOX_H_THIRD,
                (int)(SBOX_W/1.5), (int)(SBOX_H/1.5));
	// X for infantry
        g.drawLine(x-(SBOX_W_HALF), y-(SBOX_H_HALF),
                x+(SBOX_W_HALF), y+(SBOX_H_HALF));
        g.drawLine(x-(SBOX_W_HALF), y+(SBOX_H_HALF),
                x+(SBOX_W_HALF), y-(SBOX_H_HALF));
    }
    private void drawUnitSymbolADA(Graphics g, int x, int y, Color lineColor) {
        g.setColor(lineColor);
        
        int xpoints[] = new int[9];
        int ypoints[] = new int[9];
        int i = 0;
        xpoints[i] = x-(SBOX_W_HALF);
        ypoints[i++] = y+(SBOX_H_HALF);
        xpoints[i] = x-((int)(.45*SBOX_W));
        ypoints[i++] = y+((int)(.35*SBOX_H));
        xpoints[i] = x-((int)(.40*SBOX_W));
        ypoints[i++] = y+((int)(.30*SBOX_H));
        xpoints[i] = x-((int)(.30*SBOX_W));
        ypoints[i++] = y+((int)(.15*SBOX_H));
        xpoints[i] = x;
        ypoints[i++] = y+((int)(.10*SBOX_H));
        xpoints[i] = x+((int)(.30*SBOX_W));
        ypoints[i++] = y+((int)(.15*SBOX_H));
        xpoints[i] = x+((int)(.40*SBOX_W));
        ypoints[i++] = y+((int)(.30*SBOX_H));
        xpoints[i] = x+((int)(.45*SBOX_W));
        ypoints[i++] = y+((int)(.35*SBOX_H));
        xpoints[i] = x+(SBOX_W_HALF);
        ypoints[i++] = y+(SBOX_H_HALF);
        g.drawPolygon(xpoints, ypoints, xpoints.length);
    }
    
    private void drawEllipse(Graphics g, int x, int y, int width, int  height) {
        int newx = x - width/2;
        int newy = y - height/2;
        //	int newx = x;
        //	int newy = y;
        //	Debug.debug(this, "MapObject.drawEllipse: Old x,y = "+x+", "+y+" new x,y="+newx+", "+newy);
        
        Ellipse2D.Double ellipse = new Ellipse2D.Double(newx, newy, width, height);
        AffineTransform at = new AffineTransform();
        at.setToIdentity();
        at.rotate(Math.toRadians(ellipseOrientation), x, y);
        g.setColor(ELLIPSE_COLOR);
        AffineTransform origTrans = ((Graphics2D)g).getTransform();
        ((Graphics2D)g).setTransform(at);
        ((Graphics2D)g).draw(ellipse);
        ((Graphics2D)g).setTransform(origTrans);
    }
    
    private void drawImageIcon(Graphics g, int x, int y, DrawColors colors, boolean selected, boolean hilighted) {
        //	Debug.debug(this, "MapObject.drawImageIcon:Entering");
        if(null == imageIcon)
            return;
        int width = imageIcon.getIconWidth();
        int height = imageIcon.getIconHeight();
        if((width < 0) || (height < 0))
            return;
        int newx = x - width/2;
        int newy = y - height/2;
        // @todo: transparent background color - can we just use null instead?  test.
        Color background = new Color(0,0,0,0);
        g.drawImage(imageIcon.getImage(), newx, newy, background, null);
        g.setColor(colors.back);
        ((Graphics2D)g).setStroke(new BasicStroke(LINE_STROKE));
        g.drawRect(newx, newy, width, height);
        g.setColor(colors.fore);
        g.drawRect(newx+2, newy+2, width-4, height-4);
        if(BDA_DEAD == getBda()) {
            g.setColor(colors.back);
            g.drawLine(newx+2, newy+2, newx+2+width-4, newy+2+height-4);
            g.drawLine(newx+2, newy+2+height-4, newx+2+width-4, newy+2);
        }
        //	Debug.debug(this, "MapObject.drawImageIcon:Leaving");
    }
    
    public void drawMouseOver(Graphics g, int x, int y, DrawColors colors, boolean selected, boolean hilighted) {
	if(!hilighted) {
	    //	    Debug.debug(this, "MapObject: not hilighted, returning.");
	    return;
	}
	//	Debug.debug(this, "MapObject: Entering drawMouseOver for "+ getName());

	int xOffset = SBOX_W;
	int yOffset = g.getFontMetrics().getHeight();
	if(DRAW_MOUSE_OVER_PICTURE) {
	    //	    Debug.debug(this, "MapObject: Drawing mouseover picture for "+getName());
	    if(null != mouseOverImage) {
		int width = mouseOverImage.getWidth(null);
		int height = mouseOverImage.getHeight(null);
		if((width < 0) || (height < 0))
		    return;
		int newx = x - width/2;
		int newy = y - height/2;
		newx = x + 50;
		newy = y - height - yOffset;

		g.setColor(MOUSE_OVER_PICTURE_FRAME_COLOR);
		g.drawRect(newx - 1 , newy - 1, width+2, height+2);

		// @todo: transparent background color - can we just use null instead?  test.
		Color background = new Color(0,0,0,0);	
		g.drawImage(mouseOverImage, newx, newy, null, null);
		xOffset = width;
	    }
	}
	if(DRAW_MOUSE_OVER_TEXT) {
	    if(null != mouseOverText) {
		//		Debug.debug(this, "MapObject: Drawing mouseover text for "+getName());
		g.setColor(colors.shadow);
		g.drawString(mouseOverText, x+(xOffset/2)+4+1, y + 1);
		g.setColor(colors.text);
		g.drawString(mouseOverText, x+(xOffset/2)+4, y);
	    }
	    else {
		//		Debug.debug(this, "MapObject: mouseover text is null for "+getName());
	    }
	}
	else if(DRAW_MOUSE_OVER_NAME && DRAW_MOUSE_OVER_TYPE) {
	    if(null != mouseOverText)
	    g.setColor(colors.shadow);
	    g.drawString(getLabel(), x+(xOffset/2)+4+1, y + 1);
	    g.setColor(colors.text);
	    g.drawString(getLabel(), x+(xOffset/2)+4, y);
	}       
	else if(DRAW_MOUSE_OVER_NAME) {
	    g.setColor(colors.shadow);
	    g.drawString(name, x+(xOffset/2)+4+1, y + 1);
	    g.setColor(colors.text);
	    g.drawString(name, x+(xOffset/2)+4, y);
	}
	else if(DRAW_MOUSE_OVER_TYPE) {
	    g.setColor(colors.shadow);
	    g.drawString(getTypeAbbr(), x+(xOffset/2)+4+1, y + 1);
	    g.setColor(colors.text);
	    g.drawString(getTypeAbbr(), x+(xOffset/2)+4, y);
	}
    }
    
    private void drawXplane(Graphics g, int x, int y) {
        ((Graphics2D)g).setStroke(new BasicStroke(2.0f));
	g.setColor(Color.blue);
	//	int width = 150;
	//	int height = 80;
	int width = 75;
	int height = 40;
        int newx = x - width/2;
        int newy = y - height/2;
        Ellipse2D.Double ellipse = new Ellipse2D.Double(newx, newy, width, height);
        ((Graphics2D)g).draw(ellipse);
	width = 37;
	height = 37;
	//	width = 75;
	//	height = 75;
	newx = x - width/2;
	newy = y - height/2;
	ellipse = new Ellipse2D.Double(newx, newy, width, height);
        ((Graphics2D)g).draw(ellipse);
    }

    // ADA map symbols - http://www.fas.org/spp/starwars/docops/fm44-100-2fd/appendixb.htm
    //
    // http://www.mca-marines.org/Gazette/tdgsym.html
    private void drawUnit(Graphics g, int x, int y, DrawColors colors, ViewPort viewPort, boolean drawTrace, boolean selected, boolean hilighted) {
	//Debug.info(this, "MapObject.drawUnit: Entering drawUnit: "+unitType);
 
        if(isTasked() && BDA_DEAD != bda) {
            int circleSize = (int)(SBOX_W * 1.2);
            g.setColor(colors.fore);
            g.drawOval(x - (int)circleSize/2, y - (int)circleSize/2,
                    (int)(circleSize), (int)(circleSize));
        }
        
        if(null != imageIcon) {
            drawImageIcon(g, x, y,colors, selected, hilighted);
        }
        
        else {
            
            if(BDA_DEAD == bda) {
                drawDeadUnitBox(g,x,y,colors.fore, colors.back);
                drawDeadUnit(g, x, y, colors.fore);
            } else {           
                //	Debug.debug(this, "MapObject.drawUnit: calling drawUnitBox.");
                drawUnitBox(g,x,y,colors.fore, colors.back);
                if(unitType == UnitTypes.ARMOR) {
                    drawUnitSymbolArmor(g,x,y,colors.fore);
                } else if(unitType == UnitTypes.AIR_FORCES) {
                    drawUnitSymbolAir(g,x,y,colors.fore);
                } else if(unitType == UnitTypes.LIGHT_INFANTRY) {
                    //Debug.info(1, "Call drawUnitSymbolLightInf for "+getKey());
                    drawUnitSymbolLightInf(g,x,y,colors.fore);
                } else if(unitType == UnitTypes.MECH_INFANTRY) {
                    drawUnitSymbolMechInf(g,x,y,colors.fore);
                } else if(unitType == UnitTypes.CIVILIAN) {
                    drawUnitSymbolCivilian(g,x,y,colors.fore);
                } else if(unitType == UnitTypes.MILITARY_TRUCK) {
                    drawUnitSymbolMilitaryTruck(g,x,y,colors.fore);
                } else if(unitType == UnitTypes.AIR_DEFENSE_ARTILLERY) {
                    drawUnitSymbolADA(g,x,y,colors.fore);
                } else {
                    //	    Debug.error(this, "MapObject.drawUnit: unknown unitType="+unitType);
                }            
            }
        }
        drawMouseOver(g, x, y,colors, selected, hilighted);
	if(xplane) {
	    drawXplane(g,x,y);
	}
    }
    
    private void drawBuilding(Graphics g, int x, int y, DrawColors colors, ViewPort viewPort, boolean drawTrace, boolean selected, boolean hilighted) {
	//Debug.info(this, "MapObject.drawUnit: Entering drawUnit: "+unitType);
 
        if(null != imageIcon) {
            drawImageIcon(g, x, y,colors, selected, hilighted);
        }
        
        else {
            
            if(BDA_DEAD == bda) {
                drawDeadUnitBox(g,x,y,colors.fore, colors.back);
                drawDeadUnit(g, x, y, colors.fore);
            } else {           
                //	Debug.debug(this, "MapObject.drawUnit: calling drawUnitBox.");
                drawBDBox(g,x,y,colors.fore, colors.back, viewPort);
                /*
                if(unitType == UnitTypes.TERMINAL) {
                    drawBuildingTerminal(g,x,y,colors.fore);
                }*/   
            }
        }
        drawMouseOver(g, x, y,colors, selected, hilighted);
    }
    
    private void drawStrike(Graphics g, int x, int y, int width, int height, DrawColors colors) {
        int widthIncr = width/5;
        if(hilighted)
            g.setColor(colors.back);
        else
            g.setColor(colors.fore);
        g.drawOval(x,y,1,1);
        for(int loopw = 0; loopw <= width; loopw+= widthIncr) {
            //	    Debug.debug(this, "MapObject.drawStrike: g.drawOval("+(x - (loopw/2))+", "+(y - (loopw/2))+", "+loopw+", "+loopw+")");
            g.drawOval(x - (loopw/2) , y - (loopw/2), loopw, loopw);
        }
    }
    
    private void drawDetonation(Graphics g, ViewPort viewPort, int x, int y) {
        if(sizeX <= 0)
            return;
        double now = (double)System.currentTimeMillis();
        double percent = (now - (double) timeCreated)/DETONATION_DURATION;
        // @todo: SRO Fri Apr 14 21:42:12 EDT 2006: after detonation
        // duration is up, we really should remove ourselves from
        // mapdb somehow.
        if(percent > 1.0) {
            this.flagAnimationDone = true;
            return;
        }
        
        //int width = (int)(viewPort.getScale() * percent * sizeX);
        //int height = (int)(viewPort.getScale() * percent * sizeY);
        int width = (int)(percent * sizeX);
        int height = (int)(percent * sizeY);
        Debug.debug(this, "MapObject.drawDetonation: drawing detonation at x,y="+x+","+y+" size="+sizeX+" width="+width+" percent="+percent);
        g.setColor(DETONATION_COLOR);
        
        g.fillOval(x - (width/2) , y - (width/2), width, width);
    }
    
    // Draw a flash outline around
    private void drawFlash(Graphics g, ViewPort viewPort, int x, int y) {
        //	Debug.debug(this, "MapObject.drawFlash: Entering, flash="+flash+", flashOn="+flashOn);
        if(!flash)
            return;
        
        if(flashOn) {
            int width = FLASH_BOX_WIDTH;
            int height = FLASH_BOX_HEIGHT;
            if(flashSizeSpecified) {
                width = (int)(viewPort.getScale() * flashWidth);
                height = (int)(viewPort.getScale() * flashHeight);
            }
            //	    Debug.debug(this, "MapObject.drawFlash: drawing flash at x,y="+x+","+y);
            g.setColor(FLASH_COLOR);
            ((Graphics2D)g).setStroke(new BasicStroke(FLASH_STROKE));
            if(0 == flashHeight)
                g.drawOval(x - (width/2) , y - (width/2), width, width);
            else
                g.drawRect(x - (width/2) , y - (height/2), width, height);
        }
        
        long now = System.currentTimeMillis();
        if(flashOn) {
            if((lastFlashSwitch+flashInterval) < now) {
                lastFlashSwitch = now;
                flashOn = !flashOn;
                flashInterval += FLASH_INTERVAL_INC;
                if(flashInterval > FLASH_INTERVAL_MAX) {
                    flashInterval = FLASH_INTERVAL_MAX;
                }
                if(now > (timeStartedFlashing + FLASH_DURATION))
                    flash = false;
            }
        } else {
            if((lastFlashSwitch+FLASH_OFF_INTERVAL) < now) {
                lastFlashSwitch = now;
                flashOn = !flashOn;
                if(now > (timeStartedFlashing + FLASH_DURATION))
                    flash = false;
            }
        }
        
        //	Debug.debug(this, "MapObject.drawFlash: Leaving, flash="+flash+", flashOn="+flashOn);
    }
    
    private void drawMapLine(Graphics g, int x, int y, DrawColors colors, ViewPort viewPort) {
        ((Graphics2D)g).setStroke(new BasicStroke(LINE_STROKE));
        drawCross(g, x, y, colors.fore);
        int lastx = 0;
        int lasty = 0;
        boolean firstPoint = true;
        Iterator iter = linePoints.iterator();
        if(type == TYPE_ROAD)
            g.setColor(ROAD_COLOR);
        if(type == TYPE_RAIL)
            g.setColor(RAIL_COLOR);
        while(iter.hasNext()) {
            Point2D.Float p = (Point2D.Float)iter.next();
            int nextx = (int)viewPort.sourceToDestX(p.getX());
            int nexty = (int)viewPort.sourceToDestY(p.getY());
            if(firstPoint) {
                firstPoint = false;
                g.drawLine(nextx,nexty, nextx, nexty);
            } else {
                g.drawLine(lastx, lasty, nextx, nexty);
            }
            lastx = nextx;
            lasty = nexty;
        }
    }
    

    Polygon pCache = null;
    double cacheLocalSrcLeftX = 0.0;
    double cacheLocalSrcBottomY = 0.0;
    double cacheSourceHeight = 0.0;
    double cacheScale = 0.0;
    private void drawMapPoly(Graphics g, int x, int y, DrawColors colors, ViewPort viewPort) {
	if(null == poly)
	    return;

	if((cacheLocalSrcLeftX != viewPort.getLocalSrcLeftX())
	   || (cacheLocalSrcBottomY != viewPort.getLocalSrcBottomY())
	   || (cacheSourceHeight != viewPort.getSourceHeight())
	   || (cacheScale != viewPort.getScale()))
	    pCache = null;

	Polygon p = null;
	if(null != pCache)
	    p = pCache;
	else {
	    cacheLocalSrcLeftX = viewPort.getLocalSrcLeftX();
	    cacheLocalSrcBottomY = viewPort.getLocalSrcBottomY();
	    cacheSourceHeight = viewPort.getSourceHeight();
	    cacheScale = viewPort.getScale();
	    p = new Polygon();

	    for(int loopi = 0; loopi < poly.length; loopi++) {
		int nextx = (int)viewPort.sourceToDestX(poly[loopi].getX());
		int nexty = (int)viewPort.sourceToDestY(poly[loopi].getY());
		p.addPoint(nextx,nexty);
		//	    System.err.println("MapObject.drawMapPoly: point "+poly[loopi].getX()+","+poly[loopi].getY()+" translated to "+nextx+","+nexty);

	    }
	}
        ((Graphics2D)g).setStroke(new BasicStroke(POLY_STROKE));
	g.setColor(mapGraphicLineColor);
	g.drawPolygon(p);
	if(mapGraphicFillOn) {
	    g.setColor(mapGraphicColor);
	    if(mapGraphicFillOn)
		g.fillPolygon(p);
	}
	if(null == pCache)
	    pCache = p;
    }

    private void drawMapArea(Graphics g, int x, int y, double scale) {
	//	    Debug.debug(this, "MapObject:Drawing area "+name+", type="+type+", key="+key);
	AffineTransform save = ((Graphics2D)g).getTransform();
	AffineTransform sf = AffineTransform.getScaleInstance(scale, scale);

	if(TYPE_CLEARING == type) {
	    g.setColor(CLEARING_TRANS);
	}
	else if(TYPE_CORRIDOR == type) {
	    g.setColor(CORRIDOR_TRANS);
	}
	else if(TYPE_AA_PRIMARY == type) {
	    g.setColor(SHORTEST_PATH_TRANS);
	}
	else if(TYPE_AA_SECONDARY == type) {
	    g.setColor(SECOND_SHORTEST_PATH_TRANS);
	}
	else {
	    g.setColor(MAP_AREA_DEFAULT_COLOR);
	}

	((Graphics2D)g).setTransform(sf);
	((Graphics2D)g).fill(area);
	((Graphics2D)g).setTransform(save);
	g.setColor(MAP_AREA_BOUNDS_BOX_COLOR);
	Rectangle bounds = area.getBounds();
	int boundsx = (int)((bounds.x + (bounds.width/2)) * scale);
	int boundsy = (int)((bounds.y + (bounds.height/2)) * scale);
	x = boundsx;
	y = boundsy;
	//	    Debug.debug(this, "MapObject:scale="+scale+", boundsx="+boundsx+", boundsy+"+boundsy);

	//	    Debug.debug(this, "MapObject:Done drawing area "+name);
    }
    


    private void drawBoxOrCircle(Graphics g, int x, int y, int width, int height, DrawColors colors) {
        if((TYPE_NOGO_ZONE == type) && !hilighted)
            colors.back = nogoZoneColor;
        else if((TYPE_PATROL_ZONE == type) && !hilighted)
            colors.back = patrolZoneColor;
        else if((TYPE_MAP_GRAPHIC == type) && !hilighted)
            colors.back = mapGraphicColor;
        Graphics2D g2 = (Graphics2D)g;
        if(sizeY > 1.0) {
            //		g.drawRect(x - width/2, y - height/2, width, height);
            //		Debug.info("MapObject: minefield orientation="+orientation+" sizex = "+sizeX+", sizey = "+sizeY+", screen width="+width+", height="+height);
            
            AffineTransform saveAFT = g2.getTransform();
            g2.setTransform(g2.getDeviceConfiguration().getDefaultTransform());                 //set identity transform
            g2.rotate(orientation * (Math.PI/180), x, y);
            
	    if(TYPE_MAP_GRAPHIC == type) {
		if(mapGraphicFillOn) {
		    g2.setColor(new Color(mapGraphicColor.getRed(), mapGraphicColor.getGreen(), mapGraphicColor.getBlue(), mapGraphicAlpha));
		    g2.fillRect(x - width/2, y - height/2, width, height);
		}
		g2.setColor(mapGraphicLineColor);
		g2.drawRect(x - width/2, y - height/2, width, height);
	    }
            else {
		if((TYPE_MINEFIELD == type)
		   || (TYPE_ANTITANK_DITCH == type)
		   || (TYPE_POLITICAL_OBSTACLE == type)
		   || (TYPE_WEATHER_OBSTACLE == type)
		   || (TYPE_GENERIC_OBSTACLE == type)
		   || (TYPE_NOGO_ZONE == type)
		   || (TYPE_PATROL_ZONE == type)
		   || (TYPE_OBJECTIVE == type)
		   || (TYPE_EA_INFERENCED == type)) {
		    g2.setColor(new Color(colors.back.getRed(), colors.back.getGreen(), colors.back.getBlue(), FILL_ALPHA_LEVEL));
		    g2.fillRect(x - width/2, y - height/2, width, height);
		}
		g2.setColor(colors.fore);
		g2.drawRect(x - width/2, y - height/2, width, height);
	    }
            g2.setTransform(saveAFT);
        } else {
            //		Debug.debug(this, "MapObject: Drawing oval of width="+sizeX+", height="+sizeY+", screen width="+width+", height="+height);
	    if(TYPE_MAP_GRAPHIC == type) {
		if(mapGraphicFillOn) {
		    g2.setColor(new Color(mapGraphicColor.getRed(), mapGraphicColor.getGreen(), mapGraphicColor.getBlue(), mapGraphicAlpha));
		    g.fillOval((int)(x - (width/2)), (int)(y - (width/2)),(int)width, width);
		}
		g2.setColor(mapGraphicLineColor);
		g.drawOval((int)(x - (width/2)), (int)(y - (width/2)),(int)width, width);
	    }
	    else {
		if((TYPE_MINEFIELD == type)
		   || (TYPE_ANTITANK_DITCH == type)
		   || (TYPE_POLITICAL_OBSTACLE == type)
		   || (TYPE_WEATHER_OBSTACLE == type)
		   || (TYPE_GENERIC_OBSTACLE == type)
		   || (TYPE_NOGO_ZONE == type)
		   || (TYPE_PATROL_ZONE == type)
		   || (TYPE_OBJECTIVE == type)
		   || (TYPE_EA_INFERENCED == type)) {
		    g.setColor(new Color(colors.back.getRed(), colors.back.getGreen(), colors.back.getBlue(), FILL_ALPHA_LEVEL));
		    g.fillOval((int)(x - (width/2)), (int)(y - (width/2)),(int)width, width);
		} else if(TYPE_OBSERVATION_POST == type) {
		    g.setColor(OP_BASE);
		    g.fillOval((int)(x - (width/2)), (int)(y - (width/2)),(int)width, width);
		}
		g.setColor(colors.fore);
		g.drawOval((int)(x - (width/2)), (int)(y - (width/2)),(int)width, width);
	    }
        }
    }
    
    private void drawOrientationLine(Graphics g, int x, int y, DrawColors colors) {
        if(!orientationSet)
            return;
        double radians = (-orientation)*(Math.PI/180);
        int linex = x + (int)(Math.cos(radians)*SYMBOL_ORIENTATION_LINE_SIZE);
        int liney = y + (int)(Math.sin(radians)*SYMBOL_ORIENTATION_LINE_SIZE);
        g.setColor(colors.fore);
        g.drawLine(x, y, linex, liney);
    }
    
    public void drawTraceLine(Graphics g, ViewPort viewPort) {
        drawTraceLine(g, viewPort, DEFAULT_TRACE_LINE_LENGTH);
    }
    
    public void drawTraceLine(Graphics g, ViewPort viewPort, int maxTraceLength) {
	try {
	    double len = 0;

	    Point[] tracePointAry;
	    synchronized(tracePoints) {
		if(tracePoints.size() < 2)
		    return;
		tracePointAry = tracePoints.toArray(new Point[1]);
	    }
	    if(null == tracePointAry)
		return;
	    if(tracePointAry.length <= 2)
		return;
	    if(null == tracePointAry[0])
		return;

	    //	if(name.equals("63"))
	    //	   Debug.debug(this, "MapObject("+getName()+").drawTraceLine: Starting");

 	    if(unitType == UnitTypes.MUAV) {
 		Color traceColor = new Color(TRACE_COLOR.getRed(), TRACE_COLOR.getGreen(), TRACE_COLOR.getBlue(), TRACE_ALPHA_LEVEL);
 		((Graphics2D)g).setStroke(UAV_TRACE_STROKE_OBJ);
 		g.setColor(traceColor);
		((Graphics2D)g).setStroke(new BasicStroke(TRACE_STROKE));
		g.setColor(TRACE_COLOR);
 	    }
 	    else {
		((Graphics2D)g).setStroke(new BasicStroke(TRACE_STROKE));
		g.setColor(TRACE_COLOR);
 	    }

	    GeneralPath gp = new GeneralPath();
	    Point prev = tracePointAry[tracePointAry.length - 1];
	    if(null == prev)
		return;
	    int lastx = (int)viewPort.sourceToDestX(prev.x);
	    int lasty = (int)viewPort.sourceToDestY(prev.y);
	    gp.moveTo(lastx, lasty);
	    for(int loopi = tracePointAry.length - 2; loopi >= 0; loopi--) {
		Point cur = tracePointAry[loopi];
		if(null == cur)
		    continue;
		int nextx = (int)viewPort.sourceToDestX(cur.x);
		int nexty = (int)viewPort.sourceToDestY(cur.y);
		gp.lineTo(nextx, nexty);
		double diffx = cur.x - prev.x;
		double diffy = cur.y - prev.y;
		len += Math.sqrt((diffx*diffx) + (diffy*diffy));
		if(len > maxTraceLength) {
		    break;
		}
		prev = cur;
	    }
	    ((Graphics2D)g).draw(gp);

// 	    Point prev = (Point)tracePoints.get(tracePoints.size() - 1);
// 	    int lastx = (int)viewPort.sourceToDestX(prev.x);
// 	    int lasty = (int)viewPort.sourceToDestY(prev.y);
// 	    for(int loopi = tracePoints.size() - 2; loopi >= 0; loopi--) {
// 		Point cur = (Point)tracePoints.get(loopi);
// 		int nextx = (int)viewPort.sourceToDestX(cur.x);
// 		int nexty = (int)viewPort.sourceToDestY(cur.y);
// 		g.drawLine(lastx, lasty, nextx, nexty);
// 		double diffx = cur.x - prev.x;
// 		double diffy = cur.y - prev.y;
// 		double segLenSqd = (diffx*diffx) + (diffy*diffy);
// 		lenSqd += segLenSqd;
// 		len = Math.sqrt(lenSqd);
// 		// 	    if(name.equals("63"))
// 		// 	       Debug.debug(this, "MapObject("+getName()+").drawTraceLine:\t"+maxLen+"\t"+segLenSqd+"\t"+lenSqd+"\t"+len);
//
// 		//	    if(lenSqd > maxLenSqd) {
// 		if(len > maxLen) {
// 		    // 		if(name.equals("63"))
// 		    // 		    Debug.debug(this, "MapObject("+getName()+").drawTraceLine: Done");
// 		    return;
// 		}
// 		lastx = nextx;
// 		lasty = nexty;
// 		prev = cur;
// 	    }
            ((Graphics2D)g).setStroke(new BasicStroke(LINE_STROKE));
        } catch(Exception e) {
            Debug.error(this, "MapObject.drawTraceLine:Ignoring exception while drawing for "+getKey()+", e="+e);
            e.printStackTrace();
        }
    }
    public void drawPlannedPathLine(Graphics g, ViewPort viewPort) {
	try {
	    double len = 0;

	    if(plannedPathPoints.size() < 2)
		return;

	    //	if(name.equals("63"))
	    //	   Debug.debug(this, "MapObject("+getName()+").drawPlannedPathLine: Starting");

	    ((Graphics2D)g).setStroke(new BasicStroke(PLANNEDPATH_STROKE));
	    if(!plannedPathConflict)
		g.setColor(PLANNEDPATH_COLOR);
	    else
		g.setColor(PLANNEDPATH_CONFLICT_COLOR);

	    GeneralPath gp = new GeneralPath();

	    Point[] pointAry = plannedPathPoints.toArray(new Point[0]);
	    if(null == pointAry)
		return;
	    if(pointAry.length < 2)
		return;
	    Point nextPoint = pointAry[0];
	    int nextx = (int)viewPort.sourceToDestX(nextPoint.x);
	    int nexty = (int)viewPort.sourceToDestY(nextPoint.y);
	    gp.moveTo(nextx, nexty);
	    for(int loopi = 1; loopi < pointAry.length; loopi++) {
		nextPoint = pointAry[loopi];
		if(null == nextPoint)
		    continue;
		nextx = (int)viewPort.sourceToDestX(nextPoint.x);
		nexty = (int)viewPort.sourceToDestY(nextPoint.y);
		gp.lineTo(nextx, nexty);
	    }
	    ((Graphics2D)g).draw(gp);

	    ((Graphics2D)g).setStroke(new BasicStroke(LINE_STROKE));
	}
	catch(Exception e) {
	    Debug.error(this, "MapOBject.drawPlannedPathLine:Ignoring exception while drawing for "+getKey()+", e="+e);
	    e.printStackTrace();
	}
    }

    // This draws the object at an x/y on the canvas.  The viewport
    // stuff (i.e. converting from world coordinates to
    // image/screen/canvas coordinates) should have already been done
    // by someone else.  Not sure if this is the best idea, or if the
    // actual drawing code should be done elsewhere, but gotta start
    // this sometime.  Move the drawing code later if necessary.
    public void draw(Graphics g, ViewPort viewPort, boolean drawType, boolean drawName, boolean drawTrace) {
	// 	System.err.println("MapObject.draw: key="+key+", name="+name+", type="+type);
        Graphics2D g2 = (Graphics2D)g;
        // @todo: why is this 50.0?  Because we're using 50m grid cell
        // sizes?  This should come from the grid file.
        double scale = viewPort.getScale() * 50.0;
        DrawColors colors = null;
        if((TYPE_UNIT == type) && (UnitTypes.WASM == unitType)) {
            colors = new DrawColors(COLORS_WASM);
        } else if((TYPE_UNIT == type) && (UnitTypes.MUAV == unitType)) {
            colors = new DrawColors(COLORS_WASM);
        } else if((TYPE_STRIKE == type)) {
            if(isTasked())
                colors = new DrawColors(COLORS_STRIKE_TASKED);
            else
                colors = new DrawColors(COLORS_STRIKE);
        } else if((TYPE_PICTURE_ORDER == type)) {
            if(isTasked())
                colors = new DrawColors(COLORS_PICTURE_ORDER_TASKED);
            else
                colors = new DrawColors(COLORS_PICTURE_ORDER);
        } else if(ForceIds.UNKNOWN == forceId) {
            colors = new DrawColors(COLORS_UNKNOWN);
        } else if(ForceIds.OPFOR == forceId) {
            colors = new DrawColors(COLORS_OPFOR);
        } else if(ForceIds.BLUEFOR == forceId) {
            colors = new DrawColors(COLORS_BLUEFOR);
        } else if(ForceIds.NEUTRAL == forceId) {
            colors = new DrawColors(COLORS_NEUTRAL);
        } else {
            colors = new DrawColors(COLORS_DEFAULT);
        }
        
        if(selected) {
            colors.selected();
        } else if(hilighted) {
            colors.hilighted();
        } else if(BDA_DEAD == bda) {
            //@TODO: Do more for visualization effect.
            colors.dead();
        }
        
        g.setFont(new Font("BeliefDisplayFont", Font.BOLD, 18));
        g.setColor(colors.fore);
        
        double dispoint[] = {disX, disY, disZ};
        Point2D.Double tcc = viewPort.transformGccToTcc(dispoint);
        //	Debug.info("MapObject.draw: id = "+key+" gcc=("+disX+", "+disY+", "+disZ+"), pos=("+posX+", "+posY+", "+posZ+"), tcc=("+tcc.x+", "+tcc.y+")");
        
        int x = (int)viewPort.sourceToDestX(posX);
        int y = (int)viewPort.sourceToDestY(posY);
        
        if(TYPE_UNIT == type) {
            drawFlash(g, viewPort, x, y);
            //	    Debug.debug(this, "MapObject.draw: mo is a unit.");
            ((Graphics2D)g).setStroke(new BasicStroke(LINE_STROKE));
            if(UnitTypes.MISSILE == unitType) {
                drawMissile(g, x, y, colors.fore,colors.back);
            } else if(UnitTypes.SA_MISSILE == unitType) {
                drawSAMissile(g, x, y, colors.fore,colors.back);
            } else if(UnitTypes.SS_MISSILE == unitType) {
                drawSSMissile(g, x, y, colors.fore,colors.back);
            } else if(UnitTypes.WASM == unitType) {
                drawWASM(g, x, y, colors.fore, colors.back);
            } else if(UnitTypes.MUAV == unitType) {
                drawMUAV(g, x, y, colors.fore, colors.back);
            } else if(UnitTypes.CIVILIAN == unitType){
                    drawUnitSymbolCivilian(g,x,y,colors.fore);
            } else if(UnitTypes.SENSOR == unitType){
                //drawSensorFootprint(g, x, y);
                // ((RadioDetectorFootprint)asset).drawBeam(g,viewPort);
                if(null != drawable) {
                    drawable.mapDraw((Graphics2D)g,viewPort);
                } else {
                    drawSensor(g, x, y, colors, viewPort, drawTrace, selected, hilighted);
                }
            } else if(UnitTypes.EMITTER == unitType){
                // @todo: a new interface, MapObjectDrawable, has been
                // created with one method, the draw method.  Need to
                // change Emitter to implement MapObjectDrawable, then
                // chnage this and setAsset to use MapObjectDrawable,
                // thus removing MapObject's dependency on machinetta
                // code.
                //
                //drawEmitterFootprint(g, x, y);
                //		((Emitter)asset).drawEmitterFootprint(g2,viewPort);
                if(null != drawable) {
                   drawable.mapDraw(g2,viewPort);
                } else {
                   drawUnit(g, x, y, colors, viewPort, drawTrace, selected, hilighted);
                }
                if(ellipseFlag) {
                   int width = (int)(viewPort.getScale() * ellipseWidth);
                   int height = (int)(viewPort.getScale() * ellipseHeight);
                   drawEllipse(g, x, y, width, height);
                }
           } else if (UnitTypes.TERMINAL == unitType) {
                drawBuilding(g, x, y, colors, viewPort, drawTrace, selected, hilighted);                
           } else {
                //		Debug.debug(this, "MapObject.draw: Calling drawUnit.");
                if(ellipseFlag) {
                    int width = (int)(viewPort.getScale() * ellipseWidth);
                    int height = (int)(viewPort.getScale() * ellipseHeight);
                    drawEllipse(g, x, y, width, height);
                }

                //Debug.info(1, "Call drawUnit for  "+getKey());
                drawUnit(g, x, y, colors, viewPort, drawTrace, selected, hilighted);
           }

            /*
            if((UnitTypes.MISSILE != unitType)
            && (UnitTypes.WASM != unitType)
            && (UnitTypes.SA_MISSILE != unitType)
            && (UnitTypes.SS_MISSILE != unitType)) {
                drawOrientationLine(g, x, y, colors);
            }
             */
            if(destSet) {
                g.setColor(destColor);
                int dx = (int)viewPort.sourceToDestX(destX);
                int dy = (int)viewPort.sourceToDestY(destY);
                g.drawLine(x, y, dx, dy);
            }            
 
        } else if(TYPE_PICTURE_ORDER == type) {
            drawPictureOrder(g, x, y, colors,viewPort, selected, hilighted);
        } else if(TYPE_PICTURE == type) {
            drawPicture(g, x, y, colors,viewPort, selected, hilighted);
        } else if(TYPE_DETONATION == type) {
            drawDetonation(g, viewPort, x, y);
        } else if(TYPE_LINE == type || TYPE_ROAD == type || TYPE_RAIL == type) {
            //	    Debug.debug(this, "MapObject: Drawing line for object "+getKey()+" at "+x+", "+y);
            drawMapLine(g, x, y, colors, viewPort);
        } else if(TYPE_OBJECTIVE == type) {
            if((0 == sizeX) && (0 == sizeY)) {
                drawCross(g, x, y,colors.fore);
            } else {
                int width = (int)(viewPort.getScale() * sizeX);
                int height = (int)(viewPort.getScale() * sizeY);
                drawBoxOrCircle(g, x, y, width, height, colors);
            }
        } else if(TYPE_UNKNOWN == type || TYPE_ASSEMBLY_AREA == type || TYPE_OBJECTIVE == type || TYPE_CHECKPOINT == type) {
            drawCross(g, x, y,colors.fore);
        } else if(TYPE_STRIKE == type) {
            //	    Debug.debug(this, "MapObject:Drawing strike at "+x+","+y);
            // @todo having a bad width is causing problems - this should
            // probably be set elsewhere properly.
            if(sizeX > 5000)
                sizeX = 5000;
            else if(sizeX <= 0)
                sizeX = 5000;
            int width = (int)(viewPort.getScale() * sizeX);
            int height = (int)(viewPort.getScale() * sizeY);
            drawStrike(g, x, y, width, height, colors);
        } else if(TYPE_POLY == type) {
            drawMapPoly(g, x, y, colors,viewPort);
	    return;
        } else if(null != area) {
            drawMapArea(g, x, y, scale);
        } else {
            int width = (int)(viewPort.getScale() * sizeX);
            int height = (int)(viewPort.getScale() * sizeY);
            drawBoxOrCircle(g, x, y, width, height, colors);
        }
        if(drawType && drawName) {
            g.setColor(colors.shadow);
            g.drawString(getLabel(), x+(SBOX_W_HALF)+4+1, y + 1);
            g.setColor(colors.text);
            g.drawString(getLabel(), x+(SBOX_W_HALF)+4, y);
        } else if(drawName) {
            g.setColor(colors.shadow);
            g.drawString(name, x+(SBOX_W_HALF)+4+1, y + 1);
            g.setColor(colors.text);
            g.drawString(name, x+(SBOX_W_HALF)+4, y);
        } else if(drawType) {
            g.setColor(colors.shadow);
            g.drawString(getTypeAbbr(), x+(SBOX_W_HALF)+4+1, y + 1);
            g.setColor(colors.text);
            g.drawString(getTypeAbbr(), x+(SBOX_W_HALF)+4, y);
        }
    }
    
    public String toString() {
        StringBuffer buf=new StringBuffer(100);
        return buf.append("MapObject key:")
        .append(key)
        .append(" name:")
        .append(name)
        .append(" forceid:")
        .append(ForceIds.getName(forceId))
        .append(" type:")
        .append(getTypeName())
        .append(" unit:")
        .append(UnitTypes.getName(unitType))
        .append(" size:")
        .append(UnitSizes.getName(unitSize))
        .append(" pos:")
        .append(posX)
        .append(",")
        .append(posY)
        .append(",")
        .append(posZ)
        .append(" bda:")
        .append(bda)
        .toString();
    }
}

