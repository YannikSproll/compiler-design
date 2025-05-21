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
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo;

import java.util.*;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class CodeGenerator {
    private static String NON_EXECUTABLE_STACK =
            ".section .note.GNU-stack,\"\",@progbits\n";


    public String generateCode(List<IrGraph> program) {
        StringBuilder builder = new StringBuilder();
        for (IrGraph graph : program) {
            LivenessAnalysis livenessAnalysis = new LivenessAnalysis();
            AasmRegisterAllocator allocator = new AasmRegisterAllocator(livenessAnalysis);
            NodeSequence nodeSequence = getTotallyOrderedNodes(graph);

            RegisterAllocationResult allocationResult = allocator.allocateRegisters(nodeSequence);

            X86InstructionGenerator instructionGenerator = new X86InstructionGenerator(builder);
            instructionGenerator//.generateFile(1, "first.l1")
                    .generateGlobal("main")
                    .generateGlobal("_main")
                    .generateText()
                    .generateLabel("main")
                    .generateCall("_main")
                    .generateMoveInstruction(X86Register.REG_AX, X86Register.REG_DI, BitSize.BIT_64)
                    .generateMoveInstruction(new IntegerConstantParameter(0x3C), X86Register.REG_AX, BitSize.BIT_64)
                    .generateSyscall()
                    .generateLabel("_main");

            //builder.append(X86_HEADER_ASSEMBLY);
            generateInstructions(nodeSequence, instructionGenerator, allocationResult);
            builder.append(NON_EXECUTABLE_STACK);
        }
        return builder.toString();
    }



    private void generateInstructions(NodeSequence nodeSequence, X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult) {
        ;

        generateStackPointerPush(instructionGenerator);

        int numberOfStackSlots = (int) allocationResult.registers().stream().filter(x -> x instanceof StackSlot).count();
        if (numberOfStackSlots > 0) {
            generateStackAllocation(instructionGenerator, allocationResult, numberOfStackSlots);
        }

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
            case ReturnNode r ->  {
                generateReturn(instructionGenerator, allocationResult, r);
            }
            case ConstIntNode c -> generateConstantInstruction(instructionGenerator, allocationResult, c);
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
    }

    private static void generateStackPointerPush(X86InstructionGenerator instructionGenerator) {
        instructionGenerator.generatePushInstruction(X86Register.REG_BP, BitSize.BIT_64)
                .generateMoveInstruction(X86Register.REG_SP, X86Register.REG_BP, BitSize.BIT_64);
    }

    private static void generateStackPointerPop(X86InstructionGenerator instructionGenerator) {
        instructionGenerator.generatePopInstruction(X86Register.REG_BP, BitSize.BIT_64);
    }

    private static void generateStackAllocation(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, int numberOfStackSlots) {
        instructionGenerator.generateSubtractionInstruction(new IntegerConstantParameter((numberOfStackSlots + 1) * 8), X86Register.REG_SP, BitSize.BIT_64);
    }

    private static void generateStackDeallocation(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, int numberOfStackSlots) {
        instructionGenerator.generateAdditionInstruction(new IntegerConstantParameter((numberOfStackSlots + 1) * 8), X86Register.REG_SP, BitSize.BIT_64);
    }

    private static void generateConstantInstruction(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, ConstIntNode constIntNode) {
        //generateLineDebugging(instructionGenerator, constIntNode);

        instructionGenerator.generateIntConstInstruction(allocationResult.nodeToRegisterMapping().get(constIntNode), constIntNode.value(), BitSize.BIT_32);
    }

    private static void generateAdd(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, AddNode addNode) {
        //generateLineDebugging(instructionGenerator, addNode);

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(addNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(addNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(addNode);

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateAdditionInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);

        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateAdditionInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateAdditionInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateAdditionInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    private  static void generateSub(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, SubNode subNode) {
        //generateLineDebugging(instructionGenerator, subNode);

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(subNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(subNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(subNode);

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateSubtractionInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                                .generateSubtractionInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                                .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateSubtractionInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateSubtractionInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }

    private  static void generateMult(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, MulNode mulNode) {
        //generateLineDebugging(instructionGenerator, mulNode);

        Register leftOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(mulNode, BinaryOperationNode.LEFT));
        Register rightOperandRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(mulNode, BinaryOperationNode.RIGHT));
        Register targetRegister = allocationResult.nodeToRegisterMapping().get(mulNode);

        if (targetRegister instanceof StackSlot) {
            instructionGenerator.generateMoveInstruction(rightOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMultiplicationInstruction(leftOperandRegister, allocationResult.tempRegister(), BitSize.BIT_32)
                    .generateMoveInstruction(allocationResult.tempRegister(), targetRegister, BitSize.BIT_32);

        } else {
            if (rightOperandRegister == targetRegister) {
                instructionGenerator.generateMultiplicationInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            } else if (leftOperandRegister == targetRegister) {
                instructionGenerator.generateMultiplicationInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32);
            } else {
                instructionGenerator.generateMoveInstruction(rightOperandRegister, targetRegister, BitSize.BIT_32)
                        .generateMultiplicationInstruction(leftOperandRegister, targetRegister, BitSize.BIT_32);
            }
        }
    }


    private static void generateDiv(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, DivNode divNode) {
        //generateLineDebugging(instructionGenerator, divNode);

        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(divNode, BinaryOperationNode.LEFT)), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(divNode, BinaryOperationNode.RIGHT)), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_AX, allocationResult.nodeToRegisterMapping().get(divNode), BitSize.BIT_32);
    }

    private static void generateMod(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, ModNode modNode) {
        generateLineDebugging(instructionGenerator, modNode);

        instructionGenerator
                .generateMoveInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(modNode, BinaryOperationNode.LEFT)), X86Register.REG_AX, BitSize.BIT_32)
                .generateSignExtendInstruction(BitSize.BIT_32)
                .generateIntegerDivisionInstruction(allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(modNode, BinaryOperationNode.RIGHT)), BitSize.BIT_32)
                .generateMoveInstruction(X86Register.REG_DX, allocationResult.nodeToRegisterMapping().get(modNode), BitSize.BIT_32);
    }

    private static void generateReturn(X86InstructionGenerator instructionGenerator, RegisterAllocationResult allocationResult, ReturnNode returnNode) {
        //generateLineDebugging(instructionGenerator, returnNode);

        Register returnValueRegister = allocationResult.nodeToRegisterMapping().get(predecessorSkipProj(returnNode, ReturnNode.RESULT));
        if (returnValueRegister != X86Register.REG_AX) {
            instructionGenerator.generateMoveInstruction(returnValueRegister, X86Register.REG_AX, BitSize.BIT_32);
        }

        int numberOfStackSlots = (int) allocationResult.registers().stream().filter(x -> x instanceof StackSlot).count();
        if (numberOfStackSlots > 0) {
            generateStackDeallocation(instructionGenerator, allocationResult, numberOfStackSlots);
        }

        generateStackPointerPop(instructionGenerator);

        instructionGenerator.generateReturnInstruction();
    }

    private static void generateLineDebugging(X86InstructionGenerator instructionGenerator, Node node) {
        if (node.debugInfo() instanceof DebugInfo.SourceInfo(edu.kit.kastel.vads.compiler.Span span)) {
            instructionGenerator.generateLOCAnnotation(1, span.start().line(), span.start().column());
        }
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
