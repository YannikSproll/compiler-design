package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;
import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SsaConstruction implements TypedResultVisitor<SsaConstructionContext, SSAConstructionResult> {

    public IrFile generateIr(TypedFile typedFile) {
        SsaConstructionContext context = new SsaConstructionContext();
        SSAConstructionResult result = typedFile.accept(this, context);
        return result.asFile();
    }

    @Override
    public SSAConstructionResult visit(TypedAssignment assignment, SsaConstructionContext context) {
        SSAConstructionResult result = assignment.initializer().accept(this, context);

        IrMoveInstruction moveInstruction = new IrMoveInstruction(
                context.generateNewSSAValue(),
                result.asSSAValue());

        context.currentBlock().addInstruction(moveInstruction);
        context.introduceNewSSAValue(
                assignment.lValue().asVariable().symbol(),
                moveInstruction.target());

        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedBinaryOperation operation, SsaConstructionContext context) {
        SSAConstructionResult lExpResult = operation.lhsExpression().accept(this, context);
        SSAConstructionResult rExpResult = operation.rhsExpression().accept(this, context);

        SSAValue targetValue = context.generateNewSSAValue();
        IrValueProducingInstruction instruction = switch (operation.operator()) {
            case ADD -> new IrAddInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case SUBTRACT -> new IrSubInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case MULTIPLY -> new IrMulInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case DIVIDE -> new IrDivInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case MODULO -> new IrModInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case LEFT_SHIFT -> new IrLeftShiftInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case RIGHT_SHIFT -> new IrRightShiftInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case EQUAL_TO -> new IrEqualsInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case UNEQUAL_TO -> new IrUnequalsInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case GREATER_THAN -> new IrGreaterThanInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case LESS_THAN -> new IrLessThanInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case GREATER_THAN_OR_EQUAL_TO -> new IrGreaterThanOrEqualInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case LESS_THAN_OR_EQUAL_TO -> new IrLessThanOrEqualInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case BITWISE_OR -> new IrBitwiseOrInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case BITWISE_AND -> new IrBitwiseAndInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
            case BITWISE_XOR -> new IrBitwiseXorInstruction(targetValue, lExpResult.asSSAValue(), rExpResult.asSSAValue());
        };

        context.currentBlock().addInstruction(instruction);

        return SSAConstructionResult.ssaValue(instruction.target());
    }

    @Override
    public SSAConstructionResult visit(TypedBlock block, SsaConstructionContext context) {
        for (TypedStatement statement : block.statements()) {
            statement.accept(this, context);
        }
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedBoolLiteral literal, SsaConstructionContext context) {
        IrBoolConstantInstruction boolConstantInstruction = new IrBoolConstantInstruction(
                context.generateNewSSAValue(),
                literal.value());
        context.currentBlock().addInstruction(boolConstantInstruction);
        return SSAConstructionResult.ssaValue(boolConstantInstruction.target());
    }

    @Override
    public SSAConstructionResult visit(TypedBreak breakStatement, SsaConstructionContext context) {
        generateJumpInstruction(context.currentBlock(), context.getLoopContext().exitLoopBlock());
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedConditionalExpression conditionalExpression, SsaConstructionContext context) {
        SSAConstructionResult conditionResult = conditionalExpression.conditionExpression().accept(this, context);

        IrBlock thenBlock = context.createBlock("if_then");
        IrBlock fBlock = context.createBlock("if_merge");
        IrBlock elseBlock = context.createBlock("if_else");

        generateBranchInstruction(conditionResult.asSSAValue(), context.currentBlock(), thenBlock, elseBlock);


        context.newCurrentBlock(elseBlock);
        SSAConstructionResult elseResult = conditionalExpression.elseExpression().accept(this, context);
        generateJumpInstruction(context.currentBlock(), fBlock);

        context.newCurrentBlock(thenBlock);
        SSAConstructionResult thenResult = conditionalExpression.thenExpression().accept(this, context);
        generateJumpInstruction(context.currentBlock(), fBlock);

        context.newCurrentBlock(fBlock);
        IrPhi phi = new IrPhi(
                context.generateNewSSAValue(),
                List.of(
                        new IrPhi.IrPhiItem(elseResult.asSSAValue(), elseBlock),
                        new IrPhi.IrPhiItem(thenResult.asSSAValue(), thenBlock)
                ));
        context.currentBlock().addInstruction(phi);

        return SSAConstructionResult.ssaValue(phi.target());
    }

    @Override
    public SSAConstructionResult visit(TypedContinue continueStatement, SsaConstructionContext context) {
        generateJumpInstruction(context.currentBlock(), context.getLoopContext().reevaluateConditionBlock());
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedDeclaration declaration, SsaConstructionContext context) {
        if (declaration.initializer().isPresent()) {
            SSAConstructionResult result = declaration.initializer().get().accept(this, context);

            IrMoveInstruction moveInstruction = new IrMoveInstruction(
                    context.generateNewSSAValue(),
                    result.asSSAValue());

            context.introduceNewSSAValue(declaration.symbol(), moveInstruction.target());
            context.currentBlock().addInstruction(moveInstruction);
        }

        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedFile file, SsaConstructionContext context) {
        List<IrFunction> functions = new ArrayList<>();

        for (TypedFunction function : file.functions()) {
            SSAConstructionResult result = function.accept(this, context);
            functions.add(result.asFunction());
        }

        IrFile irFile = new IrFile(functions);
        return SSAConstructionResult.file(irFile);
    }

    @Override
    public SSAConstructionResult visit(TypedFunction function, SsaConstructionContext context) {
        IrBlock startBlock = context.beginFunction(function.symbol().name());
        function.body().accept(this, context);

        IrFunction irFunction = new IrFunction(startBlock, context.blocks(), function.isMainFunction());

        return SSAConstructionResult.function(irFunction);
    }

    @Override
    public SSAConstructionResult visit(TypedIf ifStatement, SsaConstructionContext context) {
        SSAConstructionResult conditionResult = ifStatement.conditionExpression().accept(this, context);

        IrBlock conditionBlock = context.currentBlock();
        IrBlock thenBlock = context.createBlock("if_then");
        IrBlock fBlock = context.createBlock("if_merge");

        Map<Symbol, SSAValue> elseValues;
        Map<Symbol, SSAValue> thenValues;

        if (ifStatement.elseStatement().isPresent()) {
            IrBlock elseBlock = context.createBlock("if_else");

            generateBranchInstruction(conditionResult.asSSAValue(), context.currentBlock(), thenBlock, elseBlock);

            context.newCurrentBlock(elseBlock);
            ifStatement.elseStatement().get().accept(this, context);
            generateJumpInstruction(context.currentBlock(), fBlock);

            elseValues = context.getLatestSSAValues(context.currentBlock());

            context.newCurrentBlock(thenBlock);
            ifStatement.thenStatement().accept(this, context);
            generateJumpInstruction(context.currentBlock(), fBlock);

            thenValues = context.getLatestSSAValues(context.currentBlock());

            List<IrPhi> phis = createPhis(elseBlock, elseValues, thenBlock, thenValues, context);
            context.newCurrentBlock(fBlock);
            for (IrPhi phi : phis) {
                context.currentBlock().addInstruction(phi);
            }
        } else {
            generateBranchInstruction(conditionResult.asSSAValue(), context.currentBlock(), thenBlock, fBlock);

            elseValues = context.getLatestSSAValues(context.currentBlock());

            context.newCurrentBlock(thenBlock);
            ifStatement.thenStatement().accept(this, context);
            generateJumpInstruction(context.currentBlock(), fBlock);

            thenValues = context.getLatestSSAValues(context.currentBlock());

            List<IrPhi> phis = createPhis(conditionBlock, elseValues, thenBlock, thenValues, context);
            context.newCurrentBlock(fBlock);
            for (IrPhi phi : phis) {
                context.currentBlock().addInstruction(phi);
            }
        }

        return SSAConstructionResult.empty();
    }

    private static void generateJumpInstruction(IrBlock from, IrBlock to) {
        IrJumpInstruction jumpInstruction = new IrJumpInstruction(to);
        from.addInstruction(jumpInstruction);
        from.addSuccessorBlock(to);
    }

    private static void generateBranchInstruction(SSAValue conditionValue, IrBlock from, IrBlock trueBranch, IrBlock falseBranch) {
        IrBranchInstruction branchInstruction = new IrBranchInstruction(conditionValue, trueBranch, falseBranch);
        from.addInstruction(branchInstruction);
        from.addSuccessorBlock(trueBranch);
        from.addSuccessorBlock(falseBranch);
    }

    private static List<IrPhi> createPhis(
            IrBlock firstBlock, Map<Symbol, SSAValue> firstBlockReassignments,
            IrBlock secondBlock, Map<Symbol, SSAValue> secondBlockReassignments,
            SsaConstructionContext context) {
        ArrayList<IrPhi> phis = new ArrayList<>();

        for (Map.Entry<Symbol, SSAValue> entry : firstBlockReassignments.entrySet()) {
            Symbol currentSymbol = entry.getKey();
            if (secondBlockReassignments.containsKey(currentSymbol)) {
                SSAValue phiTargetValue = context.generateNewSSAValue();
                context.introduceNewSSAValue(currentSymbol, phiTargetValue);

                IrPhi phi = new IrPhi(
                        phiTargetValue,
                        List.of(
                                new IrPhi.IrPhiItem(firstBlockReassignments.get(currentSymbol), firstBlock),
                                new IrPhi.IrPhiItem(secondBlockReassignments.get(currentSymbol), secondBlock)
                        ));
                phis.add(phi);
            }
        }

        return phis;
    }

    @Override
    public SSAConstructionResult visit(TypedIntLiteral literal, SsaConstructionContext context) {
        IrIntConstantInstruction constantInstruction = new IrIntConstantInstruction(
                context.generateNewSSAValue(),
                literal.value());
        context.currentBlock().addInstruction(constantInstruction);

        return SSAConstructionResult.ssaValue(constantInstruction.target());
    }

    @Override
    public SSAConstructionResult visit(TypedLoop loop, SsaConstructionContext context) {
        IrBlock bodyBlock = context.createBlock("loop_body");
        IrBlock postIterationStatementBlock = context.createBlock("loop_post_iteration");
        IrBlock conditionEvaluationBlock = context.createBlock("loop_condition");
        IrBlock loopExitBlock = context.createBlock("loop_exit");

        LoopContext loopContext = new LoopContext(
                loop.postIterationStatement().isPresent() ? postIterationStatementBlock : conditionEvaluationBlock,
                loopExitBlock);
        context.enterLoop(loopContext);

        // Generate skip loop if condition is false initially
        SSAConstructionResult headerConditionResult = loop.conditionExpression().accept(this, context);

        generateBranchInstruction(headerConditionResult.asSSAValue(), context.currentBlock(), bodyBlock, loopExitBlock);

        IrBlock preLoopBlock = context.currentBlock();
        Map<Symbol, SSAValue> preLoopValues = context.getLatestSSAValues(preLoopBlock);

        // Generate body block
        context.newCurrentBlock(bodyBlock);
        loop.body().accept(this, context);

        List<IrPhi> phis;

        if (loop.postIterationStatement().isPresent()) {
            generateJumpInstruction(context.currentBlock(), postIterationStatementBlock);

            Map<Symbol, SSAValue> bodyValues = context.getLatestSSAValues(bodyBlock);

            //Add post iteration statement block if post iteration statement is present
            context.newCurrentBlock(postIterationStatementBlock);
            loop.postIterationStatement().get().accept(this, context);
            generateJumpInstruction(context.currentBlock(), conditionEvaluationBlock);

            Map<Symbol, SSAValue> postIterationValues = context.getLatestSSAValues(postIterationStatementBlock);

            Map<Symbol, SSAValue> updatedSSAValues = updateSSAValuesIfPresent(
                    bodyValues,
                    postIterationValues);

            phis = createPhis(conditionEvaluationBlock, updatedSSAValues, preLoopBlock, preLoopValues, context);

        } else {
            generateJumpInstruction(context.currentBlock(), conditionEvaluationBlock);

            Map<Symbol, SSAValue> bodyValues = context.getLatestSSAValues(bodyBlock);

            phis = createPhis(
                    bodyBlock, bodyValues,
                    preLoopBlock, preLoopValues,
                    context);
        }

        // Generate the condition evaluation block
        context.newCurrentBlock(conditionEvaluationBlock);
        SSAConstructionResult bottomConstructionResult = loop.conditionExpression().accept(this, context);
        generateBranchInstruction(bottomConstructionResult.asSSAValue(), context.currentBlock(), bodyBlock, loopExitBlock);

        for (IrPhi phi : phis.reversed()) {
            // Insert phis to the begin of the body block
            bodyBlock.insertInstruction(0, phi);
        }

        context.exitLoop(loopContext);

        // Prepare the block for further instructions
        context.newCurrentBlock(loopExitBlock);

        return SSAConstructionResult.empty();
    }

    private Map<Symbol, SSAValue> updateSSAValuesIfPresent(
            Map<Symbol, SSAValue> baseValues,
            Map<Symbol, SSAValue> potentialNewerValues) {
        Map<Symbol, SSAValue> updatedValues = new HashMap<>();

        // Update reassignments in baseValues
        for (Map.Entry<Symbol, SSAValue> entry : baseValues.entrySet()) {
            updatedValues.put(entry.getKey(), potentialNewerValues.getOrDefault(entry.getKey(), entry.getValue()));
        }

        // Add remaining values of potential updates
        for (Map.Entry<Symbol, SSAValue> entry : potentialNewerValues.entrySet()) {
            if (!updatedValues.containsKey(entry.getKey())) {
                updatedValues.put(entry.getKey(), entry.getValue());
            }
        }

        return updatedValues;
    }

    @Override
    public SSAConstructionResult visit(TypedReturn returnStatement, SsaConstructionContext context) {
        SSAConstructionResult result = returnStatement.returnExpression().accept(this, context);
        IrReturnInstruction returnInstruction = new IrReturnInstruction(result.asSSAValue());

        context.currentBlock().addInstruction(returnInstruction);

        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedUnaryOperation operation, SsaConstructionContext context) {
        SSAConstructionResult result = operation.expression().accept(this, context);

        SSAValue targetValue = context.generateNewSSAValue();
        IrValueProducingInstruction instruction = switch(operation.operator()) {
            case BITWISE_NOT -> new IrBitwiseNotInstruction(targetValue, result.asSSAValue());
            case NEGATION -> new IrNegateInstruction(targetValue, result.asSSAValue());
            case LOGICAL_NOT -> new IrLogicalNotInstruction(targetValue, result.asSSAValue());
        };

        context.currentBlock().addInstruction(instruction);

        return SSAConstructionResult.ssaValue(targetValue);
    }

    @Override
    public SSAConstructionResult visit(TypedVariable variable, SsaConstructionContext context) {
        // TODO: Solve declaration problem
        return SSAConstructionResult.ssaValue(context.getLatestSSAValue(variable.symbol()));
    }
}
