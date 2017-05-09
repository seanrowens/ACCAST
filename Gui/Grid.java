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
// @version     $Id: Grid.java,v 1.4 2008/01/28 02:17:01 owens Exp $ 

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;

public abstract class Grid extends Observable {
    public final static String MOKSAF_VERSION_STRING = "version: #MokSaf MapData(3.3) -";
    public final static int PASSABLE = Integer.MAX_VALUE;
    public final static int IMPASSABLE = 0;
    public final static int TYPE_UNKNOWN = 0;
    public final static int TYPE_SOIL = 1;
    public final static int TYPE_ELEVATION = 2;
    public final static int TYPE_ROAD = 3;
    public final static int TYPE_RAIL = 4;

    // Stuff from the header that we mostly want to save if we have to
    // write this grid out again.
    String ctdbfilename = null;
    String ctdbmin = null;
    String ctdbmax = null;
    String ctdbmid = null;
    String origsouthlat = null;
    String origwestlng = null;
    String orignorthlat = null;
    String origeastlng = null;
    String tcc_gcc_rotmatrix = null;
    String tcc_origin_in_gcc = null;
    String tcc_xunit_in_gcc = null;
    String tcc_yunit_in_gcc = null;
    String tcc_zunit_in_gcc = null;
    String tcc_gcc_transform = null;

    protected int type = TYPE_UNKNOWN;
    public void setType(int type) {
	this.type = type;
    }

    protected int gridCellSize = 50;
    public int getGridCellSize() { return gridCellSize; }
    protected int bottomLeftX = 0;
    public int getBottomLeftX() { return bottomLeftX;}
    public void setBottomLeftX(int value) { bottomLeftX = value;}
    protected int bottomLeftY = 0;
    public int getBottomLeftY() { return bottomLeftY;}
    public void setBottomLeftY(int value) { bottomLeftY = value;}

    protected DebugInterface debug = null;
    protected String fileName = null;
    public String getFileName() { return fileName; }
    protected String originalFileName = null;
    public String getOriginalFileName() { return originalFileName; }
    protected boolean validData = false;
    public boolean getValidData() { return validData; }
    protected int width = 0;
    public int getWidth() { return width;}
    protected int height = 0;
    public int getHeight() { return height;}
    protected double widthMeters = 50000.0;
    public double getWidthMeters() { return widthMeters;}
    protected double heightMeters = 50000.0;
    public double getHeightMeters() { return heightMeters;}

    protected double[][] gccToTccTransform = new double[4][4]; 
    public double[][] getGccToTccTransform() { return gccToTccTransform;}
    protected double[][] tccToGccTransform = new double[4][4]; 
    public double[][] getTccToGccTransform() { return tccToGccTransform;}

    protected double southLat = 0.0;
    public double getSouthLat() { return southLat;}
    protected double northLat = 0.0;
    public double getNorthLat() { return northLat;}
    protected double westLong = 0.0;
    public double getWestLong() { return westLong;}
    protected double eastLong = 0.0;
    public double getEastLong() { return eastLong;}

    protected void copyHeaders(Grid grid) {
	this.ctdbfilename = grid.ctdbfilename;
	this.ctdbmin = grid.ctdbmin;
	this.ctdbmax = grid.ctdbmax;
	this.ctdbmid = grid.ctdbmid;
	this.origsouthlat = grid.origsouthlat;
	this.origwestlng = grid.origwestlng;
	this.orignorthlat = grid.orignorthlat;
	this.origeastlng = grid.origeastlng;
	this.tcc_gcc_rotmatrix = grid.tcc_gcc_rotmatrix;
	this.tcc_origin_in_gcc = grid.tcc_origin_in_gcc;
	this.tcc_xunit_in_gcc = grid.tcc_xunit_in_gcc;
	this.tcc_yunit_in_gcc = grid.tcc_yunit_in_gcc;
	this.tcc_zunit_in_gcc = grid.tcc_zunit_in_gcc;
	this.tcc_gcc_transform = grid.tcc_gcc_transform;
	this.type = grid.type;
	this.gridCellSize = grid.gridCellSize;
	this.bottomLeftX = grid.bottomLeftX;
	this.bottomLeftY = grid.bottomLeftY;
	this.originalFileName = grid.fileName;
	this.fileName = grid.fileName;
	this.width = grid.width;
	this.height = grid.height;
	this.widthMeters = grid.widthMeters;
	this.heightMeters = grid.heightMeters;
	this.gccToTccTransform = grid.gccToTccTransform;
	this.tccToGccTransform = grid.tccToGccTransform;
	this.southLat = grid.southLat;
	this.northLat = grid.northLat;
	this.westLong = grid.westLong;
	this.eastLong = grid.eastLong;
	this.validData = grid.validData;
    }

    public void changed() {
	setChanged();
	notifyObservers(this);
    }

    protected final static int ANIMATED_COUNTER_MAX=5000;
    protected boolean animated = false;
    public void setAnimated(boolean val) { animated = val;}

    protected int animationCounter = 0;
    protected void animate(int sleepMs) {
	if(animated) {
	    animationCounter++;
	    if(animationCounter > ANIMATED_COUNTER_MAX) {
		animationCounter = 0;
		changed();
		System.gc();
		// TODO: should this be a wait instead?  I.e. give up
		// any locks on this object while sleeping, so redraw
		// can happen?  Will need this if paint is
		// synchronized and animate gets called from a
		// synchronized method.
		try {
		    Thread.sleep(sleepMs);
		}
		catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
    }


    public boolean insideGrid(int x, int y) {
	if((x < 0) || (y < 0) || (x >= width) || (y >= height)) 
	    return false;
	else
	    return true;
    }

    abstract void setValue(int x, int y, int value);
    abstract void setValue(int x, int y, double value);

    abstract void fastSetValue(int x, int y, int value);
    abstract void fastSetValue(int x, int y, double value);

    // New file format, header is;
    //
    // version: #MokSaf MapData(3.2) -
    // ctdbfilename: /usr1/home/otbsaf/dev/OTB/terrain/azerbaijan_tin-0002_061099.c7l
    // ctdbmin: 0.000000 0.000000
    // ctdbmax: 356250.000000 345250.000000
    // ctdbmid: 178125.000000 172625.000000
    // origsouthlat: 37.895170897548283051
    // origwestlng: 45.769747587261697674
    // orignorthlat: 41.108220479438962514
    // origeastlng: 50.002553516981350867
    // southlat: 39.351433853448853029
    // westlng: 46.445143012578142816
    // northlat: 39.993578447657270658
    // eastlng: 47.264970687387005910
    // filetype: SOIL
    // gridcellsize: 100
    // bottomleftx: 56844
    // bottomlefty: 156479
    // width: 69000
    // height: 69000
    // cols: 690
    // rows: 690
    // soiltype:0=unknown # Unknown 
    // soiltype:1=asphalt #  Asphalt or other hard surface
    // soiltype:2=packed_dirt #  Packed soil or dirt road
    // soiltype:3=soft_sand #  Soft sandy soil
    // soiltype:4=deep_water #  Impassable deep water
    // soiltype:5=shallow_water #  Passable shallow water
    // soiltype:6=mud #  Muddy soil
    // soiltype:7=muddy_road #  Wet dirt road
    // soiltype:8=ice #  Slick ice surface
    // soiltype:9=swamp #  Very soft surface
    // soiltype:10=canopy_Forest # Canopy or forested area
    // soiltype:11=US_railroad # Railroad w/ US specifications
    // soiltype:12=Euro_railroad # Railroad w/ European specs.
    // soiltype:13=small_rocks # Small rocks <= 18 inches
    // soiltype:14=boulders # Large boulders 6 ft. high
    // soiltype:15=no_go # No go
    // tcc_gcc_rotmatrix: -0.75660155630934367021467323866090737283229827880859375 0.6533547602862694869685356025001965463161468505859375 0.026108278410315439999056508213470806367695331573486328125 -0.40078103784258534147966201999224722385406494140625 -0.494923728624538539211386023453087545931339263916015625 0.77098966436016780345852339451084844768047332763671875 0.516651462405261607813145019463263452053070068359375 0.57286826486497932631181129181641153991222381591796875 0.63631220128573084959811012595309875905513763427734375
    // tcc_origin_in_gcc: 3500970.1880839769728481769561767578125 3624999.2571931933052837848663330078125 3896268.47609124146401882171630859375
    // tcc_xunit_in_gcc: -0.7397091104649007320404052734375 0.668643272481858730316162109375 0.0422859187237918376922607421875
    // tcc_yunit_in_gcc: -0.3866904233582317829132080078125 -0.4775246200151741504669189453125 0.7864362052641808986663818359375
    // tcc_zunit_in_gcc: 0.5482081132940948009490966796875 0.5676295128650963306427001953125 0.6142186908982694149017333984375
    // tcc_gcc_transform: -0.7397091104649007320404052734375 -0.3866904233582317829132080078125 0.5482081132940948009490966796875 3500970.1880839769728481769561767578125 0.668643272481858730316162109375 -0.4775246200151741504669189453125 0.5676295128650963306427001953125 3624999.2571931933052837848663330078125 0.0422859187237918376922607421875 0.7864362052641808986663818359375 0.6142186908982694149017333984375 3896268.47609124146401882171630859375 0 0 0 1
    // samplepoint1: 282265.00 355096.00 970.00 3146613.07 3635012.86 4178618.73 3155395.74 3644717.37 4188060.45 -8782.67 -9704.51 -9441.73
    // ENDHEADER
    // 
    // Elevation files have basically identical headers, except
    // filetype will be "ELEVATION", and the soiltype list will be
    // absent.
    //
    // widthheight and topleft are in GCS coordinates (meters), not
    // grid cell coordinates.  filetype should be SOIL or ELEVATION.
    // cols and rows are number of columns/rows in file. (I.e. number
    // of grid cells.)
    protected boolean loadFileHeader(BufferedReader reader) {
	try {
	    while(true) {
		String line = reader.readLine();
		if(line.startsWith("ENDHEADER")) {
		    return true;
		}
		else if(line.startsWith("version:")) {
		    if(!line.equalsIgnoreCase(MOKSAF_VERSION_STRING)) {
			debug.error("Unknown file version.  Trying to load anyway. (Expecting '"+MOKSAF_VERSION_STRING+"', got '"+line+"')");
		    }
		}
		else if(line.startsWith("ctdbfilename:")) {
		    ctdbfilename = new String(line);
		}		   
		else if(line.startsWith("ctdbmin:")) {
		    ctdbmin = new String(line);
		}		   
		else if(line.startsWith("ctdbmax:")) {
		    ctdbmax = new String(line);
		}		   
		else if(line.startsWith("ctdbmid:")) {
		    ctdbmid = new String(line);
		}		   
		else if(line.startsWith("origsouthlat:")) {
		    origsouthlat = new String(line);
		}
		else if(line.startsWith("origwestlng:")) {
		    origwestlng = new String(line);
		}
		else if(line.startsWith("orignorthlat:")) {
		    orignorthlat = new String(line);
		}
		else if(line.startsWith("origeastlng:")) {
		    origeastlng = new String(line);
		}
		else if(line.startsWith("filetype:")) {
		    if(line.equals("filetype: SOIL")) {
			type = TYPE_SOIL;
		    }
		    else if(line.equals("filetype: ELEVATION")) {
			type = TYPE_ELEVATION;
		    }
		    else if(line.equals("filetype: UNKNOWN")) {
			type = TYPE_UNKNOWN;
		    }
		    else {
			debug.error("Unknown file type ="+line);
			type = TYPE_UNKNOWN;
		    }
		}		   
		else if(line.startsWith("gridcellsize:")) {
		    gridCellSize =  Integer.parseInt(line.substring("gridcellsize:".length()).trim());
		}		   
		// DEPRECATED
		else if(line.startsWith("topleftx:")) {
		    debug.warn("found deprecated field name topleftx for line="+line);
		    bottomLeftX =  Integer.parseInt(line.substring("topleftx:".length()).trim());
		}		   
		// DEPRECATED
		else if(line.startsWith("toplefty:")) {
		    debug.warn("found deprecated field name toplefty for line="+line);
		    bottomLeftY =  Integer.parseInt(line.substring("toplefty:".length()).trim());
		}		   
		else if(line.startsWith("bottomleftx:")) {
		    bottomLeftX =  Integer.parseInt(line.substring("bottomleftx:".length()).trim());
		}		   
		else if(line.startsWith("bottomlefty:")) {
		    bottomLeftY =  Integer.parseInt(line.substring("bottomlefty:".length()).trim());
		}		   
		else if(line.startsWith("width:")) {
		    widthMeters =  Integer.parseInt(line.substring("width:".length()).trim());
		}		   
		else if(line.startsWith("height:")) {
		    heightMeters =  Integer.parseInt(line.substring("height:".length()).trim());
		}		   
		else if(line.startsWith("cols:")) {
		    width =  Integer.parseInt(line.substring("cols:".length()).trim());
		}		   
		else if(line.startsWith("rows:")) {
		    height =  Integer.parseInt(line.substring("rows:".length()).trim());
		}
		else if(line.startsWith("southlat:")) {
		    southLat =  Double.parseDouble(line.substring("southlat:".length()).trim());
		}		   
		else if(line.startsWith("northlat:")) {
		    northLat =  Double.parseDouble(line.substring("northlat:".length()).trim());
		}		   
		else if(line.startsWith("eastlng:") || line.startsWith("eastlong:") || line.startsWith("eastlon:")) {
		    eastLong =  Double.parseDouble(line.substring("eastlng:".length()).trim());
		}		   
		else if(line.startsWith("westlng:") || line.startsWith("westlong:") || line.startsWith("westlon:")) {
		    westLong =  Double.parseDouble(line.substring("westlng:".length()).trim());
		}		   
		else if(line.startsWith("tcc_gcc_rotmatrix:")) {
		    tcc_gcc_rotmatrix = new String(line);
		}
		else if(line.startsWith("tcc_origin_in_gcc:")) {
		    tcc_origin_in_gcc = new String(line);
		}
		else if(line.startsWith("tcc_xunit_in_gcc:")) {
		    tcc_xunit_in_gcc = new String(line);
		}
		else if(line.startsWith("tcc_yunit_in_gcc:")) {
		    tcc_yunit_in_gcc = new String(line);
		}
		else if(line.startsWith("tcc_zunit_in_gcc:")) {
		    tcc_zunit_in_gcc = new String(line);
		}
		else if(line.startsWith("tcc_gcc_transform:")) {
		    tcc_gcc_transform = new String(line);
		    double[] tempValues =  StringUtils.parseDoubleList(16, line.substring("tcc_gcc_transform:".length()).trim());
		    for(int loopy = 0; loopy <= 3; loopy++) 
			for(int loopx = 0; loopx <= 3; loopx++) 
			    tccToGccTransform[loopx][loopy] = tempValues[(loopy * 4) + loopx];

// 		    debug.info("Tcc_gcc transformation matrix header="+line);
// 		    debug.info("Loaded tcc_gcc transformation matrix="
// 			       +tccToGccTransform[0][0]+" "
// 			       +tccToGccTransform[1][0]+" "
// 			       +tccToGccTransform[2][0]+" "
// 			       +tccToGccTransform[3][0]+" "
// 			       +tccToGccTransform[0][1]+" "
// 			       +tccToGccTransform[1][1]+" "
// 			       +tccToGccTransform[2][1]+" "
// 			       +tccToGccTransform[3][1]+" "
// 			       +tccToGccTransform[0][2]+" "
// 			       +tccToGccTransform[1][2]+" "
// 			       +tccToGccTransform[2][2]+" "
// 			       +tccToGccTransform[3][2]+" "
// 			       +tccToGccTransform[0][3]+" "
// 			       +tccToGccTransform[1][3]+" "
// 			       +tccToGccTransform[2][3]+" "
// 			       +tccToGccTransform[3][3]);
		    Matrix matrix = new Matrix();
		    gccToTccTransform = matrix.inverse(tccToGccTransform);
		    double[][] inverse = matrix.inverse(tccToGccTransform);
		    gccToTccTransform = inverse;

// 		    debug.info("Created gcc_tcc transformation matrix="
// 			       +gccToTccTransform[0][0]+" "
// 			       +gccToTccTransform[1][0]+" "
// 			       +gccToTccTransform[2][0]+" "
// 			       +gccToTccTransform[3][0]+" "
// 			       +gccToTccTransform[0][1]+" "
// 			       +gccToTccTransform[1][1]+" "
// 			       +gccToTccTransform[2][1]+" "
// 			       +gccToTccTransform[3][1]+" "
// 			       +gccToTccTransform[0][2]+" "
// 			       +gccToTccTransform[1][2]+" "
// 			       +gccToTccTransform[2][2]+" "
// 			       +gccToTccTransform[3][2]+" "
// 			       +gccToTccTransform[0][3]+" "
// 			       +gccToTccTransform[1][3]+" "
// 			       +gccToTccTransform[2][3]+" "
// 			       +gccToTccTransform[3][3]);

		}
		else if(line.startsWith("gcc_tcc_transform:")) {
// 		    double[] tempValues =  StringUtils.parseDoubleList(16, line.substring("gcc_tcc_transform:".length()).trim());
// 		    for(int loopy = 0; loopy <= 3; loopy++) 
// 			for(int loopx = 0; loopx <= 3; loopx++) 
// 			    gccToTccTransform[loopx][loopy] = tempValues[(loopy * 4) + loopx];
// 		    debug.info("Gcc_tcc transformation matrix header="+line);
// 		    debug.info("Loaded gcc_tcc transformation matrix="
// 			       +gccToTccTransform[0][0]+" "
// 			       +gccToTccTransform[1][0]+" "
// 			       +gccToTccTransform[2][0]+" "
// 			       +gccToTccTransform[3][0]+" "
// 			       +gccToTccTransform[0][1]+" "
// 			       +gccToTccTransform[1][1]+" "
// 			       +gccToTccTransform[2][1]+" "
// 			       +gccToTccTransform[3][1]+" "
// 			       +gccToTccTransform[0][2]+" "
// 			       +gccToTccTransform[1][2]+" "
// 			       +gccToTccTransform[2][2]+" "
// 			       +gccToTccTransform[3][2]+" "
// 			       +gccToTccTransform[0][3]+" "
// 			       +gccToTccTransform[1][3]+" "
// 			       +gccToTccTransform[2][3]+" "
// 			       +gccToTccTransform[3][3]);
		}
		else if(line.startsWith("tcc_")) {
		    // ignore.
		}
		else if(line.startsWith("gcc_")) {
		    // ignore.
		}
		else if(line.startsWith("samplepoint")) {
		    // sample points are somehwat randomnly chosen
		    // points in TCC coordinates that have been
		    // translated both by libcoords and by the
		    // tcc_gcc_transform matrix included in the file.

		    // samplepoint1: 10.00 10.00 1.00 -760939.02 -5418318.58 3266729.14 -760939.02 -5418318.58 3266729.14
		    // samplepoint2: 50.00 10.00 1.00 -760899.43 -5418324.33 3266728.83 -760899.43 -5418324.33 3266728.83
		    // samplepoint3: 10.00 13.00 5.00 -760939.26 -5418320.45 3266733.77 -760939.26 -5418320.45 3266733.77
		    // samplepoint4: 45.00 76.00 33.00 -760902.87 -5418317.17 3266801.93 -760902.87 -5418317.18 3266801.93
		    // samplepoint5: 8453.00 33863.00 1264.00 -749987.91 -5403287.89 3296292.09 -750002.73 -5403371.87 3296336.50
		    // samplepoint6: 19388.00 8976.00 1045.00 -741151.69 -5417395.30 3274785.69 -741158.77 -5417425.81 3274803.29
		    // samplepoint7: 50000.00 0.00 5000.00 -711993.58 -5429590.05 3268807.06 -712051.76 -5429751.38 3268908.33
		    // samplepoint8: 0.00 50000.00 5000.00 -757484.94 -5396927.15 3312092.71 -757514.49 -5397113.73 3312160.94
		    // samplepoint9: 50000.00 50000.00 5000.00 -707933.52 -5403955.86 3311597.42 -708020.65 -5404299.93 3311773.30
		    
		    // The first 3 numbers are the sample point in TCC
		    // coordinate system.  The second 3 are the same
		    // sample point in GCC coordinate system, as
		    // translated by libcoordinates coord_convert.
		    // The last 3 are the same sample point in GCC
		    // coordinate system as translated by the
		    // tcc_gcc_transform matrix multiply.
		    //
		    // Note, there are round off errors.  These aren't
		    // big enough to worry about with points close to
		    // the TCC origin, but when we get further out
		    // they can get large.  Still, for the moment
		    // we're ignoring this issue in the hopes that
		    // they'll be 'good enough'.

		    double[] tempValues =  StringUtils.parseDoubleList(12, line.substring("samplepoint1:".length()).trim());
		    double[] originalTcc = new double[4]; 
		    originalTcc[0] = tempValues[0];
		    originalTcc[1] = tempValues[1];
		    originalTcc[2] = tempValues[2];
		    originalTcc[3] = 1.0;

		    double[] originalGcc = new double[4]; 
		    originalGcc[0] = tempValues[3];
		    originalGcc[1] = tempValues[4];
		    originalGcc[2] = tempValues[5];
		    originalGcc[3] = 1.0;

		    double[] resultGcc = new double[4]; 
		    double[] resultTcc = new double[4]; 

		    ViewPort.transform(originalTcc, tccToGccTransform, resultGcc);
		    ViewPort.transform(originalGcc, gccToTccTransform, resultTcc);

// 		    debug.debug(line);
// 		    debug.debug("    Orig gcc     =("+originalGcc[0]+" "+originalGcc[1]+" "+originalGcc[2]+")"); 
// 		    debug.debug("    Trans to tcc =("+resultTcc[0]+" "+resultTcc[1]+" "+resultTcc[2]+")"); 
// 		    debug.debug("    Orig tcc     =("+originalTcc[0]+" "+originalTcc[1]+" "+originalTcc[2]+")");
// 		    debug.debug("    Trans to gcc =("+resultGcc[0]+" "+resultGcc[1]+" "+resultGcc[2]+")"); 

		    
		    double[][] ninety = new double[4][4];
		    ninety[0][0] = Math.cos(Math.toRadians(90));
		    ninety[1][0] = Math.sin(Math.toRadians(90));
		    ninety[2][0] = 0.0;
		    ninety[3][0] = 0.0;
		    ninety[0][1] = -Math.sin(Math.toRadians(90));
		    ninety[1][1] = Math.cos(Math.toRadians(90));
		    ninety[2][1] = 0.0;
		    ninety[3][1] = 0.0;
		    ninety[0][2] = 0.0;
		    ninety[1][2] = 0.0;
		    ninety[2][2] = 1.0;
		    ninety[3][2] = 0.0;
		    ninety[0][3] = 0.0;
		    ninety[1][3] = 0.0;
		    ninety[2][3] = 0.0;
		    ninety[3][3] = 1.0;
		    double[] start = new double[4];
		    double[] finish = new double[4];
		    start[0] = 987654321.0987654321;
		    start[1] = 0.0;
		    start[2] = 0.0;
		    start[3] = 0.0;

		    ViewPort.transform(start, ninety, finish);
// 		    debug.debug("Translated start="+start[0]+", "+start[1]+", "+start[2]+", "+start[3]+", to finish="+finish[0]+", "+finish[1]+", "+finish[2]+", "+finish[3]);

		}
		else if(line.startsWith("gccsamplepoint")) {
		    // gccsamplepoint1: -760939.02 -5418318.58 3266729.14 10.00 10.00 1.00 

		    double[] tempValues =  StringUtils.parseDoubleList(9, line.substring("gccsamplepoint1:".length()).trim());
		    double[] original = new double[4]; 
		    original[0] = tempValues[0];
		    original[1] = tempValues[1];
		    original[2] = tempValues[2];
		    original[3] = 1.0;
		    double[] resultTcc = new double[4]; 
		    ViewPort.transform(original, gccToTccTransform, resultTcc);
// 		    debug.info(line+", tcc=("+resultTcc[0]+" "+resultTcc[1]+" "+resultTcc[2]+", "+resultTcc[3]+")");
		}
		else if(line.startsWith("soiltype:")) {
		    // ignore.
		}
		else {
		    debug.error("Unknown header field="+line);
		}
	    }
	}
	catch(Exception e){
	    debug.error("Unable to read grid file." + e);
	    e.printStackTrace();
	    return false;
	}
    }

    abstract protected void allocateValues(int height, int width);
    
    abstract protected void parseAndLoadGridFileLine(int loopY, String line);

    // Format for our grid files is going to be (hopefully) somewhat
    // standard. 
    //
    // see LoadFileHeader above.
    public synchronized boolean loadGridFile(String fileName) {
	debug.debug("Loading grid file="+fileName);
	
	int nextLoadingMessageCounter = 0;
	validData = false;
	width = 0;
	height = 0;
	this.fileName = fileName;
	
	try {
	
	    FileReader fileReader = new FileReader(fileName);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);

	    if(!loadFileHeader(bufferedReader)) {
		debug.error("Couldn't load file header, giving up on loading file. ");
		return false;
	    }

	    debug.debug(this.getClass().getName()+" contains "+width+" by "+height+" points.");

	    allocateValues(width, height);
	    
	    for(int loopY = 0; loopY < height; loopY++) {

		// Read the line and parse it.
		String line = bufferedReader.readLine();

		// abstract in parent class, overloaded in child class
		parseAndLoadGridFileLine(loopY, line);

		// Do debug printing/counter progress report stuff, 
		if(loopY == nextLoadingMessageCounter) {
		    debug.debug("Loading is "+(loopY*100)/height+"% complete."); 
		    nextLoadingMessageCounter += height/10;
		}
	    }
	    validData = true;
	    debug.debug("Finished reading grid file");
	}
	catch(Exception e){
	    debug.error("Unable to read grid file." + e);
	    e.printStackTrace();
	    return false;
	}
	changed();
	return true;
    }

    protected boolean saveFileHeader(PrintWriter writer) {
	writer.println(MOKSAF_VERSION_STRING);
	if(null != ctdbfilename) writer.println(ctdbfilename);
	if(null != ctdbmin) writer.println(ctdbmin);
	if(null != ctdbmax) writer.println(ctdbmax);
	if(null != ctdbmid) writer.println(ctdbmid);
	if(null != origsouthlat) writer.println(origsouthlat);
	if(null != origwestlng) writer.println(origwestlng);
	if(null != orignorthlat) writer.println(orignorthlat);
	if(null != origeastlng) writer.println(origeastlng);
	writer.println("southlat: "+southLat);
	writer.println("westlng: "+westLong);
	writer.println("northlat: "+northLat);
	writer.println("eastlng: "+eastLong);
	if(TYPE_SOIL == type) {
	    writer.println("filetype: SOIL");
	}
	else if(TYPE_ELEVATION == type) {
	    writer.println("filetype: ELEVATION");
	}
	else {
	    writer.println("filetype: UNKNOWN");
	}
	writer.println("gridcellsize: "+gridCellSize);
	writer.println("bottomleftx: "+bottomLeftX);
	writer.println("bottomlefty: "+bottomLeftY);
	writer.println("width: "+(int)widthMeters);
	writer.println("height: "+(int)heightMeters);
	writer.println("cols: "+width);
	writer.println("rows: "+height);
	if(null != tcc_gcc_rotmatrix) writer.println(tcc_gcc_rotmatrix);
	if(null != tcc_origin_in_gcc) writer.println(tcc_origin_in_gcc);
	if(null != tcc_xunit_in_gcc) writer.println(tcc_xunit_in_gcc);
	if(null != tcc_yunit_in_gcc) writer.println(tcc_yunit_in_gcc);
	if(null != tcc_zunit_in_gcc) writer.println(tcc_zunit_in_gcc);
	if(null != tcc_gcc_transform) writer.println(tcc_gcc_transform);
	// These aren't really necessary for the grid code, and are
	// actually pretty much ignored, but they are included in the
	// hope that should someone have to deal with the data files
	// without having source code on hand, it will make life
	// easier.
	if(TYPE_SOIL == type) {
	    writer.println("soiltype:0=unknown # Unknown");
	    writer.println("soiltype:1=asphalt #  Asphalt or other hard surface");
	    writer.println("soiltype:2=packed_dirt #  Packed soil or dirt road");
	    writer.println("soiltype:3=soft_sand #  Soft sandy soil");
	    writer.println("soiltype:4=deep_water #  Impassable deep water");
	    writer.println("soiltype:5=shallow_water #  Passable shallow water");
	    writer.println("soiltype:6=mud #  Muddy soil");
	    writer.println("soiltype:7=muddy_road #  Wet dirt road");
	    writer.println("soiltype:8=ice #  Slick ice surface");
	    writer.println("soiltype:9=swamp #  Very soft surface");
	    writer.println("soiltype:10=canopy_Forest # Canopy or forested area");
	    writer.println("soiltype:11=US_railroad # Railroad w/ US specifications");
	    writer.println("soiltype:12=Euro_railroad # Railroad w/ European specs.");
	    writer.println("soiltype:13=small_rocks # Small rocks <= 18 inches");
	    writer.println("soiltype:14=boulders # Large boulders 6 ft. high");
	    writer.println("soiltype:15=no_go # No go");
	}
	writer.println("ENDHEADER");
	return true;
    }

    abstract protected String gridFileLineToString(int loopY);

    public synchronized void saveGridFile(String fileName) {
	debug.debug("Saving grid file="+fileName);
	
	int nextSavingMessageCounter = 0;
	
	try {
	
	    FileWriter fileWriter = new FileWriter(fileName);
	    PrintWriter writer = new PrintWriter(new BufferedWriter(fileWriter));

	    if(!saveFileHeader(writer)) {
		debug.error("Couldn't save file header, giving up on saving file. ");
		return;
	    }

	    debug.debug(this.getClass().getName()+" contains "+width+" by "+height+" points.");

	    for(int loopY = 0; loopY < height; loopY++) {

		writer.println(gridFileLineToString(loopY));

		// Do debug printing/counter progress report stuff, 
		if(loopY == nextSavingMessageCounter) {
		    debug.debug("Saving is "+(loopY*100)/height+"% complete."); 
		    nextSavingMessageCounter += height/10;
		}
	    }
	    writer.flush();
	    writer.close();
	    debug.debug("Finished writing grid file");
	}
	catch(Exception e){
	    debug.error("Unable to write grid file." + e);
	    e.printStackTrace();
	}
    }

    public int toGridX(double localX) {
	return (int)((localX - bottomLeftX)/(double)gridCellSize);
    }
    public int toGridY(double localY) {
	return height - (((int)localY - bottomLeftY)/gridCellSize);
    }

    public double toDoubleGridX(double localX) {
	return (int)((localX - bottomLeftX)/(double)gridCellSize);
    }
    public double toDoubleGridY(double localY) {
	return height - (((int)localY - bottomLeftY)/gridCellSize);
    }

    public double toLocalX(double gridX) {
	return (gridX * gridCellSize) + bottomLeftX;
    }
    public double toLocalY(double gridY) {
	return (gridCellSize * (height - gridY)) + bottomLeftY;
    }

    public double gridToLat(double gridX) {
	return ((((double)(gridX*gridCellSize))/widthMeters) * ( northLat - southLat)) + southLat;
    }
    public double gridToLong(double gridY) {
	return ((((double)(gridY*gridCellSize))/heightMeters) * ( eastLong - westLong)) + westLong;
    }

    public double localToLong(double localX) {
	return ((localX/widthMeters) * ( eastLong - westLong)) + westLong;
    }
    public double localToLat(double localY) {
	return ((localY/heightMeters) * ( northLat - southLat)) + southLat;
    }

    public double longToLocalX(double longitude) {
	return widthMeters * (longitude - westLong) / ( eastLong - westLong);
    }
    public double latToLocalY(double latitude) {
	return heightMeters * (latitude - southLat) /( northLat - southLat);
    }

    // Find the number of grid cells contained in an 'Area' object.
    public int areaSize(Area gridArea) {
	int size = 0;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		// TODO: Shouldn't this be x, y instead of y, x?
		if(gridArea.contains((double)loopY,(double)loopX)) {
		    size++;
		}
	    }
	}
	return size;
    }

    public void setCircle(int gridX, int gridY, int gridRadius, double value) {
	int rSquared = gridRadius * gridRadius;
	int dSquared = 0;
	for(int loopY = 0; loopY < (gridRadius * 2); loopY++) {
	    for(int loopX = 0; loopX < (gridRadius * 2); loopX++) {
		dSquared = ((loopY - gridRadius) * (loopY - gridRadius)) 
		    + ((loopX - gridRadius) * (loopX - gridRadius));
		if(dSquared <= rSquared) 
		    setValue(loopX+gridX-gridRadius, loopY+gridY - gridRadius, value);
	    }
	}
	//	setValue(gridX, gridY, 0.0);
    }
    
    public void setRectangle(int gridX, int gridY, int gridWidth, int gridHeight, double value) {
	for(int loopY = 0; loopY < gridHeight; loopY++) {
	    for(int loopX = 0; loopX < gridWidth; loopX++) {
		setValue(loopX+gridX, loopY+gridY, value);
	    }
	}
    }
    
    // EXAMPLE OF USING setArea
    //
    // 		    int posX = localToGridX(mo.posX-(mo.sizeX/2));
    // 		    int posY = localToGridY(mo.posY+(mo.sizeY/2));
    // 		    int sizeX = (int)(mo.sizeX / modifiedObstacleGrid.getGridCellSize());
    // 		    int sizeY = (int)(mo.sizeY / modifiedObstacleGrid.getGridCellSize());
    // 		    debug.debug("adding obstacles to grid, posX="+posX+", posY="+posY+", sizeX="+sizeX+", sizeY="+sizeY);
    // 		    //		    modifiedObstacleGrid.setRectangle(posX, posY, sizeX, sizeY, IntGrid.IMPASSABLE);
    // 		    double rotation = Math.toRadians(mo.getOrientation());
    // 		    Rectangle2D.Double oRect = new Rectangle2D.Double(posX, posY, sizeX, sizeY);
    // 		    AffineTransform rot = AffineTransform.getRotateInstance(rotation, (posX + sizeX/2), (posY + sizeY/2));
    // 		    Shape rotatedRect = rot.createTransformedShape(oRect);
    // 		    Area oArea = new Area(rotatedRect);
    // 		    modifiedObstacleGrid.setArea(oArea, IntGrid.IMPASSABLE);
    public void setArea(Area gridArea, double value) {
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		if(gridArea.contains((double)loopX,(double)loopY)) {
		    setValue(loopX, loopY, value);
		}
	    }
	}
    }

    public void setRect(int localX, int localY, int localSizeX, int localSizeY, double orientation, double value) {
	int gridX = toGridX(localX);
	int gridY = toGridY(localY);
	int gridSizeX = (int)(localSizeX / getGridCellSize());
	int gridSizeY = (int)(localSizeY / getGridCellSize());
	double rotation = Math.toRadians(orientation);
	Rectangle2D.Double oRect = new Rectangle2D.Double(gridX, gridY, gridSizeX, gridSizeY);
	AffineTransform rot = AffineTransform.getRotateInstance(rotation, (gridX + gridSizeX/2), (gridY + gridSizeY/2));
	Shape rotatedRect = rot.createTransformedShape(oRect);
	Area oArea = new Area(rotatedRect);
	setArea(oArea, value);
    }

    public void setGridRect(int gridX, int gridY, int gridSizeX, int gridSizeY, double orientation, double value) {
	double rotation = Math.toRadians(orientation);
	Rectangle2D.Double oRect = new Rectangle2D.Double(gridX, gridY, gridSizeX, gridSizeY);
	AffineTransform rot = AffineTransform.getRotateInstance(rotation, (gridX + gridSizeX/2), (gridY + gridSizeY/2));
	Shape rotatedRect = rot.createTransformedShape(oRect);
	Area oArea = new Area(rotatedRect);
	setArea(oArea, value);
    }

    public void setCone(int localX, int localY, double orientationDegrees, double widthDegrees, double localRange, double value) {
	double gridRange = (localRange * 2) / getGridCellSize();
	double gridX = toGridX(localX - localRange);
	double gridY = toGridY(localY + localRange);
	Arc2D.Double oArc = new Arc2D.Double(gridX, gridY, gridRange, gridRange, 
					     orientationDegrees - (widthDegrees/2), 
					     widthDegrees, Arc2D.PIE);
	Area oArea = new Area(oArc);
	setArea(oArea, value);
    }

    public void setGridDistanceFromPoint(int gridX, int gridY) {
	double[][] cache = new double[width][height];
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		cache[loopX][loopY] = -1;
	    }
	}

	int xdiff;
	int ydiff;
	for(int loopY = 0; loopY < height; loopY++) {
	    for(int loopX = 0; loopX < width; loopX++) {
		xdiff = loopX - gridX;
		ydiff = loopY - gridY;
		if(true) {
		    if(xdiff < 0) xdiff *= -1;
		    if(ydiff < 0) ydiff *= -1;
		    if(xdiff < ydiff) {
			int temp = xdiff;
			xdiff = ydiff;
			ydiff = temp;
		    }
		    if(-1 == cache[xdiff][ydiff]) {
			cache[xdiff][ydiff] = Math.sqrt(xdiff * xdiff + ydiff * ydiff);
		    }
		    setValue(loopX, loopY, cache[xdiff][ydiff]);
		} 
		else {
		    setValue(loopX, loopY, Math.sqrt(xdiff * xdiff + ydiff * ydiff));
		}
	    }
	}
	debug.debug("Done setting distances from "+gridX+", "+gridY);
    }

    //---------------------------------------------------------------------------
    // A version of the above method with doubles substituting for
    // GreatEarthCircle bounds.
    //---------------------------------------------------------------------------
    public int mapToAxis (double min_bound, double max_bound,
			  int draw_region_offset, int max_map_side,
			  int min_map_side) {
	short degrees = 0; // This is set by the constructor for GreatEarthCircle.
	int axis_point = 0;

	axis_point = draw_region_offset +
	    (int)((double) (max_bound - degrees) / (max_bound - min_bound) *
		  (max_map_side - min_map_side) + min_map_side);

	return (axis_point);
    }

    //---------------------------------------------------------------------------
    // Convert an EarthPoint's longitude and latitude into x and y coordinates,
    // respectively, on the map given by MapBounds.  Drawing is performed by a
    // graphical-mirror subclass of FlightPath.
    //---------------------------------------------------------------------------
    public void mapToPoint () {
	double min_deg_lon = 0.0;
	double max_deg_lon = 0.0;
	int x_offset = 0;
	int max_x = 0;
	int min_x = 0;
	double min_deg_lat = 0.0;
	double max_deg_lat = 0.0;
	int y_offset = 0;
	int max_y = 0;
	int min_y = 0;
	short degrees = 0; // This is set by the constructor for GreatEarthCircle.

	int x = mapToAxis (min_deg_lon, max_deg_lon, x_offset, max_x, min_x);
	x = x_offset + (int)(
			     (
			      (
			       (max_deg_lon - degrees) 
			       /
			       (max_deg_lon - min_deg_lon)
			       ) *
			      (max_x - min_x)
			      ) + min_x);
	int y = mapToAxis (min_deg_lat, max_deg_lat, y_offset, max_y, min_y);
	y = y_offset + (int)(
			     (
			      (
			       (max_deg_lat - degrees)
			       /
			       (max_deg_lat - min_deg_lat)
			       )
			   
			      *
			      (max_y - min_y)
			      )
			     + min_y);
    }

    // ----------------------------------------------------------------------


    public String toString() { 
	return "fileName="+fileName+", type="+type+", validData="+validData+", cellsize="+gridCellSize+", botleft=("+bottomLeftX+", "+bottomLeftY+"), widthMeters="+widthMeters+", heightMeters="+heightMeters+", width="+width+", height="+height;
    }

}
