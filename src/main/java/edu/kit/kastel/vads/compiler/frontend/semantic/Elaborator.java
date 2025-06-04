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

        Symbol lValueSymbol = lValueResult.lvalue().asVariable().symbol();

        if (lValueSymbol.type() != expressionResult.expression().type()) {
            throw new SemanticException("Type mismatch of variable " + lValueSymbol.name() + " and assigned expression");
        }

        return switch (assignmentTree.operator().type()) {
            case Operator.OperatorType.ASSIGN:  {

                if (!lValueSymbol.isAssigned()) {
                    lValueSymbol.markAsAssigned(assignmentTree.span());
                }

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
                 Operator.OperatorType.ASSIGN_RIGHT_SHIFT:{
                if (!lValueSymbol.isAssigned()) {
                    throw new SemanticException("Variable " + lValueSymbol + " must be assigned before using it.");
                }

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

    private static BinaryOperator mapAssignmentOperationToBinaryOperator(Operator operator) {
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

    private static BinaryOperator mapBinaryOperator(Operator.OperatorType operatorType) {
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
                if (lhsResult.expression().type() != HirType.INT
                    || rhsResult.expression().type() != HirType.INT) {
                    throw new IllegalStateException("Unexpected expression type: " + lhsResult.expression().type());
                }

                yield new TypedBinaryOperation(
                        lhsResult.expression().type(),
                        mapBinaryOperator(binaryOperationTree.operatorType()),
                        lhsResult.expression(),
                        rhsResult.expression(),
                        binaryOperationTree.span());
            }
            case EQUAL_TO, UNEQUAL_TO -> {
                if (lhsResult.expression().type() != rhsResult.expression().type()) {
                    throw new IllegalStateException("Unexpected expression type: " + lhsResult.expression().type());
                }
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
                if (lhsResult.expression().type() != HirType.INT
                        || rhsResult.expression().type() != HirType.INT) {
                    throw new IllegalStateException("Unexpected expression type: " + lhsResult.expression().type());
                }
                yield new TypedBinaryOperation(
                        HirType.BOOLEAN,
                        mapBinaryOperator(binaryOperationTree.operatorType()),
                        lhsResult.expression(),
                        rhsResult.expression(),
                        binaryOperationTree.span());
            }
            case LOGICAL_AND -> {
                if (lhsResult.expression().type() != HirType.BOOLEAN
                        || rhsResult.expression().type() != HirType.BOOLEAN) {
                    throw new IllegalStateException("Unexpected expression type: " + lhsResult.expression().type());
                }

                yield new TypedConditionalExpression(
                        HirType.BOOLEAN,
                        lhsResult.expression(),
                        rhsResult.expression(),
                        new TypedBoolLiteral(false, HirType.BOOLEAN, binaryOperationTree.span()),
                        binaryOperationTree.span());
            }
            case LOGICAL_OR -> {
                if (lhsResult.expression().type() != HirType.BOOLEAN
                        || rhsResult.expression().type() != HirType.BOOLEAN) {
                    throw new IllegalStateException("Unexpected expression type: " + lhsResult.expression().type());
                }

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

        if (conditionResult.expression().type() != HirType.BOOLEAN) {
            throw new SemanticException("Conditions must have boolean type");
        }

        if (thenExpResult.expression().type() != elseExpResult.expression().type()) {
            throw new SemanticException("The expression at"
                    + thenExpResult.expression().span() + " and at "
                    + elseExpResult.expression().span() + " are not of the same type.");
        }

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

        TypedUnaryOperation typedUnaryOperation = null;
         switch (negateTree.operatorType()) {
             case Operator.OperatorType.MINUS,
                  Operator.OperatorType.BITWISE_NOT: {

                 if (typedExpression.type() != HirType.BOOLEAN) {
                     throw new SemanticException("The unary operator " + negateTree.operatorType() + " requires an integer expression.");
                 }

                  typedUnaryOperation = new TypedUnaryOperation(
                        mapUnaryOperator(negateTree.operatorType()),
                        typedExpression,
                        negateTree.span());
                  break;
            }
            case Operator.OperatorType.LOGICAL_NOT: {

                if (typedExpression.type() != HirType.BOOLEAN) {
                    throw new SemanticException("The unary operator " + negateTree.operatorType() + " requires a bool expression.");
                }

                 typedUnaryOperation = new TypedUnaryOperation(
                        UnaryOperator.LOGICAL_NOT,
                        typedExpression,
                        negateTree.span());
                break;
            }
             default: throw new SemanticException("Unsupported expression type: " + typedExpression.type());
        };

        return ElaborationResult.expression(typedUnaryOperation);
    }

    private static UnaryOperator mapUnaryOperator(Operator.OperatorType operatorType) {
        return switch (operatorType) {
            case Operator.OperatorType.MINUS -> UnaryOperator.NEGATION;
            case Operator.OperatorType.BITWISE_NOT -> UnaryOperator.BITWISE_NOT;
            case Operator.OperatorType.LOGICAL_NOT -> UnaryOperator.LOGICAL_NOT;
            default -> throw new SemanticException("Unsupported operator type: " + operatorType);
        };
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

        // This changes as soon as function are allowed to return more than ints
        if (expressionResult.expression().type() != HirType.INT) {
            throw new SemanticException("Return statement must have an int type.");
        }

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
        ElaborationResult initializerResult = null;
         if (forTree.initializationStatementTree() != null){
             initializerResult = forTree.initializationStatementTree().accept(this, context);
         }

        ElaborationResult conditionResult = forTree.conditionExpressionTree().accept(this, context);

        // TODO: Check this is no declaration
        ElaborationResult stepResult = null;

        if (forTree.postIterationStatementTree() != null) {
            stepResult = forTree.postIterationStatementTree().accept(this, context);

            // TODO: Maybe add separate analysis?
            if (stepResult.statement() instanceof TypedDeclaration) {
                throw new SemanticException("Step statement of for loop may not be a declaration.");
            }
        }

        ElaborationResult bodyElaborationResult = forTree.bodyStatementTree().accept(this, context);

        TypedLoop typedLoop = new TypedLoop(
                new TypedBlock(
                        List.of(
                                generateLoopBreakIf(conditionResult.expression()),
                                bodyElaborationResult.block()), forTree.span()),
                stepResult != null ? Optional.of(stepResult.statement()) : Optional.empty(),
                forTree.span());

        TypedBlock typedBlock = new TypedBlock(
                initializerResult != null
                ? List.of(
                        initializerResult.statement(),
                        typedLoop)
                : List.of(typedLoop),
                forTree.span());

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
        ElaborationResult conditionResult = whileTree.conditionExpressionTree().accept(this, context);
        ElaborationResult bodyResult = whileTree.statementTree().accept(this, context);

        if (conditionResult.expression().type() != HirType.BOOLEAN) {
            throw new SemanticException("While statement must have an boolean type.");
        }

        TypedLoop typedLoop = new TypedLoop(
                new TypedBlock(
                        List.of(
                                generateLoopBreakIf(conditionResult.expression()),
                                bodyResult.block()
                        ),
                        whileTree.span()),
                Optional.empty(),
                whileTree.span());


        return ElaborationResult.statement(typedLoop);
    }

    private static TypedIf generateLoopBreakIf(TypedExpression conditionExpression) {
        return new TypedIf(
                new TypedUnaryOperation(
                        UnaryOperator.NEGATION,
                        conditionExpression,
                        conditionExpression.span()),
                new TypedBlock(
                        List.of(
                                new TypedBreak(
                                        conditionExpression.span())),
                        conditionExpression.span()),
                Optional.empty(),
                conditionExpression.span());
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
