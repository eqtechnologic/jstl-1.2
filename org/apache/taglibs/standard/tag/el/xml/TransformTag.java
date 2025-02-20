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

package org.apache.taglibs.standard.tag.el.xml;

import jakarta.servlet.jsp.JspException;
import javax.xml.transform.Result;

import org.apache.taglibs.standard.tag.common.xml.TransformSupport;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;

/**
 * <p>A handler for &lt;transform&gt; that accepts attributes as Strings
 * and evaluates them as expressions at runtime.</p>
 *
 * @author Shawn Bayern
 */
public class TransformTag extends TransformSupport {

    //*********************************************************************
    // 'Private' state (implementation details)

    private String xml_;                        // stores EL-based property
    private String xmlSystemId_;                // stores EL-based property
    private String xslt_;			// stores EL-based property
    private String xsltSystemId_;		// stores EL-based property
    private String result_;			// stores EL-based property


    //*********************************************************************
    // Constructor

    public TransformTag() {
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

    // for EL-based attribute
    public void setXml(String xml_) {
        this.xml_ = xml_;
    }

    // for EL-based attribute
    public void setXmlSystemId(String xmlSystemId_) {
        this.xmlSystemId_ = xmlSystemId_;
    }

    // for EL-based attribute
    public void setXslt(String xslt_) {
        this.xslt_ = xslt_;
    }

    // for EL-based attribute
    public void setXsltSystemId(String xsltSystemId_) {
        this.xsltSystemId_ = xsltSystemId_;
    }

    /* Removed for RI 0.5 
     // for EL-based attribute
     public void setTransformer(String transformer_) {
         this.transformer_ = transformer_;
     }
    */

    // for EL-based attribute
    public void setResult(String result_) {
        this.result_ = result_;
    }


    //*********************************************************************
    // Private (utility) methods

    // (re)initializes state (during release() or construction)
    private void init() {
        // null implies "no expression"
	xml_ = xmlSystemId = xslt_ = xsltSystemId_ = result_ = null;
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

	xml = ExpressionUtil.evalNotNull(
	    "transform", "xml", xml_, Object.class, this, pageContext);
	xmlSystemId = (String) ExpressionUtil.evalNotNull(
	    "transform", "xmlSystemId", xmlSystemId_, String.class,
            this, pageContext);
	xslt= ExpressionUtil.evalNotNull(
	    "transform", "xslt", xslt_, Object.class, this,
	    pageContext);
	xsltSystemId = (String) ExpressionUtil.evalNotNull(
	    "transform", "xsltSystemId", xsltSystemId_, String.class,
	    this, pageContext);
	result = (Result) ExpressionUtil.evalNotNull(
	    "transform", "result", result_, Result.class, this, pageContext);

    }
}
