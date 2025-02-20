/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Portions Copyright Apache Software Foundation.
 */ 

package org.apache.taglibs.standard.tag.common.sql;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.jstl.core.Config;
import javax.sql.DataSource;

import org.apache.taglibs.standard.resources.Resources;


/**
 * <p>A simple <code>DataSource</code> utility for the standard
 * <code>DriverManager</code> class.
 *
 * TO DO: need to cache DataSource
 * 
 * @author Justyna Horwat
 */
public class DataSourceUtil {

    private static final String ESCAPE = "\\";
    private static final String TOKEN = ",";

    /**
     * If dataSource is a String first do JNDI lookup.
     * If lookup fails parse String like it was a set of JDBC parameters
     * Otherwise check to see if dataSource is a DataSource object and use as
     * is
     */
    static DataSource getDataSource(Object rawDataSource, PageContext pc)
	throws JspException
    {
	DataSource dataSource = null;

        if (rawDataSource == null) {
            rawDataSource = Config.find(pc, Config.SQL_DATA_SOURCE);
        }

	if (rawDataSource == null) {
	    return null;
	}

        /*
	 * If the 'dataSource' attribute's value resolves to a String
	 * after rtexpr/EL evaluation, use the string as JNDI path to
	 * a DataSource
	 */
        if (rawDataSource instanceof String) {
            try {
                Context ctx = new InitialContext();
                // relative to standard JNDI root for J2EE app
                Context envCtx = (Context) ctx.lookup("java:comp/env");
                dataSource = (DataSource) envCtx.lookup((String) rawDataSource);
            } catch (NamingException ex) {
                dataSource = getDataSource((String) rawDataSource);
            }
        } else if (rawDataSource instanceof DataSource) {
            dataSource = (DataSource) rawDataSource;
        } else {
	    throw new JspException(
                Resources.getMessage("SQL_DATASOURCE_INVALID_TYPE"));
	}

	return dataSource;
    }

    /**
     * Parse JDBC parameters and setup dataSource appropriately
     */
    private static DataSource getDataSource(String params)
	throws JspException
    {
        DataSourceWrapper dataSource = new DataSourceWrapper();

        String[] paramString = new String[4];
        int escCount = 0; 
        int aryCount = 0; 
        int begin = 0;

        for(int index=0; index < params.length(); index++) {
            char nextChar = params.charAt(index);
            if (TOKEN.indexOf(nextChar) != -1) {
                if (escCount == 0) {
                    paramString[aryCount] = params.substring(begin,index).trim();
                    begin = index + 1;
                    if (++aryCount > 4) {
                        throw new JspTagException(
                            Resources.getMessage("JDBC_PARAM_COUNT"));
                    }
                }
            }
            if (ESCAPE.indexOf(nextChar) != -1) {
                escCount++;
            }
            else {
                escCount = 0;
            }
        }
        paramString[aryCount] = params.substring(begin).trim();

	// use the JDBC URL from the parameter string
        dataSource.setJdbcURL(paramString[0]);

	// try to load a driver if it's present
        if (paramString[1] != null) {
            try {
                dataSource.setDriverClassName(paramString[1]);
            } catch (Exception ex) {
                throw new JspTagException(
                    Resources.getMessage("DRIVER_INVALID_CLASS",
					 ex.toString()), ex);
            }
	}

	// set the username and password
        dataSource.setUserName(paramString[2]);
        dataSource.setPassword(paramString[3]);

	return dataSource;
    }

}
