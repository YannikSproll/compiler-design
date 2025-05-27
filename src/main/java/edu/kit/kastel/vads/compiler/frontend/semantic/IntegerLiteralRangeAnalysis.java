package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Unit;

public class IntegerLiteralRangeAnalysis implements NoOpVisitor<Namespace<Void>> {

    @Override
    public Unit visit(ConditionalExpressionTree conditionalExpressionTree, Namespace<Void> data) {
        return null;
    }

    @Override
    public Unit visit(IntLiteralTree intLiteralTree, Namespace<Void> data) {
      intLiteralTree.parseValue()
          .orElseThrow(
              () -> new SemanticException("invalid integer literal " + intLiteralTree.value())
          );
        return NoOpVisitor.super.visit(intLiteralTree, data);
    }

    @Override
    public Unit visit(BoolLiteralTree boolLiteralTree, Namespace<Void> data) {
        return null;
    }

    @Override
    public Unit visit(BreakTree breakTree, Namespace<Void> data) {
        return null;
    }

    @Override
    public Unit visit(ContinueTree continueTree, Namespace<Void> data) {
        return null;
    }

    @Override
    public Unit visit(ForTree forTree, Namespace<Void> data) {
        return null;
    }

    @Override
    public Unit visit(IfTree ifTree, Namespace<Void> data) {
        return null;
    }

    @Override
    public Unit visit(ElseTree elseTree, Namespace<Void> data) {
        return null;
    }

    @Override
    public Unit visit(WhileTree whileTree, Namespace<Void> data) {
        return null;
    }
}
