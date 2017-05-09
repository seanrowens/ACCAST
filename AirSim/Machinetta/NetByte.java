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
 * NetByte.java
 *
 * Created on August 30, 2007, 9:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package AirSim.Machinetta;

public class NetByte {

    /** Size of a <code>byte</code> (1). */
    public static final int byteSize_   = 1;
    /** Size of a <code>short</code> (2). */
    public static final int shortSize_  = 2;
    /** Size of a <code>char</code> (2). */
    public static final int charSize_   = 2;
    /** Size of an <code>int</code> (4). */
    public static final int intSize_    = 4;
    /** Size of a <code>long</code> (8). */
    public static final int longSize_   = 8;
    /** Size of a <code>float</code> (4). */
    public static final int floatSize_  = 4;
    /** Size of a <code>doublne</code> (8). */
    public static final int doubleSize_ = 8;


    /**
       Turns a <code>short</code> into a byte stream.

       @param s the value to encode.

       @return a <code>byte</code> array of size 2 with the value
       in network byte order, most-significant bit first.
    */
    public static byte[] encodeShort(short s)
    {
        byte[] b = new byte[2];
        b[0] = (byte) ((s & 0x0000ff00) >> 8);
        b[1] = (byte)  (s & 0x000000ff);
        return b;
    }

    /**
       Turns a <code>short</code> into a byte stream.

       @param s the value to encode.

       @param b the byte array in which to write the encoded value,
       with the value in network byte order, most-significant bit first.

       @param offset the offset at which to start writing in the byte array.

       @throws NetByteEncodingException if the byte array is too small.
    */
    public static void encodeShort(short s, byte[] b, int offset)
        throws NetByteEncodingException
    {
        if (badParams(b, offset, shortSize_)) {
            throw new NetByteEncodingException(eMsgs_[1]);
        }
        b[offset]     = (byte) ((s & 0x0000ff00) >> 8);
        b[offset + 1] = (byte)  (s & 0x000000ff);
    }


    /**
       Turns an encoding of 2 bytes into a <code>short</code> value.

       @param b the byte array in which contains the encoded value,
       with the value in network byte order, most-significant bit first.

       @return a <code>short</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static short decodeShort(byte[] b)
        throws NetByteDecodingException
    {
        if (badParams(b, 0, shortSize_)) {
            throw new NetByteDecodingException(eMsgs_[0]);
        }
        int s = 0;
        s = ((int) b[0] & 0x000000ff) << 8 |
            ((int) b[1] & 0x000000ff);
        return (short) (s & 0x0000ffff);
    }


    /**
       Turns an encoding of 2 bytes into a <code>short</code> value.

       @param b the byte array from which to read the encoded value.

       @param offset the offset at which to start reading from the byte array.

       @return a <code>short</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static short decodeShort(byte[] b, int offset)
        throws NetByteDecodingException
    {
        if (badParams(b, offset, shortSize_)) {
            throw new NetByteDecodingException(eMsgs_[1]);
        }
        int s = 0;
        s = ((int) b[offset]     & 0x000000ff) << 8 |
            ((int) b[offset + 1] & 0x000000ff);
        return (short) (s & 0x0000ffff);        
    }


    /**
       Turns a <code>char</code> into a byte stream.

       @param c the value to encode.

       @return a <code>byte</code> array of size 2 with the value
       in network byte order, most-significant bit first.
    */
    public static byte[] encodeChar(char c)
    {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0x0000ff00) >> 8);
        b[1] = (byte)  (c & 0x000000ff);
        return b;
    }


    /**
       Turns a <code>char</code> into a byte stream.

       @param c the value to encode.

       @param b the byte array in which to write the encoded value,
       with the value in network byte order, most-significant bit first.

       @param offset the offset at which to start writing in the byte array.

       @throws NetByteEncodingException if the byte array is too small.
    */
    public static void encodeChar(char c, byte[] b, int offset)
        throws NetByteEncodingException
    {
        if (badParams(b, offset, charSize_)) {
            throw new NetByteEncodingException(eMsgs_[1]);
        }
        b[offset]     = (byte) ((c & 0x0000ff00) >> 8);
        b[offset + 1] = (byte)  (c & 0x000000ff);        
    }


    /**
       Turns an encoding of 2 bytes into a <code>char</code> value.

       @param b the byte array in which contains the encoded value.

       @return a <code>char</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static char decodeChar(byte[] b)
        throws NetByteDecodingException
    {
        if (badParams(b, 0, charSize_)) {
            throw new NetByteDecodingException(eMsgs_[0]);
        }
        int c = 0;
        c = ((int) b[0] & 0x000000ff) << 8 |
            ((int) b[1] & 0x000000ff);
        return (char) (c & 0x0000ffff);
    }


    /**
       Turns an encoding of 2 bytes into a <code>char</code> value.

       @param b the byte array from which to read the encoded value.

       @param offset the offset at which to start reading from the byte array.

       @return a <code>short</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static char decodeChar(byte[] b, int offset)
        throws NetByteDecodingException
    {
        if (badParams(b, offset, charSize_)) {
            throw new NetByteDecodingException(eMsgs_[1]);
        }
        int c = 0;
        c = ((int) b[offset]     & 0x000000ff) << 8 |
            ((int) b[offset + 1] & 0x000000ff);
        return (char) (c & 0x0000ffff);
    }


    /**
       Turns an <code>int</code> into a byte stream.

       @param i the value to encode.

       @return a <code>byte</code> array of size 4 with the value
       in network byte order, most-significant bit first.
    */
    public static byte[] encodeInt(int i)
    {
        byte[] b = new byte[4];
        b[0] = (byte) ((i & 0xff000000) >> 24);
        b[1] = (byte) ((i & 0x00ff0000) >> 16);
        b[2] = (byte) ((i & 0x0000ff00) >>  8);
        b[3] = (byte)  (i & 0x000000ff);
        return b;
    }


    /**
       Turns an <code>int</code> into a byte stream.

       @param i the value to encode.

       @param b the byte array in which to write the encoded value,
       with the value in network byte order, most-significant bit first.

       @param offset the offset at which to start writing in the byte array.

       @throws NetByteEncodingException if the byte array is too small.
    */
    public static void encodeInt(int i, byte[] b, int offset)
        throws NetByteEncodingException
    {
        if (badParams(b, offset, intSize_)) {
            throw new NetByteEncodingException(eMsgs_[1]);
        }
        b[offset + 3]     = (byte) ((i & 0xff000000) >> 24);
        b[offset + 2] = (byte) ((i & 0x00ff0000) >> 16);
        b[offset + 1] = (byte) ((i & 0x0000ff00) >>  8);
        b[offset    ] = (byte)  (i & 0x000000ff);
    }


    /**
       Turns an encoding of 4 bytes into an <code>int</code> value.

       @param b the byte array in which contains the encoded value.

       @return a <code>int</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static int decodeInt(byte[] b)
        throws NetByteDecodingException
    {
        if (badParams(b, 0, intSize_)) {
            throw new NetByteDecodingException(eMsgs_[0]);
        }
        int i = 0;
        i = ((int) b[0] & 0x000000ff) << 24 |
            ((int) b[1] & 0x000000ff) << 16 |
            ((int) b[2] & 0x000000ff) <<  8 | 
            ((int) b[3] & 0x000000ff);
        return i;
    }


    /**
       Turns an encoding of 4 bytes into an <code>int</code> value.

       @param b the byte array from which to read the encoded value.

       @param offset the offset at which to start reading from the byte array.

       @return a <code>int</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static int decodeInt(byte[] b, int offset)
        throws NetByteDecodingException
    {
        if (badParams(b, offset, intSize_)) {
            throw new NetByteDecodingException(eMsgs_[1]);
        }
        int i = 0;
        //i = ((int) b[offset]     & 0x000000ff) << 24 |
          //  ((int) b[offset + 1] & 0x000000ff) << 16 |
            //((int) b[offset + 2] & 0x000000ff) <<  8 | 
            //((int) b[offset + 3] & 0x000000ff);
        i = ((int) b[offset+3]     & 0x000000ff) << 24 |
            ((int) b[offset + 2] & 0x000000ff) << 16 |
            ((int) b[offset + 1] & 0x000000ff) <<  8 | 
            ((int) b[offset] & 0x000000ff);
        return i;
    }


    /**
       Turns a <code>long</code> into a byte stream.

       @param l the value to encode.

       @return a <code>byte</code> array of size 8 with the value
       in network byte order, most-significant bit first.
    */
    public static byte[] encodeLong(long l)
    {
        byte[] b = new byte[8];
        b[0] = (byte) ((l & 0xff00000000000000L) >> 56);
        b[1] = (byte) ((l & 0x00ff000000000000L) >> 48);
        b[2] = (byte) ((l & 0x0000ff0000000000L) >> 40);
        b[3] = (byte) ((l & 0x000000ff00000000L) >> 32);
        b[4] = (byte) ((l & 0x00000000ff000000L) >> 24);
        b[5] = (byte) ((l & 0x0000000000ff0000L) >> 16);
        b[6] = (byte) ((l & 0x000000000000ff00L) >>  8);
        b[7] = (byte)  (l & 0x00000000000000ffL);
        return b;
    }

    /**
       Turns a <code>long</code> into a byte stream.

       @param l the value to encode.

       @param b the byte array in which to write the encoded value,
       with the value in network byte order, most-significant bit first.

       @param offset the offset at which to start writing in the byte array.

       @throws NetByteEncodingException if the byte array is too small.
    */
    public static void encodeLong(long l, byte[] b, int offset)
        throws NetByteEncodingException
    {
        if (badParams(b, offset, longSize_)) {
            throw new NetByteEncodingException(eMsgs_[1]);
        }
        b[offset]     = (byte) ((l & 0xff00000000000000L) >> 56);
        b[offset + 1] = (byte) ((l & 0x00ff000000000000L) >> 48);
        b[offset + 2] = (byte) ((l & 0x0000ff0000000000L) >> 40);
        b[offset + 3] = (byte) ((l & 0x000000ff00000000L) >> 32);
        b[offset + 4] = (byte) ((l & 0x00000000ff000000L) >> 24);
        b[offset + 5] = (byte) ((l & 0x0000000000ff0000L) >> 16);
        b[offset + 6] = (byte) ((l & 0x000000000000ff00L) >>  8);
        b[offset + 7] = (byte)  (l & 0x00000000000000ffL);
    }


    /**
       Turns an encoding of 8 bytes into a <code>long</code> value.

       @param b the byte array in which contains the encoded value.

       @return a <code>long</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static long decodeLong(byte[] b)
        throws NetByteDecodingException
    {
        if (badParams(b, 0, longSize_)) {
            throw new NetByteDecodingException(eMsgs_[0]);
        }
        long l = 0;
        l = ((long) b[0] & 0x00000000000000ffL) << 56 |
            ((long) b[1] & 0x00000000000000ffL) << 48 |
            ((long) b[2] & 0x00000000000000ffL) << 40 |
            ((long) b[3] & 0x00000000000000ffL) << 32 |
            ((long) b[4] & 0x00000000000000ffL) << 24 |
            ((long) b[5] & 0x00000000000000ffL) << 16 |
            ((long) b[6] & 0x00000000000000ffL) <<  8 |
            ((long) b[7] & 0x00000000000000ffL);
        return l;
    }


    /**
       Turns an encoding of 8 bytes into a <code>long</code> value.

       @param b the byte array from which to read the encoded value.

       @param offset the offset at which to start reading from the byte array.

       @return a <code>long</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static long decodeLong(byte[] b, int offset)
        throws NetByteDecodingException
    {
        if (badParams(b, offset, longSize_)) {
            throw new NetByteDecodingException(eMsgs_[1]);
        }
        long l = 0;
        l = ((long) b[offset]     & 0x00000000000000ffL) << 56 |
            ((long) b[offset + 1] & 0x00000000000000ffL) << 48 |
            ((long) b[offset + 2] & 0x00000000000000ffL) << 40 |
            ((long) b[offset + 3] & 0x00000000000000ffL) << 32 |
            ((long) b[offset + 4] & 0x00000000000000ffL) << 24 |
            ((long) b[offset + 5] & 0x00000000000000ffL) << 16 |
            ((long) b[offset + 6] & 0x00000000000000ffL) <<  8 |
            ((long) b[offset + 7] & 0x00000000000000ffL);
        return l;
    }


    /**
       Turns a <code>float</code> into a byte stream.

       @param f the value to encode.

       @return a <code>byte</code> array of size 4 with the value,
       in ANSI/IEE 754 (1985) format, sign-bit first.
    */
    public static byte[] encodeFloat(float f)
    {
        int i = Float.floatToIntBits(f); // get bit pattern
        return encodeInt(i);
    }


    /**
       Turns a <code>float</code> into a byte stream.

       @param f the value to encode.

       @param b the byte array in which to write the encoded value,
       in ANSI/IEE 754 (1985) format, sign-bit first.

       @param offset the offset at which to start writing in the byte array.

       @throws NetByteEncodingException if the byte array is too small.
    */
    public static void encodeFloat(float f, byte[] b, int offset)
        throws NetByteEncodingException
    {
        if (badParams(b, offset, floatSize_)) {
            throw new NetByteEncodingException(eMsgs_[1]);
        }
        int i = Float.floatToIntBits(f);
        encodeInt(i, b, offset);
    }


    /**
       Turns an encoding of 4 bytes into a <code>float</code> value.

       @param b the byte array in which contains the encoded value.

       @return a <code>float</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static float decodeFloat(byte[] b)
        throws NetByteDecodingException
    {
        if (badParams(b, 0, floatSize_)) {
            throw new NetByteDecodingException(eMsgs_[0]);
        }
        int i = decodeInt(b);
        return Float.intBitsToFloat(i);
    }


    /**
       Turns an encoding of 4 bytes into a <code>float</code> value.

       @param b the byte array from which to read the encoded value.

       @param offset the offset at which to start reading from the byte array.

       @return a <code>float</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static float decodeFloat(byte[] b, int offset)
        throws NetByteDecodingException
    {
        if (badParams(b, offset, floatSize_)) {
            throw new NetByteDecodingException(eMsgs_[1]);
        }
        int i = decodeInt(b, offset);
        return Float.intBitsToFloat(i);
    }


    /**
       Turns a <code>double</code> into a byte stream.

       @param d the value to encode.

       @return a <code>byte</code> array of size 8 with the value,
       in ANSI/IEE 754 (1985) format, sign-bit first.
    */
    public static byte[] encodeDouble(double d)
    {
        long l = Double.doubleToLongBits(d); // get bit pattern
        return encodeLong(l);
    }


    /**
       Turns a <code>double</code> into a byte stream.

       @param d the value to encode.

       @param b the byte array in which to write the encoded value,
       in ANSI/IEE 754 (1985) format, sign-bit first..

       @param offset the offset at which to start writing in the byte array.

       @throws NetByteEncodingException if the byte array is too small.
    */
    public static void encodeDouble(double d, byte[] b, int offset)
        throws NetByteEncodingException
    {
        if (badParams(b, offset, doubleSize_)) {
            throw new NetByteEncodingException(eMsgs_[1]);
        }
        long l = Double.doubleToLongBits(d); // get bit pattern
        encodeLong(l, b, offset);
    }


    /**
       Turns an encoding of 8 bytes into a <code>double</code> value.

       @param b the byte array in which contains the encoded value.

       @return a <code>double</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static double decodeDouble(byte[] b)
        throws NetByteDecodingException
    {
        if (badParams(b, 0, doubleSize_)) {
            throw new NetByteDecodingException(eMsgs_[0]);
        }
        long l = decodeLong(b);
        return Double.longBitsToDouble(l);
    }


    /**
       Turns an encoding of 8 bytes into a <code>double</code> value.

       @param b the byte array from which to read the encoded value.
       @param offset the offset at which to start reading from the byte array.

       @return a <code>double</code> value formed from the byte array.

       @throws NetByteDecodingException if the byte array is too small.
    */
    public static double decodeDouble(byte[] b, int offset)
        throws NetByteDecodingException
    {
        if (badParams(b, offset, doubleSize_)) {
            throw new NetByteDecodingException(eMsgs_[1]);
        }
        long l = decodeLong(b, offset);
        return Double.longBitsToDouble(l);
    }


    /**
       Gives the hexadecimal representation of a <code>byte</code> value.

       @param b the byte value.

       @return a String object containing the hexadecimal representation.

    */
    public static String byteToHexString(byte b)
    {
        int i = (int) b & 0x000000ff;
        return new String(hexStrings_[i]);
    }


    /**
       Gives the hexadecimal representation of the values in a
       <code>byte</code> array.

       @param buffer the byte array.

       @return a String object containing the hexadecimal representation.

       <br /><br />
       <b>Note:</b> if the length or offset arguments would result
       in accessing past the end of the byte array, the returned String
       object will contain representations of the byte values up to the end
       of the byte array only, and no error will be generated.
    */
    public static String bytesToHexString(byte[] buffer, int length, int offset)
    {
        int stop = offset + length;
        String hex = new String("");
        for(int j = offset; j < stop && j < buffer.length; ++j) {
            int i = (int) buffer[j] & 0x000000ff;
            hex += hexStrings_[i];
        }
        return hex;
    }


    /* ************************************************************
    ** private stuff below
    */

    private static String[] eMsgs_ = {
        "check buffer length",
        "check buffer length and offset value"
    };


    private static boolean badParams(byte[] b, int offset, int size)
    { return ((offset < 0) || (b.length < offset + size));  }


    private static String[] hexStrings_ = {
"00","01","02","03","04","05","06","07","08","09","0a","0b","0c","0d","0e","0f",
"10","11","12","13","14","15","16","17","18","19","1a","1b","1c","1d","1e","1f",
"20","21","22","23","24","25","26","27","28","29","2a","2b","2c","2d","2e","2f",
"30","31","32","33","34","35","36","37","38","39","3a","3b","3c","3d","3e","3f",
"40","41","42","43","44","45","46","47","48","49","4a","4b","4c","4d","4e","4f",
"50","51","52","53","54","55","56","57","58","59","5a","5b","5c","5d","5e","5f",
"60","61","62","63","64","65","66","67","68","69","6a","6b","6c","6d","6e","6f",
"70","71","72","73","74","75","76","77","78","79","7a","7b","7c","7d","7e","7f",
"80","81","82","83","84","85","86","87","88","89","8a","8b","8c","8d","8e","8f",
"90","91","92","93","94","95","96","97","98","99","9a","9b","9c","9d","9e","9f",
"a0","a1","a2","a3","a4","a5","a6","a7","a8","a9","aa","ab","ac","ad","ae","af",
"b0","b1","b2","b3","b4","b5","b6","b7","b8","b9","ba","bb","bc","bd","be","bf",
"c0","c1","c2","c3","c4","c5","c6","c7","c8","c9","ca","cb","cc","cd","ce","cf",
"d0","d1","d2","d3","d4","d5","d6","d7","d8","d9","da","db","dc","dd","de","df",
"e0","e1","e2","e3","e4","e5","e6","e7","e8","e9","ea","eb","ec","ed","ee","ef",
"f0","f1","f2","f3","f4","f5","f6","f7","f8","f9","fa","fb","fc","fd","fe","ff"
    };
    
} // class
