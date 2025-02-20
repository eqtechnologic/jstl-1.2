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

import javax.xml.xpath.XPathVariableResolver;
import javax.xml.namespace.QName;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;


/**
  * The XPathVariableResolver implementation provides access to user
  * XPath variables.
  */
public class JSTLXPathVariableResolver implements XPathVariableResolver {

    //*********************************************************************
    // Support for XPath evaluation

    private PageContext pageContext;

    // The URLs
    private static final String PAGE_NS_URL = 
            "http://java.sun.com/jstl/xpath/page";
    private static final String REQUEST_NS_URL = 
            "http://java.sun.com/jstl/xpath/request";
    private static final String SESSION_NS_URL = 
            "http://java.sun.com/jstl/xpath/session";
    private static final String APP_NS_URL = 
            "http://java.sun.com/jstl/xpath/app";
    private static final String PARAM_NS_URL = 
            "http://java.sun.com/jstl/xpath/param";
    private static final String INITPARAM_NS_URL = 
            "http://java.sun.com/jstl/xpath/initParam";
    private static final String COOKIE_NS_URL = 
            "http://java.sun.com/jstl/xpath/cookie";
    private static final String HEADER_NS_URL = 
            "http://java.sun.com/jstl/xpath/header";
    
    //*********************************************************************
    // Constructor

    public JSTLXPathVariableResolver(PageContext pc) {
        pageContext = pc;
    }

    /**
     * Find variable in set of variables
     *
     * @param QName variable name
     *
     * @return QName variables value
     *
     * @throws NullPointerException if variable name is null
     */
    public Object resolveVariable(QName qname) throws NullPointerException {

        Object varObject = null;

        if (qname == null) {
            throw new NullPointerException("Cannot resolve null variable");
        }

        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localName = qname.getLocalPart();
        // p("[resolveVariable] namespace: " + namespace + " prefix: " + prefix + " localName: " + localName);

        try {
            varObject = getVariableValue(namespace, prefix, localName);
        } catch (UnresolvableException ue) {
            System.out.println("JSTLXpathVariableResolver.resolveVariable threw UnresolvableException: " + ue);
        }

        // p("[resolveVariable] varObject: " + varObject);
        return varObject;
    }

    /** 
     * Retrieve an XPath's variable value using JSTL's custom 
     * variable-mapping rules
     */ 
    protected Object getVariableValue(String namespace,
                                      String prefix,
                                      String localName) 
                                      throws UnresolvableException {       
        // p("resolving: ns=" + namespace + " prefix=" + prefix + " localName=" + localName);  
        // We can match on namespace with Xalan but leaving as is
        // [ I 'd prefer to match on namespace, but this doesn't appear
        // to work in Jaxen] 
        if (namespace == null || namespace.equals("")) {
            return notNull(
            pageContext.findAttribute(localName),
            namespace,
            localName);
        } else if (namespace.equals(PAGE_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,PageContext.PAGE_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(REQUEST_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,
            PageContext.REQUEST_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(SESSION_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,
            PageContext.SESSION_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(APP_NS_URL)) {
            return notNull(
            pageContext.getAttribute(localName,
            PageContext.APPLICATION_SCOPE),
            namespace,
            localName);
        } else if (namespace.equals(PARAM_NS_URL)) {
            return notNull(
            pageContext.getRequest().getParameter(localName),
            namespace,
            localName);            
        } else if (namespace.equals(INITPARAM_NS_URL)) {
            return notNull(
            pageContext.getServletContext().
            getInitParameter(localName),
            namespace,
            localName);
        } else if (namespace.equals(HEADER_NS_URL)) {
            HttpServletRequest hsr =
            (HttpServletRequest) pageContext.getRequest();
            return notNull(
            hsr.getHeader(localName),
            namespace,
            localName);
        } else if (namespace.equals(COOKIE_NS_URL)) {
            HttpServletRequest hsr =
            (HttpServletRequest) pageContext.getRequest();
            Cookie[] c = hsr.getCookies();
            for (int i = 0; i < c.length; i++)
                if (c[i].getName().equals(localName))
                    return c[i].getValue();
            throw new UnresolvableException("$" + namespace + ":" + localName);
        } else {
            throw new UnresolvableException("$" + namespace + ":" + localName);
        }
    }   

    /**
     * Validate that the Object returned is not null. If it is
     * null, throw an exception.
     */
    private Object notNull(Object o, String namespace, String localName)
        throws UnresolvableException {
        if (o == null) {
            throw new UnresolvableException("$" + (namespace==null?"":namespace+":") + localName);
        }
        // p("resolved to: " + o);
        return o;
    } 
    
    //*********************************************************************
    // Utility methods

    private static void p(String s) {
        System.out.println("[JSTLXPathVariableResolver] " + s);
    }

}
