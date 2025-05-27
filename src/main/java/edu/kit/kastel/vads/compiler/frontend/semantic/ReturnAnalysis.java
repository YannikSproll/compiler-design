package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Unit;

/// Checks that functions return.
/// Currently only works for straight-line code.
class ReturnAnalysis implements NoOpVisitor<ReturnAnalysis.ReturnState> {

    static class ReturnState {
        boolean returns = false;
    }

    @Override
    public Unit visit(ReturnTree returnTree, ReturnState data) {
        data.returns = true;
        return NoOpVisitor.super.visit(returnTree, data);
    }

    @Override
    public Unit visit(BreakTree breakTree, ReturnState data) {
        return null;
    }

    @Override
    public Unit visit(ContinueTree continueTree, ReturnState data) {
        return null;
    }

    @Override
    public Unit visit(ForTree forTree, ReturnState data) {
        return null;
    }

    @Override
    public Unit visit(IfTree ifTree, ReturnState data) {
        return null;
    }

    @Override
    public Unit visit(ElseTree elseTree, ReturnState data) {
        return null;
    }

    @Override
    public Unit visit(WhileTree whileTree, ReturnState data) {
        return null;
    }

    @Override
    public Unit visit(FunctionTree functionTree, ReturnState data) {
        if (!data.returns) {
            throw new SemanticException("function " + functionTree.name() + " does not return");
        }
        data.returns = false;
        return NoOpVisitor.super.visit(functionTree, data);
    }

    @Override
    public Unit visit(ConditionalExpressionTree conditionalExpressionTree, ReturnState data) {
        return null;
    }

    @Override
    public Unit visit(BoolLiteralTree boolLiteralTree, ReturnState data) {
        return null;
    }
}
