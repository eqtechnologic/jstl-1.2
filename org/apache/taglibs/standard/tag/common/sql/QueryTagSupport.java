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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.jstl.core.Config;
import jakarta.servlet.jsp.jstl.sql.Result;
import jakarta.servlet.jsp.jstl.sql.SQLExecutionTag;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.TryCatchFinally;
import javax.sql.DataSource;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.core.Util;


/**
 * <p>Tag handler for &lt;Query&gt; in JSTL.  
 * 
 * @author Hans Bergsten
 * @author Justyna Horwat
 */

public abstract class QueryTagSupport extends BodyTagSupport 
    implements TryCatchFinally, SQLExecutionTag {

    private String var;
    private int scope;

    /*
     * The following properties take expression values, so the
     * setter methods are implemented by the expression type
     * specific subclasses.
     */
    protected Object rawDataSource;
    protected boolean dataSourceSpecified;
    protected String sql;
    protected int maxRows;
    protected boolean maxRowsSpecified;
    protected int startRow;

    /*
     * Instance variables that are not for attributes
     */
    private Connection conn;
    private List parameters;
    private boolean isPartOfTransaction;


    //*********************************************************************
    // Constructor and initialization

    public QueryTagSupport() {
        super();
        init();
    }

    private void init() {
        startRow = 0;
        maxRows = -1;
	maxRowsSpecified = dataSourceSpecified = false;
	isPartOfTransaction = false;
	conn = null;
	rawDataSource = null;
	parameters = null;
	sql = null;
	var = null;
        scope = PageContext.PAGE_SCOPE;
    }


    //*********************************************************************
    // Accessor methods

    /**
     * Setter method for the name of the variable to hold the
     * result.
     */
    public void setVar(String var) {
	this.var = var;
    }

    /**
     * Setter method for the scope of the variable to hold the
     * result.
     */
    public void setScope(String scopeName) {
        scope = Util.getScope(scopeName);
    }

    //*********************************************************************
    // Public utility methods

    /**
     * Called by nested parameter elements to add PreparedStatement
     * parameter values.
     */
    public void addSQLParameter(Object o) {
	if (parameters == null) {
	    parameters = new ArrayList();
	}
	parameters.add(o);
    }

    //*********************************************************************
    // Tag logic

    /**
     * Prepares for execution by setting the initial state, such as
     * getting the <code>Connection</code>
     */
    public int doStartTag() throws JspException {

        if (!maxRowsSpecified) {
	    Object obj = Config.find(pageContext, Config.SQL_MAX_ROWS);
	    if (obj != null) {
		if (obj instanceof Integer) {
		    maxRows = ((Integer) obj).intValue();
		} else if (obj instanceof String) {
		    try {
			maxRows = Integer.parseInt((String) obj);
		    } catch (NumberFormatException nfe) {
			throw new JspException(
			    Resources.getMessage("SQL_MAXROWS_PARSE_ERROR",
						 (String) obj),
			    nfe);
		    }
		} else {
		    throw new JspException(
                        Resources.getMessage("SQL_MAXROWS_INVALID"));
		}
            }
        }

	try {
	    conn = getConnection();
	} catch (SQLException e) {
	    throw new JspException(sql + ": " + e.getMessage(), e);
	}

	return EVAL_BODY_BUFFERED;
    }

    /**
     * <p>Execute the SQL statement, set either through the <code>sql</code>
     * attribute or as the body, and save the result as a variable
     * named by the <code>var</code> attribute in the scope specified
     * by the <code>scope</code> attribute, as an object that implements
     * the Result interface.
     *
     * <p>The connection used to execute the statement comes either
     * from the <code>DataSource</code> specified by the
     * <code>dataSource</code> attribute, provided by a parent action
     * element, or is retrieved from a JSP scope  attribute
     * named <code>jakarta.servlet.jstl.sql.dataSource</code>.
     */
    public int doEndTag() throws JspException {
	/*
	 * Use the SQL statement specified by the sql attribute, if any,
	 * otherwise use the body as the statement.
	 */
	String sqlStatement = null;
	if (sql != null) {
	    sqlStatement = sql;
	}
	else if (bodyContent != null) {
	    sqlStatement = bodyContent.getString();
	}
	if (sqlStatement == null || sqlStatement.trim().length() == 0) {
	    throw new JspTagException(
                Resources.getMessage("SQL_NO_STATEMENT"));
	}
        /*
         * We shouldn't have a negative startRow or illegal maxrows
         */
        if ((startRow < 0) || (maxRows < -1)) {
            throw new JspException(
                Resources.getMessage("PARAM_BAD_VALUE"));
        }

	Result result = null;
	/* 
	 * Note! We must not use the setMaxRows() method on the
	 * the statement to limit the number of rows, since the
	 * Result factory must be able to figure out the correct
	 * value for isLimitedByMaxRows(); there's no way to check
	 * if it was from the ResultSet.
          */
	try {
	    PreparedStatement ps = conn.prepareStatement(sqlStatement);
	    setParameters(ps, parameters);
	    ResultSet rs = ps.executeQuery();
	    result = new ResultImpl(rs, startRow, maxRows);
            ps.close();
	}
	catch (Throwable e) {
	    throw new JspException(sqlStatement + ": " + e.getMessage(), e);
	}
	pageContext.setAttribute(var, result, scope);
	return EVAL_PAGE;
    }

    /**
     * Just rethrows the Throwable.
     */
    public void doCatch(Throwable t) throws Throwable {
	throw t;
    }

    /**
     * Close the <code>Connection</code>, unless this action is used
     * as part of a transaction.
     */
    public void doFinally() {
	if (conn != null && !isPartOfTransaction) {
	    try {
		conn.close();
	    } catch (SQLException e) {} // Not much we can do
	}

	conn = null;
	parameters = null;
    }


    //*********************************************************************
    // Private utility methods

    private Connection getConnection() throws JspException, SQLException {
	// Fix: Add all other mechanisms
	Connection conn = null;
	isPartOfTransaction = false;

	TransactionTagSupport parent = (TransactionTagSupport) 
	    findAncestorWithClass(this, TransactionTagSupport.class);
	if (parent != null) {
            if (dataSourceSpecified) {
                throw new JspTagException(
                    Resources.getMessage("ERROR_NESTED_DATASOURCE"));
            }
	    conn = parent.getSharedConnection();
            isPartOfTransaction = true;
	} else {
	    if ((rawDataSource == null) && dataSourceSpecified) {
		throw new JspException(
		    Resources.getMessage("SQL_DATASOURCE_NULL"));
	    }
	    DataSource dataSource = DataSourceUtil.getDataSource(rawDataSource,
								 pageContext);
            try {
	        conn = dataSource.getConnection();
            } catch (Exception ex) {
                throw new JspException(
                    Resources.getMessage("DATASOURCE_INVALID", 
                                         ex.toString()));
            }
	}

	return conn;
    }

    private void setParameters(PreparedStatement ps, List parameters) 
        throws SQLException
    {
	if (parameters != null) {
	    for (int i = 0; i < parameters.size(); i++) {
                /* The first parameter has index 1.  If a null
                 * is passed to setObject the parameter will be
                 * set to JDBC null so an explicit call to
                 * ps.setNull is not required.
                 */
		ps.setObject(i + 1, parameters.get(i));
	    }
	}
    }
}
