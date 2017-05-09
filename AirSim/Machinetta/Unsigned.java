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

public class Unsigned {
    public static int readLittleUnsignedByteToInt(byte b0) {
	return 0x000000FF & ((int)b0);
    }
    public static int readLittleUnsignedShortToInt(byte b0, byte b1) {
	return (0x000000FF & ((int)b0) ) 
	    | ((0x000000FF & ((int)b1) )<<8);
    }
    public static long readLittleUnsignedIntToLong(byte b0, byte b1, byte b2, byte b3) {
	return (0x000000FF & ((int)b0) ) 
	    | ((0x000000FF & ((int)b1) )<<8) 
	    | ((0x000000FF & b2)<<16) 
	    | ((0x000000FF & b3)<<24);
    }

    public static short readLittleShort(byte b0, byte b1) {
	return (short)((0x000000FF & ((int)b0) ) 
		       | ((0x000000FF & ((int)b1) )<<8));
    }

    public static int readLittleInt(byte b0, byte b1, byte b2, byte b3) {
	return (0x000000FF & ((int)b0) ) 
	    | ((0x000000FF & ((int)b1) )<<8) 
	    | ((0x000000FF & ((int)b2) )<<16) 
	    | ((0x000000FF & ((int)b3) )<<24);
    }

    public static long readLittleLong(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
	return (0x000000FF & ((int)b0) ) 
	    | ((0x000000FF & ((int)b1) )<<8) 
	    | ((0x000000FF & ((int)b2) )<<16) 
	    | ((0x000000FF & ((int)b3) )<<24) 
	    | ((0x000000FF & ((int)b4) )<<32) 
	    | ((0x000000FF & ((int)b5) )<<40) 
	    | ((0x000000FF & ((int)b6) )<<48) 
	    | ((0x000000FF & ((int)b7) )<<56);
    }

    public static float readLittleFloat(byte b0, byte b1, byte b2, byte b3) {
	return Float.intBitsToFloat((0x000000FF & ((int)b0) ) 
				    | ((0x000000FF & ((int)b1) )<<8) 
				    | ((0x000000FF & ((int)b2) )<<16) 
				    | ((0x000000FF & ((int)b3) )<<24));
    }

    public static double readLittleDouble(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
	return Double.longBitsToDouble((0x000000FF & ((int)b0) ) 
				       | ((0x000000FF & ((int)b1) )<<8) 
				       | ((0x000000FF & ((int)b2) )<<16) 
				       | ((0x000000FF & ((int)b3) )<<24) 
				       | ((0x000000FF & ((int)b4) )<<32) 
				       | ((0x000000FF & ((int)b5) )<<40) 
				       | ((0x000000FF & ((int)b6) )<<48) 
				       | ((0x000000FF & ((int)b7) )<<56));
    }

    public static int readBigUnsignedByteToInt(byte b0) {
	return 0x000000FF & ((int)b0) ;
    }
    public static int readBigUnsignedShortToInt(byte b0, byte b1) {
	return (0x000000FF & ((int)b1) ) | ((0x000000FF & ((int)b0) )<<8);
    }
    public static long readBigUnsignedIntToLong(byte b0, byte b1, byte b2, byte b3) {
	return (0x000000FF & ((int)b3) ) 
	    | ((0x000000FF & ((int)b2) )<<8) 
	    | ((0x000000FF & ((int)b1) )<<16) 
	    | ((0x000000FF & ((int)b0) )<<24);
    }

    public static short readBigShort(byte b0, byte b1) {
	return (short)((0x000000FF & ((int)b1) ) | ((0x000000FF & ((int)b0) )<<8));
    }

    public static int readBigInt(byte b0, byte b1, byte b2, byte b3) {
	return (0x000000FF & ((int)b3) ) 
	    | ((0x000000FF & ((int)b2) )<<8) 
	    | ((0x000000FF & ((int)b1) )<<16) 
	    | ((0x000000FF & ((int)b0) )<<24);
    }

    public static long readBigLong(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
	return (0x000000FF & ((int)b7) ) 
	    | ((0x000000FF & ((int)b6) )<<8) 
	    | ((0x000000FF & ((int)b5) )<<16) 
	    | ((0x000000FF & ((int)b4) )<<24) 
	    | ((0x000000FF & ((int)b3) )<<32) 
	    | ((0x000000FF & ((int)b2) )<<40) 
	    | ((0x000000FF & ((int)b1) )<<48) 
	    | ((0x000000FF & ((int)b0) )<<56);
    }

    public static float readBigFloat(byte b0, byte b1, byte b2, byte b3) {
	return Float.intBitsToFloat((0x000000FF & ((int)b3) ) 
				    | ((0x000000FF & ((int)b2) )<<8) 
				    | ((0x000000FF & ((int)b1) )<<16) 
				    | ((0x000000FF & ((int)b0) )<<24));
    }

    public static double readBigDouble(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
	return Double.longBitsToDouble((0x000000FF & ((int)b7) ) 
				       | ((0x000000FF & ((int)b6) )<<8) 
				       | ((0x000000FF & ((int)b5) )<<16) 
				       | ((0x000000FF & ((int)b4) )<<24) 
				       | ((0x000000FF & ((int)b3) )<<32) 
				       | ((0x000000FF & ((int)b2) )<<40) 
				       | ((0x000000FF & ((int)b1) )<<48) 
				       | ((0x000000FF & ((int)b0) )<<56));
    }
}
