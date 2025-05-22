package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.IrGraph;

public interface NodeSequenceAnalysis {
    NodeSequence sequenceNodes(IrGraph graph);
}
