package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.IrPhi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IrBlock {
    private final String name;
    private final List<IrBlock> predecessorBlocks;
    private final List<IrBlock> successorBlocks;
    private final List<IrInstruction> instructions;

    public IrBlock(String name) {
        this.name = name;
        predecessorBlocks = new ArrayList<>();
        successorBlocks = new ArrayList<>();
        instructions = new ArrayList<>();
    }

    public String name() {
        return name;
    }

    public List<IrBlock> getPredecessorBlocks() {
        return Collections.unmodifiableList(predecessorBlocks);
    }
    public List<IrBlock> getSuccessorBlocks() {
        return Collections.unmodifiableList(successorBlocks);
    }
    public List<IrInstruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    private void addPredecessor(IrBlock predecessorBlock) {
        if (!predecessorBlocks.contains(predecessorBlock)) {
            predecessorBlocks.add(predecessorBlock);
        }
    }

    public void addSuccessorBlock(IrBlock successorBlock) {
        addSuccessor(successorBlock);
        successorBlock.addPredecessor(this);
    }

    private void addSuccessor(IrBlock successorBlock) {
        if (!successorBlocks.contains(successorBlock)) {
            successorBlocks.add(successorBlock);
        }
    }

    public void addInstruction(IrInstruction instruction) {
        instructions.add(instruction);
    }
    public void insertInstruction(int index, IrInstruction instruction) {
        instructions.add(index, instruction);
    }

    public void removePhis() {
        instructions.removeIf(irInstruction -> irInstruction instanceof IrPhi);
    }
    public void removeInstruction(IrInstruction instruction) {
        instructions.remove(instruction);
    }
}
