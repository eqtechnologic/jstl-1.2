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
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.apache.taglibs.standard.tag.common.core.NullAttributeException;

/**
 * <p>Contains some static utilities to facilitate common forms of
 * expression evaluation.</p>
 *
 * @author Shawn Bayern
 */

public class ExpressionUtil {

    /** Evaluates an expression if present, but does not allow the expression
     *  to evaluate to 'null', throwing a NullAttributeException if it
     *  does.  The function <b>can</b> return null, however, if the
     *  expression itself is null.
     */
    public static Object evalNotNull(String tagName,
				     String attributeName,
	                             String expression,
				     Class expectedType,
				     Tag tag,
	                             PageContext pageContext)
	        throws JspException {
        if (expression != null) {
            Object r = ExpressionEvaluatorManager.evaluate(
                attributeName, expression, expectedType, tag, pageContext);
            if (r == null)
                throw new NullAttributeException(tagName, attributeName);
	    return r;
        } else
	    return null;
    }
}
