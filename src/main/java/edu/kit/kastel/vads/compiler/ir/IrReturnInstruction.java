package edu.kit.kastel.vads.compiler.ir;

public record IrReturnInstruction(
        SSAValue src) implements IrInstruction {
}
