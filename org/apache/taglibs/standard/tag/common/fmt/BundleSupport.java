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
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.jstl.core.Config;
import jakarta.servlet.jsp.jstl.fmt.LocalizationContext;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * Support for tag handlers for &lt;bundle&gt;, the resource bundle loading
 * tag in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class BundleSupport extends BodyTagSupport {
    

    //*********************************************************************
    // Private constants

    private static final Locale EMPTY_LOCALE = new Locale("", "");


    //*********************************************************************
    // Protected state

    protected String basename;                  // 'basename' attribute
    protected String prefix;                    // 'prefix' attribute


    //*********************************************************************
    // Private state

    private Locale fallbackLocale;
    private LocalizationContext locCtxt;


    //*********************************************************************
    // Constructor and initialization

    public BundleSupport() {
	super();
	init();
    }

    private void init() {
	basename = prefix = null;
	locCtxt = null;
    }

    
    //*********************************************************************
    // Collaboration with subtags

    public LocalizationContext getLocalizationContext() {
	return locCtxt;
    }

    public String getPrefix() {
	return prefix;
    }


    //*********************************************************************
    // Tag logic

    public int doStartTag() throws JspException {
	locCtxt = getLocalizationContext(pageContext, basename);
	return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
	if (bodyContent != null) {
	    try {
		pageContext.getOut().print(bodyContent.getString());
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
    // Public utility methods

    /**
     * Gets the default I18N localization context.
     *
     * @param pc Page in which to look up the default I18N localization context
     */    
    public static LocalizationContext getLocalizationContext(PageContext pc) {
	LocalizationContext locCtxt = null;

	Object obj = Config.find(pc, Config.FMT_LOCALIZATION_CONTEXT);
	if (obj == null) {
	    return null;
	}

	if (obj instanceof LocalizationContext) {
	    locCtxt = (LocalizationContext) obj;
	} else {
	    // localization context is a bundle basename
	    locCtxt = getLocalizationContext(pc, (String) obj);
	}

	return locCtxt;
    }

    /**
     * Gets the resource bundle with the given base name, whose locale is
     * determined as follows:
     *
     * Check if a match exists between the ordered set of preferred
     * locales and the available locales, for the given base name.
     * The set of preferred locales consists of a single locale
     * (if the <tt>jakarta.servlet.jsp.jstl.fmt.locale</tt> configuration
     * setting is present) or is equal to the client's preferred locales
     * determined from the client's browser settings.
     *
     * <p> If no match was found in the previous step, check if a match
     * exists between the fallback locale (given by the
     * <tt>jakarta.servlet.jsp.jstl.fmt.fallbackLocale</tt> configuration
     * setting) and the available locales, for the given base name.
     *
     * @param pageContext Page in which the resource bundle with the
     * given base name is requested
     * @param basename Resource bundle base name
     *
     * @return Localization context containing the resource bundle with the
     * given base name and the locale that led to the resource bundle match,
     * or the empty localization context if no resource bundle match was found
     */
    public static LocalizationContext getLocalizationContext(PageContext pc,
							     String basename) {
	LocalizationContext locCtxt = null;
	ResourceBundle bundle = null;

	if ((basename == null) || basename.equals("")) {
	    return new LocalizationContext();
	}

	// Try preferred locales
	Locale pref = SetLocaleSupport.getLocale(pc, Config.FMT_LOCALE);
	if (pref != null) {
	    // Preferred locale is application-based
	    bundle = findMatch(basename, pref);
	    if (bundle != null) {
		locCtxt = new LocalizationContext(bundle, pref);
	    }
	} else {
	    // Preferred locales are browser-based
	    locCtxt = findMatch(pc, basename);
	}
	
	if (locCtxt == null) {
	    // No match found with preferred locales, try using fallback locale
	    pref = SetLocaleSupport.getLocale(pc, Config.FMT_FALLBACK_LOCALE);
	    if (pref != null) {
		bundle = findMatch(basename, pref);
		if (bundle != null) {
		    locCtxt = new LocalizationContext(bundle, pref);
		}
	    }
	}

	if (locCtxt == null) {
	    // try using the root resource bundle with the given basename
	    try {
		bundle = ResourceBundle.getBundle(basename, EMPTY_LOCALE,
						  Thread.currentThread().getContextClassLoader());
		if (bundle != null) {
		    locCtxt = new LocalizationContext(bundle, null);
		}
	    } catch (MissingResourceException mre) {
		// do nothing
	    }
	}
		 
	if (locCtxt != null) {
	    // set response locale
	    if (locCtxt.getLocale() != null) {
		SetLocaleSupport.setResponseLocale(pc, locCtxt.getLocale());
	    }
	} else {
	    // create empty localization context
	    locCtxt = new LocalizationContext();
	}

	return locCtxt;
    }


    //*********************************************************************
    // Private utility methods
    
    /*
     * Determines the client's preferred locales from the request, and compares
     * each of the locales (in order of preference) against the available
     * locales in order to determine the best matching locale.
     *
     * @param pageContext the page in which the resource bundle with the
     * given base name is requested
     * @param basename the resource bundle's base name
     *
     * @return the localization context containing the resource bundle with
     * the given base name and best matching locale, or <tt>null</tt> if no
     * resource bundle match was found
     */
    private static LocalizationContext findMatch(PageContext pageContext,
						 String basename) {
	LocalizationContext locCtxt = null;
	
	// Determine locale from client's browser settings.
        
	for (Enumeration enum_ = Util.getRequestLocales((HttpServletRequest)pageContext.getRequest());
	     enum_.hasMoreElements(); ) {
	    Locale pref = (Locale) enum_.nextElement();
	    ResourceBundle match = findMatch(basename, pref);
	    if (match != null) {
		locCtxt = new LocalizationContext(match, pref);
		break;
	    }
	}
        	
	return locCtxt;
    }

    /*
     * Gets the resource bundle with the given base name and preferred locale.
     * 
     * This method calls java.util.ResourceBundle.getBundle(), but ignores
     * its return value unless its locale represents an exact or language match
     * with the given preferred locale.
     *
     * @param basename the resource bundle base name
     * @param pref the preferred locale
     *
     * @return the requested resource bundle, or <tt>null</tt> if no resource
     * bundle with the given base name exists or if there is no exact- or
     * language-match between the preferred locale and the locale of
     * the bundle returned by java.util.ResourceBundle.getBundle().
     */
    private static ResourceBundle findMatch(String basename, Locale pref) {
	ResourceBundle match = null;

	try {
	    ResourceBundle bundle =
		ResourceBundle.getBundle(basename, pref,
					 Thread.currentThread().getContextClassLoader());
	    Locale avail = bundle.getLocale();
	    if (pref.equals(avail)) {
		// Exact match
		match = bundle;
	    } else {
                /*
                 * We have to make sure that the match we got is for
                 * the specified locale. The way ResourceBundle.getBundle()
                 * works, if a match is not found with (1) the specified locale,
                 * it tries to match with (2) the current default locale as 
                 * returned by Locale.getDefault() or (3) the root resource 
                 * bundle (basename).
                 * We must ignore any match that could have worked with (2) or (3).
                 * So if an exact match is not found, we make the following extra
                 * tests:
                 *     - avail locale must be equal to preferred locale
                 *     - avail country must be empty or equal to preferred country
                 *       (the equality match might have failed on the variant)
		 */
                if (pref.getLanguage().equals(avail.getLanguage())
		    && ("".equals(avail.getCountry()) || pref.getCountry().equals(avail.getCountry()))) {
		    /*
		     * Language match.
		     * By making sure the available locale does not have a 
		     * country and matches the preferred locale's language, we
		     * rule out "matches" based on the container's default
		     * locale. For example, if the preferred locale is 
		     * "en-US", the container's default locale is "en-UK", and
		     * there is a resource bundle (with the requested base
		     * name) available for "en-UK", ResourceBundle.getBundle()
		     * will return it, but even though its language matches
		     * that of the preferred locale, we must ignore it,
		     * because matches based on the container's default locale
		     * are not portable across different containers with
		     * different default locales.
		     */
		    match = bundle;
		}
	    }
	} catch (MissingResourceException mre) {
	}

	return match;
    }
}
