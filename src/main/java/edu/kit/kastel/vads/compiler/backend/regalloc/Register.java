package edu.kit.kastel.vads.compiler.backend.regalloc;

import edu.kit.kastel.vads.compiler.backend.aasm.StackSlot;
import edu.kit.kastel.vads.compiler.backend.aasm.X86Register;

public sealed interface Register extends InstructionParameter permits X86Register, StackSlot {

}
