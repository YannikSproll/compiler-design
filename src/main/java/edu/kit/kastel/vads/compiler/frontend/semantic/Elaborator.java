package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.type.BasicType;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class Elaborator implements
        Visitor<ElaborationContext, ElaborationResult> {

    public TypedFile elaborate(ProgramTree program) {

        SymbolTable symbolTable = new SymbolTable();
        ElaborationContext context = new ElaborationContext(symbolTable);

        program.accept(this, context);
        ElaborationResult elaborationResult = this.visit(program, context);

        return elaborationResult
                .nodes()
                .getFirst()
                .asTypedFile();
    }

    @Override
    public ElaborationResult visit(AssignmentTree assignmentTree, ElaborationContext context) {
        ElaborationResult lValueResult = assignmentTree.lValue().accept(this, context);
        ElaborationResult expressionResult = assignmentTree.expression().accept(this, context);

        //TODO: Handle assignment operators
        Symbol lValueSymbol = lValueResult.lvalue().asVariable().symbol();


        if (!lValueSymbol.isAssigned()) {
            lValueSymbol.markAsAssigned(assignmentTree.span());
        }

        TypedAssignment assignment = new TypedAssignment(
                lValueResult.lvalue(),
                expressionResult.expression(),
                assignmentTree.span());
        return ElaborationResult.statement(assignment);
    }

    @Override
    public ElaborationResult visit(BinaryOperationTree binaryOperationTree, ElaborationContext context) {
        ElaborationResult lhsResult = binaryOperationTree.lhs().accept(this, context);

        ElaborationResult rhsResult = binaryOperationTree.rhs().accept(this, context);

        //TODO: Check types
        TypedBinaryOperation typedBinaryOperation = new TypedBinaryOperation(
                HirType.INT,
                lhsResult.expression(),
                rhsResult.expression(),
                binaryOperationTree.span());
        return ElaborationResult.expression(typedBinaryOperation);
    }

    @Override
    public ElaborationResult visit(BlockTree blockTree, ElaborationContext context) {
        context.symbolTable().enterScope();

        List<TypedStatement> statements = new ArrayList<>();
        for (StatementTree statementTree : blockTree.statements()) {
            ElaborationResult statementResult = statementTree.accept(this, context);
            statements.addAll(statementResult.statements());
        }

        context.symbolTable().exitScope();

        TypedBlock typedBlock = new TypedBlock(statements, blockTree.span());
        return ElaborationResult.block(typedBlock);
    }

    @Override
    public ElaborationResult visit(DeclarationTree declarationTree, ElaborationContext context) {
        ElaborationResult typeResult = declarationTree.type().accept(this, context);
        ElaborationResult nameResult = declarationTree.name().accept(this, context);

        if (context.symbolTable().isVariableDeclared(nameResult.name())) {
            throw new SemanticException("The variable " + nameResult.name() + " is already declared.");
        }


        Optional<TypedExpression> typedInitializer = Optional.empty();
        if (declarationTree.initializer() != null) {
            ElaborationResult initializerResult = declarationTree.initializer().accept(this, context);
            // TODO: Check if error.
            typedInitializer = Optional.of(initializerResult.expression());
        }

        Symbol declaredVariableSymbol = new Symbol(
                nameResult.name(),
                typeResult.type(),
                declarationTree.span(),
                declarationTree.initializer() != null ? Optional.of(declarationTree.span()) : Optional.empty());
        context.symbolTable().getCurrentScope().putType(nameResult.name(), declaredVariableSymbol);

        TypedDeclaration typedDeclaration = new TypedDeclaration(
                declaredVariableSymbol,
                typeResult.type(),
                typedInitializer,
                declarationTree.span());
        return ElaborationResult.statement(typedDeclaration);
    }

    @Override
    public ElaborationResult visit(FunctionTree functionTree, ElaborationContext context) {
        context.symbolTable().enterScope();

        ElaborationResult returnTypeResult = functionTree.returnType().accept(this, context);
        ElaborationResult nameResult = functionTree.name().accept(this, context);
        ElaborationResult bodyResult = functionTree.body().accept(this, context);

        context.symbolTable().exitScope();

        Symbol functionSymbol = new Symbol(
                nameResult.name(),
                returnTypeResult.type(),
                functionTree.span(),
                Optional.empty());
        TypedFunction typedFunction = new TypedFunction(functionSymbol, bodyResult.block());
        return ElaborationResult.node(typedFunction);
    }

    @Override
    public ElaborationResult visit(IdentExpressionTree identExpressionTree, ElaborationContext context) {
        ElaborationResult nameResult = identExpressionTree.name().accept(this, context);

        Symbol variableSymbol = context.symbolTable().getCurrentScope().typeOf(nameResult.name());

        if (!variableSymbol.isAssigned()) {
            throw new SemanticException("Variable " + nameResult.name() + " can not be used before it is assigned.");
        }

        TypedVariable typedVariable = new TypedVariable(variableSymbol, identExpressionTree.span());
        return ElaborationResult.expression(typedVariable);
    }

    @Override
    public ElaborationResult visit(ConditionalExpressionTree conditionalExpressionTree, ElaborationContext context) {
        ElaborationResult conditionResult = conditionalExpressionTree.conditionTree().accept(this, context);
        ElaborationResult thenExpResult = conditionalExpressionTree.thenTree().accept(this, context);
        ElaborationResult elseExpResult = conditionalExpressionTree.elseTree().accept(this,context);

        TypedConditionalExpression typedConditionalExpression = new TypedConditionalExpression(
                HirType.INT,
                conditionResult.expression(),
                thenExpResult.expression(),
                elseExpResult.expression(),
                conditionalExpressionTree.span());
        return ElaborationResult.expression(typedConditionalExpression);
    }

    @Override
    public ElaborationResult visit(IntLiteralTree intLiteralTree, ElaborationContext context) {
        OptionalLong value = intLiteralTree.parseValue();
        if (value.isEmpty()) {
            // Not valid int range value.
            // Todo: Create error or warning
            throw new SemanticException("invalid integer literal " + intLiteralTree.value());
        }

        TypedIntLiteral typedIntLiteral = new TypedIntLiteral(
                (int) value.getAsLong(),
                HirType.INT,
                intLiteralTree.span());

        return ElaborationResult.expression(typedIntLiteral);
    }

    @Override
    public ElaborationResult visit(BoolLiteralTree boolLiteralTree, ElaborationContext context) {
        TypedBoolLiteral typedBoolLiteral = new TypedBoolLiteral(
                boolLiteralTree.value(),
                HirType.INVALID,
                boolLiteralTree.span());
        return ElaborationResult.expression(typedBoolLiteral);
    }

    @Override
    public ElaborationResult visit(LValueIdentTree lValueIdentTree, ElaborationContext context) {
        ElaborationResult nameResult = lValueIdentTree.name().accept(this, context);

        if (!context.symbolTable().isVariableDeclared(nameResult.name())) {
            throw new SemanticException("Variable " + nameResult.name() + " can not be used before it is declared.");
        }
        Symbol variableSymbol = context.symbolTable().getCurrentScope().typeOf(nameResult.name());

        TypedVariable typedVariable = new TypedVariable(variableSymbol, lValueIdentTree.span());
        return ElaborationResult.lvalue(typedVariable);
    }

    @Override
    public ElaborationResult visit(NameTree nameTree, ElaborationContext context) {
        return ElaborationResult.name(nameTree.name().asString());
    }

    @Override
    public ElaborationResult visit(NegateTree negateTree, ElaborationContext context) {
        TypedExpression typedExpression = negateTree.expression().accept(this, context)
                .expression();
        TypedUnaryOperation typedUnaryOperation = new TypedUnaryOperation(
                UnaryOperator.NEGATION,
                typedExpression,
                negateTree.span());
        return ElaborationResult.expression(typedUnaryOperation);
    }

    @Override
    public ElaborationResult visit(ProgramTree programTree, ElaborationContext context) {
        context.symbolTable().enterScope();

        List<TypedFunction> functions = new ArrayList<>();
        for (FunctionTree functionTree : programTree.topLevelTrees()) {
            functions.addAll(
                    functionTree.accept(this, context)
                            .nodes()
                            .stream()
                            .map(TypedNode::asTypedFunction)
                            .toList());
        }

        context.symbolTable().exitScope();

        TypedFile typedFile = new TypedFile(functions);
        return ElaborationResult.node(typedFile);
    }

    @Override
    public ElaborationResult visit(ReturnTree returnTree, ElaborationContext context) {
        ElaborationResult expressionResult = returnTree.expression().accept(this, context);
        TypedReturn typedReturn = new TypedReturn(expressionResult.expression(), returnTree.span());
        return ElaborationResult.statement(typedReturn);
    }

    @Override
    public ElaborationResult visit(BreakTree breakTree, ElaborationContext context) {
        return ElaborationResult.statement(new TypedBreak(breakTree.span()));
    }

    @Override
    public ElaborationResult visit(ContinueTree continueTree, ElaborationContext context) {
        return ElaborationResult.statement(new TypedContinue(continueTree.span()));
    }

    @Override
    public ElaborationResult visit(ForTree forTree, ElaborationContext context) {
        ElaborationResult initializerResult = forTree.initializationStatementTree().accept(this, context);
        ElaborationResult conditionResult = forTree.conditionExpressionTree().accept(this, context);
        ElaborationResult stepResult = forTree.postIterationStatementTree().accept(this, context);

        ElaborationResult bodyElaborationResult = forTree.bodyStatementTree().accept(this, context);

        TypedLoop typedLoop = new TypedLoop(
                new TypedBlock(
                        List.of(
                                new TypedIf(
                                        new TypedUnaryOperation(
                                                UnaryOperator.NEGATION,
                                                conditionResult.expression(),
                                                conditionResult.expression().span()),
                                        new TypedBlock(
                                                List.of(
                                                        new TypedBreak(
                                                                conditionResult.expression().span())),
                                                conditionResult.expression().span()),
                                        Optional.empty(),
                                        conditionResult.expression().span()),
                                bodyElaborationResult.block(),
                                stepResult.statement()),
                        forTree.span()),
                forTree.span());

        TypedBlock typedBlock = new TypedBlock(
                List.of(
                        initializerResult.statement(),
                        typedLoop),
                forTree.span());

        return ElaborationResult.block(typedBlock);
    }

    @Override
    public ElaborationResult visit(IfTree ifTree, ElaborationContext context) {
        ElaborationResult conditionResult = ifTree.conditionExpressionTree().accept(this, context);
        ElaborationResult thenResult = ifTree.statementTree().accept(this, context);

        Optional<TypedStatement> elseResult = Optional.empty();
        if (ifTree.elseTree() != null) {
            //elseResult = Optional.of(ifTree.elseTree().accept(this, context).statements())
        }
        TypedIf typedIf = new TypedIf(
                conditionResult.expression(),
                thenResult.block(),
                Optional.empty(),
                ifTree.span());

        return ElaborationResult.statement(typedIf);
    }

    @Override
    public ElaborationResult visit(ElseTree elseTree, ElaborationContext context) {
        return elseTree.statementTree().accept(this, context);
    }

    @Override
    public ElaborationResult visit(WhileTree whileTree, ElaborationContext context) {
        ElaborationResult conditionResult = whileTree.conditionExpressionTree().accept(this, context);
        ElaborationResult bodyResult = whileTree.statementTree().accept(this, context);

        TypedLoop typedLoop = new TypedLoop(
                new TypedBlock(
                        List.of(
                                new TypedIf(
                                        new TypedUnaryOperation(
                                                UnaryOperator.NEGATION,
                                                conditionResult.expression(),
                                                conditionResult.expression().span()),
                                        new TypedBlock(
                                                List.of(
                                                        new TypedBreak(
                                                                conditionResult.expression().span())),
                                                conditionResult.expression().span()),
                                        Optional.empty(),
                                        conditionResult.expression().span()),
                                bodyResult.block()
                        ),
                        whileTree.span()),
                whileTree.span());


        return ElaborationResult.statement(typedLoop);
    }

    @Override
    public ElaborationResult visit(TypeTree typeTree, ElaborationContext context) {
        HirType hirType = switch (typeTree.type()) {
            case BasicType.INT -> HirType.INT;
            case BasicType.BOOL -> HirType.BOOLEAN;
        };
        return ElaborationResult.type(hirType);
    }
}
