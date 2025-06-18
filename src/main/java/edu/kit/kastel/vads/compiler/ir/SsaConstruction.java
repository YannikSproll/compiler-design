package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;
import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.*;

import java.util.*;

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
                context.generateNewSSAValue(assignment.lValue().asVariable().symbol(), context.currentBlock()),
                result.asSSAValue());

        context.currentBlock().addInstruction(moveInstruction);

        return SSAConstructionResult.statement();
    }

    @Override
    public SSAConstructionResult visit(TypedBinaryOperation operation, SsaConstructionContext context) {
        SSAConstructionResult lExpResult = operation.lhsExpression().accept(this, context);
        SSAConstructionResult rExpResult = operation.rhsExpression().accept(this, context);

        SSAValue targetValue = context.generateNewSSAValue(operation.type(), context.currentBlock());
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
            SSAConstructionResult result = statement.accept(this, context);
            if (result.asTerminationType() != SSAConstructionResult.TerminationType.NONE) {
                return SSAConstructionResult.statement(result.asTerminationType());
            }
        }

        return SSAConstructionResult.statement();
    }

    @Override
    public SSAConstructionResult visit(TypedBoolLiteral literal, SsaConstructionContext context) {
        IrBoolConstantInstruction boolConstantInstruction = new IrBoolConstantInstruction(
                context.generateNewSSAValue(literal.type(), context.currentBlock()),
                literal.value());
        context.currentBlock().addInstruction(boolConstantInstruction);
        return SSAConstructionResult.ssaValue(boolConstantInstruction.target());
    }

    @Override
    public SSAConstructionResult visit(TypedBreak breakStatement, SsaConstructionContext context) {
        generateJumpInstruction(context.currentBlock(), context.getLoopContext().exitLoopBlock());
        context.getLoopContext().requireBreak();
        return SSAConstructionResult.statement(SSAConstructionResult.TerminationType.WEAK);
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
        IrBlock elseBranchLastBlock = context.currentBlock();
        generateJumpInstruction(elseBranchLastBlock, fBlock);

        context.newCurrentBlock(thenBlock);
        SSAConstructionResult thenResult = conditionalExpression.thenExpression().accept(this, context);
        IrBlock thenBranchLastBlock = context.currentBlock();
        generateJumpInstruction(thenBranchLastBlock, fBlock);

        context.newCurrentBlock(fBlock);
        IrPhi phi = new IrPhi(
                context.generateNewSSAValue(conditionalExpression.type(), context.currentBlock()),
                List.of(
                        new IrPhi.IrPhiItem(elseResult.asSSAValue(), elseBranchLastBlock),
                        new IrPhi.IrPhiItem(thenResult.asSSAValue(), thenBranchLastBlock)
                ));
        context.currentBlock().addInstruction(phi);

        return SSAConstructionResult.ssaValue(phi.target());
    }

    @Override
    public SSAConstructionResult visit(TypedContinue continueStatement, SsaConstructionContext context) {
        generateJumpInstruction(context.currentBlock(), context.getLoopContext().reevaluateConditionBlock());
        context.getLoopContext().requireContinue();
        return SSAConstructionResult.statement(SSAConstructionResult.TerminationType.WEAK);
    }

    @Override
    public SSAConstructionResult visit(TypedDeclaration declaration, SsaConstructionContext context) {
        if (declaration.initializer().isPresent()) {
            SSAConstructionResult result = declaration.initializer().get().accept(this, context);

            IrMoveInstruction moveInstruction = new IrMoveInstruction(
                    context.generateNewSSAValue(declaration.symbol(), context.currentBlock()),
                    result.asSSAValue());
            context.currentBlock().addInstruction(moveInstruction);
        }

        return SSAConstructionResult.statement();
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

        new IrPhiGenerator().addPhis(irFunction, context.getSSAVariables(), context.ssaValueGenerator());

        return SSAConstructionResult.function(irFunction);
    }

    @Override
    public SSAConstructionResult visit(TypedFunctionCall functionCall, SsaConstructionContext ssaConstructionContext) {
        return null;
    }

    @Override
    public SSAConstructionResult visit(TypedArgument argument, SsaConstructionContext ssaConstructionContext) {
        return null;
    }

    @Override
    public SSAConstructionResult visit(TypedIf ifStatement, SsaConstructionContext context) {
        SSAConstructionResult conditionResult = ifStatement.conditionExpression().accept(this, context);

        IrBlock thenBlock = context.createBlock("if_then");
        IrBlock fBlock = context.createBlock("if_merge");

        if (ifStatement.elseStatement().isPresent()) {
            IrBlock elseBlock = context.createBlock("if_else");

            generateBranchInstruction(conditionResult.asSSAValue(), context.currentBlock(), thenBlock, elseBlock);

            context.newCurrentBlock(elseBlock);
            SSAConstructionResult elseResult = ifStatement.elseStatement().get().accept(this, context);
            if (elseResult.asTerminationType() == SSAConstructionResult.TerminationType.NONE) {
                // If it terminates then a jump or return is already generated
                // So only generate jump if else does not terminate
                generateJumpInstruction(context.currentBlock(), fBlock);
            }

            context.newCurrentBlock(thenBlock);
            SSAConstructionResult thenResult = ifStatement.thenStatement().accept(this, context);
            if (thenResult.asTerminationType() == SSAConstructionResult.TerminationType.NONE) {
                // If it terminates then a jump or return is already generated
                // So only generate jump if else does not terminate
                generateJumpInstruction(context.currentBlock(), fBlock);
            }

            if (thenResult.asTerminationType() == SSAConstructionResult.TerminationType.STRONG
                && elseResult.asTerminationType() == SSAConstructionResult.TerminationType.STRONG) {
                // Both branches return
                // No need for further phi nodes.
                return SSAConstructionResult.statement(SSAConstructionResult.TerminationType.STRONG);
            }
            context.newCurrentBlock(fBlock);
            return SSAConstructionResult.statement(
                    elseResult.asTerminationType().merge(
                            thenResult.asTerminationType()));
        } else {
            generateBranchInstruction(conditionResult.asSSAValue(), context.currentBlock(), thenBlock, fBlock);

            context.newCurrentBlock(thenBlock);
            SSAConstructionResult thenResult = ifStatement.thenStatement().accept(this, context);

            if (thenResult.asTerminationType() == SSAConstructionResult.TerminationType.NONE) {
                generateJumpInstruction(context.currentBlock(), fBlock);
            }
            context.newCurrentBlock(fBlock);
            return SSAConstructionResult.statement(SSAConstructionResult.TerminationType.NONE);
        }
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


    @Override
    public SSAConstructionResult visit(TypedIntLiteral literal, SsaConstructionContext context) {
        IrIntConstantInstruction constantInstruction = new IrIntConstantInstruction(
                context.generateNewSSAValue(literal.type(), context.currentBlock()),
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

        // Generate skip loop if condition is false initially
        SSAConstructionResult headerConditionResult = loop.conditionExpression().accept(this, context);

        generateBranchInstruction(headerConditionResult.asSSAValue(), context.currentBlock(), bodyBlock, loopExitBlock);

        LoopContext loopContext = new LoopContext(
                loop.postIterationStatement().isPresent() ? postIterationStatementBlock : conditionEvaluationBlock,
                loopExitBlock);
        context.enterLoop(loopContext);

        // Generate body block
        context.newCurrentBlock(bodyBlock);
        SSAConstructionResult bodyResult = loop.body().accept(this, context);

        context.exitLoop(loopContext);

        if (bodyResult.asTerminationType() == SSAConstructionResult.TerminationType.STRONG
            && !loopContext.hasBreak()
            && !loopContext.hasContinue()) {
            // Body ends in a return statements on all paths
            // => Post condition and branch are dead code

            context.newCurrentBlock(loopExitBlock);
            // Return none because loop might not be executed
            return SSAConstructionResult.statement(SSAConstructionResult.TerminationType.NONE);
        }

        if (loop.postIterationStatement().isPresent()) {
            if (bodyResult.asTerminationType() == SSAConstructionResult.TerminationType.NONE) {
                // Body does not end in break or continue,
                // which would have already generated a jump to the post iteration statement
                generateJumpInstruction(context.currentBlock(), postIterationStatementBlock);
            }
            else {
                // Add this to avoid error during variable symbol resolving in condition expression
                // ssa generation
                context.currentBlock().addSuccessorBlock(postIterationStatementBlock);
            }

            //Add post iteration statement block if post iteration statement is present
            context.newCurrentBlock(postIterationStatementBlock);
            // We assume post iteration statement is an assignment => can not terminate
            loop.postIterationStatement().get().accept(this, context);
            generateJumpInstruction(context.currentBlock(), conditionEvaluationBlock);

        } else {
            if (bodyResult.asTerminationType() == SSAConstructionResult.TerminationType.NONE) {
                // Body does not end in break or continue,
                // which would have already generated a jump to the post iteration statement
                generateJumpInstruction(context.currentBlock(), conditionEvaluationBlock);
            } else {
                // Add this to avoid error during variable symbol resolving in condition expression
                // ssa generation
                context.currentBlock().addSuccessorBlock(conditionEvaluationBlock);
            }
        }

        // Generate the condition evaluation block
        context.newCurrentBlock(conditionEvaluationBlock);
        SSAConstructionResult bottomConstructionResult = loop.conditionExpression().accept(this, context);
        generateBranchInstruction(bottomConstructionResult.asSSAValue(), context.currentBlock(), bodyBlock, loopExitBlock);

        // Prepare the block for further instructions
        context.newCurrentBlock(loopExitBlock);

        return SSAConstructionResult.statement();
    }


    @Override
    public SSAConstructionResult visit(TypedReturn returnStatement, SsaConstructionContext context) {
        SSAConstructionResult result = returnStatement.returnExpression().accept(this, context);
        IrReturnInstruction returnInstruction = new IrReturnInstruction(result.asSSAValue());

        context.currentBlock().addInstruction(returnInstruction);

        return SSAConstructionResult.statement(SSAConstructionResult.TerminationType.STRONG);
    }

    @Override
    public SSAConstructionResult visit(TypedUnaryOperation operation, SsaConstructionContext context) {
        SSAConstructionResult result = operation.expression().accept(this, context);

        SSAValue targetValue = context.generateNewSSAValue(operation.type(), context.currentBlock());
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
        return SSAConstructionResult.ssaValue(context.getLatestSSAValue(variable.symbol(), context.currentBlock()));
    }
}
