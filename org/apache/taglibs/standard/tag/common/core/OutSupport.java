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

import java.io.IOException;
import java.io.Reader;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

/**
 * <p>Support for handlers of the &lt;out&gt; tag, which simply evalutes and
 * prints the result of the expression it's passed.  If the result is
 * null, we print the value of the 'default' attribute's expression or
 * our body (which two are mutually exclusive, although this constraint
 * is enforced outside this handler, in our TagLibraryValidator).</p>
 *
 * @author Shawn Bayern
 */
public class OutSupport extends BodyTagSupport {

    /*
     * (One almost wishes XML and JSP could support "anonymous tags,"
     * given the amount of trouble we had naming this one!)  :-)  - sb
     */

    //*********************************************************************
    // Internal state

    protected Object value;                     // tag attribute
    protected String def;			// tag attribute
    protected boolean escapeXml;		// tag attribute
    private boolean needBody;			// non-space body needed?

    //*********************************************************************
    // Construction and initialization

    /**
     * Constructs a new handler.  As with TagSupport, subclasses should
     * not provide other constructors and are expected to call the
     * superclass constructor.
     */
    public OutSupport() {
        super();
        init();
    }

    // resets local state
    private void init() {
        value = def = null;
        escapeXml = true;
	needBody = false;
    }

    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Tag logic

    // evaluates 'value' and determines if the body should be evaluted
    public int doStartTag() throws JspException {

      needBody = false;			// reset state related to 'default'
      this.bodyContent = null;  // clean-up body (just in case container is pooling tag handlers)
      
      try {
	// print value if available; otherwise, try 'default'
	if (value != null) {
            out(pageContext, escapeXml, value);
	    return SKIP_BODY;
	} else {
	    // if we don't have a 'default' attribute, just go to the body
	    if (def == null) {
		needBody = true;
		return EVAL_BODY_BUFFERED;
	    }

	    // if we do have 'default', print it
	    if (def != null) {
		// good 'default'
                out(pageContext, escapeXml, def);
	    }
	    return SKIP_BODY;
	}
      } catch (IOException ex) {
	throw new JspException(ex.toString(), ex);
      }
    }

    // prints the body if necessary; reports errors
    public int doEndTag() throws JspException {
      try {
	if (!needBody)
	    return EVAL_PAGE;		// nothing more to do

	// trim and print out the body
	if (bodyContent != null && bodyContent.getString() != null)
            out(pageContext, escapeXml, bodyContent.getString().trim());
	return EVAL_PAGE;
      } catch (IOException ex) {
	throw new JspException(ex.toString(), ex);
      }
    }


    //*********************************************************************
    // Public utility methods

    /**
     * Outputs <tt>text</tt> to <tt>pageContext</tt>'s current JspWriter.
     * If <tt>escapeXml</tt> is true, performs the following substring
     * replacements (to facilitate output to XML/HTML pages):
     *
     *    & -> &amp;
     *    < -> &lt;
     *    > -> &gt;
     *    " -> &#034;
     *    ' -> &#039;
     *
     * See also Util.escapeXml().
     */
    public static void out(PageContext pageContext,
                           boolean escapeXml,
                           Object obj) throws IOException {
        JspWriter w = pageContext.getOut();
	if (!escapeXml) {
            // write chars as is
            if (obj instanceof Reader) {
                Reader reader = (Reader)obj;
                char[] buf = new char[4096];
                int count;
                while ((count=reader.read(buf, 0, 4096)) != -1) {
                    w.write(buf, 0, count);
                }
            } else {
                w.write(obj.toString());
            }
        } else {
            // escape XML chars
            if (obj instanceof Reader) {
                Reader reader = (Reader)obj;
                char[] buf = new char[4096];
                int count;
                while ((count = reader.read(buf, 0, 4096)) != -1) {
                    writeEscapedXml(buf, count, w);
                }
            } else {
                String text = obj.toString();
                writeEscapedXml(text.toCharArray(), text.length(), w);
            }
        }
    }

   /**
     *
     *  Optimized to create no extra objects and write directly
     *  to the JspWriter using blocks of escaped and unescaped characters
     *
     */
    private static void writeEscapedXml(char[] buffer, int length, JspWriter w) throws IOException{
        int start = 0;

        for (int i = 0; i < length; i++) {
            char c = buffer[i];
            if (c <= Util.HIGHEST_SPECIAL) {
                char[] escaped = Util.specialCharactersRepresentation[c];
                if (escaped != null) {
                    // add unescaped portion
                    if (start < i) {
                        w.write(buffer,start,i-start);
                    }
                    // add escaped xml
                    w.write(escaped);
                    start = i + 1;
                }
            }
        }
        // add rest of unescaped portion
        if (start < length) {
            w.write(buffer,start,length-start);
        }
    }
}
