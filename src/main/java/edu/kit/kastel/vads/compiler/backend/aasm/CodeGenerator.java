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
            LivenessAnalysis livenessAnalysis = new LivenessAnalysis();
            AasmRegisterAllocator allocator = new AasmRegisterAllocator(livenessAnalysis);
            NodeSequence nodeSequence = getTotallyOrderedNodes(graph);

            RegisterAllocationResult allocationResult = allocator.allocateRegisters(nodeSequence);

            builder.append(X86_HEADER_ASSEMBLY);
            generateInstructions(nodeSequence, builder, allocationResult);
            builder.append(NON_EXECUTABLE_STACK);
        }
        return builder.toString();
    }



    private void generateInstructions(NodeSequence nodeSequence, StringBuilder builder, RegisterAllocationResult allocationResult) {
        X86InstructionGenerator instructionGenerator = new X86InstructionGenerator(builder);

        for (Node node : nodeSequence.getSequence()) {
            generateInstructionForNode(node, instructionGenerator, allocationResult);
        }
    }

    private void generateInstructionForNode(Node node, X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult) {
        switch (node) {
            case AddNode add -> generateAdd(instructionGenerator, allocationResult, add);
            case SubNode sub -> generateSub(instructionGenerator, allocationResult, sub);
            case MulNode mul -> generateMult(instructionGenerator, allocationResult, mul);
            case DivNode div -> generateDiv(instructionGenerator, allocationResult, div);
            case ModNode mod -> generateMod(instructionGenerator, allocationResult, mod);
            case ReturnNode r -> generateReturn(instructionGenerator, allocationResult, r);
            case ConstIntNode c -> instructionGenerator.generateIntConstInstruction(allocationResult.nodeToRegisterMapping().get(c), c.value());
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
    }

    private static void generateAdd(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, AddNode addNode) {
        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(addNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(addNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(addNode);

        Register sourceRegister;
        if (leftOperandRegister != targetRegister && rightOperandRegister != targetRegister) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister);
            sourceRegister = rightOperandRegister;
        } else {
            sourceRegister = leftOperandRegister != targetRegister ? leftOperandRegister : rightOperandRegister;
        }

        instructionGenerator.generateAdditionInstruction(sourceRegister, targetRegister);
    }

    private  static void generateSub(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, SubNode subNode) {
        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(subNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(subNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(subNode);

        Register sourceRegister;
        if (leftOperandRegister != targetRegister && rightOperandRegister != targetRegister) {
            // None of the operands is in the target register, but this is required in x86, so we move it there.
            instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister);
            sourceRegister = rightOperandRegister;
        } else if (rightOperandRegister == targetRegister) { // Right operand is the target register -> swap registers
            sourceRegister = rightOperandRegister;
        } else { // Left operand register is the target register
            sourceRegister = leftOperandRegister;
        }

        instructionGenerator.generateSubtractionInstruction(sourceRegister, targetRegister);
    }

    private  static void generateMult(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, MulNode mulNode) {
        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(mulNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(mulNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(mulNode);

        Register sourceRegister;
        if (leftOperandRegister != targetRegister && rightOperandRegister != targetRegister) {
            // None of the operands is in the target register, but this is required in x86, so we move it there.
            instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister);
            sourceRegister = rightOperandRegister;
        } else {
            sourceRegister = leftOperandRegister != targetRegister ? leftOperandRegister : rightOperandRegister;
        }

        instructionGenerator.generateMultiplicationInstruction(sourceRegister, targetRegister);
    }


    private static void generateDiv(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, DivNode divNode) {
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(divNode, BinaryOperationNode.LEFT)), X86Register.REG_AX)
                .generateSignExtendInstruction()
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(divNode, BinaryOperationNode.RIGHT)))
                .generateMoveInstruction(X86Register.REG_AX, allocationResult.nodeToRegisterMapping().get(divNode));
    }

    private static void generateMod(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, ModNode modNode) {
        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(modNode, BinaryOperationNode.LEFT)), X86Register.REG_AX)
                .generateSignExtendInstruction()
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(modNode, BinaryOperationNode.RIGHT)))
                .generateMoveInstruction(X86Register.REG_DX, allocationResult.nodeToRegisterMapping().get(modNode));
    }

    private static void generateReturn(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, ReturnNode returnNode) {
        Register returnValueRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(returnNode, ReturnNode.RESULT));
        if (returnValueRegister != X86Register.REG_AX) {
            instructionGenerator.generateMoveInstruction(returnValueRegister, X86Register.REG_AX);
        }

        instructionGenerator.generateReturnInstruction();
    }

    private NodeSequence getTotallyOrderedNodes(IrGraph graph) {
        Node endBlock = graph.endBlock();
        List<Node> totallyOrderedNodes = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        getTotallyOrderedNodesRecursive(endBlock, visited, totallyOrderedNodes);
        return NodeSequence.createFrom(totallyOrderedNodes);
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
