package edu.kit.kastel.vads.compiler.ir.data;

import java.util.List;

public record IrFunction(
        IrBlock startBlock,
        List<IrBlock> blocks) {
}
