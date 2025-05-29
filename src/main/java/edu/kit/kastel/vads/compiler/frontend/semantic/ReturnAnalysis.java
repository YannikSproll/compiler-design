package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Unit;

import java.util.HashMap;

/// Checks that functions return.
/// Currently only works for straight-line code.
class ReturnAnalysis implements NoOpVisitor<ReturnAnalysis.ReturnState> {

    static class ReturnState {
        HashMap<Tree, Boolean> returns = new HashMap<>();

        public void putTreeReturn(Tree tree, boolean treeReturns) {
            returns.put(tree, treeReturns);
        }

        public boolean doesTreeReturn(Tree tree) {
            return returns.containsKey(tree)
                    && returns.get(tree);
        }
    }

    @Override
    public Unit visit(ReturnTree returnTree, ReturnState data) {
        data.putTreeReturn(returnTree, true);

        return NoOpVisitor.super.visit(returnTree, data);
    }

    @Override
    public Unit visit(FunctionTree functionTree, ReturnState data) {
        boolean bodyReturns = data.doesTreeReturn(functionTree.body());
        if (!bodyReturns) {
            throw new SemanticException("function " + functionTree.name() + " does not return");
        }

        data.putTreeReturn(functionTree, true);

        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(DeclarationTree declarationTree, ReturnState data) {
        boolean returns = declarationTree.initializer() == null || data.doesTreeReturn(declarationTree.initializer());
        data.putTreeReturn(declarationTree, returns);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(AssignmentTree assignmentTree, ReturnState data) {
        data.putTreeReturn(assignmentTree, false);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(IfTree ifTree, ReturnState data) {
        boolean returns = data.doesTreeReturn(ifTree.statementTree())
                && (ifTree.elseTree() == null || data.doesTreeReturn(ifTree.elseTree()));

        data.putTreeReturn(ifTree, returns);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(ForTree forTree, ReturnState data) {
        data.putTreeReturn(forTree, false);
        return Unit.INSTANCE;
    }

    @Override
    public Unit visit(WhileTree whileTree, ReturnState data) {
        data.putTreeReturn(whileTree, false);
        return Unit.INSTANCE;
    }

    public Unit visit(BlockTree blockTree, ReturnState data) {
        boolean returns = blockTree.statements().stream().anyMatch(data::doesTreeReturn);
        data.putTreeReturn(blockTree, returns);
        return Unit.INSTANCE;
    }
}
