package edu.kit.kastel.vads.compiler.frontend.parser.ast;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;

public record ContinueTree(Span span) implements ControlTree{

    @Override
    public <T, R> R accept(Visitor<T, R> visitor, T data) {
        return visitor.visit(this, data);
    }
}
