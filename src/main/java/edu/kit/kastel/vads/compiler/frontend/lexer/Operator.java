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
        // L1
        MUL("*", 12, OperatorAssociativity.LEFT),
        DIV("/", 12, OperatorAssociativity.LEFT),
        MOD("%", 12, OperatorAssociativity.LEFT),
        MINUS("-", 11, OperatorAssociativity.LEFT),
        PLUS("+", 11, OperatorAssociativity.LEFT),
        ASSIGN_MINUS("-=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_PLUS("+=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_MUL("*=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_DIV("/=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_MOD("%=", 1, OperatorAssociativity.RIGHT),
        ASSIGN("=", 1, OperatorAssociativity.RIGHT),
        // L2
        LOGICAL_NOT("!", 13, OperatorAssociativity.RIGHT),
        BITWISE_NOT("~", 13, OperatorAssociativity.RIGHT),
        LEFT_SHIFT("<<", 10, OperatorAssociativity.LEFT),
        RIGHT_SHIFT(">>", 10, OperatorAssociativity.LEFT),
        LESS_THAN("<", 9, OperatorAssociativity.LEFT),
        GREATER_THAN(">", 9, OperatorAssociativity.LEFT),
        LESS_OR_EQUAL("<=", 9, OperatorAssociativity.LEFT),
        GREATER_OR_EQUAL(">=", 9, OperatorAssociativity.LEFT),
        EQUAL_TO("==", 8, OperatorAssociativity.LEFT),
        UNEQUAL_TO("!=", 8, OperatorAssociativity.LEFT),
        BITWISE_AND("&", 7, OperatorAssociativity.LEFT),
        BITWISE_XOR("^", 6, OperatorAssociativity.LEFT),
        BITWISE_OR("|", 5, OperatorAssociativity.LEFT),
        LOGICAL_AND("&&", 4, OperatorAssociativity.LEFT),
        LOGICAL_OR("||", 3, OperatorAssociativity.LEFT),
        QUESTION("?", 2, OperatorAssociativity.RIGHT),
        TERNARY(":", 2, OperatorAssociativity.RIGHT),
        ASSIGN_BITWISE_AND("&=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_BITWISE_OR("|=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_BITWISE_XOR("^=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_LEFT_SHIFT("<<=", 1, OperatorAssociativity.RIGHT),
        ASSIGN_RIGHT_SHIFT(">>=", 1, OperatorAssociativity.RIGHT),
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
