package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import java.util.List;

public sealed interface ElaborationResult permits ElaborationResult.ArgumentResult, ElaborationResult.BlockElaborationResult, ElaborationResult.ExpressionElaborationResult, ElaborationResult.FunctionCallResult, ElaborationResult.LValueElaborationResult, ElaborationResult.NameElaborationResult, ElaborationResult.NodeElaborationResult, ElaborationResult.ParameterResult, ElaborationResult.SingleStatementElaborationResult, ElaborationResult.StatementSequenceElaborationResult, ElaborationResult.TypeElaborationResult {

    default TypedExpression expression() { throw new IllegalStateException(); }
    default TypedStatement statement() { throw new IllegalStateException(); }
    default List<TypedStatement> statements() { throw new IllegalStateException(); }
    default List<TypedNode> nodes() { throw new IllegalStateException(); }
    default HirType type() { throw new IllegalStateException(); }
    default String name() { throw new IllegalStateException(); }
    default TypedBlock block() { throw new IllegalStateException(); }
    default TypedLValue lvalue() { throw new IllegalStateException(); }
    default TypedStatement statementOrBlock() { throw new IllegalStateException(); }
    default TypedParameter parameter() { throw new IllegalStateException(); }
    default TypedArgument argument() { throw new IllegalStateException(); }


    static ElaborationResult expression(TypedExpression expression) {
        return new ExpressionElaborationResult(expression);
    }

    static ElaborationResult statement(TypedStatement statement) {
        return new SingleStatementElaborationResult(statement);
    }

    static ElaborationResult node(TypedNode node) {
        return new NodeElaborationResult(List.of(node));
    }

    static ElaborationResult type(HirType type) {
        return new TypeElaborationResult(type);
    }

    static ElaborationResult name(String name) {
        return new NameElaborationResult(name);
    }

    static ElaborationResult block(TypedBlock block) {
        return new BlockElaborationResult(block);
    }

    static ElaborationResult lvalue(TypedLValue lvalue) {
        return new LValueElaborationResult(lvalue);
    }

    static ElaborationResult parameter(TypedParameter parameter) { return new ParameterResult(parameter); }

    static ElaborationResult argument(TypedArgument argument) { return new ArgumentResult(argument); }

    static ElaborationResult functionCall(TypedFunctionCall functionCall) { return new FunctionCallResult(functionCall); }


    record ExpressionElaborationResult(TypedExpression expression) implements ElaborationResult {
        @Override
        public TypedExpression expression() {
            return expression;
        }
    }

    record SingleStatementElaborationResult(TypedStatement statement) implements ElaborationResult {
        @Override
        public TypedStatement statement() {
            return statement;
        }

        @Override
        public List<TypedStatement> statements() {
            return List.of(statement);
        }

        @Override
        public TypedStatement statementOrBlock() { return statement; }
    }

    record StatementSequenceElaborationResult(List<TypedStatement> statements) implements ElaborationResult {
        @Override
        public List<TypedStatement> statements() {
            return statements;
        }
    }

    record NodeElaborationResult(List<TypedNode> nodes) implements ElaborationResult {
        @Override
        public List<TypedNode> nodes() {
            return nodes;
        }
    }

    record TypeElaborationResult(HirType type) implements ElaborationResult {
        @Override
        public HirType type() {
            return type;
        }
    }

    record NameElaborationResult(String name) implements ElaborationResult {
        @Override
        public String name() {
            return name;
        }
    }

    record BlockElaborationResult(TypedBlock block) implements ElaborationResult {
        @Override
        public TypedBlock block() {
            return block;
        }

        @Override
        public TypedStatement statement() {
            return block;
        }

        @Override
        public List<TypedStatement> statements() {
            return List.of(block);
        }

        @Override
        public TypedStatement statementOrBlock() { return block; }
    }

    record LValueElaborationResult(TypedLValue lvalue) implements ElaborationResult {
        @Override
        public TypedLValue lvalue() {
            return lvalue;
        }
    }

    record ParameterResult(TypedParameter parameter) implements ElaborationResult {
        @Override
        public TypedParameter parameter() {
            return parameter;
        }
    }

    record ArgumentResult(TypedArgument argument) implements ElaborationResult {
        @Override
        public TypedArgument argument() {
            return argument;
        }
    }

    record FunctionCallResult(TypedFunctionCall functionCall) implements ElaborationResult {
        @Override
        public TypedStatement statement() {
            return functionCall;
        }

        @Override
        public TypedExpression expression() {
            return functionCall;
        }
    }
}
