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

import java.util.List;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtils;

public final class CheckLoggingUtil {

    public static final String LOGGER_TYPE_NAME = "Logger";
    public static final String LOGGER_TYPE_FULL_NAME = "org.slf4j.Logger";
    public static final String LOGGER_VAR_NAME = "LOG";
    private static final List<String> LOG_METHODS =
            Lists.newArrayList("LOG.debug", "LOG.info", "LOG.error", "LOG.warn", "LOG.trace");

    private CheckLoggingUtil() {
        throw new UnsupportedOperationException("Utility class should not be instantiated!");
    }

    public static String getTypeName(final DetailAST ast) {
        final FullIdent ident = CheckUtils.createFullType(ast.findFirstToken(TokenTypes.TYPE));
        return ident.getText();
    }

    public static boolean isLoggerType(final DetailAST ast) {
        final String typeName = getTypeName(ast);
        return typeName.equals(LOGGER_TYPE_FULL_NAME) || typeName.equals(LOGGER_TYPE_NAME);
    }

    public static String getVariableName(final DetailAST ast) {
        final DetailAST identifier = ast.findFirstToken(TokenTypes.IDENT);
        return identifier.getText();
    }

    public static boolean isAFieldVariable(final DetailAST ast) {
        return ast.getParent().getType() == TokenTypes.OBJBLOCK;
    }

    /**
     * Returns the name the method (and the enclosing class) at a given point specified by the
     * passed-in abstract syntax tree (AST).
     *
     * @param ast an abstract syntax tree (AST) pointing to method call
     * @return the name of the method being called
     */
    public static String getMethodName(final DetailAST ast) {
        if (ast.getFirstChild().getLastChild() != null) {
            return ast.getFirstChild().getFirstChild().getText() + "." + ast.getFirstChild().getLastChild().getText();
        }
        return ast.getFirstChild().getText();
    }

    public static boolean isLogMethod(final String methodName) {
        return LOG_METHODS.contains(methodName);
    }

    /**
     * Returns the name of the closest enclosing class of the point by the passed-in abstract syntax
     * tree (AST).
     *
     * @param ast an abstract syntax tree (AST)
     * @return the name of the closest enclosign class
     */
    public static String getClassName(final DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent.getType() != TokenTypes.CLASS_DEF && parent.getType() != TokenTypes.ENUM_DEF) {
            parent = parent.getParent();
        }
        return parent.findFirstToken(TokenTypes.IDENT).getText();
    }

}
