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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>Tag handler for &lt;choose&gt; in JSTL.</p>
 * 
 * <p>&lt;choose&gt; is a very simple tag that acts primarily as a container;
 * it always includes its body and allows exactly one of its child
 * &lt;when&gt; tags to run.  Since this tag handler doesn't have any
 * attributes, it is common.core to both the rtexprvalue and expression-
 * evaluating versions of the JSTL library.
 *
 * @author Shawn Bayern
 */

public class ChooseTag extends TagSupport {

    //*********************************************************************
    // Constructor and lifecycle management

    // initialize inherited and local state
    public ChooseTag() {
        super();
        init();
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Private state

    private boolean subtagGateClosed;      // has one subtag already executed?


    //*********************************************************************
    // Public methods implementing exclusivity checks

    /**
     * Returns status indicating whether a subtag should run or not.
     *
     * @return <tt>true</tt> if the subtag should evaluate its condition
     *         and decide whether to run, <tt>false</tt> otherwise.
     */
    public synchronized boolean gainPermission() {
        return (!subtagGateClosed);
    }

    /**
     * Called by a subtag to indicate that it plans to evaluate its
     * body.
     */
    public synchronized void subtagSucceeded() {
        if (subtagGateClosed)
            throw new IllegalStateException(
		Resources.getMessage("CHOOSE_EXCLUSIVITY"));
        subtagGateClosed = true;
    }


    //*********************************************************************
    // Tag logic

    // always include body
    public int doStartTag() throws JspException {
        subtagGateClosed = false;	// when we start, no children have run
        return EVAL_BODY_INCLUDE;
    }


    //*********************************************************************
    // Private utility methods

    private void init() {
        subtagGateClosed = false;                          // reset flag
    }
}
