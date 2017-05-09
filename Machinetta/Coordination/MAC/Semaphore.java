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
/**
*Represents the Semaphore mechanism
**/
package Machinetta.Coordination.MAC;

public class Semaphore {
    //Class used to represent a semaphore mechanism
    private int count;
    
    /**
    *Constructor with no args. Defaults to 0 meaning 1 process only allowed to
    *enter CS. For more, use the other constructor
    **/
    public Semaphore() {
        count=0;        
    }
    
    /**
    *Overridden constructor allows more than one process to enter their CS.
    *Use Semaphore(5) for example to allow 5 processes to enter their CS at a
    *time.
    *@param i Number of processes allowed to enter their CS at a time
    **/
    public Semaphore(int i) {
        count=i;
        //System.out.println(i+" processes allowed in their CSes at a time");
    }
    
    public int getValue() { return count; }
    
    /**
    *Call this method before you go into a CS.
    *@param t The process thread.
    **/
    public synchronized void p(Thread t) {        
        count--; //Signal that we wanna go into out CS
        if (count < 0) {
            //System.out.println("Process "+t+" is being suspended.");
            try {
                wait();
	    }
            catch (Exception e) {}
            //System.out.println("Process "+t+" has been resumed.");
        }
    }
    
    /**
    *Call this method after you come out of a CS.
    *@param t The process thread
    **/
    public synchronized void v(Thread t) {
        //System.out.println("V called.");
        count++; //Signal that we have left our CS
        if (count<=0) {
            //System.out.println("Process "+t+" is resuming another thread.");
            notify();
        }
    }
        
}        
