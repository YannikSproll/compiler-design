package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.data.SSAValue;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.Map;
import java.util.Set;

public record RegisterAllocationResult(LivenessAnalysisResult livenessAnalysisResult, Map<SSAValue, Register> nodeToRegisterMapping, Register tempRegister, Set<Register> registers) { }
