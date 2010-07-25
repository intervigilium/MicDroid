/* Sample.java
   An auto-tune app for Android

   Copyright (c) 2010 Ethan Chen

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.intervigil.micdroid;

/** Packet of audio to pass between reader and writer threads. */
public class Sample {
	public short[] buffer;
	public int bufferSize;
	public boolean isEnd;
	
	public Sample(short[] buffer, int bufferSize) {
		this.buffer = buffer;
		this.bufferSize = bufferSize;
		this.isEnd = false;
	}
	
	public Sample() {
		this.isEnd = true;
	}
}