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

package org.apache.taglibs.standard.tag.common.fmt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * Support for tag handlers for &lt;parseNumber&gt;, the number parsing tag
 * in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class ParseNumberSupport extends BodyTagSupport {

    //*********************************************************************
    // Private constants

    private static final String NUMBER = "number";    
    private static final String CURRENCY = "currency";
    private static final String PERCENT = "percent";


    //*********************************************************************
    // Protected state

    protected String value;                      // 'value' attribute
    protected boolean valueSpecified;	         // status
    protected String type;                       // 'type' attribute
    protected String pattern;                    // 'pattern' attribute
    protected Locale parseLocale;                // 'parseLocale' attribute
    protected boolean isIntegerOnly;             // 'integerOnly' attribute
    protected boolean integerOnlySpecified;


    //*********************************************************************
    // Private state

    private String var;                          // 'var' attribute
    private int scope;                           // 'scope' attribute


    //*********************************************************************
    // Constructor and initialization

    public ParseNumberSupport() {
	super();
	init();
    }

    private void init() {
	value = type = pattern = var = null;
	valueSpecified = false;
	parseLocale = null;
	integerOnlySpecified = false;
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
    // Tag logic

    public int doEndTag() throws JspException {
	String input = null;

        // determine the input by...
	if (valueSpecified) {
	    // ... reading 'value' attribute
	    input = value;
	} else {
	    // ... retrieving and trimming our body
	    if (bodyContent != null && bodyContent.getString() != null)
	        input = bodyContent.getString().trim();
	}

	if ((input == null) || input.equals("")) {
	    if (var != null) {
		pageContext.removeAttribute(var, scope);
	    }
	    return EVAL_PAGE;
	}

	/*
	 * Set up parsing locale: Use locale specified via the 'parseLocale'
	 * attribute (if present), or else determine page's locale.
	 */
	Locale loc = parseLocale;
	if (loc == null)
	    loc = SetLocaleSupport.getFormattingLocale(
                pageContext,
	        this,
		false,
	        NumberFormat.getAvailableLocales());
	if (loc == null) {
	    throw new JspException(
                    Resources.getMessage("PARSE_NUMBER_NO_PARSE_LOCALE"));
	}

	// Create parser
	NumberFormat parser = null;
	if ((pattern != null) && !pattern.equals("")) {
	    // if 'pattern' is specified, 'type' is ignored
	    DecimalFormatSymbols symbols = new DecimalFormatSymbols(loc);
	    parser = new DecimalFormat(pattern, symbols);
	} else {
	    parser = createParser(loc);
	}

	// Configure parser
	if (integerOnlySpecified)
	    parser.setParseIntegerOnly(isIntegerOnly);

	// Parse number
	Number parsed = null;
	try {
	    parsed = parser.parse(input);
	} catch (ParseException pe) {
	    throw new JspException(
	            Resources.getMessage("PARSE_NUMBER_PARSE_ERROR", input),
		    pe);
	}

	if (var != null) {
	    pageContext.setAttribute(var, parsed, scope);	
	} else {
	    try {
		pageContext.getOut().print(parsed);
	    } catch (IOException ioe) {
		throw new JspTagException(ioe.toString(), ioe);
	    }
	}

	return EVAL_PAGE;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
	init();
    }


    //*********************************************************************
    // Private utility methods

    private NumberFormat createParser(Locale loc) throws JspException {
	NumberFormat parser = null;

	if ((type == null) || NUMBER.equalsIgnoreCase(type)) {
	    parser = NumberFormat.getNumberInstance(loc);
	} else if (CURRENCY.equalsIgnoreCase(type)) {
	    parser = NumberFormat.getCurrencyInstance(loc);
	} else if (PERCENT.equalsIgnoreCase(type)) {
	    parser = NumberFormat.getPercentInstance(loc);
	} else {
	    throw new JspException(
                    Resources.getMessage("PARSE_NUMBER_INVALID_TYPE", 
					 type));
	}

	return parser;
    }
}
