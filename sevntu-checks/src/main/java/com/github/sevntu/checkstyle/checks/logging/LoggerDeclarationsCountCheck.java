////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.github.sevntu.checkstyle.checks.logging;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * This checks that classes have at most 1 Logger.
 *
 * @author TBA, Cisco Systems, Inc. and others.
 */
public class LoggerDeclarationsCountCheck extends AbstractCheck {

    /**
     * Violation message key.
     */
    public static final String MSG_KEY = "logging.count.check";

    /**
     * Field to hold previously seen Logger.
     */
    private String prevClassName = "";

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (CheckLoggingUtil.isLoggerType(ast) && CheckLoggingUtil.isAFieldVariable(ast)) {
            final String className = CheckLoggingUtil.getClassName(ast);
            if (this.prevClassName.equals(className)) {
                log(ast.getLineNo(), MSG_KEY);
            }
            this.prevClassName = className;
        }
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        super.finishTree(rootAST);
        this.prevClassName = "";
    }

}
