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

package org.apache.taglibs.standard.tlv;

import java.util.Set;
import java.util.Stack;

import jakarta.servlet.jsp.tagext.PageData;
import jakarta.servlet.jsp.tagext.ValidationMessage;

import org.apache.taglibs.standard.resources.Resources;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>A SAX-based TagLibraryValidator for the JSTL SQL tag library.
 * 
 * @author Shawn Bayern
 */
public class JstlSqlTLV extends JstlBaseTLV {

    //*********************************************************************
    // Constants

    // tag names
    private final String SETDATASOURCE = "setDataSource";
    private final String QUERY = "query";
    private final String UPDATE = "update";
    private final String TRANSACTION = "transaction";
    private final String PARAM = "param";
    private final String DATEPARAM = "dateParam";

    private final String JSP_TEXT = "jsp:text";

    // attribute names
    private final String SQL = "sql";
    private final String DATASOURCE = "dataSource";


    //*********************************************************************
    // set its type and delegate validation to super-class
    public  ValidationMessage[] validate(
	    String prefix, String uri, PageData page) {
	return super.validate( TYPE_SQL, prefix, uri, page );
    }


    //*********************************************************************
    // Contract fulfillment

    protected DefaultHandler getHandler() {
	return new Handler();
    }


    //*********************************************************************
    // SAX event handler

    /** The handler that provides the base of our implementation. */
    private class Handler extends DefaultHandler {

	// parser state
	private int depth = 0;
        private Stack queryDepths = new Stack();
        private Stack updateDepths = new Stack();
        private Stack transactionDepths = new Stack();
	private String lastElementName = null;
	private boolean bodyNecessary = false;
	private boolean bodyIllegal = false;

	// process under the existing context (state), then modify it
	public void startElement(
	        String ns, String ln, String qn, Attributes a) {

	    // substitute our own parsed 'ln' if it's not provided
	    if (ln == null)
		ln = getLocalPart(qn);

	    // for simplicity, we can ignore <jsp:text> for our purposes
	    // (don't bother distinguishing between it and its characters)
	    if (qn.equals(JSP_TEXT))
		return;

	    // check body-related constraint
	    if (bodyIllegal)
		fail(Resources.getMessage("TLV_ILLEGAL_BODY", lastElementName));

	    // validate expression syntax if we need to
	    Set expAtts;
	    if (qn.startsWith(prefix + ":")
		    && (expAtts = (Set) config.get(ln)) != null) {
		for (int i = 0; i < a.getLength(); i++) {
		    String attName = a.getLocalName(i);
		    if (expAtts.contains(attName)) {
			String vMsg =
			    validateExpression(
				ln,
				attName,
				a.getValue(i));
			if (vMsg != null)
			    fail(vMsg);
		    }
		}
	    }

            // validate attributes
            if (qn.startsWith(prefix + ":") && !hasNoInvalidScope(a))
                fail(Resources.getMessage("TLV_INVALID_ATTRIBUTE",
                    SCOPE, qn, a.getValue(SCOPE))); 
	    if (qn.startsWith(prefix + ":") && hasEmptyVar(a))
		fail(Resources.getMessage("TLV_EMPTY_VAR", qn));
	    if (qn.startsWith(prefix + ":") && hasDanglingScope(a) &&
                !qn.startsWith(prefix + ":" + SETDATASOURCE))
		fail(Resources.getMessage("TLV_DANGLING_SCOPE", qn));

	    // now, modify state

            /*
             * Make sure <sql:param> is nested inside <sql:query> or
             * <sql:update>. Note that <sql:param> does not need to
             * be a direct child of <sql:query> or <sql:update>.
             * Otherwise, the following would not work:
             *
             *  <sql:query sql="..." var="...">
             *   <c:forEach var="arg" items="...">
             *    <sql:param value="${arg}"/>
             *   </c:forEach>
             *  </sql:query>
             */
            if ( (isSqlTag(ns, ln, PARAM) || isSqlTag(ns, ln, DATEPARAM)) 
                && (queryDepths.empty() && updateDepths.empty()) ) {
                fail(Resources.getMessage("SQL_PARAM_OUTSIDE_PARENT"));
            }

            // If we're in a <query>, record relevant state
            if (isSqlTag(ns, ln, QUERY)) {
                queryDepths.push(new Integer(depth));
            }
            // If we're in a <update>, record relevant state
            if (isSqlTag(ns, ln, UPDATE)) {
                updateDepths.push(new Integer(depth));
            }
            // If we're in a <transaction>, record relevant state
            if (isSqlTag(ns, ln, TRANSACTION)) {
                transactionDepths.push(new Integer(depth));
            }

	    // set up a check against illegal attribute/body combinations
	    bodyIllegal = false;
	    bodyNecessary = false;

            if (isSqlTag(ns, ln, QUERY) || isSqlTag(ns, ln, UPDATE)) {
                if (!hasAttribute(a, SQL)) {
                    bodyNecessary = true;
                }
                if (hasAttribute(a, DATASOURCE) && !transactionDepths.empty()) {
                    fail(Resources.getMessage("ERROR_NESTED_DATASOURCE"));
                }
            }

            if (isSqlTag(ns, ln, DATEPARAM)) {
                bodyIllegal = true;
            }

	    // record the most recent tag (for error reporting)
	    lastElementName = qn;
	    lastElementId = a.getValue("http://java.sun.com/JSP/Page", "id");

	    // we're a new element, so increase depth
	    depth++;
	}

	public void characters(char[] ch, int start, int length) {

	    bodyNecessary = false;		// body is no longer necessary!

	    // ignore strings that are just whitespace
	    String s = new String(ch, start, length).trim();
	    if (s.equals(""))
		return;

	    // check and update body-related constraints
	    if (bodyIllegal)
		fail(Resources.getMessage("TLV_ILLEGAL_BODY", lastElementName));
	}

	public void endElement(String ns, String ln, String qn) {

	    // consistently, we ignore JSP_TEXT
	    if (qn.equals(JSP_TEXT))
		return;

	    // handle body-related invariant
	    if (bodyNecessary)
		fail(Resources.getMessage("TLV_MISSING_BODY",
		    lastElementName));
	    bodyIllegal = false;	// reset: we've left the tag

            // update <query>-related state
            if (isSqlTag(ns, ln, QUERY)) {
                queryDepths.pop();
            }
            // update <update>-related state
            if (isSqlTag(ns, ln, UPDATE)) {
                updateDepths.pop();
            }
            // update <update>-related state
            if (isSqlTag(ns, ln, TRANSACTION)) {
                transactionDepths.pop();
            }

	    // update our depth
	    depth--;
	}
    }
}
