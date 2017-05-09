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
// @version     $Id: ForceIds.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.awt.*;

public class ForceIds {

    // Note, the integer values assigned here correspond to positions
    // in arrays of strings below, so if any of these change, make
    // sure they correspond correctly.

    public final static int UNKNOWN = 0;
    public final static int BLUEFOR = 1;
    public final static int OPFOR = 2;
    public final static int NEUTRAL = 3;
    
    public final static Color COLOR_UNKNOWN = Color.white;
    // dogerBlue is slightly darker than deepskyblue.  Cornflowerblue
    // is another good candidate, royalblue is darker yet.
    public final static Color COLOR_BLUEFOR = RGB.royalBlue;
    //    public final static Color COLOR_BLUEFOR = RGB.blue; // //

    // @TODO: Mon Feb 20 22:48:09 EST 2006 SRO
    //
    // Paul wants the opfor color set back to red.
    //
    // crimson is darker than red, firebrick is darker sorta, kinda
    // off, alizarin Crimson is slightly brigher than crimson.
    // Scarlet is red with a little green added.
    //
    //    public final static Color COLOR_OPFOR = Color.red;
    public final static Color COLOR_OPFOR = RGB.alizarinCrimson;

    public final static Color COLOR_NEUTRAL = Color.green;

    public final static String names[] = { "Unknown",
					   "BlueFor",
					   "OpFor",
					   "Neutral"
    };

    public final static String abbrs[] = {"Unk",
					  "Blue",
					  "Op",
					  "Neutral"
    };
    public final static String getName(int id) {
	if((id <= UNKNOWN) || (id > NEUTRAL))
	    return "ILLEGAL FORCEID="+id;
	else
	    return names[id];
    }
    public final static String getAbbr(int id) {
	if((id <= UNKNOWN) || (id > NEUTRAL))
	    return "ILL "+id;
	else
	    return abbrs[id];
    }
}
