package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.Phi;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;

import java.util.*;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class CodeGenerator {

    private static String X86_HEADER_ASSEMBLY =
            ".global main\n" +
            ".global _main\n" +
            ".text\n" +
            "main:\n" +
            "  call _main\n" +
            "  movq %rax, %rdi\n" +
            "  movq $0x3C, %rax\n" +
            "  syscall\n" +
            "_main:\n";
    private static String NON_EXECUTABLE_STACK =
            ".section .note.GNU-stack,\"\",@progbits\n";


    public String generateCode(List<IrGraph> program) {
        StringBuilder builder = new StringBuilder();
        for (IrGraph graph : program) {
            AasmRegisterAllocator allocator = new AasmRegisterAllocator();
            List<Node> totallyOrderedNodes = getTotallyOrderedNodes(graph);

            Map<Node, Register> registers = allocator.allocateRegisters(totallyOrderedNodes);
            //builder.append("function ")
            //   .append(graph.name())
            //    .append(" {\n");
            builder.append(X86_HEADER_ASSEMBLY);
            generateForGraph(graph, builder, registers);
            builder.append(NON_EXECUTABLE_STACK);
            //builder.append("}");
        }
        return builder.toString();
    }

    private List<Node> getTotallyOrderedNodes(IrGraph graph) {
        Node endBlock = graph.endBlock();
        List<Node> totallyOrderedNodes = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        getTotallyOrderedNodesRecursive(endBlock, visited, totallyOrderedNodes);
        return totallyOrderedNodes;
    }

    private void getTotallyOrderedNodesRecursive(Node node, Set<Node> visited, List<Node> orderedNodes) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                getTotallyOrderedNodesRecursive(predecessor, visited, orderedNodes);
            }
        }

        switch (node) {
            case ReturnNode _, BinaryOperationNode _, ConstIntNode _ -> {
                orderedNodes.add(node);
            }
            case Block _, ProjNode _, StartNode _, Phi _ -> {
            }
        };
    }

    private void generateForGraph(IrGraph graph, StringBuilder builder, Map<Node, Register> registers) {
        Set<Node> visited = new HashSet<>();
        scan(graph.endBlock(), visited, builder, registers);
    }

    private void scan(Node node, Set<Node> visited, StringBuilder builder, Map<Node, Register> registers) {
        for (Node predecessor : node.predecessors()) {
            if (visited.add(predecessor)) {
                scan(predecessor, visited, builder, registers);
            }
        }

        switch (node) {
            case AddNode add -> generateAdd(builder, registers, add); //binary(builder, registers, add, "add");
            case SubNode sub -> generateSub(builder, registers, sub); //binary(builder, registers, sub, "sub");
            case MulNode mul -> generateMult(builder, registers, mul); //binary(builder, registers, mul, "mul");
            case DivNode div -> generateDiv(builder, registers, div);
            case ModNode mod -> generateMod(builder, registers, mod);
            case ReturnNode r -> generateReturn(builder, registers, r); /*builder.repeat(" ", 2).append("ret ")
                .append(registers.get(predecessorSkipProj(r, ReturnNode.RESULT)));*/
            case ConstIntNode c -> generateConst(builder, registers, c); /*builder.repeat(" ", 2)
                .append(registers.get(c))
                .append(" = const ")
                .append(c.value());*/
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
        builder.append("\n");
    }

    private static void generateConst(StringBuilder builder, Map<Node, Register> registers, ConstIntNode node) {
        builder.repeat(" ", 2)
                .append("movq ")
                .append("$")
                .append(node.value())
                .append(", ")
                .append(registers.get(node));
    }

    private static void generateAdd(StringBuilder builder, Map<Node, Register> registers, AddNode addNode) {
        generateOperation(builder, registers, addNode, "addq");
    }

    private  static void generateSub(StringBuilder builder, Map<Node, Register> registers, SubNode subNode) {
        generateOperation(builder, registers, subNode, "subq");
    }

    private  static void generateMult(StringBuilder builder, Map<Node, Register> registers, MulNode mulNode) {
        generateOperation(builder, registers, mulNode, "imulq");
    }



    private static void generateOperation(StringBuilder builder, Map<Node, Register> registers, Node node, String operation) {

        Register leftOperandRegister = registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT));
        Register rightOperandRegister = registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT));
        Register targetRegister = registers.get(node);

        if (leftOperandRegister != targetRegister && rightOperandRegister != targetRegister) {
            int x = 5;
        }

        Register sourceRegister = leftOperandRegister != targetRegister ? leftOperandRegister : rightOperandRegister;

        builder/*.repeat(" ", 2)
                .append("movq ")
                .append()
                .append(", ")
                .append(registers.get(node))
                .append("\n")*/
                .repeat(" ", 2)
                .append(operation)
                .append(" ")
                .append(sourceRegister)
                .append(", ")
                .append(targetRegister);
    }

    private static void generateDiv(StringBuilder builder, Map<Node, Register> registers, DivNode divNode) {
        builder.repeat(" ", 2)
                .append("movq ")
                .append(registers.get(predecessorSkipProj(divNode, BinaryOperationNode.LEFT)))
                .append(", ")
                .append("%rax")
                .append("\n")
                .repeat(" ", 2)
                .append("CLTD\n")
                .repeat(" ", 2)
                .append("idivq ")
                .append(registers.get(predecessorSkipProj(divNode, BinaryOperationNode.RIGHT)))
                .append("\n")
                .repeat(" ", 2)
                .append("movq %rax, ")
                .append(registers.get(divNode));
    }

    private static void generateMod(StringBuilder builder, Map<Node, Register> registers, ModNode modNode) {
        builder.repeat(" ", 2)
                .append("movq ")
                .append(registers.get(predecessorSkipProj(modNode, BinaryOperationNode.LEFT)))
                .append(", ")
                .append("%rax")
                .append("\n")
                .repeat(" ", 2)
                .append("CLTD\n")
                .repeat(" ", 2)
                .append("idivq ")
                .append(registers.get(predecessorSkipProj(modNode, BinaryOperationNode.RIGHT)))
                .append("\n")
                .repeat(" ", 2)
                .append("movq %rdx, ")
                .append(registers.get(modNode));
    }

    private static void generateReturn(StringBuilder builder, Map<Node, Register> registers, ReturnNode returnNode) {
        builder.repeat(" ", 2)
                .append("movq ")
                .append(registers.get(predecessorSkipProj(returnNode, ReturnNode.RESULT)))
                .append(", ")
                .append("%rax")
                .append("\n")
                .repeat(" ", 2)
                .append("ret");
    }

    private static void binary(
        StringBuilder builder,
        Map<Node, Register> registers,
        BinaryOperationNode node,
        String opcode
    ) {
        builder.repeat(" ", 2).append(registers.get(node))
            .append(" = ")
            .append(opcode)
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.LEFT)))
            .append(" ")
            .append(registers.get(predecessorSkipProj(node, BinaryOperationNode.RIGHT)));
    }
}
