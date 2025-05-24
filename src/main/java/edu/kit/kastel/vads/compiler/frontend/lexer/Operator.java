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

    public enum OperatorType {
        ASSIGN_MINUS("-="),
        MINUS("-"),
        ASSIGN_PLUS("+="),
        PLUS("+"),
        MUL("*"),
        ASSIGN_MUL("*="),
        ASSIGN_DIV("/="),
        DIV("/"),
        ASSIGN_MOD("%="),
        MOD("%"),
        ASSIGN("="),
        // L2
        LOGICAL_NOT("!"),
        BITWISE_NOT("~"),
        LEFT_SHIFT("<<"),
        RIGHT_SHIFT(">>"),
        LESS_THAN("<"),
        GREATER_THAN(">"),
        LESS_OR_EQUAL("<="),
        GREATER_OR_EQUAL(">="),
        EQUAL_TO("=="),
        UNEQUAL_TO("!="),
        BITWISE_AND("&"),
        BITWISE_OR("|"),
        BITWISE_XOR("^"),
        LOGICAL_AND("&&"),
        LOGICAL_OR("||"),
        ASSIGN_BITWISE_AND("&="),
        ASSIGN_BITWISE_OR("|="),
        ASSIGN_BITWISE_XOR("^="),
        ASSIGN_LEFT_SHIFT("<<="),
        ASSIGN_RIGHT_SHIFT(">>="),
        ;

        private final String value;

        OperatorType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
