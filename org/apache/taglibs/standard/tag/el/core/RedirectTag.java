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

package org.apache.taglibs.standard.tag.el.core;

import jakarta.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.core.RedirectSupport;

/**
 * <p>A handler for &lt;redirect&gt;, which redirects the browser to a
 * new URL.
 *
 * @author Shawn Bayern
 */

public class RedirectTag extends RedirectSupport {

    //*********************************************************************
    // 'Private' state (implementation details)

    private String url_;			// stores EL-based property
    private String context_;			// stores EL-based property


    //*********************************************************************
    // Constructor

    /**
     * Constructs a new URLEncodeTag.  As with TagSupport, subclasses
     * should not provide other constructors and are expected to call
     * the superclass constructor
     */
    public RedirectTag() {
        super();
        init();
    }


    //*********************************************************************
    // Tag logic

    // evaluates expression and chains to parent
    public int doStartTag() throws JspException {

        // evaluate any expressions we were passed, once per invocation
        evaluateExpressions();

	// chain to the parent implementation
	return super.doStartTag();
    }


    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Accessor methods

    public void setUrl(String url_) {
        this.url_ = url_;
    }

    public void setContext(String context_) {
        this.context_ = context_;
    }

    //*********************************************************************
    // Private (utility) methods

    // (re)initializes state (during release() or construction)
    private void init() {
        // null implies "no expression"
	url_ = context_ = null;
    }

    /* Evaluates expressions as necessary */
    private void evaluateExpressions() throws JspException {
        /* 
         * Note: we don't check for type mismatches here; we assume
         * the expression evaluator will return the expected type
         * (by virtue of knowledge we give it about what that type is).
         * A ClassCastException here is truly unexpected, so we let it
         * propagate up.
         */

	url = (String) ExpressionUtil.evalNotNull(
	    "redirect", "url", url_, String.class, this, pageContext);
	context = (String) ExpressionUtil.evalNotNull(
	    "redirect", "context", context_, String.class, this, pageContext);
    }
}
