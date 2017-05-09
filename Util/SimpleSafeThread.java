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
 * SimpleSafeThread.java
 *
 * Created on May 29, 2005, 4:01 PM
 */

package Util;

/**
 *
 * @author paul
 */
public abstract class SimpleSafeThread extends Thread {
    
    boolean running = false;
    private int loopDelay = 0;
    
    public SimpleSafeThread(int loopDelay) {
        this.loopDelay = loopDelay;
    }
    
    public abstract void mainLoop();
    public void setLoopDelay(int delay) {
        loopDelay = delay;
        interrupt();
    }
    
    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();
	    try {
		mainLoop();
	    }
	    catch(Throwable t) {
		System.err.println("SimpleSafeThread.run: ignoring caught throwable t="+t);
		t.printStackTrace();
	    }
            if (loopDelay > 0) {
                while (System.currentTimeMillis() - loopDelay < startTime) {
                    try {
                        long sleepTime = loopDelay - (System.currentTimeMillis() - startTime);
                        if (sleepTime > 0)
                            sleep(sleepTime);
                    } catch (InterruptedException e) {}
                }
            }
        }
    }
    
    public final void start() { safeStart(); }
    
    public void safeStart() {
        if (!running) {
            running = true;
            super.start();
        }
    }
    
    public void safeStop() {
        if (running) {
            running = false;
        }
    }
}
