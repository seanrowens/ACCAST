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
// @version     $Id: UnitTypes.java,v 1.3 2007/12/04 22:51:32 junyounk Exp $ 

import javax.swing.ImageIcon;
import java.awt.Image;

public class UnitTypes {

    // Note, the integer values assigned here correspond to positions
    // in arrays of strings below, so if any of these change, make
    // sure they correspond correctly.

    public final static int UNKNOWN = 0;
    public final static int ARMOR = 1;
    public final static int LIGHT_INFANTRY = 2;
    public final static int MECH_INFANTRY = 3;
    public final static int LIGHT_FIELD_ARTILLERY = 4;
    public final static int MECH_FIELD_ARTILLERY = 5;
    public final static int MILITARY_POLICE = 6;
    public final static int AIR_DEFENSE_ARTILLERY = 7;
    public final static int TACTICAL_OPERATION_CENTER = 8;
    public final static int ADMIN_LOGISTICS_OPERATIONS_CENTER = 9;
    public final static int FORWARD_SUPPORT_BATTALION = 10;
    public final static int MAIN_SUPPORT_BATTALION = 11;
    public final static int AIR_FORCES = 12;
    public final static int MISSILE = 13;
    public final static int SA_MISSILE = 14;
    public final static int SS_MISSILE = 15;
    public final static int WASM = 16;
    public final static int MUAV = 17;
    public final static int CIVILIAN = 18;
    public final static int CIVILIAN_TRUCK = 19;
    public final static int MILITARY_TRUCK = 20;
    public final static int CLUTTER = 21;
    public final static int SENSOR = 22;
    public final static int EMITTER = 23;
    public final static int BUILDING = 24;
    public final static int TERMINAL = 25;
    public final static int MAX_TYPE = 25;
    
    private static ImageIcon[] imageIcons = new ImageIcon[MAX_TYPE+1];
    public static ImageIcon getImageIcon(int type) {
	if(type > MAX_TYPE)
	    return null;
	return imageIcons[type];
    }
    
    public static void setImageIcon(int type, ImageIcon imageIcon) {
	if(type > MAX_TYPE)
	    return;
	imageIcons[type] = imageIcon;
    }

    public final static boolean isAir(int type) {
	return ((AIR_FORCES == type)
		|| (MISSILE == type)
		|| (SA_MISSILE == type)
		|| (SS_MISSILE == type)
		|| (WASM == type)
		|| (SENSOR == type)
		|| (MUAV == type)
		);
    }

    public final static String getName(int id) {
	if((id <= UNKNOWN) || (id > MAX_TYPE))
	    return "ILLEGAL UNITTYPE="+id;
	else
	    return names[id];
    }
    public final static String getAbbr(int id) {
	if((id <= UNKNOWN) || (id > MAX_TYPE))
	    return "ILL "+id;
	else
	    return abbrs[id];
    }
    
    public final static String names[] = { "Unknown",
					   "Armor",
					   "Light Infantry",
					   "Mech Infantry",
					   "Light Field Artillery",
					   "Mech Field Artillery",
					   "Military Police",
					   "Air Defense Artillery",
					   "Tactical Operation Center",
					   "Admin Logistics Operations Center",
					   "Forward Support Battalion",
					   "Main Support Battalion",
					   "Air Forces",
					   "Missile",
					   "SA Missile",
					   "SS Missile",
					   "WASM",
					   "MUAV",
					   "CIVILIAN",
					   "CIVILIAN TRUCK",
					   "MILITARY TRUCK",
					   "CLUTTER",
                                           "SENSOR",
                                           "EMITTER",
                                           "BUILDING",
                                           "TERMINAL"
    };


    public final static String abbrs[] = {"Unk",
					  "Arm",
					  "Light Inf",
					  "Mech Inf",
					  "Light FA",
					  "Mech FA",
					  "MPs",
					  "ADA",
					  "TOC",
					  "ALOC",
					  "FSB",
					  "MSB",
					   "AF",
					   "M",
					  "SAM",
					  "SSM",
					  "WASM",
					  "MUAV",
					  "CIV",
					  "CIV TRK",
					  "MIL TRK",
					  "CLTTR",
                                          "SNSR",
                                          "EMTTR",
                                          "BD",
                                          "TM"
    };
                        
    public static int getUnitType(String unitTypeName) {
	// lifeForm_USSR_DIGroup6_MgGndRfl
	// vehicle_USSR_BMP2
	// vehicle_USSR_BTR80
	// vehicle_USSR_SA_6_FCR
	// vehicle_USSR_SA_6_TEL
	// vehicle_USSR_T72M
	// vehicle_USSR_T80
	// vehicle_USSR_XM375S
	// vehicle_USSR_XMG1S
	// vehicle_USSR_XMLTS
	// vehicle_USSR_XMTSS
	// vehicle_USSR_ZSU23_4M
	// vehicle_US_F16C
	// vehicle_US_F16D
	// vehicle_US_A10

	int unitType = UnitTypes.UNKNOWN;

	if(unitTypeName.equals("lifeForm_USSR_DIGroup6_MgGndRfl")) {
	    unitType = UnitTypes.LIGHT_INFANTRY;
	}
	else if(unitTypeName.equals("lifeForm_USSR_DIGroup3_Ags17")) {
	    unitType = UnitTypes.LIGHT_INFANTRY;
	}
	else if(unitTypeName.equals("lifeForm_USSR_DIGroup2_Lfk5")) {
	    unitType = UnitTypes.LIGHT_INFANTRY;
	}
	else if(unitTypeName.equals("vehicle_USSR_BMP2")) {
	    unitType = UnitTypes.MECH_INFANTRY;
	}
	else if(unitTypeName.equals("vehicle_USSR_BMP1")) {
	    unitType = UnitTypes.MECH_INFANTRY;
	}
	else if(unitTypeName.equals("vehicle_USSR_BTR80")) {
	    unitType = UnitTypes.MECH_INFANTRY;
	}
	else if(unitTypeName.equals("US___M977_________")) {
	    unitType = UnitTypes.MECH_INFANTRY;
	}
	else if(unitTypeName.equals("USSR_T72M_________")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("USSR_T80__________")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("USSR_ZSU23_4M_____")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("USSR_2S6__________")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_US_M1")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("vehicle_US_M1A1")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("vehicle_US_M1A2")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("US___M1___________")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("US___M1A1_________")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("US___M1A2_________")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("US___M35__________")) {
	    unitType = UnitTypes.MECH_INFANTRY;
	}
	else if(unitTypeName.equals("vehicle_USSR_SA_6_FCR")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_SA_6_TEL")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_T72M")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("vehicle_USSR_T80")) {
	    unitType = UnitTypes.ARMOR;
	}
	else if(unitTypeName.equals("vehicle_USSR_XM375S")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_XMG1S")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_XMLTS")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_XMTSS")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_ZSU23_4M")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_ZSU23_4M")) {
	    unitType = UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(unitTypeName.equals("vehicle_USSR_MIG29")) {
	    unitType = UnitTypes.AIR_FORCES;
	}
	else if(unitTypeName.equals("vehicle_US_F16C")) {
	    unitType = UnitTypes.AIR_FORCES;
	}
	else if(unitTypeName.equals("vehicle_US_F16D")) {
	    unitType = UnitTypes.AIR_FORCES;
	}
	else if(unitTypeName.equals("vehicle_US_A10")) {
	    unitType = UnitTypes.AIR_FORCES;
	}
	else if(unitTypeName.equals("munition_US_Maverick")) {
	    unitType = UnitTypes.MISSILE;
	}
	else if(unitTypeName.startsWith("lifeForm_")) {
	    unitType = UnitTypes.LIGHT_INFANTRY;
	}
	else if(unitTypeName.equals("unknown")) {
	    unitType = UnitTypes.UNKNOWN;
	}
	else {
	    Debug.info("UnitTypes.getUnitType: Unknown unitTypeName="+unitTypeName);
	}

	return unitType;
    }

    public static String trimUnitTypeName(String unitTypeName) {
	if(unitTypeName.startsWith("vehicle_")) {
	    unitTypeName = unitTypeName.substring("vehicle_".length());
	}
	if(unitTypeName.startsWith("lifeForm_")) {
	    unitTypeName = unitTypeName.substring("vehicle_".length());
	}
	if(unitTypeName.startsWith("USSR_")) {
	    unitTypeName = unitTypeName.substring("USSR_".length());
	}
	if(unitTypeName.startsWith("munition_")) {
	    unitTypeName = unitTypeName.substring("munition_".length());
	}
	while(unitTypeName.endsWith("_")) {
	    unitTypeName = unitTypeName.substring(0, unitTypeName.length() - 1);
	}
	return unitTypeName;
    }
    


}
