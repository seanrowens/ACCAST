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
// @version     $Id: Roads.java,v 1.1 2007/11/30 23:15:59 owens Exp $ 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.georgeandabe.tigerline.model.TLData;
import com.georgeandabe.tigerline.model.TLParser;


public class Roads {
    public static TLData load(TLData data, String roadFullPathName) {
	if(null == roadFullPathName)
	    return data;
	if(null == data) {
	    data = new TLData();
	}
	DebugInterface debug = new DebugFacade("Roads");
        debug.info("      ***Road data parsing!!!"+roadFullPathName);
	long timeStart = System.currentTimeMillis();
        File roadFile = new File(roadFullPathName);
	TLParser parser = new TLParser();
        try {
            parser.parse(new FileInputStream(roadFile), data);
        } catch (FileNotFoundException ex) {
	    debug.error("FileNotFoundException parsing TigerLine file='"+roadFullPathName+"', e="+ex);
            ex.printStackTrace();
        } catch (IOException ex) {
	    debug.error("IOException parsing TigerLine file='"+roadFullPathName+"', e="+ex);
            ex.printStackTrace();
        }
	long timeEnd = System.currentTimeMillis();
	long timeElapsed = timeEnd - timeStart;
        debug.info("      ***Done Road data parsing, elapsed="+timeElapsed);
	return data;
    }
    public static TLData load(TLData roadData, ArrayList<String> roadFileNames) {
	if(null == roadFileNames)
	    return roadData;
	if(0 == roadFileNames.size())
	    return roadData;
	for(int loopi = 0; loopi < roadFileNames.size(); loopi++) {
	    roadData = load(roadData, roadFileNames.get(loopi));
	}
	return roadData;
    }
}
