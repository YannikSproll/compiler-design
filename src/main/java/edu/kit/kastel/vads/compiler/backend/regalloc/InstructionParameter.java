package edu.kit.kastel.vads.compiler.backend.regalloc;

import edu.kit.kastel.vads.compiler.backend.aasm.IntegerConstantParameter;

public sealed interface InstructionParameter permits Register, IntegerConstantParameter {
}
