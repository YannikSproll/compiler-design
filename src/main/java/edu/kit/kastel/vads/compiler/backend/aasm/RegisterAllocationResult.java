package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.Map;

public record RegisterAllocationResult(LivenessAnalysisResult livenessAnalysisResult, Map<Node, Register> nodeToRegisterMapping) { }
