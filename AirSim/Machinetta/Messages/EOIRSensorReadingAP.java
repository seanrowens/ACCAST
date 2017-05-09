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
 * EOIRSensorReadingAP.java
 *
 * Created on April 12, 2006, 11:56 AM
 *
 */

package AirSim.Machinetta.Messages;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;

import java.awt.image.BufferedImage;

/**
 *
 * @author pscerri
 */
public class EOIRSensorReadingAP extends RPMessage {
    
    public String sensorID = "Unknown";
    
    public byte [] imgData = null;
    
    // NOTE: These fields are for the 'simulated user' to use in
    // deciding what plans to issue.
    public ForceId forceId;
    public State state;
    public Asset.Types type;

    /** Creates a new instance of EOIRSensorReadingAP */
    public EOIRSensorReadingAP(byte [] img) {
        super(RPMessage.MessageTypes.EOIR_SENSOR_READING);
        imgData = img;
    }
    
    static final long serialVersionUID = 1L;
}