package edu.kit.kastel.vads.compiler.frontend.parser.ast;

import edu.kit.kastel.vads.compiler.Position;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

public record IfTree(ExpressionTree conditionExpressionTree, StatementTree statementTree, @Nullable ElseTree elseTree, Position start) implements ControlTree {

    @Override
    public Span span() {
        if (elseTree != null) {
            return new Span.SimpleSpan(start, elseTree.span().end());
        } else {
            return new Span.SimpleSpan(start, statementTree.span().end());
        }
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
