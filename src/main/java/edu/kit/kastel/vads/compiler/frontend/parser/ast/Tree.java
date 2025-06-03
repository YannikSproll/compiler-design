package edu.kit.kastel.vads.compiler.frontend.parser.ast;

import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.PreVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;

public sealed interface Tree permits ExpressionTree, FunctionTree, LValueTree, NameTree, ProgramTree, StatementTree, TypeTree {

    Span span();

    <T, R> R accept(Visitor<T, R> visitor, T data);

    <T> void accept(PreVisitor<T> visitor, T data);
}
