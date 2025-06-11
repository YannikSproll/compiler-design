package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.IrBlock;
import edu.kit.kastel.vads.compiler.ir.SSAValue;

import java.util.List;

public record IrPhi(
        SSAValue target,
        List<IrPhiItem> sources) implements IrValueProducingInstruction {

    public record IrPhiItem(SSAValue value, IrBlock block) {
    }
}
