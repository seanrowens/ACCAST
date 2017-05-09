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
// @version     $Id: UnitSizes.java,v 1.3 2006/09/19 00:18:40 owens Exp $ 

import java.awt.Color;

public class UnitSizes {

    // Note, the integer values assigned here correspond to positions
    // in arrays of strings below, so if any of these change, make
    // sure they correspond correctly.

    public final static int UNKNOWN = 0;
    public final static int SINGLE = 1;
    public final static int PLATOON = 2;
    public final static int COMPANY_OR_BATTERY = 3;
    public final static int COMPANY = 4;
    public final static int BATTERY = 5;
    public final static int BATTALION = 6;
    public final static int BRIGADE = 7;
    
    public final static Color UNKNOWN_COLOR = Color.red;
    public final static Color PLATOON_COLOR = Color.yellow;
    public final static Color COMPANY_COLOR = Color.black;
    public final static Color BATTALION_COLOR = Color.gray;
    public final static Color BRIGADE_COLOR = Color.magenta;
    public final static Color DIVISION_COLOR = Color.green;
    public final static Color LARGER_THAN_DIVISION_COLOR = Color.black;

    public final static String getName(int id) {
	if((id <= UNKNOWN) || (id > BRIGADE))
	    return "ILLEGAL UNITSIZE="+id;
	else
	    return names[id];
    }
    public final static String getAbbr(int id) {
	if((id <= UNKNOWN) || (id > BRIGADE))
	    return "ILL "+id;
	else
	    return abbrs[id];
    }

    // from http://155.217.58.58/cgi-bin/atdl.dll/fm/34-130/Appb.htm
    //
    // Appendix B figure B-17, Typical widths of mobility corridors
    //
    // Division		6km
    // Brigade/Regiment	3km
    // Battalion		1.5km
    // Company			500m
    //
    // So we guess at platoon being 1/4 company 
    public final static double UNIT_SIZE_PLATOON = 125.0;
    public final static double UNIT_SIZE_COMPANY = 500.0;
    public final static double UNIT_SIZE_BATTALION = 1500.0;
    public final static double UNIT_SIZE_BRIGADE = 3000.0;
    public final static double UNIT_SIZE_DIVISION = 6000.0;

    public static int[] getUnitColors(int gridCellSize) {
	if(gridCellSize <= 0)
	    Debug.error("UnitSizes.getUnitColors: Can't create unit colors if grid cell size == 0");

	int[] colorRGBs  = null;
// 	if(Flags.UNIT_SIZES_PRINTABLE_COLORS) {
// 	    colorRGBs = new int[(int)(UNIT_SIZE_DIVISION/((double)gridCellSize))+20];
// 	    for(int loopi = 0; loopi < (int)((UNIT_SIZE_DIVISION/((double)gridCellSize))+20); loopi++) {
// 		if(loopi == 0) {
// 		    colorRGBs[loopi] = Color.getHSBColor((float)0.0, (float)1.0, (float).7).getRGB();
// 		}
// 		else if(loopi <= (UNIT_SIZE_PLATOON/gridCellSize)) {
// 		    colorRGBs[loopi] = Color.getHSBColor((float)0.166, (float)1.0, (float)0.8).getRGB();
// 		}
// 		else if(loopi <= (UNIT_SIZE_COMPANY/gridCellSize)) {
// 		    colorRGBs[loopi] = Color.getHSBColor((float)0.166, (float)1.0, (float)0.8).getRGB();
// 		}
// 		else if(loopi <= (UNIT_SIZE_BATTALION/gridCellSize)) {
// 		    colorRGBs[loopi] = Color.getHSBColor((float)0.0, (float)0.0, (float)1.0).getRGB();
// 		}
// 		else if(loopi <= (UNIT_SIZE_BRIGADE/gridCellSize)) {
// 		    colorRGBs[loopi] = Color.getHSBColor((float)0.833, (float)1.0, (float)1.0).getRGB();
// 		}
// 		else if(loopi <= (UNIT_SIZE_DIVISION/gridCellSize)) {
// 		    colorRGBs[loopi] = Color.getHSBColor((float).625, (float)1.0, (float)1.0).getRGB();
// 		}
// 		else {
// 		    colorRGBs[loopi] = Color.getHSBColor((float)1.0, (float)1.0, (float)1.0).getRGB();
// 		}
// 	    }
// 	}
// 	else {
	    colorRGBs = new int[(int)(UNIT_SIZE_DIVISION/((double)gridCellSize))+20];
	    for(int loopi = 0; loopi < (int)((UNIT_SIZE_DIVISION/((double)gridCellSize))+20); loopi++) {
		if(loopi == 0) {
		    colorRGBs[loopi] = UNKNOWN_COLOR.getRGB();
		}
		else if(loopi <= (UNIT_SIZE_PLATOON/gridCellSize)) {
		    colorRGBs[loopi] = PLATOON_COLOR.getRGB();
		}
		else if(loopi <= (UNIT_SIZE_COMPANY/gridCellSize)) {
		    colorRGBs[loopi] = COMPANY_COLOR.getRGB();
		}
		else if(loopi <= (UNIT_SIZE_BATTALION/gridCellSize)) {
		    colorRGBs[loopi] = BATTALION_COLOR.getRGB();
		}
		else if(loopi <= (UNIT_SIZE_BRIGADE/gridCellSize)) {
		    colorRGBs[loopi] = BRIGADE_COLOR.getRGB();
		}
		else if(loopi <= (UNIT_SIZE_DIVISION/gridCellSize)) {
		    colorRGBs[loopi] = DIVISION_COLOR.getRGB();
		}
		else {
		    colorRGBs[loopi] = LARGER_THAN_DIVISION_COLOR.getRGB();
		}
	    }
// 	}
// 	if(Flags.UNIT_SIZES_SHOW_OBSTACLES_ONLY) {
// 	    colorRGBs[0] = Color.red.getRGB();
// 	    for(int loopi = 1; loopi < (int)((UNIT_SIZE_DIVISION/((double)gridCellSize))+20); loopi++) {
// 		colorRGBs[loopi] = Color.lightGray.getRGB();
// 	    }
// 	}

	return colorRGBs;
    }

    public final static String names[] = {"Unknown",
					  "",
					  "Platoon",
					  "Company/Battery",
					  "Company",
					  "Battery",
					  "Battalion",
					  "Brigade"};
    public final static String abbrs[] = {"Unk",
					 "",
					 "PLT",
					 "CO/BTRY",
					 "CO",
					 "BTRY",
					 "BN",
					 "BDE"};

}
