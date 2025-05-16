package edu.kit.kastel.vads.compiler.backend.regalloc;

import edu.kit.kastel.vads.compiler.backend.aasm.NodeSequence;
import edu.kit.kastel.vads.compiler.backend.aasm.RegisterAllocationResult;

public interface RegisterAllocator {

    RegisterAllocationResult allocateRegisters(NodeSequence nodeSequence);
}
