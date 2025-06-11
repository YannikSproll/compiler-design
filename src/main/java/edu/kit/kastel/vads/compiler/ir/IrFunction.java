package edu.kit.kastel.vads.compiler.ir;

import java.util.List;

public record IrFunction(
        IrBlock startBlock,
        List<IrBlock> blocks,
        boolean isEntryPoint) {
}
