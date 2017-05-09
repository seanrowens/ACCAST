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
 * Task.java
 *
 * Created on Wed Aug  3 18:04:14 EDT 2005
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Assets.*;

public abstract class Task {
    TaskType type = TaskType.noTask;
    // Asset asset = null;
    //    public boolean setAsset(Asset value) { this.asset = value);
    
    /**
     * IMPORTANT: Implementations of step should not actually move the asset
     * only set the heading of the asset (as well as fire etc.)  The actual
     * movement is handled separately for speed sake
     *
     * The trouble with this particular design is that it is hard/impossible
     * for an asset to simultaneously execute mulitiple tasks, but in general
     * this would be reasonable (and nice, structurally).  
     *
     * For now, I propose that this method is called when the Asset has only the
     * one task and we keep thinking how to execute mulitple.
     *
     * @todo Allow execution of multiple tasks, not high priority.
     *
     * @parameter a The asset to be moved.  The object can assume it will be repeatedly
     * called by only the same object
     * @parameter time The length of time of this step.  This is used so we can speed the 
     * simulation up.  
     */
    public abstract void step(Asset a, long time);
    public abstract boolean finished(Asset a);
}
