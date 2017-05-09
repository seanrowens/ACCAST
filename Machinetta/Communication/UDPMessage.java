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
 * UDPMessage.java
 *
 * Created on May 29, 2005, 3:40 PM
 */

package Machinetta.Communication;

import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.State.BeliefType.MAC.RoleAgentBelief;
import Machinetta.State.BeliefType.MAC.JIAgentBelief;
import Machinetta.State.BeliefType.NamedProxyID;

import java.io.*;
import java.util.ConcurrentModificationException;
import java.util.Random;

/**
 *
 * @author paul
 */
public class UDPMessage {
    public static final boolean PRINT_ERROR_MSG_FOR_NULL_SOURCE = true;

    public static final String SIM_NAME="Sanjaya";

    /**
     * Message types: <br>
     * PP: Proxy to Proxy <br>
     * PR: Proxy to RAP <br>
     * RP: RAP to Proxy <br>
     * ACK: Acknowledging message receipt. <br>
     */
    public static final byte UNKNOWN=-1, PP = 0, PR = 1, RP = 2, ACK = 3;
    byte getType() { return type;}
    String getTypeName() {
        if(PP == type)
            return "Prx2Prx";
        if(RP == type)
            return "RAP2Prx";
        if(PR == type)
            return "Prx2RAP";
        if(ACK == type)
            return "Ack    ";
        return "Unknown";
    }
    public boolean isACK() { return ACK == type; }
    public boolean isPP() { return PP == type; }
    public boolean isPR() { return RP == type; }
    public boolean isRP() { return RP == type; }
    
    transient static Random rand = new Random();

    public byte type = 0;
    public byte sendCount = 0;
    public boolean reqAck = true;
    public boolean hello = false;
    public long id = -1L;
    Machinetta.State.BeliefType.ProxyID source, dest;
    
    public Object msg = null;

    private  String msgClass = null;
    
    public String summary() {
	return "type="+getTypeName()+" sendCount="+sendCount+" reqAck="+reqAck+" hello="+hello+" id="+id+" source='"+(null == source ? null : source.toString())+"' dest='"+(null == dest ? null : dest.toString())+"' msgClass="+msgClass;
    }

    static public String summary(byte[] data) {
	return "type="+getType(data)+" sendCount="+getSendCount(data)+" reqAck="+getReqAck(data)+" hello="+getHello(data)+" id="+getID(data)+" source='"+getSourceID(data)+"' dest='"+getDestID(data)+"'";
    }

    public static UDPMessage makeHelloMessage(Machinetta.State.BeliefType.ProxyID sourceID, byte type) {
	UDPMessage msg = new UDPMessage(sourceID, sourceID, 0);
	msg.dest = sourceID;
	msg.type = type;
	msg.hello = true;
	msg.reqAck = false;
	return msg;
    }

    /** Acknowledgement of message reciept */
    public UDPMessage(Machinetta.State.BeliefType.ProxyID sourceID, Machinetta.State.BeliefType.ProxyID destID, long id) {
        type = ACK;
        source = sourceID;
        dest = destID;
        this.id = id;
        
//        if(null == source)
//            Machinetta.Debugger.debug( 3,"SourceID was null!");
    }
    
    /** Message between proxies */
    public UDPMessage(Machinetta.State.BeliefType.ProxyID sourceID, Machinetta.State.BeliefType.ProxyID destID, Message msg) {
        type = PP;
        source = sourceID;
        dest = destID;
        this.msg = msg;

	// @TODO: serialize msg right away instead of waiting, to
	// avoid concurrent modification exceptions
	//
	// Why?  This problem doesn't show up until we're under heavy
	// load.  The way the code is now, when a UDPMessage is
	// constructed it's contents are not serialized until the
	// comms thread is ready to process it.  If the contents are
	// based on something like a List, this can result in a
	// ConcurrentModification exception if the list happens to be
	// being modified at that time.  Even if no exception is
	// thrown, this could (potentially) result in much harder to
	// track down issues since the contents of the Message object
	// are not threadsafe.
        
        id = rand.nextLong();
        
        if (msg == null || msg.getCriticality() == Message.Criticality.NONE) {
            reqAck = false;
        }
        
        if(null == source) {
            Machinetta.Debugger.debug( 3,"SourceID was null!");
	    
	}
        
    }
    
    /** Message from Proxy to RAP */
    public UDPMessage(Machinetta.State.BeliefType.ProxyID sourceID, Machinetta.State.BeliefType.ProxyID destID, OutputMessage msg) {
        type = PR;
        source = sourceID;
        dest = destID;
        this.msg = msg;
        
	// @TODO: serialize msg right away instead of waiting, to
	// avoid concurrent modification exceptionsw.  See TODO above
	// for explanation.

        id = rand.nextLong();
        
        if(null == source) {
            Machinetta.Debugger.debug( 3,"SourceID was null!");
	    //	    throw new RuntimeException("SourceID is null!");
	}
        
    }
    
    /**
     * Message from RAP to proxy
     *
     * In this case, I guess sourceID really means dest?
     */
    public UDPMessage(Machinetta.State.BeliefType.ProxyID sourceID, Machinetta.State.BeliefType.ProxyID destID, InputMessage msg) {
        type = RP;
        source = sourceID;
        
	if(null != msg)
	    msgClass = msg.getClass().toString();
        
        if(null == source)
            Machinetta.Debugger.debug( 3,"SourceID was null!");
        
        dest = destID;

        this.msg = msg;
        
	// @TODO: serialize msg right away instead of waiting, to
	// avoid concurrent modification exceptions.  See TODO above
	// for explanation.

        if (msg == null || msg.getCriticality() == Message.Criticality.NONE) {
	    reqAck = false;
	}
        
        id = rand.nextLong();
    }
    
    public synchronized byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//  	Machinetta.Debugger.debug( 1, "getBytes  : type = "+type);
// 	Machinetta.Debugger.debug( 1, "getBytes  : sendCount = "+sendCount);
// 	Machinetta.Debugger.debug( 1, "getBytes  : reqAck = "+reqAck);
// 	Machinetta.Debugger.debug( 1, "getBytes  : hello = "+hello);
// 	Machinetta.Debugger.debug( 1, "getBytes  : id = "+id);
// 	if(null != source) {
// 	    Machinetta.Debugger.debug( 1, "getBytes  : sourceLen = "+source.toString().getBytes().length);
// 	    Machinetta.Debugger.debug( 1, "getBytes  : sourceID = "+source);
// 	}
// 	if(null != dest) {
// 	    Machinetta.Debugger.debug( 1, "getBytes  : destLen = "+dest.toString().getBytes().length);
// 	    Machinetta.Debugger.debug( 1, "getBytes  : destID = "+dest);
// 	}

        createHeader(out);
        
        ObjectOutputStream oout = null;
        
        try{
            oout = new ObjectOutputStream(out);
        } catch (IOException e) {
            Machinetta.Debugger.debug( 5,"Failed to create object output stream: " + e);
            return null;
        }
       
        if (msg != null) {
            synchronized(msg) {
                try {
                    oout.writeObject(msg);
		    oout.flush();
		    oout.close();
                } catch (IOException e) {
                    Machinetta.Debugger.debug( 5,"Failed to write msg to byte stream: " + e);
                } catch (ConcurrentModificationException e2) {
                    Machinetta.Debugger.debug( 5,e2 + " : " + e2.getCause());
		    e2.printStackTrace();
                }
            }

        }
	
// 	// @TODO: Hack to test stuff here;
// 	UDPMessage decodedMsg = getMessage(out.toByteArray());
// 	Machinetta.Debugger.debug( 2, "Original: "+summary());
// 	Machinetta.Debugger.debug( 2, "Decoded : "+decodedMsg.summary());

        return out.toByteArray();
    }
    
    public Machinetta.State.BeliefType.ProxyID getDest() { return dest; }
    public Machinetta.State.BeliefType.ProxyID getSource() { return source; }
    
    private final static int TYPE_OFFSET=0;
    private final static int SEND_COUNT_OFFSET=1;
    private final static int REQ_ACK_OFFSET=2;
    private final static int HELLO_OFFSET=3;
    private final static int MSG_ID_OFFSET=4;
    private final static int SOURCE_LEN_OFFSET=12;
    private final static int SOURCE_OFFSET=14;
    private final static int SIZEOF_DEST_LEN=2;

    /**
     * Creates the header for the message.
     */
    protected void createHeader(ByteArrayOutputStream out) {
        // First write the type
	try {
	writeByte(out, type);
	writeByte(out, sendCount);
	writeBoolean(out, reqAck);
	writeBoolean(out, hello);
	writeLong(out,id);

        if(null == source) {
            if(PRINT_ERROR_MSG_FOR_NULL_SOURCE) {
                if(msg != null)
                    Machinetta.Debugger.debug( 5,"ERROR: createHeader: null source in message, writing 0 length for source id.  Msg type="+getTypeName()+" sendCount="+sendCount+", message is of type "+msg.getClass().getName()+", msg="+msg.toString());
                else
                    Machinetta.Debugger.debug( 5,"ERROR: createHeader: null source in message, writing 0 length for source id.  Msg type="+getTypeName()+" sendCount="+sendCount+", msg field is null");
            }
	    writeInt(out, 0);
        } 
	else {
	    // Then the source id.
	    byte[] sourceBytes = source.toString().getBytes();
	    if(sourceBytes != null && sourceBytes.length > 0) {
		writeStringIntLen(out,source.toString(),null);
	    }
	    else {
		writeInt(out,0);
	    }
        }
        
	if(null == dest) {
	    Machinetta.Debugger.debug( 5,"PP message with null dest, (certain message types are allowed to have null dest, but not Proxy2Proxy) of type="+getTypeName()+", hello="+hello+" reqAck="+reqAck+", sendCount="+sendCount+", id="+id+" msgClass="+msgClass);
	    writeInt(out,0);
	}
	else {
	    byte[] destBytes = dest.toString().getBytes();
	    if(destBytes != null && destBytes.length > 0) {
		writeStringIntLen(out,dest.toString(),null);
	    }
	    else {
		writeInt(out,0);
	    }
	}
	}
	catch(java.io.IOException e) {
	    Machinetta.Debugger.debug(3,"Exception writing header to binary, e="+e);
	    e.printStackTrace();
	}
    }

    public static byte getType(byte [] data) {
	return readByte(data,TYPE_OFFSET);
    }

    public static byte getSendCount(byte [] data) {
	return readByte(data,SEND_COUNT_OFFSET);
    }

    public static boolean getReqAck(byte [] data) {
	return readBoolean(data,REQ_ACK_OFFSET);
    }

    public static boolean requiresAck(byte [] data) {
	return getReqAck(data);
    }

    // Is this a 'hello' message, i.e. it is not meant to really be
    // delivered but rathre just to announce to the world where a
    // UDPCommon instance is.
    public static boolean getHello(byte [] data) {
	return readBoolean(data,HELLO_OFFSET);
    }

    public static long getID(byte [] data) {
	return readLong(data,MSG_ID_OFFSET);
    }

    public static String getSourceID(byte [] data) {
	int index = SOURCE_LEN_OFFSET;
	int len = readInt(data,index);
	if(len <= 0)
	    return null;
	index += 4;
	return readString(data, index, len);
    }

    public static String getDestID(byte [] data) {
	int index = SOURCE_LEN_OFFSET;
	// Skip over source
	int len = readInt(data,index);
	index += 4;
	index += len;
	// Get dest len
	len = readInt(data,index);
	if(len <= 0)
	    return null;
	index += 4;
	return readString(data, index, len);
    }

    public Message getMessage() { if (msg instanceof Message) return (Message)msg; else return null; }
    public InputMessage getInputMessage() { if (msg instanceof InputMessage) return (InputMessage)msg; else return null; }
    public OutputMessage getOutputMessage() { if (msg instanceof OutputMessage) return (OutputMessage)msg; else return null; }
    
    public static UDPMessage getMessage(byte [] data) {
        Machinetta.State.BeliefType.ProxyID dest = null;
        Machinetta.State.BeliefType.ProxyID source = null;

        ByteArrayInputStream in = new ByteArrayInputStream(data);

	byte type = getType(data);
	in.skip(1);
// 	Machinetta.Debugger.debug( 1, "getMessage: type = "+type);
	byte sendCount = getSendCount(data);
	in.skip(1);
// 	Machinetta.Debugger.debug( 1, "getMessage: sendCount = "+sendCount);
	boolean reqAck = getReqAck(data);
	in.skip(1);
// 	Machinetta.Debugger.debug( 1, "getMessage: reqAck = "+reqAck);
	boolean hello = getHello(data);
	in.skip(1);
// 	Machinetta.Debugger.debug( 1, "getMessage: hello = "+hello);
	long id = getID(data);
	in.skip(8);
// 	Machinetta.Debugger.debug( 1, "getMessage: id = "+id);
	String sourceID = getSourceID(data);
	if(null != sourceID)
	    source = new NamedProxyID(sourceID);

	int srcLen = readInt(data,SOURCE_LEN_OFFSET);
	in.skip(4);
	in.skip(srcLen);
// 	Machinetta.Debugger.debug( 1, "getMessage: sourceID = "+sourceID);

	String destID = getDestID(data);
	if(null != destID)
	    dest = new NamedProxyID(destID);
	int dstLen = readInt(data,SOURCE_LEN_OFFSET+4+srcLen);
	in.skip(4);
	in.skip(dstLen);
// 	Machinetta.Debugger.debug( 1, "getMessage: destID = "+destID);

      

	Object msgData = null;
	if(!hello) {
	    try {
		ObjectInputStream oin = new ObjectInputStream(in);
		msgData = oin.readObject();
	    } catch (IOException e) {
		Machinetta.Debugger.debug( 5,"Failed to translate data block: " + e);
		e.printStackTrace();
	    } catch (ClassNotFoundException e2) {
		Machinetta.Debugger.debug( 5,"Failed to translate data block: " + e2);
		e2.printStackTrace();
	    }
	}        

        UDPMessage msg = null;
        switch (type) {
            case PP:
                msg = new UDPMessage(source, dest, (Message)msgData);
                break;
            case PR:
                msg = new UDPMessage(source, dest, (OutputMessage)msgData);
                break;
            case RP:
                msg = new UDPMessage(source, dest, (InputMessage)msgData);
                break;
            case ACK:
                //                Machinetta.Debugger.debug( 5,"message is ACK");
                msg = new UDPMessage(source, dest, id);
                break;
            default:
                Machinetta.Debugger.debug( 5,"Could not translate UDP data to message");
        }
        
        if (msg != null) {
	    msg.type = type;
            msg.sendCount = sendCount;
	    msg.reqAck = reqAck;
	    msg.hello = hello;
            msg.id = id;
	    msg.source = source;
	    msg.dest = dest;
        }
        return msg;
    }
    
    /**
     * Returns true iff the data in the byte array represents a
     * proxy-to-proxy message to ProxyID
     */
    public static boolean isCommToLocal(byte [] data, Machinetta.State.BeliefType.ProxyID id) {
        if (getType(data) == PP) {
	    String destID = getDestID(data);

            if (destID != null && id != null) {
                Machinetta.Debugger.debug( 0,"isCommToLocal: DestID: " + destID + " localname: " + id + "? " + destID.equalsIgnoreCase("" + id));
                return destID.equalsIgnoreCase(id.toString());
            } else {
                Machinetta.Debugger.debug( 3,"isCommToLocal: Did not know DestID: " + destID + " or ID: " + id);
            }
        }
        return false;
    }

    // Quick test if the data for this message is a proxy2proxy message
    public static boolean isCommToLocal(byte [] data) {
	return getType(data) == PP;
    }    

    /**
     * Returns true iff the data in the byte array represents a
     * proxy-to-RAP message
     */
    public static boolean isRAPInput(byte [] data, Machinetta.State.BeliefType.ProxyID id) {
        if (getType(data) == RP) {
// 	    String sourceID = getSourceID(data);
//             Machinetta.Debugger.debug( 0,"isRAPInput: SourceID: " + sourceID + " localname: " + id + "? " + sourceID.equalsIgnoreCase(id.toString()));
//             return sourceID.equalsIgnoreCase(id.toString());
	    return true;
        }
        return false;
    }
    
    public static boolean isRAPInput(byte [] data, String id) {
        if (getType(data) == RP) {
	    return true;
// 	    String sourceID = getSourceID(data);
//             return sourceID.equalsIgnoreCase(id.toString());
        }
        return false;
    }
    
    public static boolean isRAPInput(byte [] data) {
	return getType(data) == RP;
    }
    /**
     * Returns true iff the data in the byte array represents a
     * RAP-to-proxy message
     */
    public static boolean isRAPOutput(byte [] data) {
        return getType(data) == PR;
    }
    
    /**
     * Returns true iff the data in the byte array represents a
     * RAP-to-proxy message to ProxyId
     */
    public static boolean isRAPOutput(byte [] data, Machinetta.State.BeliefType.ProxyID id) {
        if (getType(data) == PR) {
	    String destID = getDestID(data);
            return destID.equalsIgnoreCase(id.toString());
        }
        return false;
    }
    
    public static boolean isACK(byte [] data) {
        return getType(data) == ACK;
    }
    
    public Object getMsg() { return msg; }
    
    public String toString() { return "UDPMessage: " + getTypeName() + " from " + source + " to " + dest; }
    
    public static boolean decodeMsg(Message msg) {
        if(msg instanceof ObjectMessage) {
            Object omsg = ((ObjectMessage)msg).o;
            if(null == omsg) {
                Machinetta.Debugger.debug(1,"Sending: "+omsg.getClass().getName()+"\tObjectMessage\tmsg is null");
                return true;
            }
            if(omsg instanceof RoleAgentBelief)
                Machinetta.Debugger.debug(1,"Sending: "+omsg.getClass().getName()+"\tRoleAgentBelief\t"+omsg.toString());
            else if(omsg instanceof JIAgentBelief)
                Machinetta.Debugger.debug(1,"Sending: "+omsg.getClass().getName()+"\tJIAgentBelief\t"+omsg.toString());
            else
                Machinetta.Debugger.debug(1,"Sending: "+omsg.getClass().getName()+"\tomsg\t"+omsg.toString());
            return true;
        } else {
            Machinetta.Debugger.debug(1,"Sending: non object message="+msg.getClass().getName()+"\tmsg\t"+msg.toString());
            return false;
        }
    }
    
    public static boolean readBoolean(byte[] buf, int index) {
        return (buf[index] != 0);
    }
    public static boolean readBoolean(InputStream is) throws java.io.IOException {
        return (is.read() != 0);
    }

    public static int writeBoolean(byte[] buf, int index, boolean aBoolean) {
        buf[index++] = (aBoolean ? (byte)1 : (byte)0);
        return index;
    }
    public static void writeBoolean(OutputStream os, boolean aBoolean) throws java.io.IOException {
        try {
            os.write((aBoolean ? 1 : 0));
        }
        catch (java.io.IOException e) {
        }
    }

    public static byte readByte(byte[] buf, int index) {
        return buf[index];
    }
    public static int writeByte(byte[] buf, int index, byte aByte) {
        buf[index++] = aByte;
        return index;
    }
    public static byte readByte(InputStream is) throws java.io.IOException {
        return (byte)is.read();
    }
    public static void writeByte(OutputStream os, byte aByte) throws java.io.IOException {
        os.write(aByte);
    }

    public static short readShort(byte[] buf, int index) {
        return (short)
            (((0xFF & buf[index]) << 8) | (0xFF & buf[index+1]));
    }

    public static short readShort(InputStream is) throws java.io.IOException {
        return (short)
            (((0xFF & is.read()) << 8) | (0xFF & is.read()));
    }

    public static int writeShort(byte[] buf, int index, short  aShort) {
        buf[index++] = (byte)((aShort & 0xFF00L) >> 8);
        buf[index++] = (byte) (aShort & 0x00FFL);

        return index;
    }

    public static void writeShort(OutputStream os, short  aShort) throws java.io.IOException {
        os.write((byte)((aShort & 0xFF00L) >> 8));
        os.write((byte)(aShort  & 0x00FFL));
    }
    public static int readInt(byte[] buf, int index) {
        return (int) ((0x000000FF & ((int)buf[index])) << 24
                      | (0x000000FF & ((int)buf[index+1])) << 16
                      | (0x000000FF & ((int)buf[index+2])) << 8
                      | (0x000000FF & ((int)buf[index+3])));
    }

    public static int readInt(InputStream is) throws java.io.IOException {
        return (int) ((0x000000FF & is.read()) << 24
                      | (0x000000FF & is.read()) << 16
                      | (0x000000FF & is.read()) << 8
                      | (0x000000FF & is.read()));
    }

    public static int writeInt(byte[] buf, int index, int  anInt) {
        buf[index++] = (byte)((anInt & 0xFF000000L) >> 24);
        buf[index++] = (byte)((anInt & 0x00FF0000L) >> 16);
        buf[index++] = (byte)((anInt & 0x0000FF00L) >> 8);
        buf[index++] = (byte) (anInt & 0x000000FFL);

        return index;
    }

    public static void writeInt(OutputStream os, int  anInt) throws java.io.IOException {
        os.write((anInt & 0xFF000000) >> 24);
        os.write((anInt & 0x00FF0000) >> 16);
        os.write((anInt & 0x0000FF00) >> 8);
        os.write(anInt & 0x000000FF);
    }
    public static long readLong(byte[] buf, int index) {
        return (long) (
                       (0xFFL & ((long)buf[index])) << 56
                       | (0xFFL & ((long)buf[index+1])) << 48
                       | (0xFFL & ((long)buf[index+2])) << 40
                       | (0xFFL & ((long)buf[index+3])) << 32
                       | (0xFFL & ((long)buf[index+4])) << 24
                       | (0xFFL & ((long)buf[index+5])) << 16
                       | (0xFFL & ((long)buf[index+6])) << 8
                       | (0xFFL & ((long)buf[index+7])));
    }

    public static long readLong(InputStream is) throws java.io.IOException {
        return (long) (
                       (0xFFL & is.read()) << 56
                       | (0xFFL & is.read()) << 48
                       | (0xFFL & is.read()) << 40
                       | (0xFFL & is.read()) << 32
                       | (0xFFL & is.read()) << 24
                       | (0xFFL & is.read()) << 16
                       | (0xFFL & is.read()) << 8
                       | (0xFFL & is.read()));
    }

    public static int writeLong(byte[] buf, int index, long  aLong) {
        buf[index++] = (byte)((aLong & 0xFF00000000000000L) >> 56);
        buf[index++] = (byte)((aLong & 0x00FF000000000000L) >> 48);
        buf[index++] = (byte)((aLong & 0x0000FF0000000000L) >> 40);
        buf[index++] = (byte)((aLong & 0x000000FF00000000L) >> 32);
        buf[index++] = (byte)((aLong & 0x00000000FF000000L) >> 24);
        buf[index++] = (byte)((aLong & 0x0000000000FF0000L) >> 16);
        buf[index++] = (byte)((aLong & 0x000000000000FF00L) >> 8);
        buf[index++] = (byte) (aLong & 0x00000000000000FFL);

        return index;
    }

    public static void writeLong(OutputStream os, long  aLong) throws java.io.IOException {
        os.write((byte)((aLong & 0xFF00000000000000L) >> 56));
        os.write((byte)((aLong & 0x00FF000000000000L) >> 48));
        os.write((byte)((aLong & 0x0000FF0000000000L) >> 40));
        os.write((byte)((aLong & 0x000000FF00000000L) >> 32));
        os.write((byte)((aLong & 0x00000000FF000000L) >> 24));
        os.write((byte)((aLong & 0x0000000000FF0000L) >> 16));
        os.write((byte)((aLong & 0x000000000000FF00L) >> 8));
        os.write((byte)(aLong  & 0x00000000000000FFL));
    }

    public static int writeBytes(byte[] buf, int index, byte[] bytes) {
        System.arraycopy(bytes,0,buf,index,bytes.length);
        return index += bytes.length;
    }
    public static int writeBytesIntLen(byte[] buf, int index, byte[] bytes) {
        index = writeInt(buf,index,bytes.length);
        return writeBytes(buf,index,bytes);
    }
    public static int writeStringIntLen(byte[] buf, int index, String aString, String charset)
        throws java.io.UnsupportedEncodingException {
        byte[] aStringBytes = (null == charset) ? aString.getBytes() : aString.getBytes(charset);
        return writeBytesIntLen(buf,index,aStringBytes);
    }
    public static void writeStringIntLen(OutputStream os, String aString, String charset)
        throws java.io.UnsupportedEncodingException, java.io.IOException {
        byte[] aStringBytes = (null == charset) ? aString.getBytes() : aString.getBytes(charset);
        writeInt(os,aStringBytes.length);
        os.write(aStringBytes,0,aStringBytes.length);
    }
    public static String readString(byte[] buf, int index, int length) {
        byte[] stringBytes = new byte[length];
        System.arraycopy(buf,index,stringBytes,0, length);
        return new String(stringBytes);
    }
}
