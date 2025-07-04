package edu.kit.kastel.vads.compiler.frontend.parser.ast;

public sealed interface ExpressionTree extends Tree permits BinaryOperationTree, CallTree, ConditionalExpressionTree, IdentExpressionTree, LiteralTree, NegateTree {
}
