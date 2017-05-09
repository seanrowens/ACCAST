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
// @version     $Id: MapDB.java,v 1.3 2006/07/10 21:54:22 owens Exp $ 

import java.io.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;

public class MapDB extends Observable {
    private DebugInterface debug = null;
    private Map map = null;
    public Map getMap() { return map; }
    private boolean dirty = false;
    public boolean getDirty() { return dirty; }
    public void setDirty(boolean value) { this.dirty = value;}
    private double[][] gccToTccTransform = new double[4][4]; 
    private double[][] tccToGccTransform = new double[4][4]; 
    private IntGrid xGrid = null;
    private IntGrid yGrid = null;
    private IntGrid zGrid = null;
    //    private Converter converter = new Converter();

    public MapDB(double[][] gccToTccTransform, double[][] tccToGccTransform, IntGrid xGrid, IntGrid yGrid, IntGrid zGrid) {
	this.gccToTccTransform = gccToTccTransform;
	this.tccToGccTransform = tccToGccTransform;
	this.xGrid = xGrid;
	this.yGrid = yGrid;
	this.zGrid = zGrid;

	debug = new DebugFacade(this);
	map = new HashMap();
    }

    public void changed() {
	dirty = true;
	setChanged();
	notifyObservers(this);
    }

    public void clear() {
	synchronized(map) {
	    map.clear();
	}
	setDirty(true);
	changed();
    }

    public MapObject[] getMapObjects() {
	MapObject[] results = new MapObject[1];
	synchronized(map) {
	    results = (MapObject[])map.values().toArray(results);
	}
	return results;
    }

    public void remove(MapObject obj) {
	synchronized(map) {
	    map.remove(obj.getKey());
	}
	changed();
    }

    public void remove(String key) {
	synchronized(map) {
	    map.remove(key);
	}
	changed();
    }

    public void fixDis(MapObject obj) {
	double savex = obj.disX; 
	double savey = obj.disY; 
	double savez = obj.disZ; 

	if((null == xGrid) || (null == yGrid) || (null == zGrid))
	    return;

	int gridX = xGrid.toGridX(obj.posX);
	int gridY = xGrid.toGridY(obj.posY);
	// 	double elevation = elevationGrid.getValue(gridX, gridY);
	// 	if(Math.abs(elevation - obj.posZ) < 50) {
	obj.disX = xGrid.getValue(gridX, gridY);
	obj.disY = yGrid.getValue(gridX, gridY);
	obj.disZ = zGrid.getValue(gridX, gridY);
	debug.debug("fixdis: "+obj.getName()+" pos=["+obj.posX+", "+obj.posY+"], disgrid["+gridX+","+gridY+"] old values="+savex+", "+savey+", "+savez+", new values="+obj.disX+", "+obj.disY+", "+obj.disZ+", DIFFERENCE="+(obj.disX - savex)+", "+(obj.disY - savey)+", "+(obj.disZ - savez));
	// 	}
    }
    
    public void add(MapObject obj) {
	if((0.0 == obj.disX)
	   && (0.0 == obj.disY)
	   && (0.0 == obj.disZ)) {

	    // Transform from local to dis.
	    double[] original = new double[4]; 
	    original[0] = obj.posX;
	    original[1] = obj.posY;
	    original[2] = obj.posZ;
	    original[3] = 1.0;
	    double[] result = new double[4]; 
	    ViewPort.transform(original, tccToGccTransform, result);
	    obj.disX = result[0];
	    obj.disY = result[1];
	    obj.disZ = result[2];

	    // TODO: Fix the transformation code above.  Until then,
	    // hack it.

	    if((null != xGrid) && (null != yGrid) && (null != zGrid)) {
		int gridX = xGrid.toGridX(obj.posX);
		int gridY = xGrid.toGridY(obj.posY);
		obj.disX = xGrid.getValue(gridX, gridY);
		obj.disY = yGrid.getValue(gridX, gridY);
		obj.disZ = zGrid.getValue(gridX, gridY);
	    }
	}
	synchronized(map) {
	    map.put(obj.getKey(), obj);
	}
	changed();
    }

    public MapObject get(String key) {
	synchronized(map) {
	    return (MapObject)map.get(key);
	}
    }

    public void put(MapObject mo) {
	synchronized(map) {
	    map.put(mo.getKey(), mo);
	}
	setDirty(true);
    }

    public MapObject getFirst() {
	synchronized(map) {
	    return (MapObject)map.values().iterator().next();
	}
    }

    public Iterator getIter() {
	synchronized(map) {
	    return map.values().iterator();
	}
    }
    
    // @todo consolidate all of these versions of findWithin into one
    // version and then various convenience functions that call the one
    // version with null params.
    public MapObject findWithin(double localx, double localy, double range) {
	MapObject found = null;
	double bestDist = Double.MAX_VALUE;
	MapObject[] results = new MapObject[1];
	synchronized(map) {
	    results = (MapObject[])map.values().toArray(results);
	}
	for(int loopi = 0; loopi < results.length; loopi++) {
	    MapObject mo = results[loopi];
	    if(null == mo)
		continue;
	    double xdist = mo.posX - localx;
	    double ydist = mo.posY - localy;
	    double curDist = Math.sqrt((xdist * xdist)+(ydist * ydist));
	    if(curDist < range) {
		if(curDist < bestDist) {
		    bestDist = curDist;
		    found = mo;
		}
	    }
	}
	return found;
    }

    public MapObject findWithin(double localx, double localy, double range, BackgroundConfig config) {
	MapObject found = null;
	double bestDist = Double.MAX_VALUE;

	MapObject[] results = new MapObject[1];
	synchronized(map) {
	    results = (MapObject[])map.values().toArray(results);
	}
	for(int loopi = 0; loopi < results.length; loopi++) {
	    MapObject mo = results[loopi];
	    if(null == mo)
		continue;
	    if(!mo.isDisplayable(config))
		continue;
	    double xdist = mo.posX - localx;
	    double ydist = mo.posY - localy;
	    double curDist = Math.sqrt((xdist * xdist)+(ydist * ydist));
	    if(curDist < range) {
		if(curDist < bestDist) {
		    bestDist = curDist;
		    found = mo;
		}
	    }
	}
	return found;
    }

    public MapObject findWithin(double localx, double localy, double range, Set<Integer> typesToFind) {
	MapObject found = null;
	double bestDist = Double.MAX_VALUE;

	MapObject[] results = new MapObject[1];
	synchronized(map) {
	    results = (MapObject[])map.values().toArray(results);
	}
	for(int loopi = 0; loopi < results.length; loopi++) {
	    MapObject mo = results[loopi];
	    if(null == mo)
		continue;
	    if(null != typesToFind) {
		if(!typesToFind.contains(mo.getType()))
		    continue;
	    }
	    double xdist = mo.posX - localx;
	    double ydist = mo.posY - localy;
	    double curDist = Math.sqrt((xdist * xdist)+(ydist * ydist));
	    if(curDist < range) {
		if(curDist < bestDist) {
		    bestDist = curDist;
		    found = mo;
		}
	    }
	}
	return found;
    }

    public MapObject findWithin(double localx, double localy, double range, int mapObjectTypeToFind) {
	Set<Integer> moTypesToFind = new HashSet<Integer>();
	moTypesToFind.add(mapObjectTypeToFind);
	return findWithin(localx,localy,range,moTypesToFind);
    }

    public ArrayList<MapObject> findWithin(double localx, double localy, double localx2, double localy2, Set<Integer> typesToFind) {
	if(localx > localx2) {
	    double temp = localx2;
	    localx2 = localx;
	    localx = temp;
	}
	if(localy > localy2) {
	    double temp = localy2;
	    localy2 = localy;
	    localy = temp;
	}
	    
	ArrayList<MapObject> foundList = new ArrayList<MapObject>();
	double bestDist = Double.MAX_VALUE;

	MapObject[] results = new MapObject[1];
	synchronized(map) {
	    results = (MapObject[])map.values().toArray(results);
	}
	for(int loopi = 0; loopi < results.length; loopi++) {
	    MapObject mo = results[loopi];
	    if(null == mo)
		continue;
	    if(null != typesToFind) {
		if(!typesToFind.contains(mo.getType()))
		    continue;
	    }
	    if((mo.posX < localx) || (mo.posX > localx2) || (mo.posY < localy) || (mo.posY > localy2))
		continue;
	    foundList.add(mo);
	}
	return foundList;
    }

    public void setSelected(ArrayList<MapObject> moList, boolean value) {
	for(MapObject mo: moList) {
	    mo.setSelected(value);
	}
	setDirty(true);
    }

    public void setHilighted(ArrayList<MapObject> moList, boolean value) {
	for(MapObject mo: moList) {
	    mo.setHilighted(value);
	}
	setDirty(true);
    }
    
    public void clearSelected() {
	MapObject[] mapObjects = getMapObjects();
	if(null != mapObjects) {
	    for(MapObject mo: mapObjects) {
		if(null != mo)
		    mo.setSelected(false);
	    }
	}
	setDirty(true);
    }

    public void clearHilighted() {
	MapObject[] mapObjects = getMapObjects();
	if(null != mapObjects) {
	    for(MapObject mo: mapObjects) {
		if(null != mo)
		    mo.setHilighted(false);
	    }
	}
	setDirty(true);
    }

    public HashMap<String, Boolean> getSelectedKeys() {
	HashMap<String, Boolean> keyMap = new HashMap<String, Boolean>();
	MapObject[] mapObjects = getMapObjects();
	for(MapObject mo: mapObjects) {
	    if(mo.isSelected())
		keyMap.put(mo.getKey(), true);
	    else
		keyMap.put(mo.getKey(), false);
	}
	return keyMap;
    }
    
    public HashMap<String, MapObject> getSelected() {
	HashMap<String, MapObject> selectedMap = new HashMap<String, MapObject>();
	MapObject[] mapObjects = getMapObjects();
	for(MapObject mo: mapObjects) {
	    if(mo.isSelected())
		selectedMap.put(mo.getKey(), mo);
	}
	return selectedMap;
    }

    public void setSelectedKeys(HashMap<String, Boolean> keyMap) {
	for(String key: keyMap.keySet()) {
	    boolean selected = keyMap.get(key);
	    MapObject mo;
	    synchronized(map) {
		mo = (MapObject)map.get(key);
	    }
	    mo.setSelected(selected);
	}
	setDirty(true);
    }


}
