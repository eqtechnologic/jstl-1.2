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
package org.apache.taglibs.standard.tag.rt.sql;

import jakarta.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.sql.TransactionTagSupport;

/**
 * Subclass for the JSTL library with rtexprvalue support.
 *
 * @author Hans Bergsten
 */
public class TransactionTag extends TransactionTagSupport {
    private String isolationRT;
    
    //*********************************************************************
    // Accessor methods


    /**
     * Setter method for the SQL DataSource. DataSource can be
     * a String or a DataSource object.
     */
    public void setDataSource(Object dataSource) {
	this.rawDataSource = dataSource;
	this.dataSourceSpecified = true;
    }

    /**
     * Setter method for the Transaction Isolation level.
     */
    public void setIsolation(String isolation) {
	this.isolationRT = isolation;
    }

    public int doStartTag() throws JspException {
	if (isolationRT != null)
          super.setIsolation(isolationRT);
        return super.doStartTag();
    }
}
