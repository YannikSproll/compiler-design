package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.frontend.semantic.SemanticException;

public final class TypeChecker {

    public void expectEqualTypes(TypedExpression firstExpression, TypedExpression secondExpression) {
        if (firstExpression.type() != secondExpression.type()) {
            throw new SemanticException("Type mismatch. Expected equal types, but got ...");
        }
    }

    public void expectEqualTypes(TypedLValue lValue, TypedExpression expression) {
        if (lValue.type() != expression.type()) {
            throw new SemanticException("Type mismatch. Expected equal types, but got ...");
        }
    }

    public void expectType(HirType expectedType, TypedExpression expression) {
        if (expectedType != expression.type()) {
            throw new SemanticException("Type mismatch. Expected " + expectedType + ", but got " + expression.type());
        }
    }

    public void expectType(HirType expectedType, TypedLValue lValue) {
        if (expectedType != lValue.type()) {
            throw new SemanticException("Type mismatch. Expected " + expectedType + ", but got " + lValue.type());
        }
    }
}
