package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.lexer.Operator;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.*;
import edu.kit.kastel.vads.compiler.frontend.parser.type.BasicType;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.Visitor;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static edu.kit.kastel.vads.compiler.frontend.semantic.ElaborationUtils.*;

// Elaborates abstract syntax tree
// Construct hir tree
// Checks for integer ranges
// Does type checking
public class Elaborator implements
        Visitor<ElaborationContext, ElaborationResult> {

    private final TypeChecker typeChecker;

    public Elaborator(TypeChecker typeChecker) {
        this.typeChecker = typeChecker;
    }

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

        typeChecker.expectEqualTypes(lValueResult.lvalue(), expressionResult.expression());

        return switch (assignmentTree.operator().type()) {
            case Operator.OperatorType.ASSIGN:  {
                TypedAssignment assignment = new TypedAssignment(
                        lValueResult.lvalue(),
                        expressionResult.expression(),
                        assignmentTree.span());
                yield ElaborationResult.statement(assignment);
            }
            case Operator.OperatorType.ASSIGN_PLUS,
                 Operator.OperatorType.ASSIGN_MINUS,
                 Operator.OperatorType.ASSIGN_MUL,
                 Operator.OperatorType.ASSIGN_DIV,
                 Operator.OperatorType.ASSIGN_MOD,
                 Operator.OperatorType.ASSIGN_BITWISE_AND,
                 Operator.OperatorType.ASSIGN_BITWISE_OR,
                 Operator.OperatorType.ASSIGN_BITWISE_XOR,
                 Operator.OperatorType.ASSIGN_LEFT_SHIFT,
                 Operator.OperatorType.ASSIGN_RIGHT_SHIFT: {

                typeChecker.expectType(HirType.INT, lValueResult.lvalue());

                TypedAssignment assignment = new TypedAssignment(
                        lValueResult.lvalue(),
                        new TypedBinaryOperation(
                                HirType.INT,
                                mapAssignmentOperationToBinaryOperator(assignmentTree.operator()),
                                lValueResult.lvalue().asVariable(),
                                expressionResult.expression(),
                                assignmentTree.span()),
                        assignmentTree.span());

                yield ElaborationResult.statement(assignment);
            }
            default: {
                throw new IllegalStateException("Unexpected operator type: " + assignmentTree.operator().type());
            }
        };
    }

    @Override
    public ElaborationResult visit(BinaryOperationTree binaryOperationTree, ElaborationContext context) {
        ElaborationResult lhsResult = binaryOperationTree.lhs().accept(this, context);
        ElaborationResult rhsResult = binaryOperationTree.rhs().accept(this, context);

        TypedExpression resultExpression = switch (binaryOperationTree.operatorType()) {
            case PLUS,
                 MINUS,
                 MUL,
                 DIV,
                 MOD,
                 LEFT_SHIFT,
                 RIGHT_SHIFT,
                 BITWISE_OR,
                 BITWISE_AND,
                 BITWISE_XOR -> {
                typeChecker.expectType(HirType.INT, lhsResult.expression());
                typeChecker.expectType(HirType.INT, rhsResult.expression());

                yield new TypedBinaryOperation(
                        lhsResult.expression().type(),
                        mapBinaryOperator(binaryOperationTree.operatorType()),
                        lhsResult.expression(),
                        rhsResult.expression(),
                        binaryOperationTree.span());
            }
            case EQUAL_TO, UNEQUAL_TO -> {
                typeChecker.expectEqualTypes(lhsResult.expression(), rhsResult.expression());

                yield new TypedBinaryOperation(
                        HirType.BOOLEAN,
                        mapBinaryOperator(binaryOperationTree.operatorType()),
                        lhsResult.expression(),
                        rhsResult.expression(),
                        binaryOperationTree.span());
            }
            case GREATER_THAN,
                 LESS_THAN,
                 GREATER_OR_EQUAL,
                 LESS_OR_EQUAL -> {
                typeChecker.expectType(HirType.INT, lhsResult.expression());
                typeChecker.expectType(HirType.INT, rhsResult.expression());

                yield new TypedBinaryOperation(
                        HirType.BOOLEAN,
                        mapBinaryOperator(binaryOperationTree.operatorType()),
                        lhsResult.expression(),
                        rhsResult.expression(),
                        binaryOperationTree.span());
            }
            case LOGICAL_AND -> {
                typeChecker.expectType(HirType.BOOLEAN, lhsResult.expression());
                typeChecker.expectType(HirType.BOOLEAN, rhsResult.expression());

                yield new TypedConditionalExpression(
                        HirType.BOOLEAN,
                        lhsResult.expression(),
                        rhsResult.expression(),
                        new TypedBoolLiteral(false, HirType.BOOLEAN, binaryOperationTree.span()),
                        binaryOperationTree.span());
            }
            case LOGICAL_OR -> {
                typeChecker.expectType(HirType.BOOLEAN, lhsResult.expression());
                typeChecker.expectType(HirType.BOOLEAN, rhsResult.expression());

                yield new TypedConditionalExpression(
                        HirType.BOOLEAN,
                        lhsResult.expression(),
                        new TypedBoolLiteral(true, HirType.BOOLEAN, binaryOperationTree.span()),
                        rhsResult.expression(),
                        binaryOperationTree.span());
            }
            default -> throw new IllegalStateException("Unexpected operator type: " + binaryOperationTree.operatorType());
        };

        return ElaborationResult.expression(resultExpression);
    }

    @Override
    public ElaborationResult visit(BlockTree blockTree, ElaborationContext context) {
        Scope currentScope = context.symbolTable().enterScope(ScopeType.BLOCK);

        List<TypedStatement> statements = new ArrayList<>();
        for (StatementTree statementTree : blockTree.statements()) {
            ElaborationResult statementResult = statementTree.accept(this, context);
            statements.addAll(statementResult.statements());
        }

        context.symbolTable().exitScope();

        TypedBlock typedBlock = new TypedBlock(
                statements,
                Optional.of(currentScope),
                blockTree.span());
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
            typedInitializer = Optional.of(initializerResult.expression());
        }

        Symbol declaredVariableSymbol = new Symbol(
                nameResult.name(),
                typeResult.type(),
                declarationTree.span());
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
        Scope currentScope = context.symbolTable().enterScope(ScopeType.FUNCTION);

        ElaborationResult returnTypeResult = functionTree.returnType().accept(this, context);
        ElaborationResult nameResult = functionTree.name().accept(this, context);
        ElaborationResult bodyResult = functionTree.body().accept(this, context);

        context.symbolTable().exitScope();

        Symbol functionSymbol = new Symbol(
                nameResult.name(),
                returnTypeResult.type(),
                functionTree.span());
        TypedFunction typedFunction = new TypedFunction(
                functionSymbol,
                bodyResult.block(),
                currentScope,
                MAIN_FUNCTION_NAME.equals(functionSymbol.name()));
        return ElaborationResult.node(typedFunction);
    }

    @Override
    public ElaborationResult visit(IdentExpressionTree identExpressionTree, ElaborationContext context) {
        ElaborationResult nameResult = identExpressionTree.name().accept(this, context);

        Symbol variableSymbol = context.symbolTable().getCurrentScope().typeOf(nameResult.name());

        TypedVariable typedVariable = new TypedVariable(variableSymbol, identExpressionTree.span());
        return ElaborationResult.expression(typedVariable);
    }

    @Override
    public ElaborationResult visit(ConditionalExpressionTree conditionalExpressionTree, ElaborationContext context) {
        ElaborationResult conditionResult = conditionalExpressionTree.conditionTree().accept(this, context);
        ElaborationResult thenExpResult = conditionalExpressionTree.thenTree().accept(this, context);
        ElaborationResult elseExpResult = conditionalExpressionTree.elseTree().accept(this,context);

        typeChecker.expectType(HirType.BOOLEAN, conditionResult.expression());

        typeChecker.expectEqualTypes(thenExpResult.expression(), elseExpResult.expression());

        TypedConditionalExpression typedConditionalExpression = new TypedConditionalExpression(
                thenExpResult.expression().type(), // Is the same as for elseExpResult because it is checked above
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
                HirType.BOOLEAN,
                boolLiteralTree.span());
        return ElaborationResult.expression(typedBoolLiteral);
    }

    @Override
    public ElaborationResult visit(LValueIdentTree lValueIdentTree, ElaborationContext context) {
        ElaborationResult nameResult = lValueIdentTree.name().accept(this, context);

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

        TypedUnaryOperation typedUnaryOperation;
         switch (negateTree.operatorType()) {
             case Operator.OperatorType.MINUS,
                  Operator.OperatorType.BITWISE_NOT: {
                 typeChecker.expectType(HirType.INT, typedExpression);

                  typedUnaryOperation = new TypedUnaryOperation(
                        mapUnaryOperator(negateTree.operatorType()),
                        typedExpression,
                        negateTree.span());
                  break;
            }
            case Operator.OperatorType.LOGICAL_NOT: {
                typeChecker.expectType(HirType.BOOLEAN, typedExpression);

                 typedUnaryOperation = new TypedUnaryOperation(
                        UnaryOperator.LOGICAL_NOT,
                        typedExpression,
                        negateTree.span());
                break;
            }
             default: throw new SemanticException("Unsupported expression type: " + typedExpression.type());
        }

        return ElaborationResult.expression(typedUnaryOperation);
    }

    @Override
    public ElaborationResult visit(ProgramTree programTree, ElaborationContext context) {
        Scope currentScope = context.symbolTable().enterScope(ScopeType.FILE);

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

        TypedFile typedFile = new TypedFile(functions, currentScope);
        return ElaborationResult.node(typedFile);
    }

    @Override
    public ElaborationResult visit(ReturnTree returnTree, ElaborationContext context) {
        ElaborationResult expressionResult = returnTree.expression().accept(this, context);

        // This changes as soon as function are allowed to return more than ints
        typeChecker.expectType(HirType.INT, expressionResult.expression());

        TypedReturn typedReturn = new TypedReturn(expressionResult.expression(), returnTree.span());
        return ElaborationResult.statement(typedReturn);
    }

    @Override
    public ElaborationResult visit(BreakTree breakTree, ElaborationContext context) {
        if (!context.isCurrentlyInLoop()) {
            throw new SemanticException("break not in loop");
        }

        return ElaborationResult.statement(new TypedBreak(breakTree.span()));
    }

    @Override
    public ElaborationResult visit(ContinueTree continueTree, ElaborationContext context) {
        if (!context.isCurrentlyInLoop()) {
            throw new SemanticException("continue not in loop");
        }

        return ElaborationResult.statement(new TypedContinue(continueTree.span()));
    }

    @Override
    public ElaborationResult visit(ForTree forTree, ElaborationContext context) {
        context.incrementNestedLoopDepth();

        ElaborationResult initializerResult = null;
         if (forTree.initializationStatementTree() != null){
             initializerResult = forTree.initializationStatementTree().accept(this, context);
         }

        ElaborationResult conditionResult = forTree.conditionExpressionTree().accept(this, context);

        ElaborationResult stepResult = null;

        if (forTree.postIterationStatementTree() != null) {
            stepResult = forTree.postIterationStatementTree().accept(this, context);

            // Make sure post iteration statement is not a declaration
            if (stepResult.statement() instanceof TypedDeclaration) {
                throw new SemanticException("Step statement of for loop may not be a declaration.");
            }
        }

        ElaborationResult bodyElaborationResult = forTree.bodyStatementTree().accept(this, context);

        TypedLoop typedLoop = new TypedLoop(
                conditionResult.expression(),
                bodyElaborationResult.block(),
                stepResult != null ? Optional.of(stepResult.statement()) : Optional.empty(),
                forTree.span());

        TypedBlock typedBlock = new TypedBlock(
                initializerResult != null
                ? List.of(
                        initializerResult.statement(),
                        typedLoop)
                : List.of(typedLoop),
                Optional.empty(),
                forTree.span());

        context.decrementNestedLoopDepth();

        return ElaborationResult.block(typedBlock);
    }

    @Override
    public ElaborationResult visit(IfTree ifTree, ElaborationContext context) {
        ElaborationResult conditionResult = ifTree.conditionExpressionTree().accept(this, context);
        ElaborationResult thenResult = ifTree.statementTree().accept(this, context);

        if (conditionResult.expression().type() != HirType.BOOLEAN) {
            throw new SemanticException("If statement must have an boolean type.");
        }

        Optional<TypedStatement> elseResult = Optional.empty();
        if (ifTree.elseTree() != null) {
            elseResult = Optional.of(ifTree.elseTree().accept(this, context).statement());
        }

        TypedIf typedIf = new TypedIf(
                conditionResult.expression(),
                thenResult.block(),
                elseResult,
                ifTree.span());

        return ElaborationResult.statement(typedIf);
    }

    @Override
    public ElaborationResult visit(ElseTree elseTree, ElaborationContext context) {
        return elseTree.statementTree().accept(this, context);
    }

    @Override
    public ElaborationResult visit(WhileTree whileTree, ElaborationContext context) {
        context.incrementNestedLoopDepth();

        ElaborationResult conditionResult = whileTree.conditionExpressionTree().accept(this, context);
        ElaborationResult bodyResult = whileTree.statementTree().accept(this, context);

        if (conditionResult.expression().type() != HirType.BOOLEAN) {
            throw new SemanticException("While statement must have an boolean type.");
        }

        TypedLoop typedLoop = new TypedLoop(
                conditionResult.expression(),
                bodyResult.block(),
                Optional.empty(),
                whileTree.span());

        context.decrementNestedLoopDepth();

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
