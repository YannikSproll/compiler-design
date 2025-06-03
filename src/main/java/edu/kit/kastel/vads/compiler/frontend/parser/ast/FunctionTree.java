package edu.kit.kastel.vads.compiler.frontend.parser.ast;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.PreVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;

public record FunctionTree(TypeTree returnType, NameTree name, BlockTree body) implements Tree {
    @Override
    public Span span() {
        return new Span.SimpleSpan(returnType().span().start(), body().span().end());
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
