package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

public interface TypedResultVisitor<TContext, TResult> {

    TResult visit(TypedAssignment assignment, TContext context);
    TResult visit(TypedBinaryOperation operation, TContext context);
    TResult visit(TypedBlock block, TContext context);
    TResult visit(TypedBoolLiteral literal, TContext context);
    TResult visit(TypedBreak breakStatement, TContext context);
    TResult visit(TypedConditionalExpression conditionalExpression, TContext context);
    TResult visit(TypedContinue continueStatement, TContext context);
    TResult visit(TypedDeclaration declaration, TContext context);
    TResult visit(TypedFile file, TContext context);
    TResult visit(TypedFunction function, TContext context);
    TResult visit(TypedFunctionCall functionCall, TContext context);
    TResult visit(TypedArgument argument, TContext context);
    TResult visit(TypedIf ifStatement, TContext context);
    TResult visit(TypedIntLiteral literal, TContext context);
    TResult visit(TypedLoop loop, TContext context);
    TResult visit(TypedReturn returnStatement, TContext context);
    TResult visit(TypedUnaryOperation operation, TContext context);
    TResult visit(TypedVariable variable, TContext context);
}
