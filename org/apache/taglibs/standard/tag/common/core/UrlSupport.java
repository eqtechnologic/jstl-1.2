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

package org.apache.taglibs.standard.tag.common.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>Support for tag handlers for &lt;url&gt;, the URL creation
 * and rewriting tag in JSTL 1.0.</p>
 *
 * @author Shawn Bayern
 */

public abstract class UrlSupport extends BodyTagSupport
    implements ParamParent {

    //*********************************************************************
    // Protected state

    protected String value;                      // 'value' attribute
    protected String context;			 // 'context' attribute

    //*********************************************************************
    // Private state

    private String var;                          // 'var' attribute
    private int scope;				 // processed 'scope' attr
    private ParamSupport.ParamManager params;	 // added parameters

    //*********************************************************************
    // Constructor and initialization

    public UrlSupport() {
	super();
	init();
    }

    private void init() {
	value = var = null;
	params = null;
	context = null;
	scope = PageContext.PAGE_SCOPE;
    }


   //*********************************************************************
    // Tag attributes known at translation time

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
	this.scope = Util.getScope(scope);
    }


    //*********************************************************************
    // Collaboration with subtags

    // inherit Javadoc
    public void addParameter(String name, String value) {
	params.addParameter(name, value);
    }


    //*********************************************************************
    // Tag logic

    // resets any parameters that might be sent
    public int doStartTag() throws JspException {
	params = new ParamSupport.ParamManager();
	return EVAL_BODY_BUFFERED;
    }


    // gets the right value, encodes it, and prints or stores it
    public int doEndTag() throws JspException {
	String result;				// the eventual result

	// add (already encoded) parameters
	String baseUrl = resolveUrl(value, context, pageContext);
	result = params.aggregateParams(baseUrl);

	// if the URL is relative, rewrite it
	if (!ImportSupport.isAbsoluteUrl(result)) {
	    HttpServletResponse response =
                ((HttpServletResponse) pageContext.getResponse());
            result = response.encodeURL(result);
	}

	// store or print the output
	if (var != null)
	    pageContext.setAttribute(var, result, scope);
	else {
	    try {
	        pageContext.getOut().print(result);
	    } catch (java.io.IOException ex) {
		throw new JspTagException(ex.toString(), ex);
	    }
	}

	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
    }

    //*********************************************************************
    // Utility methods

    public static String resolveUrl(
            String url, String context, PageContext pageContext)
	    throws JspException {
	// don't touch absolute URLs
	if (ImportSupport.isAbsoluteUrl(url))
	    return url;

	// normalize relative URLs against a context root
	HttpServletRequest request =
	    (HttpServletRequest) pageContext.getRequest();
	if (context == null) {
	    if (url.startsWith("/"))
		return (request.getContextPath() + url);
	    else
		return url;
	} else {
            if (!context.startsWith("/") || !url.startsWith("/")) {
                throw new JspTagException(
                    Resources.getMessage("IMPORT_BAD_RELATIVE"));
            }
            if (context.equals("/")) {
                // Don't produce string starting with '//', many
                // browsers interpret this as host name, not as
                // path on same host.
                return url;
            } else {
                return (context + url);
            }
        }
    }

}
