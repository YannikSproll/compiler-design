package edu.kit.kastel.vads.compiler.frontend.parser.visitor;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;

public interface NoOpPreVisitor<T> extends PreVisitor<T> {

    @Override
    default void preVisit(AssignmentTree assignmentTree, T data) {

    }

    @Override
    default void preVisit(BinaryOperationTree binaryOperationTree, T data) {

    }

    @Override
    default void preVisit(BlockTree blockTree, T data) {

    }

    @Override
    default void preVisit(DeclarationTree declarationTree, T data) {

    }

    @Override
    default void preVisit(FunctionTree functionTree, T data) {

    }

    @Override
    default void preVisit(IdentExpressionTree identExpressionTree, T data) {

    }

    @Override
    default void preVisit(IntLiteralTree intLiteralTree, T data) {  }

    @Override
    default void preVisit(LValueIdentTree lValueIdentTree, T data) {

    }

    @Override
    default void preVisit(NameTree nameTree, T data) {  }

    @Override
    default void preVisit(NegateTree negateTree, T data) {

    }

    @Override
    default void preVisit(ProgramTree programTree, T data) {

    }

    @Override
    default void preVisit(ReturnTree returnTree, T data) {

    }

    @Override
    default void preVisit(TypeTree typeTree, T data) {  }

    @Override
    default void preVisit(ConditionalExpressionTree conditionalExpressionTree, T data) {  }

    @Override
    default void preVisit(BoolLiteralTree boolLiteralTree, T data) {

    }

    @Override
    default void preVisit(BreakTree breakTree, T data) {

    }

    @Override
    default void preVisit(ContinueTree continueTree, T data) {

    }

    @Override
    default void preVisit(ForTree forTree, T data) {

    }

    @Override
    default void preVisit(IfTree ifTree, T data) {

    }

    @Override
    default void preVisit(ElseTree elseTree, T data) {

    }

    @Override
    default void preVisit(WhileTree whileTree, T data) {

    }
}
