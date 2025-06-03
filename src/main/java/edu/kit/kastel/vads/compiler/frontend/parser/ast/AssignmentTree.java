package edu.kit.kastel.vads.compiler.frontend.parser.ast;

import edu.kit.kastel.vads.compiler.frontend.lexer.Operator;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.PreVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;

public record AssignmentTree(LValueTree lValue, Operator operator, ExpressionTree expression) implements SimpleStatementTree {
    @Override
    public Span span() {
        return lValue().span().merge(expression().span());
    }

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }

    @Override
    public <T> void accept(PreVisitor<T> visitor, T data) {
        visitor.preVisit(this, data);
    }
}
