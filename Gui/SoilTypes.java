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
// @version     $Id: SoilTypes.java,v 1.4 2007/07/27 01:59:28 owens Exp $ 

import java.awt.Color;

public class SoilTypes {
    
    public final static Color DEFAULT_SOIL_COLOR = Color.white;

    // From libctdb/ct_soil.h
    //
    public final static int UNKNOWN = 0;		// 	soil_unknown = 0,
    public final static int ASPHALT = 1;		// 	soil_asphalt = 1,
    public final static int PACKED_DIRT = 2;		// 	soil_packed_dirt = 2,
    public final static int SOFT_SAND = 3;		// 	soil_soft_sand = 3,
    public final static int DEEP_WATER = 4;		// 	soil_deep_water = 4,
    public final static int SHALLOW_WATER = 5;		// 	soil_shallow_water = 5,
    public final static int MUD = 6;			// 	soil_mud = 6,
    public final static int MUDDY_ROAD = 7;		// 	soil_muddy_road = 7,
    public final static int ICE = 8;			// 	soil_ice = 8,
    public final static int SWAMP = 9;			// 	soil_swamp = 9,
    public final static int CANOPY_FOREST = 10;		// 	soil_canopy_Forest = 10,
    public final static int US_RAILROAD = 11;		// 	soil_US_railroad = 11,
    public final static int EURO_RAILROAD = 12;		// 	soil_Euro_railroad = 12,
    public final static int SMALL_ROCKS = 13;		// 	soil_small_rocks = 13,
    public final static int BOULDERS = 14; 		// 	soil_boulders = 14,
    public final static int NO_GO = 15;			// 	soil_no_go = 15

    public static int[] getSoilColors() {
	int[] colorRGBs = new int[17];
// 	if(Flags.UNIT_SIZES_PRINTABLE_COLORS) {
// 	    colorRGBs[0] = Color.white.getRGB();    // 	soil_unknown = 0,
// 	    colorRGBs[1] = Color.red.getRGB();    // 	soil_asphalt = 1,
// 	    colorRGBs[2] = Color.white.getRGB();    // 	soil_packed_dirt = 2,
// 	    colorRGBs[3] = RGB.sandyBrown.getRGB();    // 	soil_soft_sand = 3,
// 	    colorRGBs[4] = RGB.deepSkyBlue.getRGB();    // 	soil_deep_water = 4,
// 	    colorRGBs[5] = RGB.lightSkyBlue1.getRGB();    // 	soil_shallow_water = 5,
// 	    colorRGBs[6] = RGB.tan4.getRGB();    // 	soil_mud = 6,
// 	    colorRGBs[7] = RGB.pink.getRGB();    // 	soil_muddy_road = 7,
// 	    colorRGBs[8] = Color.white.getRGB();    // 	soil_ice = 8,
// 	    colorRGBs[9] = RGB.darkGreen.getRGB();    // 	soil_swamp = 9,
// 	    colorRGBs[10] = RGB.forestGreen.getRGB();    // 	soil_canopy_Forest = 10,
// 	    colorRGBs[11] = RGB.darkOrange.getRGB();    // 	soil_US_railroad = 11,
// 	    colorRGBs[12] = RGB.darkOrange.getRGB();    // 	soil_Euro_railroad = 12,
// 	    colorRGBs[13] = RGB.gold3.getRGB();    // 	soil_small_rocks = 13,
// 	    colorRGBs[14] = RGB.white.getRGB();    // 	soil_boulders = 14
// 	    colorRGBs[15] = RGB.firebrick.getRGB();    //  soil_no_go = 15
// 	}
// 	else if(Flags.PAUL_COLORS) {
	    colorRGBs[0] = Color.white.getRGB();    // 	soil_unknown = 0,
	    colorRGBs[1] = Color.red.getRGB();    // 	soil_asphalt = 1,
	    colorRGBs[2] = Color.white.getRGB();    // 	soil_packed_dirt = 2,
	    colorRGBs[3] = RGB.sandyBrown.getRGB();    // 	soil_soft_sand = 3,
	    colorRGBs[4] = RGB.deepSkyBlue.getRGB();    // 	soil_deep_water = 4,
	    colorRGBs[5] = RGB.white.getRGB();    // 	soil_shallow_water = 5,
	    colorRGBs[6] = RGB.tan4.getRGB();    // 	soil_mud = 6,
	    colorRGBs[7] = RGB.pink.getRGB();    // 	soil_muddy_road = 7,
	    colorRGBs[8] = Color.white.getRGB();    // 	soil_ice = 8,
	    colorRGBs[9] = RGB.darkGreen.getRGB();    // 	soil_swamp = 9,
	    colorRGBs[10] = RGB.forestGreen.getRGB();    // 	soil_canopy_Forest = 10,
	    colorRGBs[11] = RGB.darkOrange.getRGB();    // 	soil_US_railroad = 11,
	    colorRGBs[12] = RGB.darkOrange.getRGB();    // 	soil_Euro_railroad = 12,
	    colorRGBs[13] = RGB.gold3.getRGB();    // 	soil_small_rocks = 13,
	    colorRGBs[14] = RGB.white.getRGB();    // 	soil_boulders = 14
	    colorRGBs[15] = RGB.firebrick.getRGB();    //  soil_no_go = 15
// 	}
// 	else {
// 	    colorRGBs[0] = Color.darkGray.getRGB();    // 	soil_unknown = 0,
// 	    colorRGBs[1] = Color.red.getRGB();    // 	soil_asphalt = 1,
// 	    colorRGBs[2] = Color.lightGray.getRGB();    // 	soil_packed_dirt = 2,
// 	    colorRGBs[3] = RGB.sandyBrown.getRGB();    // 	soil_soft_sand = 3,
// 	    colorRGBs[4] = RGB.deepSkyBlue.getRGB();    // 	soil_deep_water = 4,
// 	    colorRGBs[5] = RGB.shallowWaterBlue.getRGB();    // 	soil_shallow_water = 5,
// 	    colorRGBs[6] = RGB.tan4.getRGB();    // 	soil_mud = 6,
// 	    colorRGBs[7] = RGB.saddleBrown.getRGB();    // 	soil_muddy_road = 7,
// 	    colorRGBs[8] = Color.white.getRGB();    // 	soil_ice = 8,
// 	    colorRGBs[9] = RGB.darkGreen.getRGB();    // 	soil_swamp = 9,
// 	    colorRGBs[10] = RGB.forestGreen.getRGB();    // 	soil_canopy_Forest = 10,
// 	    colorRGBs[11] = RGB.darkOrange.getRGB();    // 	soil_US_railroad = 11,
// 	    colorRGBs[12] = RGB.darkOrange.getRGB();    // 	soil_Euro_railroad = 12,
// 	    colorRGBs[13] = RGB.gold3.getRGB();    // 	soil_small_rocks = 13,
// 	    colorRGBs[14] = RGB.lightGray.getRGB();    // 	soil_boulders = 14, - note, OTBSAF displays packed dirt AND boulders both as light gray.
// 	    colorRGBs[15] = RGB.firebrick.getRGB();    //  soil_no_go = 15
// 	}
	//  This should never be used - its here so if somehow the
	//  soil file has weird values > 15 in it, we'll know from the
	//  GUI.  The color here should be recognizable and different
	//  than anything else, so it stands out.
	colorRGBs[16] = Color.magenta.getRGB();
	return colorRGBs;
    }

}
