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
 * MatchableBelief.java
 *
 * Created on September 19, 2002, 5:41 PM
 */

package Machinetta.State.BeliefType.Match;

import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefID;
import java.util.Vector;
/**
 *
 * @author  pynadath
 */
public class MatchableBelief {
    
    /** Generates a belief represenation according to the specified template
     * @param keys An array of attribute keys that the belief should fill in when generating the string
     * @return A string with the specified keys filled in
     */
    public static String matchString(Belief belief,Vector keys) {
        if (keys.contains("class"))
            return belief.getClass().getName();
        else
            return "";
    }
    
    /** Determines whether this belief matches the specified template
     * @param matchStr A string that this belief should match upon extraction of the specified keys
     * @param keys An array of attribute keys that the belief should fill in when generating the string
     * @return True iff the belief matches the specified string over the specified keys
     */
    public static boolean matches(String matchStr, Matchable belief,Vector keys) {
        String beliefStr = belief.matchString(keys);
        Machinetta.Debugger.debug(0,"Matching: "+beliefStr);
        Machinetta.Debugger.debug(0," against: "+matchStr);
        return matchStr.equalsIgnoreCase(beliefStr);
    }
}

