package edu.kit.kastel.vads.compiler.frontend.parser.ast;

public sealed interface ControlTree extends StatementTree permits IfTree, ElseTree, WhileTree, ForTree, ContinueTree, BreakTree, ReturnTree {
}
