package edu.kit.kastel.vads.compiler.ir.data;

import java.util.ArrayList;
import java.util.List;

public final class IrBlock {
    private final List<IrBlock> predecessorBlocks;
    private final List<IrBlock> successorBlocks;
    private final List<IrInstruction> instructions;

    public IrBlock() {
        predecessorBlocks = new ArrayList<>();
        successorBlocks = new ArrayList<>();
        instructions = new ArrayList<>();
    }

    public List<IrBlock> getPredecessorBlocks() {
        return predecessorBlocks;
    }
    public List<IrBlock> getSuccessorBlocks() {
        return successorBlocks;
    }
    public List<IrInstruction> getInstructions() {
        return instructions;
    }

    public void addPredecessorBlock(IrBlock predecessorBlock) {
        predecessorBlocks.add(predecessorBlock);
    }

    public void addSuccessorBlock(IrBlock successorBlock) {
        successorBlocks.add(successorBlock);
    }

    public void addInstruction(IrInstruction instruction) {
        instructions.add(instruction);
    }
}
