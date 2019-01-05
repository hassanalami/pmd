/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;


/**
 * Reference to a method.
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
public final class JMethodSymbol extends JAccessibleDeclarationSymbol<ASTMethodDeclaration> {

    private final boolean isDefault;
    private final List<JLocalVariableSymbol> parameterSymbols;


    /**
     * Constructor for methods found through reflection.
     *
     * @param method Method for which to create a reference
     */
    public JMethodSymbol(Method method) {
        super(method.getModifiers(), method.getName());
        this.isDefault = method.isDefault();
        this.parameterSymbols = Arrays.stream(method.getParameters()).map(JLocalVariableSymbol::new).collect(Collectors.toList());
    }


    /**
     * Constructor using the AST node.
     *
     * @param node Node representing the method declaration
     */
    public JMethodSymbol(ASTMethodDeclaration node) {
        super(Objects.requireNonNull(node), getModifiers(node), node.getMethodName());
        this.isDefault = node.isDefault();
        this.parameterSymbols =
            iteratorToStream(node.getFormalParameters().iterator())
                .map(ASTFormalParameter::getVariableDeclaratorId)
                .map(JLocalVariableSymbol::new)
                .collect(Collectors.toList());
    }


    // TODO create util for iterators... so fkn annoying
    private <T> Stream<T> iteratorToStream(Iterator<T> iterator) {
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, 0),
            false
        );
    }


    boolean isSynchronized() {
        return Modifier.isSynchronized(modifiers);
    }


    boolean isNative() {
        return Modifier.isNative(modifiers);
    }


    boolean isDefault() {
        return isDefault;
    }


    boolean isStrict() {
        return Modifier.isStrict(modifiers);
    }


    boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }


    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }


    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    // Modifier.TRANSIENT is identical to the bit mask used for varargs
    // the reflect API uses that because they can never occur together I guess

    boolean isVarargs() {
        return (modifiers & Modifier.TRANSIENT) != 0;
    }


    private static int getModifiers(ASTMethodDeclaration declaration) {
        int i = accessNodeToModifiers(declaration);

        if (declaration.isVarargs()) {
            i |= Modifier.TRANSIENT;
        }
        return i;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        JMethodSymbol that = (JMethodSymbol) o;
        return isDefault == that.isDefault
            && Objects.equals(parameterSymbols, that.parameterSymbols);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isDefault);
    }
}
