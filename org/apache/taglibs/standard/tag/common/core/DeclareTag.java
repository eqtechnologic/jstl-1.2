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

import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * <p>Tag handler for &lt;declaregt; in JSTL.  This tag handler is
 * essentially a runtime no-op as far as tag logic is concerned; the
 * only important functionality of the tag is to expose a scripting
 * variable for an existing scoped attribute.</p>
 * 
 * @author Shawn Bayern
 */

public class DeclareTag extends TagSupport {

    /*
     * We're not identical to TagSupport only because we need to
     * accept an additional "type" attribute.
     */
    public void setType(String x) { }
}
