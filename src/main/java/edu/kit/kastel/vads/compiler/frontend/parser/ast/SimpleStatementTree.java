package edu.kit.kastel.vads.compiler.frontend.parser.ast;

public sealed interface SimpleStatementTree extends StatementTree permits AssignmentTree, CallTree, DeclarationTree {
}
