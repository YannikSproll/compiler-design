package edu.kit.kastel.vads.compiler.frontend.parser.ast;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

public record ForTree(
        @Nullable SimpleStatementTree initializationStatementTree,
        ExpressionTree conditionExpressionTree,
        @Nullable SimpleStatementTree postIterationStatementTree,
        StatementTree bodyStatementTree,
        Position start) implements ControlTree{

    @Override
    public Span span() {
        return new Span.SimpleSpan(start, bodyStatementTree.span().end());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
