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
 * PRMessage.java
 *
 * Created on June 16, 2005, 2:57 PM
 *
 */

package AirSim.Machinetta.Messages;

/**
 * 
 * @author pscerri
 */
public class PRMessage extends Machinetta.RAPInterface.OutputMessages.OutputMessage {
    
    public enum MessageType { 
        AUTHORIZATION_REQUEST,
        CAMERA_COMMAND,
        CHECK_REPEAT,
        DETECT_DATA,
        GEOLOCATE_DATA,
        GEOLOCATE_SENSOR,
        GEOLOCATE_INFORM,
        GEOLOCATE,        
        NEW_WAYPOINT,
        REQUEST_LOCATION,
        SEARCH,
        STATUS_INFORM,
        TELE_OP,
        UAV_LOCATION_INFORM,
        TRANSPORT,
	DEPLOY,
	AIRDROP,
        MOUNT,
        DISMOUNT,
        DEFEND,
        DIRECTFIRE,
        DETONATE,
        MOVE,
        FAARP_LOCATION_INFORM,
        ASK_REFUEL,
        LAND,
        LAUNCH,
        CIRCLE,
        ACTIVATE,
        DEACTIVATE,
	    NEW_TASK,
	    PATROL,
	    ISR
    };
    public MessageType type;
    
    public java.util.HashMap<String,Object> params = new java.util.HashMap<String,Object>();
    
    /** Creates a new instance of PRMessage */
    public PRMessage(MessageType type) {
        this.type = type;
    }
    
    public PRMessage(MessageType type, AirSim.Environment.Assets.Tasks.TaskType task) {
        this(type);
        if (type != MessageType.NEW_TASK) {
            Machinetta.Debugger.debug("Using wrong constructor for PRMessage!", 3, this);            
        }
        params.put("Task", task);
    }
    
    private static final long serialVersionUID = 1L;
    
    public String toString() { return "PRMessage: " + type + " with params : " + params; }
}
