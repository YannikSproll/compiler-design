package edu.kit.kastel.vads.compiler.frontend.parser.ast;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.PreVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;

import java.util.List;

public record ProgramTree(List<FunctionTree> topLevelTrees) implements Tree {
    public ProgramTree {
        assert !topLevelTrees.isEmpty() : "must be non-empty";
        topLevelTrees = List.copyOf(topLevelTrees);
    }
    @Override
    public Span span() {
        var first = topLevelTrees.getFirst();
        var last = topLevelTrees.getLast();
        return new Span.SimpleSpan(first.span().start(), last.span().end());
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
