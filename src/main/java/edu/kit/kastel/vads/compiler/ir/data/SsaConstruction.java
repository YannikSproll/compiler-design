package edu.kit.kastel.vads.compiler.ir.data;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;

import java.util.ArrayList;
import java.util.List;

public class SsaConstruction implements TypedResultVisitor<SsaConstructionContext, SSAConstructionResult> {

    public IrFile generateIr(TypedFile typedFile) {
        SsaConstructionContext context = new SsaConstructionContext();
        SSAConstructionResult result = typedFile.accept(this, context);
        return result.asFile();
    }

    @Override
    public SSAConstructionResult visit(TypedAssignment assignment, SsaConstructionContext ssaConstructionContext) {
        SSAConstructionResult result = assignment.initializer().accept(this, ssaConstructionContext);

        IrMoveInstruction moveInstruction = new IrMoveInstruction(
                ssaConstructionContext.generateNewSSAValue(),
                result.asSSAValue());

        ssaConstructionContext.currentBlock().addInstruction(moveInstruction);
        ssaConstructionContext.introduceNewSSAValue(
                assignment.lValue().asVariable().symbol(),
                moveInstruction.target());

        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedBinaryOperation operation, SsaConstructionContext ssaConstructionContext) {
        SSAConstructionResult lExpResult = operation.lhsExpression().accept(this, ssaConstructionContext);
        SSAConstructionResult rExpResult = operation.rhsExpression().accept(this, ssaConstructionContext);

        SSAValue targetValue = ssaConstructionContext.generateNewSSAValue();
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

        ssaConstructionContext.currentBlock().addInstruction(instruction);

        return SSAConstructionResult.ssaValue(instruction.target());
    }

    @Override
    public SSAConstructionResult visit(TypedBlock block, SsaConstructionContext ssaConstructionContext) {
        for (TypedStatement statement : block.statements()) {
            statement.accept(this, ssaConstructionContext);
        }
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedBoolLiteral literal, SsaConstructionContext ssaConstructionContext) {
        IrBoolConstantInstruction boolConstantInstruction = new IrBoolConstantInstruction(
                ssaConstructionContext.generateNewSSAValue(),
                literal.value());
        ssaConstructionContext.currentBlock().addInstruction(boolConstantInstruction);
        return SSAConstructionResult.ssaValue(boolConstantInstruction.target());
    }

    @Override
    public SSAConstructionResult visit(TypedBreak breakStatement, SsaConstructionContext ssaConstructionContext) {
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedConditionalExpression conditionalExpression, SsaConstructionContext ssaConstructionContext) {
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedContinue continueStatement, SsaConstructionContext ssaConstructionContext) {
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedDeclaration declaration, SsaConstructionContext ssaConstructionContext) {
        if (declaration.initializer().isPresent()) {
            SSAConstructionResult result = declaration.initializer().get().accept(this, ssaConstructionContext);

            // TODO:
            IrMoveInstruction moveInstruction = new IrMoveInstruction(
                    ssaConstructionContext.generateNewSSAValue(),
                    result.asSSAValue());

            ssaConstructionContext.introduceNewSSAValue(declaration.symbol(), moveInstruction.target());
            ssaConstructionContext.currentBlock().addInstruction(moveInstruction);
        }

        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedFile file, SsaConstructionContext ssaConstructionContext) {
        List<IrFunction> functions = new ArrayList<>();

        for (TypedFunction function : file.functions()) {
            SSAConstructionResult result = function.accept(this, ssaConstructionContext);
            functions.add(result.asFunction());
        }

        IrFile irFile = new IrFile(functions);
        return SSAConstructionResult.file(irFile);
    }

    @Override
    public SSAConstructionResult visit(TypedFunction function, SsaConstructionContext ssaConstructionContext) {
        IrBlock startBlock = ssaConstructionContext.newCurrentBlock();
        function.body().accept(this, ssaConstructionContext);

        IrFunction irFunction = new IrFunction(startBlock, ssaConstructionContext.blocks());

        return SSAConstructionResult.function(irFunction);
    }

    @Override
    public SSAConstructionResult visit(TypedIf ifStatement, SsaConstructionContext ssaConstructionContext) {
        SSAConstructionResult conditionResult = ifStatement.conditionExpression().accept(this, ssaConstructionContext);
        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedIntLiteral literal, SsaConstructionContext ssaConstructionContext) {
        IrIntConstantInstruction constantInstruction = new IrIntConstantInstruction(
                ssaConstructionContext.generateNewSSAValue(),
                literal.value());
        ssaConstructionContext.currentBlock().addInstruction(constantInstruction);

        return SSAConstructionResult.ssaValue(constantInstruction.target());
    }

    @Override
    public SSAConstructionResult visit(TypedLoop loop, SsaConstructionContext ssaConstructionContext) {

        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedReturn returnStatement, SsaConstructionContext ssaConstructionContext) {
        SSAConstructionResult result = returnStatement.returnExpression().accept(this, ssaConstructionContext);
        IrReturnInstruction returnInstruction = new IrReturnInstruction(result.asSSAValue());

        ssaConstructionContext.currentBlock().addInstruction(returnInstruction);

        return SSAConstructionResult.empty();
    }

    @Override
    public SSAConstructionResult visit(TypedUnaryOperation operation, SsaConstructionContext ssaConstructionContext) {
        SSAConstructionResult result = operation.expression().accept(this, ssaConstructionContext);

        SSAValue targetValue = ssaConstructionContext.generateNewSSAValue();
        IrValueProducingInstruction instruction = switch(operation.operator()) {
            case BITWISE_NOT -> new IrBitwiseNotInstruction(targetValue, result.asSSAValue());
            case NEGATION -> new IrNegateInstruction(targetValue, result.asSSAValue());
            case LOGICAL_NOT -> new IrLogicalNotInstruction(targetValue, result.asSSAValue());
        };

        ssaConstructionContext.currentBlock().addInstruction(instruction);

        return SSAConstructionResult.ssaValue(targetValue);
    }

    @Override
    public SSAConstructionResult visit(TypedVariable variable, SsaConstructionContext ssaConstructionContext) {
        return SSAConstructionResult.ssaValue(ssaConstructionContext.getLatestSSAValue(variable.symbol()));
    }
}
