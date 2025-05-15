package edu.kit.kastel.vads.compiler.backend.regalloc;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.List;
import java.util.Map;

public interface RegisterAllocator {

    Map<Node, Register> allocateRegisters(List<Node> totallyOrderedNodes);
}
