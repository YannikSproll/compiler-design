package edu.kit.kastel.vads.compiler.frontend.parser.ast;


public sealed interface LiteralTree extends ExpressionTree permits IntLiteralTree, BoolLiteralTree {
}
