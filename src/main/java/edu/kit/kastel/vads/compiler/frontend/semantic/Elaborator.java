package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.Span;
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

        ElaborationResult elaborationResult = program.accept(this, context);

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

            typeChecker.expectType(typeResult.type(), initializerResult.expression());

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
        ElaborationResult nameResult = functionTree.name().accept(this, context);
        TypedFunction function = context.getFunction(nameResult.name());

        context.symbolTable().enterScope(function.declaringScope());

        ElaborationResult bodyResult = functionTree.body().accept(this, context);
        function.setElaboratedBody(bodyResult.block());

        context.symbolTable().exitScope();

        return ElaborationResult.node(function);
    }

    @Override
    public ElaborationResult visit(ParameterTree parameterTree, ElaborationContext context) {
        ElaborationResult nameResult = parameterTree.name().accept(this, context);
        ElaborationResult typeResult = parameterTree.type().accept(this, context);

        Symbol parameterSymbol = new Symbol(nameResult.name(), typeResult.type(), parameterTree.span());
        TypedParameter typedParameter = new TypedParameter(parameterSymbol, typeResult.type(), parameterTree.span());
        return ElaborationResult.parameter(typedParameter);
    }

    @Override
    public ElaborationResult visit(CallTree callTree, ElaborationContext context) {
        ElaborationResult nameResult = callTree.functionName().accept(this, context);
        TypedFunction calledFunction = context.getFunction(nameResult.name());

        List<TypedArgument> arguments = new ArrayList<>();
        for (ArgumentTree argumentTree : callTree.arguments()) {
            ElaborationResult argumentResult = argumentTree.accept(this, context);
            arguments.add(argumentResult.argument());
        }

        if (calledFunction.parameters().size() != arguments.size()) {
            throw new SemanticException("Number of arguments does not match the number of parameters.");
        }

        for (int i = 0; i < arguments.size(); i++) {
            TypedParameter parameter = calledFunction.parameters().get(i);
            TypedArgument argument = arguments.get(i);
            typeChecker.expectEqualTypes(parameter, argument);
        }

        // Check types of arguments
        TypedFunctionCall functionCall = new TypedFunctionCall(
                calledFunction,
                arguments,
                calledFunction.symbol().type(),
                callTree.span());
        return ElaborationResult.functionCall(functionCall);
    }

    @Override
    public ElaborationResult visit(ArgumentTree argumentTree, ElaborationContext context) {
        ElaborationResult expressionResult = argumentTree.expression().accept(this, context);

        TypedArgument typedArgument = new TypedArgument(expressionResult.expression(), argumentTree.span());
        return ElaborationResult.argument(typedArgument);
    }

    @Override
    public ElaborationResult visit(IdentExpressionTree identExpressionTree, ElaborationContext context) {
        ElaborationResult nameResult = identExpressionTree.name().accept(this, context);

        if (!context.symbolTable().isVariableDeclared(nameResult.name())) {
            throw new SemanticException("The variable " + nameResult.name() + " is not declared.");
        }

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

        if (!context.symbolTable().isVariableDeclared(nameResult.name())) {
            throw new SemanticException("The variable " + nameResult.name() + " is not declared.");
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

        TypedExpression typedUnaryOperation;
         switch (negateTree.operatorType()) {
             case Operator.OperatorType.MINUS: {
                 typeChecker.expectType(HirType.INT, typedExpression);

                 if (typedExpression instanceof TypedIntLiteral(int value, HirType type, Span span)) {
                     typedUnaryOperation = new TypedIntLiteral(
                             -value,
                             type,
                             span);
                 } else {
                     typedUnaryOperation = new TypedUnaryOperation(
                             mapUnaryOperator(negateTree.operatorType()),
                             typedExpression,
                             negateTree.span());
                 }
                 break;
             }
             case Operator.OperatorType.BITWISE_NOT: {
                 typeChecker.expectType(HirType.INT, typedExpression);

                  typedUnaryOperation = new TypedUnaryOperation(
                        mapUnaryOperator(negateTree.operatorType()),
                        typedExpression,
                        negateTree.span());
                  break;
            }
            case Operator.OperatorType.LOGICAL_NOT: {
                typeChecker.expectType(HirType.BOOLEAN, typedExpression);

                if (typedExpression instanceof TypedBoolLiteral(boolean value, HirType type, Span span)) {
                    typedUnaryOperation = new TypedBoolLiteral(
                            !value,
                            type,
                            span);
                } else {
                    typedUnaryOperation = new TypedUnaryOperation(
                            UnaryOperator.LOGICAL_NOT,
                            typedExpression,
                            negateTree.span());
                }
                break;
            }
             default: throw new SemanticException("Unsupported expression type: " + typedExpression.type());
        }

        return ElaborationResult.expression(typedUnaryOperation);
    }

    private TypedFunction createFunctionStub(FunctionTree functionTree, ElaborationContext context) {
        ElaborationResult returnTypeResult = functionTree.returnType().accept(this, context);
        ElaborationResult nameResult = functionTree.name().accept(this, context);

        Scope functionScope = context.symbolTable().enterScope(ScopeType.FUNCTION);

        List<TypedParameter> parameters = new ArrayList<>();
        for (ParameterTree parameter : functionTree.parameters()) {
            ElaborationResult parameterResult = parameter.accept(this, context);
            TypedParameter typedParameter = parameterResult.parameter();

            context.symbolTable().putType(typedParameter.symbol().name(), typedParameter.symbol());

            parameters.add(typedParameter);
        }

        context.symbolTable().exitScope();

        Symbol functionSymbol = new Symbol(
                nameResult.name(),
                returnTypeResult.type(),
                functionTree.span());
        TypedFunction typedFunction = new TypedFunction(
                functionSymbol,
                functionScope,
                parameters);
        return typedFunction;
    }

    @Override
    public ElaborationResult visit(ProgramTree programTree, ElaborationContext context) {
        Scope currentScope = context.symbolTable().enterScope(ScopeType.FILE);

        // Create function stubs
        for (FunctionTree function : programTree.topLevelTrees()) {
            TypedFunction functionStub = createFunctionStub(function, context);
            context.defineFunction(functionStub.symbol().name(), functionStub);
        }

        // Elaborate functions
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

        // Add additional scope to prevent initializer from being visible after loop
        Scope scope = context.symbolTable().enterScope(ScopeType.BLOCK);

        ElaborationResult initializerResult = null;
         if (forTree.initializationStatementTree() != null){
             initializerResult = forTree.initializationStatementTree().accept(this, context);
         }

        ElaborationResult conditionResult = forTree.conditionExpressionTree().accept(this, context);

        context.incrementNestedLoopDepth();
        ElaborationResult bodyElaborationResult = forTree.bodyStatementTree().accept(this, context);
        context.decrementNestedLoopDepth();

        ElaborationResult stepResult = null;
        if (forTree.postIterationStatementTree() != null) {
            stepResult = forTree.postIterationStatementTree().accept(this, context);

            // Make sure post iteration statement is not a declaration
            if (stepResult.statement() instanceof TypedDeclaration) {
                throw new SemanticException("Step statement of for loop may not be a declaration.");
            }
        }

        TypedLoop typedLoop = new TypedLoop(
                conditionResult.expression(),
                bodyElaborationResult.statementOrBlock(),
                stepResult != null ? Optional.of(stepResult.statement()) : Optional.empty(),
                forTree.span());

        TypedBlock typedBlock = new TypedBlock(
                initializerResult != null
                ? List.of(
                        initializerResult.statement(),
                        typedLoop)
                : List.of(typedLoop),
                Optional.of(scope),
                forTree.span());

        context.symbolTable().exitScope();

        return ElaborationResult.block(typedBlock);
    }

    @Override
    public ElaborationResult visit(IfTree ifTree, ElaborationContext context) {
        ElaborationResult conditionResult = ifTree.conditionExpressionTree().accept(this, context);

        Scope thenBranchScope = context.symbolTable().enterScope(ScopeType.BLOCK);
        TypedStatement thenBranchStatement = ifTree.statementTree().accept(this, context).statementOrBlock();
        context.symbolTable().exitScope();
        TypedBlock thenBlock = new TypedBlock(List.of(thenBranchStatement), Optional.of(thenBranchScope), thenBranchStatement.span());

        if (conditionResult.expression().type() != HirType.BOOLEAN) {
            throw new SemanticException("If statement must have an boolean type.");
        }

        Optional<TypedStatement> elseResult = Optional.empty();
        if (ifTree.elseTree() != null) {
            Scope elseBranchScope = context.symbolTable().enterScope(ScopeType.BLOCK);
            TypedStatement elseStatement = ifTree.elseTree().accept(this, context).statementOrBlock();
            context.symbolTable().exitScope();
            TypedBlock elseBlock = new TypedBlock(List.of(elseStatement), Optional.of(elseBranchScope), elseStatement.span());
            elseResult = Optional.of(elseBlock);
        }

        TypedIf typedIf = new TypedIf(
                conditionResult.expression(),
                thenBlock,
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
                bodyResult.statementOrBlock(),
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
