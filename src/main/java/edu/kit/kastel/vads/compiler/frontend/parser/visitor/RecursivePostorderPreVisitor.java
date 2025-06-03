package edu.kit.kastel.vads.compiler.frontend.parser.visitor;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;

import java.util.function.BiFunction;

public class RecursivePostorderPreVisitor<V extends Visitor<T, R>, PV extends PreVisitor<T>, T, R> implements Visitor<T, R> {
    private final V visitor;
    private final PV preVisitor;
    private final BiFunction<T,R, T> accumulate;

    public RecursivePostorderPreVisitor(V visitor, PV preVisitor) {
        this.visitor = visitor;
        this.preVisitor = preVisitor;
        this.accumulate = (data, _) -> data;
    }

    @Override
    public R visit(AssignmentTree assignmentTree, T data) {
        this.preVisitor.preVisit(assignmentTree, data);
        R r = assignmentTree.lValue().accept(this, data);
        r = assignmentTree.expression().accept(this, accumulate.apply(data, r));
        r = this.visitor.visit(assignmentTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(BinaryOperationTree binaryOperationTree, T data) {
        this.preVisitor.preVisit(binaryOperationTree, data);
        R r = binaryOperationTree.lhs().accept(this, data);
        r = binaryOperationTree.rhs().accept(this, accumulate.apply(data, r));
        r = this.visitor.visit(binaryOperationTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(BlockTree blockTree, T data) {
        this.preVisitor.preVisit(blockTree, data);
        R r = null;
        T d = data;
        for (StatementTree statement : blockTree.statements()) {
            r = statement.accept(this, d);
            d = accumulate.apply(d, r);
        }
        r = this.visitor.visit(blockTree, d);
        return r;
    }

    @Override
    public R visit(DeclarationTree declarationTree, T data) {
        this.preVisitor.preVisit(declarationTree, data);
        R r = declarationTree.type().accept(this, data);
        r = declarationTree.name().accept(this, accumulate.apply(data, r));
        if (declarationTree.initializer() != null) {
            r = declarationTree.initializer().accept(this, accumulate.apply(data, r));
        }
        r = this.visitor.visit(declarationTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(FunctionTree functionTree, T data) {
        this.preVisitor.preVisit(functionTree, data);
        R r = functionTree.returnType().accept(this, data);
        r = functionTree.name().accept(this, accumulate.apply(data, r));
        r = functionTree.body().accept(this, accumulate.apply(data, r));
        r = this.visitor.visit(functionTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(IdentExpressionTree identExpressionTree, T data) {
        this.preVisitor.preVisit(identExpressionTree, data);
        R r = identExpressionTree.name().accept(this, data);
        r = this.visitor.visit(identExpressionTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(ConditionalExpressionTree conditionalExpressionTree, T data) {
        this.preVisitor.preVisit(conditionalExpressionTree, data);
        R r = conditionalExpressionTree.conditionTree().accept(this, data);
        r = conditionalExpressionTree.thenTree().accept(this, accumulate.apply(data, r));
        r = conditionalExpressionTree.elseTree().accept(this, accumulate.apply(data, r));
        this.visitor.visit(conditionalExpressionTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(IntLiteralTree intLiteralTree, T data) {
        this.preVisitor.preVisit(intLiteralTree, data);
        return this.visitor.visit(intLiteralTree, data);
    }

    @Override
    public R visit(BoolLiteralTree boolLiteralTree, T data) {
        this.preVisitor.preVisit(boolLiteralTree, data);
        return this.visitor.visit(boolLiteralTree, data);
    }

    @Override
    public R visit(LValueIdentTree lValueIdentTree, T data) {
        this.preVisitor.preVisit(lValueIdentTree, data);
        R r = lValueIdentTree.name().accept(this, data);
        r = this.visitor.visit(lValueIdentTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(NameTree nameTree, T data) {
        this.preVisitor.preVisit(nameTree, data);
        return this.visitor.visit(nameTree, data);
    }

    @Override
    public R visit(NegateTree negateTree, T data) {
        this.preVisitor.preVisit(negateTree, data);
        R r = negateTree.expression().accept(this, data);
        r = this.visitor.visit(negateTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(ProgramTree programTree, T data) {
        this.preVisitor.preVisit(programTree, data);
        R r = null;
        T d = data;
        for (FunctionTree tree : programTree.topLevelTrees()) {
            r = tree.accept(this, d);
            d = accumulate.apply(data, r);
        }
        r = this.visitor.visit(programTree, d);
        return r;
    }

    @Override
    public R visit(ReturnTree returnTree, T data) {
        this.preVisitor.preVisit(returnTree, data);
        R r = returnTree.expression().accept(this, data);
        r = this.visitor.visit(returnTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(BreakTree breakTree, T data) {
        this.preVisitor.preVisit(breakTree, data);
        return this.visitor.visit(breakTree, data);
    }

    @Override
    public R visit(ContinueTree continueTree, T data) {
        this.preVisitor.preVisit(continueTree, data);
        return this.visitor.visit(continueTree, data);
    }

    @Override
    public R visit(ForTree forTree, T data) {
        this.preVisitor.preVisit(forTree, data);
        R r = null;
        if (forTree.initializationStatementTree() != null) {
            r = forTree.initializationStatementTree().accept(this, data);
        }
        r = forTree.conditionExpressionTree().accept(this, accumulate.apply(data, r));
        if (forTree.postIterationStatementTree() != null) {
            r = forTree.postIterationStatementTree().accept(this, accumulate.apply(data, r));
        }
        r = forTree.bodyStatementTree().accept(this, accumulate.apply(data, r));
        r = this.visitor.visit(forTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(IfTree ifTree, T data) {
        this.preVisitor.preVisit(ifTree, data);
        R r = ifTree.conditionExpressionTree().accept(this, data);
        r = ifTree.statementTree().accept(this, accumulate.apply(data, r));
        if (ifTree.elseTree() != null) {
            r = ifTree.elseTree().accept(this, accumulate.apply(data, r));
        }
        r = this.visitor.visit(ifTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(ElseTree elseTree, T data) {
        this.preVisitor.preVisit(elseTree, data);
        R r = elseTree.statementTree().accept(this, data);
        r = this.visitor.visit(elseTree, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(WhileTree whileTree, T data) {
        this.preVisitor.preVisit(whileTree, data);
        R r = whileTree.conditionExpressionTree().accept(this, data);
        r =  whileTree.statementTree().accept(this, accumulate.apply(data, r));
        return r;
    }

    @Override
    public R visit(TypeTree typeTree, T data) {
        this.preVisitor.preVisit(typeTree, data);
        return this.visitor.visit(typeTree, data);
    }
}
