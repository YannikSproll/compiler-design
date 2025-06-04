package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.lexer.Operator;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

import java.util.List;
import java.util.Optional;

final class ElaborationUtils {
    static TypedIf generateLoopBreakIf(TypedExpression conditionExpression) {
        return new TypedIf(
                new TypedUnaryOperation(
                        UnaryOperator.LOGICAL_NOT,
                        conditionExpression,
                        conditionExpression.span()),
                new TypedBlock(
                        List.of(
                                new TypedBreak(
                                        conditionExpression.span())),
                        Optional.empty(),
                        conditionExpression.span()),
                Optional.empty(),
                conditionExpression.span());
    }

    static UnaryOperator mapUnaryOperator(Operator.OperatorType operatorType) {
        return switch (operatorType) {
            case Operator.OperatorType.MINUS -> UnaryOperator.NEGATION;
            case Operator.OperatorType.BITWISE_NOT -> UnaryOperator.BITWISE_NOT;
            case Operator.OperatorType.LOGICAL_NOT -> UnaryOperator.LOGICAL_NOT;
            default -> throw new SemanticException("Unsupported operator type: " + operatorType);
        };
    }

    static BinaryOperator mapAssignmentOperationToBinaryOperator(Operator operator) {
        return switch (operator.type()) {
            case Operator.OperatorType.ASSIGN_PLUS -> BinaryOperator.ADD;
            case Operator.OperatorType.ASSIGN_MINUS -> BinaryOperator.SUBTRACT;
            case Operator.OperatorType.ASSIGN_MUL -> BinaryOperator.MULTIPLY;
            case Operator.OperatorType.ASSIGN_DIV -> BinaryOperator.DIVIDE;
            case Operator.OperatorType.ASSIGN_MOD -> BinaryOperator.MODULO;
            case Operator.OperatorType.ASSIGN_BITWISE_AND -> BinaryOperator.BITWISE_AND;
            case Operator.OperatorType.ASSIGN_BITWISE_OR -> BinaryOperator.BITWISE_OR;
            case Operator.OperatorType.ASSIGN_BITWISE_XOR -> BinaryOperator.BITWISE_XOR;
            case Operator.OperatorType.ASSIGN_LEFT_SHIFT -> BinaryOperator.LEFT_SHIFT;
            case Operator.OperatorType.ASSIGN_RIGHT_SHIFT -> BinaryOperator.RIGHT_SHIFT;
            default -> throw new IllegalStateException("Unexpected operator type: " + operator.type());
        };
    }

    static BinaryOperator mapBinaryOperator(Operator.OperatorType operatorType) {
        return switch (operatorType) {
            case Operator.OperatorType.MUL -> BinaryOperator.MULTIPLY;
            case Operator.OperatorType.DIV -> BinaryOperator.DIVIDE;
            case Operator.OperatorType.MOD -> BinaryOperator.MODULO;
            case Operator.OperatorType.MINUS -> BinaryOperator.SUBTRACT;
            case Operator.OperatorType.PLUS -> BinaryOperator.ADD;
            case Operator.OperatorType.LEFT_SHIFT -> BinaryOperator.LEFT_SHIFT;
            case Operator.OperatorType.RIGHT_SHIFT -> BinaryOperator.RIGHT_SHIFT;
            case Operator.OperatorType.LESS_THAN -> BinaryOperator.LESS_THAN;
            case Operator.OperatorType.GREATER_THAN -> BinaryOperator.GREATER_THAN;
            case Operator.OperatorType.LESS_OR_EQUAL -> BinaryOperator.LESS_THAN_OR_EQUAL_TO;
            case Operator.OperatorType.GREATER_OR_EQUAL -> BinaryOperator.GREATER_THAN_OR_EQUAL_TO;
            case Operator.OperatorType.EQUAL_TO -> BinaryOperator.EQUAL_TO;
            case Operator.OperatorType.UNEQUAL_TO -> BinaryOperator.UNEQUAL_TO;
            case Operator.OperatorType.BITWISE_AND -> BinaryOperator.BITWISE_AND;
            case Operator.OperatorType.BITWISE_XOR -> BinaryOperator.BITWISE_XOR;
            case Operator.OperatorType.BITWISE_OR -> BinaryOperator.BITWISE_OR;
            default -> throw new IllegalStateException("Unexpected operator type: " + operatorType);
        };
    }

}
