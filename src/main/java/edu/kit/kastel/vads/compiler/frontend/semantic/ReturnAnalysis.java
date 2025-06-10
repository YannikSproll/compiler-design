package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

import java.util.HashMap;

/// Checks that functions return.
class ReturnAnalysis implements TypedVisitor<ReturnAnalysis.ReturnState> {

    @Override
    public void visit(TypedAssignment assignment, ReturnState returnState) {
        // Statement. Does not return => don't add to returnState
    }

    @Override
    public void visit(TypedBinaryOperation operation, ReturnState returnState) {
        // Expression. Does not return
    }

    @Override
    public void visit(TypedBlock block, ReturnState returnState) {
        boolean returns = block.statements().stream().anyMatch(returnState::doesTreeReturn);
        returnState.putTreeReturn(block, returns);
    }

    @Override
    public void visit(TypedBoolLiteral literal, ReturnState returnState) {
        // Expression. Does not return
    }

    @Override
    public void visit(TypedBreak breakStatement, ReturnState returnState) {
        // Statement. Does not return => don't add to returnState
    }

    @Override
    public void visit(TypedConditionalExpression conditionalExpression, ReturnState returnState) {
        // Expression. Does not return
    }

    @Override
    public void visit(TypedContinue continueStatement, ReturnState returnState) {
        // Statement. Does not return => don't add to returnState
    }

    @Override
    public void visit(TypedDeclaration declaration, ReturnState returnState) {
        // Statement. Does not return => don't add to returnState
    }

    @Override
    public void visit(TypedFile file, ReturnState returnState) {
        for (TypedFunction function : file.functions()) {
            if (!returnState.doesTreeReturn(function)) {
                throw new SemanticException("The function" + function.symbol().name() + " does not return.");
            }
        }
    }

    @Override
    public void visit(TypedFunction function, ReturnState returnState) {
        boolean returns = returnState.doesTreeReturn(function.body());
        returnState.putTreeReturn(function, returns);
    }

    @Override
    public void visit(TypedIf ifStatement, ReturnState returnState) {
        boolean doesReturn = returnState.doesTreeReturn(ifStatement.thenStatement())
                && (ifStatement.elseStatement().isEmpty() || returnState.doesTreeReturn(ifStatement.elseStatement().get()));
        returnState.putTreeReturn(ifStatement, doesReturn);
    }

    @Override
    public void visit(TypedIntLiteral literal, ReturnState returnState) {
        // Expression. Does not return
    }

    @Override
    public void visit(TypedLoop loop, ReturnState returnState) {
        // Statement. Does not return => don't add to returnState
    }

    @Override
    public void visit(TypedReturn returnStatement, ReturnState returnState) {
        returnState.putTreeReturn(returnStatement, true);
    }

    @Override
    public void visit(TypedUnaryOperation operation, ReturnState returnState) {
        // Expression. Does not return
    }

    @Override
    public void visit(TypedVariable variable, ReturnState returnState) {
        // Expression. Does not return
    }

    static class ReturnState {
        HashMap<TypedObject, Boolean> returns = new HashMap<>();

        public void putTreeReturn(TypedObject obj, boolean treeReturns) {
            returns.put(obj, treeReturns);
        }

        public boolean doesTreeReturn(TypedObject obj) {
            return returns.containsKey(obj)
                    && returns.get(obj);
        }
    }

}
