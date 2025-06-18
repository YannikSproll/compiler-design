package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TypedFunction implements TypedNode {
    private final Symbol symbol;
    private final List<TypedParameter> parameters;
    private @Nullable TypedBlock body;
    private final Scope declaringScope;
    private boolean isMainFunction;

    public TypedFunction(Symbol symbol, Scope declaringScope, List<TypedParameter> parameters) {
        this.symbol = symbol;
        this.body = null;
        this.declaringScope = declaringScope;
        this.parameters = parameters;
        this.isMainFunction = false;
    }

    @Override
    public TypedFunction asTypedFunction() {
        return this;
    }

    public void setElaboratedBody(TypedBlock body) {
        this.body = body;
    }

    public void markAsMainFunction() {
        isMainFunction = true;
    }

    public <TContext> void accept(TypedVisitor<TContext> visitor, TContext context) {
        visitor.visit(this, context);
    }

    public <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext context) {
        return visitor.visit(this, context);
    }

    public Symbol symbol() {
        return symbol;
    }

    public List<TypedParameter> parameters() { return Collections.unmodifiableList(parameters); }

    public TypedBlock body() {
        return body;
    }

    public Scope declaringScope() {
        return declaringScope;
    }

    public boolean isMainFunction() {
        return isMainFunction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TypedFunction) obj;
        return Objects.equals(this.symbol, that.symbol) &&
                Objects.equals(this.body, that.body) &&
                Objects.equals(this.declaringScope, that.declaringScope) &&
                this.isMainFunction == that.isMainFunction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, body, declaringScope, isMainFunction);
    }

    @Override
    public String toString() {
        return "TypedFunction[" +
                "symbol=" + symbol + ", " +
                "body=" + body + ", " +
                "declaringScope=" + declaringScope + ", " +
                "isMainFunction=" + isMainFunction + ']';
    }

}
