package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public sealed interface TypedExpression extends TypedObject permits TypedArgument, TypedBinaryOperation, TypedBoolLiteral, TypedConditionalExpression, TypedFunctionCall, TypedIntLiteral, TypedUnaryOperation, TypedVariable {
    HirType type();
    Span span();

    default TypedBinaryOperation asBinaryOperation() {
        throw new UnsupportedOperationException("Expression is not a binary operation");
    }

    default TypedConditionalExpression asConditionalExpression() {
        throw new UnsupportedOperationException("Expression is not a conditional expression");
    }

    default TypedIntLiteral asIntLiteral() {
        throw new UnsupportedOperationException("Expression is not an int literal");
    }

    default TypedBoolLiteral asBoolLiteral() {
        throw new UnsupportedOperationException("Expression is not a bool literal");
    }

    default TypedUnaryOperation asUnaryOperation() {
        throw new UnsupportedOperationException("Expression is not a unary operation");
    }

    default TypedVariable asVariable() {
        throw new UnsupportedOperationException("Expression is not a variable");
    }

    <TContext> void accept(TypedVisitor<TContext> visitor, TContext context);
    <TContext, TResult> TResult accept(TypedResultVisitor<TContext, TResult> visitor, TContext context);
}
