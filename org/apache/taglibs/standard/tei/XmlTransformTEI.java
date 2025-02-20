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

package org.apache.taglibs.standard.tei;

import jakarta.servlet.jsp.tagext.TagData;
import jakarta.servlet.jsp.tagext.TagExtraInfo;

/**
 * <p>An implementation of TagExtraInfo that implements validation for
 * &lt;x:transform&gt;'s attributes</p>
 *
 * @author Shawn Bayern
 */
public class XmlTransformTEI extends TagExtraInfo {

    final private static String XSLT = "xslt";
    final private static String RESULT = "result";
    final private static String VAR = "var";

    public boolean isValid(TagData us) {
	// require XSLT
	if (!Util.isSpecified(us, XSLT))
	    return false;

	// disallow both VAR and RESULT
	if (Util.isSpecified(us, VAR) && Util.isSpecified(us, RESULT))
	    return false;
        return true;
    }

}
