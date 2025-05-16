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

            builder.append(X86_HEADER_ASSEMBLY);
            generateInstructions(totallyOrderedNodes, builder, registers);
            builder.append(NON_EXECUTABLE_STACK);
        }
        return builder.toString();
    }



    private void generateInstructions(List<Node> totallyOrderedNodes, StringBuilder builder, Map<Node, Register> registers) {
        X86InstructionGenerator instructionGenerator = new X86InstructionGenerator(builder);

        for (Node node : totallyOrderedNodes) {
            generateInstructionForNode(node, instructionGenerator, registers);
        }
    }

    private void generateInstructionForNode(Node node, X86InstructionGenerator instructionGenerator, Map<Node, Register> registers) {
        switch (node) {
            case AddNode add -> generateAdd(instructionGenerator, registers, add);
            case SubNode sub -> generateSub(instructionGenerator, registers, sub);
            case MulNode mul -> generateMult(instructionGenerator, registers, mul);
            case DivNode div -> generateDiv(instructionGenerator, registers, div);
            case ModNode mod -> generateMod(instructionGenerator, registers, mod);
            case ReturnNode r -> generateReturn(instructionGenerator, registers, r);
            case ConstIntNode c -> instructionGenerator.generateIntConstInstruction(registers.get(c), c.value());
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
    }

    private static void generateAdd(X86InstructionGenerator instructionGenerator, Map<Node, Register> registers, AddNode addNode) {
        Register leftOperandRegister = registers.get(predecessorSkipProj(addNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = registers.get(predecessorSkipProj(addNode, BinaryOperationNode.RIGHT));
        Register targetRegister = registers.get(addNode);

        if (leftOperandRegister != targetRegister && rightOperandRegister != targetRegister) {
            // None of the operands is in the target register, but this is required in x86, so we move it there.
            int x = 5;
        }

        Register sourceRegister = leftOperandRegister != targetRegister ? leftOperandRegister : rightOperandRegister;

        instructionGenerator.generateAdditionInstruction(sourceRegister, targetRegister);
    }

    private  static void generateSub(X86InstructionGenerator instructionGenerator, Map<Node, Register> registers, SubNode subNode) {
        Register leftOperandRegister = registers.get(predecessorSkipProj(subNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = registers.get(predecessorSkipProj(subNode, BinaryOperationNode.RIGHT));
        Register targetRegister = registers.get(subNode);

        if (leftOperandRegister != targetRegister && rightOperandRegister != targetRegister) {
            // None of the operands is in the target register, but this is required in x86, so we move it there.
            int x = 5;
        }

        Register sourceRegister = leftOperandRegister != targetRegister ? leftOperandRegister : rightOperandRegister;

        instructionGenerator.generateSubtractionInstruction(sourceRegister, targetRegister);
    }

    private  static void generateMult(X86InstructionGenerator instructionGenerator, Map<Node, Register> registers, MulNode mulNode) {
        Register leftOperandRegister = registers.get(predecessorSkipProj(mulNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = registers.get(predecessorSkipProj(mulNode, BinaryOperationNode.RIGHT));
        Register targetRegister = registers.get(mulNode);

        if (leftOperandRegister != targetRegister && rightOperandRegister != targetRegister) {
            // None of the operands is in the target register, but this is required in x86, so we move it there.
            int x = 5;
        }

        Register sourceRegister = leftOperandRegister != targetRegister ? leftOperandRegister : rightOperandRegister;

        instructionGenerator.generateMultiplicationInstruction(sourceRegister, targetRegister);
    }


    private static void generateDiv(X86InstructionGenerator instructionGenerator, Map<Node, Register> registers, DivNode divNode) {
        instructionGenerator
                .generateMoveInstruction(registers.get(predecessorSkipProj(divNode, BinaryOperationNode.LEFT)), X86Register.REG_AX)
                .generateSignExtendInstruction()
                .generateIntegerDivisionInstruction(registers.get(predecessorSkipProj(divNode, BinaryOperationNode.RIGHT)))
                .generateMoveInstruction(X86Register.REG_AX, registers.get(divNode));
    }

    private static void generateMod(X86InstructionGenerator instructionGenerator, Map<Node, Register> registers, ModNode modNode) {
        instructionGenerator
                .generateMoveInstruction(registers.get(predecessorSkipProj(modNode, BinaryOperationNode.LEFT)), X86Register.REG_AX)
                .generateSignExtendInstruction()
                .generateIntegerDivisionInstruction(registers.get(predecessorSkipProj(modNode, BinaryOperationNode.RIGHT)))
                .generateMoveInstruction(X86Register.REG_DX, registers.get(modNode));
    }

    private static void generateReturn(X86InstructionGenerator instructionGenerator, Map<Node, Register> registers, ReturnNode returnNode) {
        Register returnValueRegister = registers.get(predecessorSkipProj(returnNode, ReturnNode.RESULT));
        if (returnValueRegister != X86Register.REG_AX) {
            instructionGenerator.generateMoveInstruction(returnValueRegister, X86Register.REG_AX);
        }

        instructionGenerator.generateReturnInstruction();
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
}
