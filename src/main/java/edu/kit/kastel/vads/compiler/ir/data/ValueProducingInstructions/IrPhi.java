package edu.kit.kastel.vads.compiler.ir.data.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.data.IrBlock;
import edu.kit.kastel.vads.compiler.ir.data.SSAValue;

import java.util.List;

public record IrPhi(
        SSAValue target,
        List<IrPhiItem> sources) implements IrValueProducingInstruction {

    public record IrPhiItem(SSAValue value, IrBlock block) {
    }
}
