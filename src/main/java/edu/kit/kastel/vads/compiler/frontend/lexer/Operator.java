package edu.kit.kastel.vads.compiler.frontend.lexer;

import edu.kit.kastel.vads.compiler.Span;

public record Operator(OperatorType type, Span span) implements Token {

    @Override
    public boolean isOperator(OperatorType operatorType) {
        return type() == operatorType;
    }

    @Override
    public String asString() {
        return type().toString();
    }

    public enum OperatorAssociativity {
        LEFT,
        RIGHT;
    }

    public enum OperatorType {
        ASSIGN_MINUS("-=", 13, OperatorAssociativity.RIGHT),
        MINUS("-", 3, OperatorAssociativity.LEFT),
        ASSIGN_PLUS("+=", 13, OperatorAssociativity.RIGHT),
        PLUS("+", 3, OperatorAssociativity.LEFT),
        MUL("*", 2, OperatorAssociativity.LEFT),
        ASSIGN_MUL("*=", 13, OperatorAssociativity.RIGHT),
        ASSIGN_DIV("/=", 13, OperatorAssociativity.RIGHT),
        DIV("/", 2, OperatorAssociativity.LEFT),
        ASSIGN_MOD("%=", 13, OperatorAssociativity.RIGHT),
        MOD("%", 2, OperatorAssociativity.LEFT),
        ASSIGN("=", 13, OperatorAssociativity.RIGHT),
        // L2
        LOGICAL_NOT("!", 1, OperatorAssociativity.RIGHT),
        BITWISE_NOT("~", 1, OperatorAssociativity.RIGHT),
        LEFT_SHIFT("<<", 4, OperatorAssociativity.LEFT),
        RIGHT_SHIFT(">>", 4, OperatorAssociativity.LEFT),
        LESS_THAN("<", 5, OperatorAssociativity.LEFT),
        GREATER_THAN(">", 5, OperatorAssociativity.LEFT),
        LESS_OR_EQUAL("<=", 5, OperatorAssociativity.LEFT),
        GREATER_OR_EQUAL(">=", 5, OperatorAssociativity.LEFT),
        EQUAL_TO("==", 6, OperatorAssociativity.LEFT),
        UNEQUAL_TO("!=", 6, OperatorAssociativity.LEFT),
        BITWISE_AND("&", 7, OperatorAssociativity.LEFT),
        BITWISE_OR("|", 9, OperatorAssociativity.LEFT),
        BITWISE_XOR("^", 8, OperatorAssociativity.LEFT),
        LOGICAL_AND("&&", 10, OperatorAssociativity.LEFT),
        LOGICAL_OR("||", 11, OperatorAssociativity.LEFT),
        QUESTION("?", 12, OperatorAssociativity.RIGHT),
        TERNARY(":", 12, OperatorAssociativity.RIGHT),
        ASSIGN_BITWISE_AND("&=", 13, OperatorAssociativity.RIGHT),
        ASSIGN_BITWISE_OR("|=", 13, OperatorAssociativity.RIGHT),
        ASSIGN_BITWISE_XOR("^=", 13, OperatorAssociativity.RIGHT),
        ASSIGN_LEFT_SHIFT("<<=", 13, OperatorAssociativity.RIGHT),
        ASSIGN_RIGHT_SHIFT(">>=", 13, OperatorAssociativity.RIGHT),
        ;

        private final String value;
        private final int precedenceLevel;
        private final OperatorAssociativity associativity;

        OperatorType(String value, int precedenceLevel, OperatorAssociativity associativity) {
            this.value = value;
            this.precedenceLevel = precedenceLevel;
            this.associativity = associativity;
        }

        public int precedenceLevel() {
            return precedenceLevel;
        }

        public OperatorAssociativity associativity() {
            return associativity;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
