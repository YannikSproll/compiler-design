package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

public final class TypedPostorderVisitor<TContext> implements TypedVisitor<TContext> {
    private final TypedVisitor<TContext> visitor;

    public TypedPostorderVisitor(TypedVisitor<TContext> visitor) {
        this.visitor = visitor;
    }

    @Override
    public void visit(TypedAssignment assignment, TContext tContext) {
        assignment.lValue().accept(visitor, tContext);
        assignment.initializer().accept(visitor, tContext);
        visitor.visit(assignment, tContext);
    }

    @Override
    public void visit(TypedBinaryOperation operation, TContext tContext) {
        operation.lhsExpression().accept(visitor, tContext);
        operation.rhsExpression().accept(visitor, tContext);
        visitor.visit(operation, tContext);
    }

    @Override
    public void visit(TypedBlock block, TContext tContext) {
        for (TypedStatement typedStatement : block.statements()) {
            typedStatement.accept(visitor, tContext);
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
        conditionalExpression.conditionExpression().accept(visitor, tContext);
        conditionalExpression.thenExpression().accept(visitor, tContext);
        conditionalExpression.elseExpression().accept(visitor, tContext);
        visitor.visit(conditionalExpression, tContext);
    }

    @Override
    public void visit(TypedContinue continueStatement, TContext tContext) {
        visitor.visit(continueStatement, tContext);
    }

    @Override
    public void visit(TypedDeclaration declaration, TContext tContext) {
        if (declaration.initializer().isPresent()) {
            declaration.initializer().get().accept(visitor, tContext);
        }
        visitor.visit(declaration, tContext);
    }

    @Override
    public void visit(TypedFile file, TContext tContext) {
        for (TypedFunction function : file.functions()) {
            function.accept(visitor, tContext);
        }
        visitor.visit(file, tContext);
    }

    @Override
    public void visit(TypedFunction function, TContext tContext) {
        function.body().accept(visitor, tContext);
        function.accept(visitor, tContext);
    }

    @Override
    public void visit(TypedIf ifStatement, TContext tContext) {
        ifStatement.conditionExpression().accept(visitor, tContext);
        ifStatement.thenStatement().accept(visitor, tContext);
        if (ifStatement.elseStatement().isPresent()) {
            ifStatement.elseStatement().get().accept(visitor, tContext);
        }
        visitor.visit(ifStatement, tContext);
    }

    @Override
    public void visit(TypedIntLiteral literal, TContext tContext) {
        visitor.visit(literal, tContext);
    }

    @Override
    public void visit(TypedLoop loop, TContext tContext) {
        loop.body().accept(visitor, tContext);
        if (loop.postIterationStatement().isPresent()) {
            loop.postIterationStatement().get().accept(visitor, tContext);
        }
        visitor.visit(loop, tContext);
    }

    @Override
    public void visit(TypedReturn returnStatement, TContext tContext) {
        returnStatement.returnExpression().accept(visitor, tContext);
        visitor.visit(returnStatement, tContext);
    }

    @Override
    public void visit(TypedUnaryOperation operation, TContext tContext) {
        operation.expression().accept(visitor, tContext);
        visitor.visit(operation, tContext);
    }

    @Override
    public void visit(TypedVariable variable, TContext tContext) {
        visitor.visit(variable, tContext);
    }
}
