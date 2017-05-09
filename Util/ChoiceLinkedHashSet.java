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
 * ChoiceLinkedHashSet.java
 * created Oct 5, 2005 at 6:27:36 PM
 * by sokamoto
 */
package Util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;

/**
 * 
 * Linked hash set that supports a choose function for getting a random element
 * 
 * @author sokamoto
 *
 */
public class ChoiceLinkedHashSet<X> extends LinkedHashSet<X> {

	public static Random rand = new Random();
	
	/**
	 * @param arg0
	 * @param arg1
	 */
	public ChoiceLinkedHashSet(int arg0, float arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ChoiceLinkedHashSet(int arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	public ChoiceLinkedHashSet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public ChoiceLinkedHashSet(Collection<X> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Selects a random element and returns it
	 * @return A randomly selected element, or null if the set is empty
	 */
	public X choose() {
		if (isEmpty()) return null;
		
		int choice = rand.nextInt(size());
		java.util.Iterator<X> iter = iterator();
		
		for (int i = 0; i < choice; i++) {
			iter.next();
		}
		return iter.next();
	}
}
