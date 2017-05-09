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
package AirSim;

public class UnitIDs {

    // These prefix constants are used to create unit ids
    // (NamedProxyID) and also file names for .blf, .cfg, and log
    // files, as well as for in the Env.txt file and for various
    // prints to stdout/err.
    public static final String UAV ="UAV";
    public static final String RSSIUAV ="RSSI-UAV";
    public static final String EOIRUAV ="EOIR-UAV";
    public static final String AUAV ="AUAV";
    public static final String UGS ="UGS";
    public static final String HUMVEE ="H";
    public static final String DIBLUE ="DI-BLUE";
    public static final String DIOP ="DI-OP";
    public static final String IM ="IM";
    public static final String AUGV ="AUGV";
    public static final String C130 ="C130-";
    public static final String OPER ="Operator";
    public static final String A10OP ="A10OP";
    public static final String SA9 ="SA9-";
    // NOTE THAT SimUser is named "Operator" - this is because we have
    // some hard coded DIRs that send beliefs to Operator, and we need
    // these same beliefs to go to SimUser when we replace the
    // Operator GUI with the SimUser GUI.  The same goes for
    // FalconView.  When we generate configs, we should have only one
    // of these three proxies present.
    public static final String SIM_USER ="Operator";
    public static final String FV ="Operator";

}
