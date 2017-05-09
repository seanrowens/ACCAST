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
 * CameraCommandPA.java
 *
 * Created on January 25, 2006, 3:37 PM
 *
 */

package AirSim.Machinetta.Messages;

/**
 * Command from UAV proxy to UAV agent requesting that camera
 * be configured in a particular way.
 *
 * Note in slides to consider gimballed cameras later.
 *
 * @author pscerri
 */
public class CameraCommandPA extends PRMessage {
  
    public double 
            latitude = -Double.MAX_VALUE, 
            longtitude = -Double.MAX_VALUE, 
            altitude = -Double.MAX_VALUE, 
            zoom = -Double.MAX_VALUE;
    
    public boolean on = false;
    
    /** Assuming that typically these will be XOR, but not enforced */
    public boolean EO = false, IR = false;
       
    /** Creates a new instance of CameraCommandPA */
    public CameraCommandPA() {
        super(MessageType.CAMERA_COMMAND);
    }
    
    static final long serialVersionUID = 1L;
    
}
