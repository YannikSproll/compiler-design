package edu.kit.kastel.vads.compiler.backend.aasm;

import edu.kit.kastel.vads.compiler.ir.IrBlock;
import edu.kit.kastel.vads.compiler.ir.IrInstruction;
import edu.kit.kastel.vads.compiler.ir.SSAValue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class LivenessAnalysisResult {
    private final Map<IrInstruction, Set<SSAValue>> livenessInformation;
    private final Map<IrBlock, LivenessAnalysis.BlockAnalysisResults> blockAnalysisResults;

    public LivenessAnalysisResult(
            Map<IrInstruction, Set<SSAValue>> livenessInformation,
            Map<IrBlock, LivenessAnalysis.BlockAnalysisResults> blockAnalysisResults) {
        this.livenessInformation = livenessInformation;
        this.blockAnalysisResults = blockAnalysisResults;
    }

    public boolean isLiveAt(IrInstruction instruction, SSAValue ssaValue) {
        if (!livenessInformation.containsKey(instruction)) {
            throw new IllegalArgumentException("Node " + instruction + " is not in liveness information");
        }

        return livenessInformation.get(instruction).contains(ssaValue);
    }

    public Set<SSAValue> getLiveNodesAt(IrInstruction instruction) {
        if (!livenessInformation.containsKey(instruction)) {
            throw new IllegalArgumentException("Node " + instruction + " is not in liveness information");
        }
        return Collections.unmodifiableSet(livenessInformation.get(instruction));
    }

    public Set<SSAValue> getBlockLiveOut(IrBlock block) {
        if (!blockAnalysisResults.containsKey(block)) {
            throw new IllegalArgumentException("Block " + block + " is not in liveness information");
        }

        return Collections.unmodifiableSet(blockAnalysisResults.get(block).out());
    }
}
