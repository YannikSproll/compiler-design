package edu.kit.kastel.vads.compiler.ir.data;

public record IrReturnInstruction(
        SSAValue src) implements IrInstruction {
}
