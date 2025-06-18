package edu.kit.kastel.vads.compiler.frontend.semantic.hir;


public interface TypedVisitor<TContext> {
    void visit(TypedAssignment assignment, TContext context);
    void visit(TypedBinaryOperation operation, TContext context);
    void visit(TypedBlock block, TContext context);
    void visit(TypedBoolLiteral literal, TContext context);
    void visit(TypedBreak breakStatement, TContext context);
    void visit(TypedConditionalExpression conditionalExpression, TContext context);
    void visit(TypedContinue continueStatement, TContext context);
    void visit(TypedDeclaration declaration, TContext context);
    void visit(TypedFile file, TContext context);
    void visit(TypedFunction function, TContext context);
    void visit(TypedFunctionCall functionCall, TContext context);
    void visit(TypedArgument argument, TContext context);
    void visit(TypedIf ifStatement, TContext context);
    void visit(TypedIntLiteral literal, TContext context);
    void visit(TypedLoop loop, TContext context);
    void visit(TypedReturn returnStatement, TContext context);
    void visit(TypedUnaryOperation operation, TContext context);
    void visit(TypedVariable variable, TContext context);
}
