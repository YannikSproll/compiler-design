package edu.kit.kastel.vads.compiler.backend.aasm;

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

public class InstructionSelector {

    private final NodeSequenceAnalysis nodeSequentializationAnalysis;

    private static String NON_EXECUTABLE_STACK =
            ".section .note.GNU-stack,\"\",@progbits\n";

    public InstructionSelector(NodeSequenceAnalysis nodeSequentializationAnalysis) {
        this.nodeSequentializationAnalysis = nodeSequentializationAnalysis;
    }

    public void generateCode(List<IrGraph> program, CodeGenerator codeGenerator, String sourceFileName) {
        X86InstructionGenerator instructionGenerator = codeGenerator.getX86InstructionGenerator();

        instructionGenerator.generateFile(1, sourceFileName)
                .generateGlobal("main")
                .generateGlobal("_main")
                .generateText()
                .generateLabel("main")
                .generateCall("_main")
                .generateMoveInstruction(X86Register.REG_AX, X86Register.REG_DI, BitSize.BIT_64)
                .generateMoveInstruction(new IntegerConstantParameter(0x3C), X86Register.REG_AX, BitSize.BIT_64)
                .generateSyscall();

        for (IrGraph graph : program) {
            instructionGenerator.generateEmptyLine()
                    .generateComment("Method: " + graph.name(), false)
                    .generateLabel("_" + graph.name());

            LivenessAnalysis livenessAnalysis = new LivenessAnalysis();
            AasmRegisterAllocator allocator = new AasmRegisterAllocator(livenessAnalysis);
            NodeSequence nodeSequence = nodeSequentializationAnalysis.sequenceNodes(graph);

            RegisterAllocationResult allocationResult = allocator.allocateRegisters(nodeSequence);

            generateInstructions(nodeSequence, codeGenerator, allocationResult);
        }

        instructionGenerator.generateFromString(NON_EXECUTABLE_STACK);
    }

    private void generateInstructions(NodeSequence nodeSequence, CodeGenerator codeGenerator, RegisterAllocationResult allocationResult) {
        codeGenerator.generateStackPointerPush();

        int numberOfStackSlots = (int) allocationResult.registers().stream().filter(x -> x instanceof StackSlot).count();
        if (numberOfStackSlots > 0) {
            codeGenerator.generateStackAllocation(numberOfStackSlots);
        }

        for (Node node : nodeSequence.getSequence()) {
            generateInstructionForNode(node, codeGenerator, allocationResult);
        }
    }

    private void generateInstructionForNode(Node node, CodeGenerator codeGenerator, RegisterAllocationResult allocationResult) {
        switch (node) {
            case AddNode add -> codeGenerator.generateAdd(allocationResult, add);
            case SubNode sub -> codeGenerator.generateSub(allocationResult, sub);
            case MulNode mul -> codeGenerator.generateMult(allocationResult, mul);
            case DivNode div -> codeGenerator.generateDiv(allocationResult, div);
            case ModNode mod -> codeGenerator.generateMod(allocationResult, mod);
            case ReturnNode r -> codeGenerator.generateReturn(allocationResult, r);
            case ConstIntNode c -> codeGenerator.generateConstantInstruction(allocationResult, c);
            case Phi _ -> throw new UnsupportedOperationException("phi");
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
        }
    }


}
