package edu.kit.kastel.vads.compiler.frontend.parser.visitor;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.LiteralTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.NegateTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.ReturnTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.TypeTree;

public interface Visitor<T, R> {

    R visit(AssignmentTree assignmentTree, T data);

    R visit(BinaryOperationTree binaryOperationTree, T data);

    R visit(BlockTree blockTree, T data);

    R visit(DeclarationTree declarationTree, T data);

    R visit(FunctionTree functionTree, T data);

    R visit(IdentExpressionTree identExpressionTree, T data);

    R visit(LiteralTree literalTree, T data);

    R visit(LValueIdentTree lValueIdentTree, T data);

    R visit(NameTree nameTree, T data);

    R visit(NegateTree negateTree, T data);

    R visit(ProgramTree programTree, T data);

    R visit(ReturnTree returnTree, T data);

    R visit(TypeTree typeTree, T data);
}
