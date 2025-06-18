package edu.kit.kastel.vads.compiler.frontend.parser.visitor;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;

public interface PreVisitor<T> {

    void preVisit(AssignmentTree assignmentTree, T data);

    void preVisit(BinaryOperationTree binaryOperationTree, T data);

    void preVisit(BlockTree blockTree, T data);

    void preVisit(DeclarationTree declarationTree, T data);

    void preVisit(FunctionTree functionTree, T data);

    void preVisit(ParameterTree parameterTree, T data);

    void preVisit(CallTree callTree, T data);

    void preVisit(ArgumentTree argumentTree, T data);

    void preVisit(IdentExpressionTree identExpressionTree, T data);

    void preVisit(ConditionalExpressionTree conditionalExpressionTree, T data);

    void preVisit(IntLiteralTree intLiteralTree, T data);

    void preVisit(BoolLiteralTree boolLiteralTree, T data);

    void preVisit(LValueIdentTree lValueIdentTree, T data);

    void preVisit(NameTree nameTree, T data);

    void preVisit(NegateTree negateTree, T data);

    void preVisit(ProgramTree programTree, T data);

    void preVisit(ReturnTree returnTree, T data);

    void preVisit(BreakTree breakTree, T data);

    void preVisit(ContinueTree continueTree, T data);

    void preVisit(ForTree forTree, T data);

    void preVisit(IfTree ifTree, T data);

    void preVisit(ElseTree elseTree, T data);

    void preVisit(WhileTree whileTree, T data);

    void preVisit(TypeTree typeTree, T data);
}
