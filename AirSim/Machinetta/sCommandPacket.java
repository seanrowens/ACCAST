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
/*
 * sCommandPacket.java
 *
 * Created on August 23, 2007, 2:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package AirSim.Machinetta;

/**
 *
 * @author Owner
 */
public class sCommandPacket {
    
    char DestAddr; //Airplane Address the command belongs to
    short CommandType;		//Command Type
    float Latitude;			//Latitude of point (not relevant for goto types)
    float Longitude;		//Longitude of point
    float Speed;			//Speed in m/s
    float Altitude;			//Alt in meters
    char GotoIndex; //Only used in goto type
    char Time;	//Time used for loiter timeout (0 = indefinite)
    float Radius;			//Radius in meters, of loiter, takeoff, approach
    float FlareSpeed;		//Approach Land Only - Flare Speed
    float FlareAltitude;	//Approach Land Only - Flaring Altitude
    float BreakAltitude;	//Approach Land Only - Altitude used when breaking out of approach
    float DescentRate;		//Approach Land Only - Descent rate in spiral
    float ApproachLat;		//Approach Land Only - Approach circle latitude
    float ApproachLong;		//Approach Land Only - Approach circle longitude
    int FutureUseInt;		//Potential Future Use Int (i.e. Payload Command)
    float FutureUseFloat;	//Potential Future Use Int (i.e. Payload Command)
    
    public sCommandPacket() {
    }
    
}
