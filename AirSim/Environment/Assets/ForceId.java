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
package AirSim.Environment.Assets;

// @author      Sean R. Owens
// @version     $Id: ForceId.java,v 1.1 2009/02/06 01:15:52 owens Exp $

public enum ForceId {

    UNKNOWN("Unknown","Unk", "U"),
	BLUEFOR("BlueFor","Blue","B"),
	OPFOR("OpFor","Op","O"),
	NEUTRAL("Neutral","Neutral","N")
	;


    private String name;
    private String abbr;
    private String singleLetter;
    ForceId(String n, String abbr, String singleLetter) { 
	this.name = n;
	this.abbr = abbr;
	this.singleLetter = singleLetter;
    }
    public String toString() { return this.name; }
    public String abbr() { return this.abbr; }
    public String singleLetter() { return this.singleLetter; }
    static public ForceId parse(String forceString) {
	if(forceString.equalsIgnoreCase(BLUEFOR.name))
	    return BLUEFOR;
	if(forceString.equalsIgnoreCase(OPFOR.name))
	    return OPFOR;
	if(forceString.equalsIgnoreCase(NEUTRAL.name))
	    return NEUTRAL;
	return UNKNOWN;
    }
}
