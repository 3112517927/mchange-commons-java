/*
 * Distributed as part of mchange-commons-java v.0.2.3
 *
 * Copyright (C) 2012 Machinery For Change, Inc.
 *
 * Author: Steve Waldman <swaldman@mchange.com>
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2.1, as 
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; see the file LICENSE.  If not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */


package com.mchange.v2.coalesce;

public class CoalesceTest
{
    final static int NUM_ITERS = 10000;

    final static Coalescer c = CoalescerFactory.createCoalescer( null, true, true );

    public static void main( String[] argv )
    {
	doTest();
	System.gc();
	System.err.println("num coalesced after gc: " + c.countCoalesced());
    }

    private static void doTest()
    {
	String[] strings = new String[ NUM_ITERS ];
	for (int i = 0; i < NUM_ITERS; ++i)
	    strings[i] = new String( "Hello" );

	long start_time = System.currentTimeMillis();
	for (int i = 0; i < NUM_ITERS; ++i)
	    {
		Object random = strings[i];
		Object normal = c.coalesce( random );
//     		System.err.println( System.identityHashCode( random ) +
//     				    "\t" +
//     				    System.identityHashCode( normal ) );
	    }
	long time_ms = System.currentTimeMillis() - start_time;
	System.out.println("avg time: " + time_ms / ((float) NUM_ITERS) +
			   "ms (" + NUM_ITERS + " iterations)");
	System.err.println("num coalesced: " + c.countCoalesced());
    }
}
