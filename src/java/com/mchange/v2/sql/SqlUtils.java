/*
 * Distributed as part of mchange-commons-java v.0.2.4
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


package com.mchange.v2.sql;

import java.sql.*;
import com.mchange.v2.log.*;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.mchange.lang.ThrowableUtils;
import com.mchange.v2.lang.VersionUtils;

public final class SqlUtils
{
    final static MLogger logger = MLog.getLogger( SqlUtils.class );

    // protected by SqlUtils.class' lock
    final static DateFormat tsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");

    public final static String DRIVER_MANAGER_USER_PROPERTY     = "user";
    public final static String DRIVER_MANAGER_PASSWORD_PROPERTY = "password";

    public static String escapeBadSqlPatternChars(String s)
    {
	StringBuffer sb = new StringBuffer(s);
	for (int i = 0, len = sb.length(); i < len; ++i)
	    if (sb.charAt(i) == '\'')
		{
		    sb.insert(i, '\'');
		    ++len;
		    i+=2;
		}
	return sb.toString();
    }

    public synchronized static String escapeAsTimestamp( Date date )
    { return "{ts '" + tsdf.format( date ) + "'}";  }

    public static SQLException toSQLException(Throwable t)
    { return toSQLException(null, t ); }

    public static SQLException toSQLException(String msg, Throwable t)
    { return toSQLException(msg, null, t);}

    public static SQLException toSQLException(String msg, String sqlState, Throwable t)
    {
        if (t instanceof SQLException)
	    {
		if (Debug.DEBUG && 
		    Debug.TRACE == Debug.TRACE_MAX && 
		    logger.isLoggable( MLevel.FINER ))
		    {
			SQLException s = (SQLException) t;
			StringBuffer tmp = new StringBuffer(255);
			tmp.append("Attempted to convert SQLException to SQLException. Leaving it alone.");
			tmp.append(" [SQLState: ");
			tmp.append( s.getSQLState() );
			tmp.append("; errorCode: " );
			tmp.append( s.getErrorCode() );
			tmp.append(']');
			if (msg != null)
			    tmp.append(" Ignoring suggested message: '" + msg + "'.");
			logger.log( MLevel.FINER, tmp.toString(), t );

			SQLException s2 = s;
			while ((s2 = s2.getNextException()) != null)
			    logger.log( MLevel.FINER, "Nested SQLException or SQLWarning: ", s2 );
		    }
		return (SQLException) t;
	    }
        else
        { 
            if (Debug.DEBUG) 
		{
		    //t.printStackTrace();
		    if ( logger.isLoggable( MLevel.FINE ) )
			logger.log( MLevel.FINE, "Converting Throwable to SQLException...", t );
		}

	    if (msg == null)
		msg = "An SQLException was provoked by the following failure: " + t.toString();
	    if ( VersionUtils.isAtLeastJavaVersion14() )
		{
		    SQLException out = new SQLException(msg);
		    out.initCause( t );
		    return out;
		}
	    else
		return new SQLException( msg + System.getProperty( "line.separator" ) +
					 "[Cause: " + ThrowableUtils.extractStackTrace(t) + ']', sqlState); 
        }
    }

    public static SQLClientInfoException toSQLClientInfoException(Throwable t)
    {
	if (t instanceof SQLClientInfoException)
	    return (SQLClientInfoException) t;
	else if (t.getCause() instanceof SQLClientInfoException)
	    return (SQLClientInfoException) t.getCause();
	else if (t instanceof SQLException)
	{
	    SQLException sqle = (SQLException) t;
	    return new SQLClientInfoException(sqle.getMessage(), sqle.getSQLState(), sqle.getErrorCode(), null, t);
	}
	else
	    return new SQLClientInfoException(t.getMessage(), null, t);
    }

    private SqlUtils()
    {}
}
