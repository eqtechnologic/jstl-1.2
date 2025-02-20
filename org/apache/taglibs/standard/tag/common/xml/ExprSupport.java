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

package org.apache.taglibs.standard.tag.common.xml;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * <p>Tag handler for &lt;expr&gt; in JSTL's XML library.</p>
 *
 * @author Shawn Bayern
 */

public abstract class ExprSupport extends TagSupport {

    //*********************************************************************
    // Internal state

    private String select;                       // tag attribute
    protected boolean escapeXml;		 // tag attribute

    //*********************************************************************
    // Construction and initialization

    /**
     * Constructs a new handler.  As with TagSupport, subclasses should
     * not provide other constructors and are expected to call the
     * superclass constructor.
     */
    public ExprSupport() {
        super();
        init();
    }

    // resets local state
    private void init() {
	select = null;
        escapeXml = true;
    }


    //*********************************************************************
    // Tag logic

    // applies XPath expression from 'select' and prints the result
    public int doStartTag() throws JspException {
        try {
	    XPathUtil xu = new XPathUtil(pageContext);
	    String result = xu.valueOf(XPathUtil.getContext(this), select);
	    org.apache.taglibs.standard.tag.common.core.OutSupport.out(
              pageContext, escapeXml, result);
	    return SKIP_BODY;
        } catch (java.io.IOException ex) {
	    throw new JspTagException(ex.toString(), ex);
        }
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Attribute accessors

    public void setSelect(String select) {
	this.select = select;
    }
}
