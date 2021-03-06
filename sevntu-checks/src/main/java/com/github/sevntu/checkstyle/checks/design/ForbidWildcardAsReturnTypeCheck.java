package com.github.sevntu.checkstyle.checks.design;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import antlr.collections.AST;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Prevents using wildcards as return type of methods.
 * <p>
 * <i>Joshua Bloch, "Effective Java (2nd edition)" Item 28: page 137 :</i>
 * </p>
 * <p>
 * "Do not use wildcard types as return types. Rather than providing
 * additional flexibility for your users,
 * it would force them to use wildcard types in client code. Properly used,
 * wildcard types are nearly invisible to users of a class. They cause methods
 * to accept the parameters they should accept and reject those they should
 * reject. If the user of a class has to think about wildcard types, there is
 * probably something wrong with the class’s API."
 * </p>
 * @author <a href='mailto:barataliba@gmail.com'>Baratali Izmailov</a>
 */
public class ForbidWildcardAsReturnTypeCheck extends Check
{
    /**
     * Key for error message.
     */
    public static final String MSG_KEY = "forbid.wildcard.as.return.type";
    /**
     * Token of 'extends' keyword in bounded wildcard.
     */
    private static final int WILDCARD_EXTENDS_IDENT =
            TokenTypes.TYPE_UPPER_BOUNDS;
    /**
     * Token of 'super' keyword in bounded wildcard.
     */
    private static final int WILDCARD_SUPER_IDENT =
            TokenTypes.TYPE_LOWER_BOUNDS;
    /**
     * Empty array of DetailAST.
     */
    private static final DetailAST[] EMPTY_DETAILAST_ARRAY = new DetailAST[0];
    /**
     * Check methods with 'public' modifier.
     */
    private boolean checkPublicMethods = true;
    /**
     * Check methods with 'protected' modifier.
     */
    private boolean checkProtectedMethods = true;
    /**
     * Check methods with 'package' modifier.
     */
    private boolean checkPackageMethods = true;
    /**
     * Check methods with 'private' modifier.
     */
    private boolean checkPrivateMethods;
    /**
     * Check methods with @Override annotation.
     */
    private boolean checkOverrideMethods;
    /**
     * Check methods with @Deprecated annotation.
     */
    private boolean checkDeprecatedMethods;
    /**
     * Allow wildcard with 'super'. Example: "? super T"
     */
    private boolean allowReturnWildcardWithSuper;
    /**
     * Allow wildcard with 'extends'. Example: "? extends T"
     */
    private boolean allowReturnWildcardWithExtends;
    /**
     * Ignore regexp for return type class names.
     */
    private Pattern returnTypeClassNamesIgnoreRegex = Pattern.compile(
            "^(Comparator|Comparable)$");

    public void setCheckPublicMethods(boolean checkPublicMethods)
    {
        this.checkPublicMethods = checkPublicMethods;
    }

    public void setCheckProtectedMethods(boolean checkProtectedMethods)
    {
        this.checkProtectedMethods = checkProtectedMethods;
    }

    public void setCheckPackageMethods(boolean checkPackageMethods)
    {
        this.checkPackageMethods = checkPackageMethods;
    }

    public void setCheckPrivateMethods(boolean checkPrivateMethods)
    {
        this.checkPrivateMethods = checkPrivateMethods;
    }

    public void setCheckOverrideMethods(boolean checkOverrideMethods)
    {
        this.checkOverrideMethods = checkOverrideMethods;
    }
    
    public void setCheckDeprecatedMethods(boolean checkDeprecatedMethods)
    {
        this.checkDeprecatedMethods = checkDeprecatedMethods;
    }

    public void
    setAllowReturnWildcardWithSuper(boolean allowReturnWildcardWithSuper)
    {
        this.allowReturnWildcardWithSuper = allowReturnWildcardWithSuper;
    }

    public void
    setAllowReturnWildcardWithExtends(boolean allowReturnWildcardWithExtends)
    {
        this.allowReturnWildcardWithExtends = allowReturnWildcardWithExtends;
    }

    public void
    setReturnTypeClassNamesIgnoreRegex(String returnTypeClassNamesIgnoreRegex)
    {
        this.returnTypeClassNamesIgnoreRegex = Pattern.compile(
                returnTypeClassNamesIgnoreRegex);
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] { TokenTypes.METHOD_DEF };
    }

    @Override
    public void visitToken(DetailAST methodDefAst)
    {
        final String methodScope = getVisibilityScope(methodDefAst);
        if (((checkPublicMethods && "public".equals(methodScope))
                || (checkPrivateMethods && "private".equals(methodScope))
                || (checkProtectedMethods && "protected".equals(methodScope))
                || (checkPackageMethods && "package".equals(methodScope)))
                && (checkOverrideMethods 
                        || !hasAnnotation(methodDefAst, "Override"))
                && (checkDeprecatedMethods
                        || !hasAnnotation(methodDefAst, "Deprecated")))
        {
            final List<DetailAST> wildcardTypeArguments =
                    getWildcardArgumentsAsMethodReturnType(methodDefAst);
            if (!wildcardTypeArguments.isEmpty()
                    && !isIgnoreCase(methodDefAst, wildcardTypeArguments))
            {
                log(methodDefAst.getLineNo(), MSG_KEY);
            }
        }
    }

    /**
     * Returns the visibility scope of method.
     * @param methodDefAst DetailAST of method definition.
     * @return one of "public", "private", "protected", "package"
     */
    private static String getVisibilityScope(DetailAST methodDefAst)
    {
        String result = "package";
        if (isInsideInterfaceDefinition(methodDefAst)) {
            result = "public";
        }
        else {
            final String[] visibilityScopeModifiers = {"public", "private",
                "protected", };
            final Set<String> methodModifiers = getModifiers(methodDefAst);
            for (final String modifier : visibilityScopeModifiers) {
                if (methodModifiers.contains(modifier)) {
                    result = modifier;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Verify that method definition is inside interface definition.
     * @param methodDefAst DetailAST of method definition.
     * @return true if method definition is inside interface definition.
     */
    private static boolean isInsideInterfaceDefinition(DetailAST methodDefAst)
    {
        boolean result = false;
        final DetailAST objBlock = methodDefAst.getParent();
        final DetailAST interfaceDef = objBlock.getParent();
        if (interfaceDef.getType() == TokenTypes.INTERFACE_DEF) {
            result = true;
        }
        return result;
    }

    /**
     * Returns the set of modifier Strings for a METHOD_DEF AST.
     * @param methodDefAst AST for a method definition
     * @return the set of modifier Strings for aMethodDefAST
     */
    private static Set<String> getModifiers(DetailAST methodDefAst)
    {
        final AST modifiersAst = methodDefAst.getFirstChild();
        final Set<String> modifiersSet = new HashSet<String>();
        AST modifierAst = modifiersAst.getFirstChild();
        while (modifierAst != null) {
            modifiersSet.add(modifierAst.getText());
            modifierAst = modifierAst.getNextSibling();
        }
        return modifiersSet;
    }

    /**
     * Get identifier of aAST.
     * @param ast
     *        DetailAST instance
     * @return identifier of aAST, null if AST does not have identifier.
     */
    private static String getIdentifier(final DetailAST ast)
    {
        String result = null;
        final DetailAST identifier = ast.findFirstToken(TokenTypes.IDENT);
        if (identifier != null) {
            result = identifier.getText();
        }
        return result;
    }

    /**
     * Verify that method definition contains specified annotation.
     * @param methodDefAst DetailAST of method definition.
     * @param annotationTitle Annotation title
     * @return true if method definition contains specified annotation.
     */
    private static boolean hasAnnotation(DetailAST methodDefAst,
            String annotationTitle)
    {
        boolean result = false;
        final DetailAST modifiersAst = methodDefAst.getFirstChild();
        if (hasChildToken(modifiersAst, TokenTypes.ANNOTATION)) {
            DetailAST modifierAst  = modifiersAst.getFirstChild();
            while (modifierAst != null) {
                if (modifierAst.getType() == TokenTypes.ANNOTATION
                        && annotationTitle.equals(getIdentifier(modifierAst))) {
                    result = true;
                    break;
                }
                modifierAst = modifierAst.getNextSibling();
            }
        }
        return result;
    }

    /**
     * Receive list of arguments(AST nodes) which have wildcard.
     * @param methodDefAst
     *        DetailAST of method definition.
     * @return list of arguments which have wildcard.
     */
    private static List<DetailAST>
    getWildcardArgumentsAsMethodReturnType(DetailAST methodDefAst)
    {
        final List<DetailAST> result = new LinkedList<DetailAST>();
        final DetailAST methodTypeAst =
                methodDefAst.findFirstToken(TokenTypes.TYPE);
        final DetailAST[] methodTypeArgumentTokens =
                getGenericTypeArguments(methodTypeAst);
        for (DetailAST typeArgumentAst: methodTypeArgumentTokens) {
            if (hasChildToken(typeArgumentAst, TokenTypes.WILDCARD_TYPE)) {
                result.add(typeArgumentAst);
            }
        }
        return result;
    }

    /**
     * Get all type arguments of TypeAST.
     * @param typeAst
     *        DetailAST of type definition.
     * @return array of type arguments.
     */
    private static DetailAST[] getGenericTypeArguments(DetailAST typeAst)
    {
        DetailAST[] result = EMPTY_DETAILAST_ARRAY;
        if (hasChildToken(typeAst, TokenTypes.TYPE_ARGUMENTS)) {
            final DetailAST typeArguments = typeAst
                    .findFirstToken(TokenTypes.TYPE_ARGUMENTS);
            final int argumentsCount = typeArguments
                    .getChildCount(TokenTypes.TYPE_ARGUMENT);
            result = new DetailAST[argumentsCount];
            DetailAST firstTypeArgument = typeArguments
                    .findFirstToken(TokenTypes.TYPE_ARGUMENT);
            int counter = 0;
            while (firstTypeArgument != null) {
                if (firstTypeArgument.getType() == TokenTypes.TYPE_ARGUMENT) {
                    result[counter] = firstTypeArgument;
                    counter++;
                }
                firstTypeArgument = firstTypeArgument.getNextSibling();
            }
        }
        return result;
    }

    /**
     * Verify that aAST has token of aTokenType type.
     * @param ast
     *        DetailAST instance.
     * @param tokenType
     *        one of TokenTypes
     * @return true if aAST has token of given type, or false otherwise.
     */
    private static boolean hasChildToken(DetailAST ast, int tokenType)
    {
        return ast.findFirstToken(tokenType) != null;
    }

    /**
     * Verify that method with wildcards as return type is allowed by current
     * check configuration.
     * @param methodDefAst DetailAST of method definition.
     * @param wildcardTypeArguments list of wildcard type arguments.
     * @return true if method is allowed by current check configuration.
     */
    private boolean isIgnoreCase(DetailAST methodDefAst,
            List<DetailAST> wildcardTypeArguments)
    {
        boolean result = false;
        if (matchesIgnoreClassNames(methodDefAst)) {
            result = true;
        }
        else {
            final boolean hasExtendsWildcardAsReturnType =
                    hasBoundedWildcardAsReturnType(wildcardTypeArguments,
                            WILDCARD_EXTENDS_IDENT);
            final boolean hasSuperWildcardAsReturnType =
                    hasBoundedWildcardAsReturnType(wildcardTypeArguments,
                            WILDCARD_SUPER_IDENT);
            final boolean hasOnlyExtendsWildcardAsReturnType =
                    hasExtendsWildcardAsReturnType
                    && !hasSuperWildcardAsReturnType;
            final boolean hasOnlySuperWildcardAsReturnType =
                    hasSuperWildcardAsReturnType
                    && !hasExtendsWildcardAsReturnType;
            final boolean hasBoundedWildcardAsReturnType =
                    hasExtendsWildcardAsReturnType
                    || hasSuperWildcardAsReturnType;
            final boolean isAllowedBoundedWildcards =
                    allowReturnWildcardWithExtends
                    && allowReturnWildcardWithSuper;
            result = (isAllowedBoundedWildcards
                            && hasBoundedWildcardAsReturnType)
                    || (allowReturnWildcardWithExtends
                            && hasOnlyExtendsWildcardAsReturnType)
                    || (allowReturnWildcardWithSuper
                            && hasOnlySuperWildcardAsReturnType);
        }
        return result;
    }

    /**
     * Verify that method's return type name matches ignore regexp.
     * @param methodDefAst DetailAST of method.
     * @return true if aMethodDefAST's name matches ignore regexp.
     *      false otherwise.
     */
    private boolean matchesIgnoreClassNames(DetailAST methodDefAst)
    {
        final DetailAST methodTypeAst =
                methodDefAst.findFirstToken(TokenTypes.TYPE);
        final String typeIdentifier = getIdentifier(methodTypeAst);
        return returnTypeClassNamesIgnoreRegex
                .matcher(typeIdentifier).matches();
    }

    /**
     * Verify that method has bounded wildcard in type arguments list.
     * @param typeArgumentsList list of type arguments.
     * @param boundedWildcardType type of bounded wildcard.
     * @return true if aTypeArgumentsList contains bounded wildcard.
     */
    private static boolean hasBoundedWildcardAsReturnType(
            final List<DetailAST> typeArgumentsList, int boundedWildcardType)
    {
        boolean result = false;
        for (DetailAST typeArgumentAst: typeArgumentsList) {
            if (hasChildToken(typeArgumentAst, boundedWildcardType)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
