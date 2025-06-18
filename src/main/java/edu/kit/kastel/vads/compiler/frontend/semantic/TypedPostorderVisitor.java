package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

public final class TypedPostorderVisitor<TContext> implements TypedVisitor<TContext> {
    private final TypedVisitor<TContext> visitor;

    public TypedPostorderVisitor(TypedVisitor<TContext> visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visit(TypedAssignment assignment, TContext tContext) {
        assignment.lValue().accept(this, tContext);
        assignment.initializer().accept(this, tContext);
        visitor.visit(assignment, tContext);
    }

    @Override
    public void visit(TypedBinaryOperation operation, TContext tContext) {
        operation.lhsExpression().accept(this, tContext);
        operation.rhsExpression().accept(this, tContext);
        visitor.visit(operation, tContext);
    }

    @Override
    public void visit(TypedBlock block, TContext tContext) {
        for (TypedStatement typedStatement : block.statements()) {
            typedStatement.accept(this, tContext);
        }
        this.visitor.visit(block, tContext);
    }

    @Override
    public void visit(TypedBoolLiteral literal, TContext tContext) {
       visitor.visit(literal, tContext);
    }

    @Override
    public void visit(TypedBreak breakStatement, TContext tContext) {
        visitor.visit(breakStatement, tContext);
    }

    @Override
    public void visit(TypedConditionalExpression conditionalExpression, TContext tContext) {
        conditionalExpression.conditionExpression().accept(this, tContext);
        conditionalExpression.thenExpression().accept(this, tContext);
        conditionalExpression.elseExpression().accept(this, tContext);
        visitor.visit(conditionalExpression, tContext);
    }

    @Override
    public void visit(TypedContinue continueStatement, TContext tContext) {
        visitor.visit(continueStatement, tContext);
    }

    @Override
    public void visit(TypedDeclaration declaration, TContext tContext) {
        if (declaration.initializer().isPresent()) {
            declaration.initializer().get().accept(this, tContext);
        }
        visitor.visit(declaration, tContext);
    }

    @Override
    public void visit(TypedFile file, TContext tContext) {
        for (TypedFunction function : file.functions()) {
            function.accept(this, tContext);
        }
        visitor.visit(file, tContext);
    }

    @Override
    public void visit(TypedFunction function, TContext tContext) {
        function.body().accept(this, tContext);
        visitor.visit(function, tContext);
    }

    @Override
    public void visit(TypedFunctionCall functionCall, TContext tContext) {

    }

    @Override
    public void visit(TypedArgument argument, TContext tContext) {

    }

    @Override
    public void visit(TypedIf ifStatement, TContext tContext) {
        ifStatement.conditionExpression().accept(this, tContext);
        ifStatement.thenStatement().accept(this, tContext);
        if (ifStatement.elseStatement().isPresent()) {
            ifStatement.elseStatement().get().accept(this, tContext);
        }
        visitor.visit(ifStatement, tContext);
    }

    @Override
    public void visit(TypedIntLiteral literal, TContext tContext) {
        visitor.visit(literal, tContext);
    }

    @Override
    public void visit(TypedLoop loop, TContext tContext) {
        loop.body().accept(this, tContext);
        if (loop.postIterationStatement().isPresent()) {
            loop.postIterationStatement().get().accept(this, tContext);
        }
        visitor.visit(loop, tContext);
    }

    @Override
    public void visit(TypedReturn returnStatement, TContext tContext) {
        returnStatement.returnExpression().accept(this, tContext);
        visitor.visit(returnStatement, tContext);
    }

    @Override
    public void visit(TypedUnaryOperation operation, TContext tContext) {
        operation.expression().accept(this, tContext);
        visitor.visit(operation, tContext);
    }

    @Override
    public void visit(TypedVariable variable, TContext tContext) {
        visitor.visit(variable, tContext);
    }
}
