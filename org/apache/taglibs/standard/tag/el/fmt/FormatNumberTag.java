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

package org.apache.taglibs.standard.tag.el.fmt;

import jakarta.servlet.jsp.JspException;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.apache.taglibs.standard.tag.common.fmt.FormatNumberSupport;

/**
 * <p>A handler for &lt;formatNumber&gt; that accepts attributes as Strings
 * and evaluates them as expressions at runtime.</p>
 *
 * @author Jan Luehe
 */

public class FormatNumberTag extends FormatNumberSupport {

    //*********************************************************************
    // 'Private' state (implementation details)

    private String value_;                       // stores EL-based property
    private String type_;                        // stores EL-based property
    private String pattern_;		         // stores EL-based property
    private String currencyCode_;   	         // stores EL-based property
    private String currencySymbol_;   	         // stores EL-based property
    private String groupingUsed_;   	         // stores EL-based property
    private String maxIntegerDigits_;   	 // stores EL-based property
    private String minIntegerDigits_;   	 // stores EL-based property
    private String maxFractionDigits_;   	 // stores EL-based property
    private String minFractionDigits_;   	 // stores EL-based property


    //*********************************************************************
    // Constructor

    /**
     * Constructs a new FormatNumberTag.  As with TagSupport, subclasses
     * should not provide other constructors and are expected to call
     * the superclass constructor
     */
    public FormatNumberTag() {
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
    public void setValue(String value_) {
        this.value_ = value_;
	this.valueSpecified = true;
    }

    // for EL-based attribute
    public void setType(String type_) {
        this.type_ = type_;
    }

    // for EL-based attribute
    public void setPattern(String pattern_) {
        this.pattern_ = pattern_;
    }

    // for EL-based attribute
    public void setCurrencyCode(String currencyCode_) {
        this.currencyCode_ = currencyCode_;
    }

    // for EL-based attribute
    public void setCurrencySymbol(String currencySymbol_) {
        this.currencySymbol_ = currencySymbol_;
    }

    // for EL-based attribute
    public void setGroupingUsed(String groupingUsed_) {
        this.groupingUsed_ = groupingUsed_;
	this.groupingUsedSpecified = true;
    }

    // for EL-based attribute
    public void setMaxIntegerDigits(String maxIntegerDigits_) {
        this.maxIntegerDigits_ = maxIntegerDigits_;
	this.maxIntegerDigitsSpecified = true;
    }

    // for EL-based attribute
    public void setMinIntegerDigits(String minIntegerDigits_) {
        this.minIntegerDigits_ = minIntegerDigits_;
	this.minIntegerDigitsSpecified = true;
    }

    // for EL-based attribute
    public void setMaxFractionDigits(String maxFractionDigits_) {
        this.maxFractionDigits_ = maxFractionDigits_;
	this.maxFractionDigitsSpecified = true;
    }

    // for EL-based attribute
    public void setMinFractionDigits(String minFractionDigits_) {
        this.minFractionDigits_ = minFractionDigits_;
	this.minFractionDigitsSpecified = true;
    }


    //*********************************************************************
    // Private (utility) methods

    // (re)initializes state (during release() or construction)
    private void init() {
        // null implies "no expression"
	value_ = type_ = pattern_ = null;
	currencyCode_ = currencySymbol_ = null;
	groupingUsed_ = null;
	maxIntegerDigits_ = minIntegerDigits_ = null;
	maxFractionDigits_ = minFractionDigits_ = null;
    }

    // Evaluates expressions as necessary
    private void evaluateExpressions() throws JspException {
	Object obj = null;

        /* 
         * Note: we don't check for type mismatches here; we assume
         * the expression evaluator will return the expected type
         * (by virtue of knowledge we give it about what that type is).
         * A ClassCastException here is truly unexpected, so we let it
         * propagate up.
         */

	// 'value' attribute
	if (value_ != null) {
	    value = ExpressionEvaluatorManager.evaluate(
	        "value", value_, Object.class, this, pageContext);
	}

	// 'type' attribute
	if (type_ != null) {
	    type = (String) ExpressionEvaluatorManager.evaluate(
	        "type", type_, String.class, this, pageContext);
	}

	// 'pattern' attribute
	if (pattern_ != null) {
	    pattern = (String) ExpressionEvaluatorManager.evaluate(
	        "pattern", pattern_, String.class, this, pageContext);
	}

	// 'currencyCode' attribute
	if (currencyCode_ != null) {
	    currencyCode = (String) ExpressionEvaluatorManager.evaluate(
	        "currencyCode", currencyCode_, String.class, this,
		pageContext);
	}

	// 'currencySymbol' attribute
	if (currencySymbol_ != null) {
	    currencySymbol = (String) ExpressionEvaluatorManager.evaluate(
	        "currencySymbol", currencySymbol_, String.class, this,
		pageContext);
	}

	// 'groupingUsed' attribute
	if (groupingUsed_ != null) {
	    obj = ExpressionEvaluatorManager.evaluate(
	        "groupingUsed", groupingUsed_, Boolean.class, this,
		pageContext);
	    if (obj != null) {
		isGroupingUsed = ((Boolean) obj).booleanValue();
	    }
	}

	// 'maxIntegerDigits' attribute
	if (maxIntegerDigits_ != null) {
	    obj = ExpressionEvaluatorManager.evaluate(
	        "maxIntegerDigits", maxIntegerDigits_, Integer.class, this,
		pageContext);
	    if (obj != null) {
		maxIntegerDigits = ((Integer) obj).intValue();
	    }
	}

	// 'minIntegerDigits' attribute	
	if (minIntegerDigits_ != null) {
	    obj = ExpressionEvaluatorManager.evaluate(
	        "minIntegerDigits", minIntegerDigits_, Integer.class, this,
		pageContext);
	    if (obj != null) {
		minIntegerDigits = ((Integer) obj).intValue();
	    }
	}

	// 'maxFractionDigits' attribute
	if (maxFractionDigits_ != null) {
	    obj = ExpressionEvaluatorManager.evaluate(
	        "maxFractionDigits", maxFractionDigits_, Integer.class, this,
		pageContext);
	    if (obj != null) {
		maxFractionDigits = ((Integer) obj).intValue();
	    }
	}

	// 'minFractionDigits' attribute
	if (minFractionDigits_ != null) {
	    obj = ExpressionEvaluatorManager.evaluate(
	        "minFractionDigits", minFractionDigits_, Integer.class, this,
		pageContext);
	    if (obj != null) {
		minFractionDigits = ((Integer) obj).intValue();
	    }
	}
    }
}

