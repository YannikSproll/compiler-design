package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.NoOpVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Unit;

public class IntegerLiteralRangeAnalysis implements NoOpVisitor<Namespace<Void>> {
    @Override
    public Unit visit(IntLiteralTree intLiteralTree, Namespace<Void> data) {
      intLiteralTree.parseValue()
          .orElseThrow(
              () -> new SemanticException("invalid integer literal " + intLiteralTree.value())
          );
        return NoOpVisitor.super.visit(intLiteralTree, data);
    }
}
