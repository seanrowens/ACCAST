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
package Util.KMeans;

/* Copyright Randall Scarberry, JavaWorld.com, 11/21/06
 * http://www.javaworld.com/javaworld/jw-11-2006/jw-1121-thread.html
 */
/**
 * Class to represent a cluster of coordinates.
 */
public class Cluster {

    // Indices of the member coordinates.
    private int[] mMemberIndexes;
    // The cluster center.
    private double[] mCenter;
    
    /**
     * Constructor.
     * 
     * @param memberIndexes indices of the member coordinates.
     * @param center the cluster center.
     */
    public Cluster(int[] memberIndexes, double[] center) {
        mMemberIndexes = memberIndexes;
        mCenter = center;
    }
    
    /**
     * Get the member indices.
     * 
     * @return an array containing the indices of the member coordinates.
     */
    public int[] getMemberIndexes() {
        return mMemberIndexes;
    }
    
    /**
     * Get the cluster center.
     * 
     * @return a reference to the cluster center array.
     */
    public double[] getCenter() {
        return mCenter;
    }
    
}
