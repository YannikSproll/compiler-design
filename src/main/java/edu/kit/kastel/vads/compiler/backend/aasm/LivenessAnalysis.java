package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.data.*;
import edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions.*;

import java.util.*;

public final class LivenessAnalysis {

    public LivenessAnalysisResult run(IrFunction function) {
        Map<IrBlock, BlockAnalysisResults> blockAnalysisResults = analyzeBlockLevelLiveness(function);

        Map<IrInstruction, Set<SSAValue>> instructionLevelResults = analyzeInstructionLevelLiveness(function, blockAnalysisResults);
        return new LivenessAnalysisResult(instructionLevelResults, blockAnalysisResults);
    }

    private Map<IrBlock, BlockAnalysisResults> analyzeBlockLevelLiveness(IrFunction function) {
        Map<IrBlock, BlockAnalysisResults> blockAnalysisResults = new HashMap<>();
        for (IrBlock block : function.blocks()) {
            generateUseAndDefSets(block, blockAnalysisResults);
        }

        for (IrBlock block : function.blocks()) {
            addUsesCausedByPhis(block, blockAnalysisResults);
        }

        generateInAndOutSets(function.blocks(), blockAnalysisResults);

        return blockAnalysisResults;
    }

    private void generateInAndOutSets(Collection<IrBlock> blocks, Map<IrBlock, BlockAnalysisResults> blockAnalysisResults) {
        Deque<IrBlock> blocksToRecompute = new ArrayDeque<>(blocks);
        while (!blocksToRecompute.isEmpty()) {
            IrBlock block = blocksToRecompute.pop();

            boolean changed = generateInAndOutSet(block, blockAnalysisResults);
            if (changed) {
                blocksToRecompute.addAll(block.getPredecessorBlocks());
            }
        }
    }

    private boolean generateInAndOutSet(IrBlock block, Map<IrBlock, BlockAnalysisResults> blockAnalysisResults) {
        Set<SSAValue> newOut = new HashSet<>();

        boolean changed = false;

        for (IrBlock successor : block.getSuccessorBlocks()) {
            newOut.addAll(blockAnalysisResults.get(successor).in());
        }

        if (!setEquals(newOut, blockAnalysisResults.get(block).out())) {
            changed = true;
            blockAnalysisResults.get(block).out().clear();
            blockAnalysisResults.get(block).out().addAll(newOut);
        }

        HashSet<SSAValue> tempIn = new HashSet<>(newOut);
        tempIn.removeAll(blockAnalysisResults.get(block).definedInBlock());
        tempIn.addAll(blockAnalysisResults.get(block).usedInBlock());

        if (!setEquals(tempIn, blockAnalysisResults.get(block).in())) {
            changed = true;
            blockAnalysisResults.get(block).in().clear();
            blockAnalysisResults.get(block).in().addAll(tempIn);
        }

        return changed;
    }

    private boolean setEquals(Set<SSAValue> a, Set<SSAValue> b) {
        return a.size() == b.size() && a.containsAll(b) && b.containsAll(a);
    }

    private void generateUseAndDefSets(IrBlock block, Map<IrBlock, BlockAnalysisResults> results) {
        HashSet<SSAValue> definedInBlock = new HashSet<>();
        HashSet<SSAValue> usedInBlock = new HashSet<>();

        for (IrInstruction instruction : block.getInstructions()) {
            addUsedAndDefinedSSAValues(instruction, usedInBlock, definedInBlock);
        }

        results.computeIfAbsent(block, _ -> BlockAnalysisResults.empty())
                .definedInBlock()
                .addAll(definedInBlock);
        results.get(block)
                .usedInBlock()
                .addAll(usedInBlock);
    }

    private void addUsesCausedByPhis(IrBlock block, Map<IrBlock, BlockAnalysisResults> results) {
        for (IrInstruction instruction : block.getInstructions()) {
            if (instruction instanceof IrPhi phi) {
                for (IrPhi.IrPhiItem item : phi.sources()) {
                    BlockAnalysisResults preResult = results.computeIfAbsent(item.block(), _ -> BlockAnalysisResults.empty());
                    addIfNotPresent(preResult.usedInBlock(), preResult.definedInBlock(), item.value());
                }
            }
        }
    }

    record BlockAnalysisResults(
            Set<SSAValue> definedInBlock,
            Set<SSAValue> usedInBlock,
            Set<SSAValue> in,
            Set<SSAValue> out) {
        public static BlockAnalysisResults empty() {
            return new BlockAnalysisResults(new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>());
        }
    }

    private void addUsedAndDefinedSSAValues(IrInstruction instruction, Set<SSAValue> usedByInstruction, Set<SSAValue> definedByInstruction) {
        switch (instruction) {
            case IrJumpInstruction _:
                break;
            case IrReturnInstruction returnInstruction:
                addIfNotPresent(usedByInstruction, definedByInstruction, returnInstruction.src());
                break;
            case IrBranchInstruction branchInstruction:
                addIfNotPresent(usedByInstruction, definedByInstruction, branchInstruction.conditionValue());
                break;
            case IrBinaryOperationInstruction binInstruction:
                addIfNotPresent(usedByInstruction, definedByInstruction, binInstruction.leftSrc());
                addIfNotPresent(usedByInstruction, definedByInstruction, binInstruction.rightSrc());
                definedByInstruction.add(binInstruction.target());
                break;
            case IrUnaryOperationInstruction unaryInstruction:
                addIfNotPresent(usedByInstruction, definedByInstruction, unaryInstruction.src());
                definedByInstruction.add(unaryInstruction.target());
                break;
            case IrBoolConstantInstruction irBoolConstantInstruction:
                definedByInstruction.add(irBoolConstantInstruction.target());
                break;
            case IrIntConstantInstruction irIntConstantInstruction:
                definedByInstruction.add(irIntConstantInstruction.target());
                break;
            case IrMoveInstruction irMoveInstruction:
                addIfNotPresent(usedByInstruction, definedByInstruction, irMoveInstruction.source());
                definedByInstruction.add(irMoveInstruction.target());
                break;
            case IrPhi irPhi:
                definedByInstruction.add(irPhi.target());
                break;
        }
    }

    private static void addIfNotPresent(Set<SSAValue> values, Set<SSAValue> presentCheckValues, SSAValue value) {
        if (!presentCheckValues.contains(value)) {
            values.add(value);
        }
    }

    private Map<IrInstruction, Set<SSAValue>> analyzeInstructionLevelLiveness(IrFunction function, Map<IrBlock, BlockAnalysisResults> blockLevelResults) {
        Map<IrInstruction, Set<SSAValue>> instructionLevelResults = new HashMap<>();
        for (IrBlock block : function.blocks()) {
            analyzeInstructionLiveness(block, blockLevelResults, instructionLevelResults);
        }

        return instructionLevelResults;
    }

    private void analyzeInstructionLiveness(
            IrBlock block,
            Map<IrBlock, BlockAnalysisResults> blockResults,
            Map<IrInstruction, Set<SSAValue>> instructionLiveness) {
        Set<SSAValue> liveOut = blockResults.get(block).out();

        Set<SSAValue> usedByInstruction = new HashSet<>();
        Set<SSAValue> definedByInstruction = new HashSet<>();

        for (IrInstruction instruction : block.getInstructions().reversed()) {
            addUsedAndDefinedSSAValues(instruction, usedByInstruction, definedByInstruction);

            HashSet<SSAValue> liveIn = new HashSet<>(liveOut);
            liveIn.removeAll(definedByInstruction);
            liveIn.addAll(usedByInstruction);

            instructionLiveness.put(instruction, liveIn);

            liveOut = liveIn;

            usedByInstruction.clear();
            definedByInstruction.clear();
        }
    }
}
