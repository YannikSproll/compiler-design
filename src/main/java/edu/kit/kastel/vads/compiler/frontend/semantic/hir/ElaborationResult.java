package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import java.util.List;

public sealed interface ElaborationResult permits ElaborationResult.BlockElaborationResult, ElaborationResult.ExpressionElaborationResult, ElaborationResult.LValueElaborationResult, ElaborationResult.NameElaborationResult, ElaborationResult.NodeElaborationResult, ElaborationResult.SingleStatementElaborationResult, ElaborationResult.StatementSequenceElaborationResult, ElaborationResult.TypeElaborationResult {

    default TypedExpression expression() { throw new IllegalStateException(); }
    default TypedStatement statement() { throw new IllegalStateException(); }
    default List<TypedStatement> statements() { throw new IllegalStateException(); }
    default List<TypedNode> nodes() { throw new IllegalStateException(); }
    default HirType type() { throw new IllegalStateException(); }
    default String name() { throw new IllegalStateException(); }
    default TypedBlock block() { throw new IllegalStateException(); }
    default TypedLValue lvalue() { throw new IllegalStateException(); }


    static ElaborationResult expression(TypedExpression expression) {
        return new ExpressionElaborationResult(expression);
    }

    static ElaborationResult statement(TypedStatement statement) {
        return new SingleStatementElaborationResult(statement);
    }

    static ElaborationResult statements(List<TypedStatement> statements) {
        return new StatementSequenceElaborationResult(statements);
    }

    static ElaborationResult nodes(List<TypedNode> node) {
        return new NodeElaborationResult(node);
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
    }

    record LValueElaborationResult(TypedLValue lvalue) implements ElaborationResult {
        @Override
        public TypedLValue lvalue() {
            return lvalue;
        }
    }
}
