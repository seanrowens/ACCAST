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
package AirSim.Machinetta;
import java.io.*;

public class DecodedPacketLogger {
    private PrintWriter logWriter = null;

    public DecodedPacketLogger(String logFileName) {
	String textLogFileName = logFileName + ".text" ;
	try {
	    logWriter = new PrintWriter(new FileOutputStream(textLogFileName));
	} catch (java.io.FileNotFoundException e) {
	    Machinetta.Debugger.debug( 3,"ERROR: Problems opening vcbridge packet log file='"+textLogFileName+"': "+e);
	    e.printStackTrace();
	}
    }

    long lastPacketTime = 0;

    final static int PASS_THROUGH_OFFSET = 8;
    final static int OFF_TYPE = 1 + PASS_THROUGH_OFFSET;
    public void decodePacket(long packetTime, byte[] buf, int length) {
	if(0 != lastPacketTime) {
	    logWriter.println("packet len="+length+" time="+packetTime+ " since last packet="+(packetTime-lastPacketTime));
	    System.out.println("packet len="+length+" time="+packetTime+ " since last packet="+(packetTime-lastPacketTime));
	}
	lastPacketTime = packetTime;

	int devIntType = Unsigned.readLittleInt(buf[0],buf[1],buf[2],buf[3]);
	if(devIntType != SocketConnector.VC_PASSTHROUGH) {
	    System.out.println("        DeveloperInterfaceType of packet is not 10 (passthrough) - type="+devIntType);
	    return;
	}


	try {
	    int packetType = 0x000000FF & ((int)buf[OFF_TYPE]);
	    if (248 == packetType) {
		DecodedPacket248 dp = new DecodedPacket248(packetTime, length) ;
		dp.decodeBuffer(buf) ;
		logWriter.println(dp.toString2()) ;
		logWriter.flush();
	    }
	    else if (249 == packetType) {
		DecodedPacket249 dp = new DecodedPacket249(packetTime, length) ;
		dp.decodeBuffer(buf) ;
		logWriter.println(dp.toString2()) ;
		logWriter.flush();
	    }
	    else {
		logWriter.println("Unknown packet type="+packetType);
		logWriter.flush();
	    }
	}
	catch(NetByteDecodingException e) {
	    System.out.println("Exception decoding fields of packet, e="+e);
	    e.printStackTrace();
	}
    }
   
}
