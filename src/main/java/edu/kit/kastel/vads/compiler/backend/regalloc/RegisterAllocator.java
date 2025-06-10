package edu.kit.kastel.vads.compiler.backend.regalloc;

import edu.kit.kastel.vads.compiler.backend.aasm.NodeSequence;
import edu.kit.kastel.vads.compiler.backend.aasm.RegisterAllocationResult;
import edu.kit.kastel.vads.compiler.ir.data.IrFunction;

public interface RegisterAllocator {

    RegisterAllocationResult allocateRegisters(IrFunction function);
}
