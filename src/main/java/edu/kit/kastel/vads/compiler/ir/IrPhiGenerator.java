package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;
import edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions.*;

import java.util.*;
import java.util.stream.Collectors;

public class IrPhiGenerator {

    public IrFunction addPhis(
            IrFunction function,
            SSAVariableRenameRecording ssaVariables,
            SSAValueGenerator ssaValueGenerator) {

        HashMap<IrBlock, Set<IrBlock>> dominators = buildDominatorTree(function);
        HashMap<IrBlock, IrBlock> immDominators = getImmediateDominators(dominators);
        HashMap<IrBlock, Set<IrBlock>> dominanceFrontiers = getDominanceFrontiers(function, immDominators);

        insertPlaceholderPhis(function, ssaVariables, dominanceFrontiers, ssaValueGenerator);
        insertPhiOperands(function.startBlock(), ssaVariables);
        // Insert phis
        HashSet<IrBlock> blocks = new HashSet<>();
        replaceOperandsWithPhiTargets(function.startBlock(), blocks);

        return function;
    }


    private void insertPlaceholderPhis(
            IrFunction function,
            SSAVariableRenameRecording ssaVariables,
            HashMap<IrBlock, Set<IrBlock>> dominanceFrontiers,
            SSAValueGenerator ssaValueGenerator) {


        Map<Symbol, List<IrBlock>> defSites = new HashMap<>();
        HashSet<IrBlock> visitedBlock = new HashSet<>();

        computeDefSites(function.startBlock(), visitedBlock, defSites, ssaVariables);

        for (Map.Entry<Symbol, List<IrBlock>> defSite : defSites.entrySet()) {
            List<IrBlock> workList = new ArrayList<>(defSite.getValue());
            HashSet<IrBlock> hasAlready = new HashSet<>();
            while (!workList.isEmpty()) {
                IrBlock current = workList.getFirst();
                workList.remove(current);


                for (IrBlock block : dominanceFrontiers.getOrDefault(current, Set.of())) {
                    if (hasAlready.contains(block)) {
                        continue;
                    }
                    SSAValue phiSSAValue = ssaValueGenerator.generateNewSSAValue();
                    IrPhi phi = new IrPhi(phiSSAValue, new ArrayList<>());
                    ssaVariables.introduceNewSSAValue(defSite.getKey(), phiSSAValue);
                    block.insertInstruction(0, phi);
                    hasAlready.add(block);

                    if (!defSites.containsKey(defSite.getKey()) || !defSites.get(defSite.getKey()).contains(block)) {
                        workList.add(block);
                    }
                }
            }
        }
    }

    private void computeDefSites(
            IrBlock block,
            HashSet<IrBlock> visited,
            Map<Symbol, List<IrBlock>> defSites,
            SSAVariableRenameRecording ssaVariables) {
        if (!visited.add(block)) {
            return;
        }

        Map<SSAValue, Symbol> invertedMappings = ssaVariables.getInvertedSSAValueMappings();

        for (IrInstruction instruction : block.getInstructions()) {
            Optional<SSAValue> ssaValueDefinedByInstruction = definesOperands(instruction);
            if (ssaValueDefinedByInstruction.isPresent() && invertedMappings.containsKey(ssaValueDefinedByInstruction.get())) {
                defSites.computeIfAbsent(
                        invertedMappings.get(ssaValueDefinedByInstruction.get()),
                        _ -> new ArrayList<>()).add(block);
            }
        }

        for (IrBlock successor : block.getSuccessorBlocks()) {
            computeDefSites(successor, visited, defSites, ssaVariables);
        }
    }

    private Optional<SSAValue> definesOperands(IrInstruction instruction) {
        return switch (instruction) {
            case IrBinaryOperationInstruction binaryOperationInstruction ->
                    Optional.of(binaryOperationInstruction.target());
            case IrBoolConstantInstruction irBoolConstantInstruction ->
                    Optional.of(irBoolConstantInstruction.target());
            case IrIntConstantInstruction irIntConstantInstruction ->
                    Optional.of(irIntConstantInstruction.target());
            case IrMoveInstruction irMoveInstruction -> Optional.of(irMoveInstruction.target());
            case IrPhi irPhi -> Optional.empty();
            case IrUnaryOperationInstruction irUnaryOperationInstruction ->
                     Optional.of(irUnaryOperationInstruction.target());
            case IrBranchInstruction _, IrJumpInstruction _, IrReturnInstruction _ -> Optional.empty();
        };
    }


    private void insertPhiOperands(IrBlock block, SSAVariableRenameRecording ssaVariables) {
        HashSet<IrBlock> visitedBlocks = new HashSet<>();
        insertPhiOperands(block, visitedBlocks, ssaVariables);
    }

    private void insertPhiOperands(IrBlock block, HashSet<IrBlock> visitedBlocks, SSAVariableRenameRecording ssaVariables) {
        if (!visitedBlocks.add(block)) {
            return;
        }

        for (IrInstruction instruction : block.getInstructions()) {
            if (instruction instanceof IrPhi phi) {
                Symbol phiTargetSymbol = ssaVariables.getInvertedSSAValueMappings().get(phi.target());
                for (IrBlock predecessor : block.getPredecessorBlocks()) {
                    SSAValue phiSource = getLatestDefOf(predecessor, phiTargetSymbol, ssaVariables);
                    phi.addPhiItem(new IrPhi.IrPhiItem(phiSource, predecessor));
                }
            }
        }

        for (IrBlock successor : block.getSuccessorBlocks()) {
            insertPhiOperands(successor, visitedBlocks, ssaVariables);
        }
    }

    private SSAValue getLatestDefOf(IrBlock block, Symbol symbol, SSAVariableRenameRecording ssaVariables) {
        List<SSAValue> ssaValues = new ArrayList<>();

        for (IrInstruction instruction : block.getInstructions()) {
            if (instruction instanceof IrValueProducingInstruction valueProducingInstruction) {
                if (ssaVariables.getInvertedSSAValueMappings().containsKey(valueProducingInstruction.target())) {
                    Symbol instructionDefinedSymbol
                            = ssaVariables.getInvertedSSAValueMappings().get(valueProducingInstruction.target());

                    if (symbol == instructionDefinedSymbol) {
                        ssaValues.add(valueProducingInstruction.target());
                    }
                }
            }
        }

        if (!ssaValues.isEmpty()) {
            return ssaValues.getLast();
        }

        HashSet<SSAValue> lastPredecessorDefs = new HashSet<>();
        for (IrBlock predecessor : block.getPredecessorBlocks()) {
            SSAValue lastPredecessorDef = getLatestDefOf(predecessor, symbol, ssaVariables);
            lastPredecessorDefs.add(lastPredecessorDef);
        }

        if (lastPredecessorDefs.size() != 1) {
            throw new IllegalArgumentException("Unexpected.");
        }

        return lastPredecessorDefs.stream().findFirst().get();
    }

    private void replaceOperandsWithPhiTargets(IrBlock block, HashSet<IrBlock> visitedBlocks) {
         if (!visitedBlocks.add(block)) {
             return;
         }

         for (IrInstruction instruction : block.getInstructions()) {
             if (instruction instanceof IrPhi phi) {
                 HashSet<IrBlock> processedBlocks = new HashSet<>();
                 HashSet<SSAValue> valuesToReplace = phi.sources().stream().map(IrPhi.IrPhiItem::value)
                         .collect(Collectors.toCollection(HashSet::new));
                 replaceInBlock(block, processedBlocks, phi.target(), valuesToReplace);
             }
         }

         for (IrBlock successor : block.getSuccessorBlocks()) {
             replaceOperandsWithPhiTargets(successor, visitedBlocks);
         }
    }

    private void replaceInBlock(IrBlock block, Set<IrBlock> processedBlocks, SSAValue newValue, Set<SSAValue> valuesToReplace) {
        if (!processedBlocks.add(block) || valuesToReplace.isEmpty()) {
            return;
        }

        for (IrInstruction instruction : block.getInstructions()) {
            replaceOperands(instruction, newValue, valuesToReplace);

            Optional<SSAValue> definedByInstruction = definesOperands(instruction, valuesToReplace);
            if (definedByInstruction.isPresent()) {
                valuesToReplace.remove(definedByInstruction.get());
                if (valuesToReplace.isEmpty()) {
                    return;
                }
            }
        }

        for (IrBlock successor : block.getSuccessorBlocks()) {
            HashSet<SSAValue> succValuesToReplace = new HashSet<>(valuesToReplace);
            replaceInBlock(successor, processedBlocks, newValue, succValuesToReplace);
        }
    }

    private void replaceOperands(IrInstruction instruction, SSAValue newValue, Set<SSAValue> valuesToReplace) {
        switch (instruction) {
            case IrBinaryOperationInstruction binaryOperationInstruction:
                if (valuesToReplace.contains(binaryOperationInstruction.leftSrc())) {
                    binaryOperationInstruction.replaceLeftSrc(newValue);
                }
                if (valuesToReplace.contains(binaryOperationInstruction.rightSrc())) {
                    binaryOperationInstruction.replaceRightSrc(newValue);
                }
                break;
            case IrBranchInstruction irBranchInstruction:
                if (valuesToReplace.contains(irBranchInstruction.conditionValue())) {
                    irBranchInstruction.replaceConditionValue(newValue);
                }
                break;
            case IrJumpInstruction _, IrIntConstantInstruction _, IrBoolConstantInstruction _:
                break;
            case IrReturnInstruction irReturnInstruction:
                if (valuesToReplace.contains(irReturnInstruction.src())) {
                    irReturnInstruction.replaceSrc(newValue);
                }
                break;
            case IrMoveInstruction irMoveInstruction:
                if (valuesToReplace.contains(irMoveInstruction.source())) {
                    irMoveInstruction.replaceSource(newValue);
                }
                break;
            case IrPhi irPhi:
                break;
            case IrUnaryOperationInstruction irUnaryOperationInstruction:
                if (valuesToReplace.contains(irUnaryOperationInstruction.src())) {
                    irUnaryOperationInstruction.replaceSrc(newValue);
                }
                break;
        }
    }

    private Optional<SSAValue> definesOperands(IrInstruction instruction, Set<SSAValue> valuesToReplace) {
        return switch (instruction) {
            case IrBinaryOperationInstruction binaryOperationInstruction ->
                    valuesToReplace.contains(binaryOperationInstruction.target())
                            ? Optional.of(binaryOperationInstruction.target()) : Optional.empty();
            case IrBoolConstantInstruction irBoolConstantInstruction ->
                    valuesToReplace.contains(irBoolConstantInstruction.target())
                            ? Optional.of(irBoolConstantInstruction.target()) : Optional.empty();
            case IrIntConstantInstruction irIntConstantInstruction ->
                    valuesToReplace.contains(irIntConstantInstruction.target()) ?
                            Optional.of(irIntConstantInstruction.target()) : Optional.empty();
            case IrMoveInstruction irMoveInstruction -> valuesToReplace.contains(irMoveInstruction.target())
                    ? Optional.of(irMoveInstruction.target()) : Optional.empty();
            case IrPhi irPhi -> valuesToReplace.contains(irPhi.target())
                    ? Optional.of(irPhi.target()) : Optional.empty();
            case IrUnaryOperationInstruction irUnaryOperationInstruction ->
                    valuesToReplace.contains(irUnaryOperationInstruction.target())
                            ? Optional.of(irUnaryOperationInstruction.target()) : Optional.empty();
            case IrBranchInstruction _, IrJumpInstruction _, IrReturnInstruction _ -> Optional.empty();
        };
    }


    private HashMap<IrBlock, Set<IrBlock>> buildDominatorTree(IrFunction function) {
        HashMap<IrBlock, Set<IrBlock>> dominators = initDominatorTree(function);

        Set<IrBlock> relevantBlocks = function.blocks()
                .stream()
                .filter(b -> b != function.startBlock())
                .collect(Collectors.toSet());

        boolean changed = true;
        while (changed) {
            changed = false;

            for (IrBlock block : relevantBlocks) {
                if (block.getPredecessorBlocks().isEmpty()) continue;

                Set<Set<IrBlock>> dominatorSet = new HashSet<>();

                for (IrBlock predecessor : block.getPredecessorBlocks()) {
                    dominatorSet.add(dominators.get(predecessor));
                }

                Set<IrBlock> newDominators = intersect(dominatorSet);
                newDominators.add(block);

                if (!setEquals(newDominators, dominators.get(block))) {
                    dominators.put(block, newDominators);
                    changed = true;
                }
            }
        }

        return dominators;
    }

    private HashMap<IrBlock, Set<IrBlock>> initDominatorTree(IrFunction function) {
        HashMap<IrBlock, Set<IrBlock>> dominators = new HashMap<>(); // Contains for each block b all other blocks that dominate b

        for (IrBlock block : function.blocks()) {
            if (block == function.startBlock()) {
                dominators.put(block, Set.of(block));
            } else {
                dominators.put(block, new HashSet<>(function.blocks()));
            }
        }

        return dominators;
    }

    private Set<IrBlock> intersect(Set<Set<IrBlock>> sets) {
        if (sets.isEmpty()) {
            return Set.of();
        } else if (sets.size() == 1) {
            return new HashSet<>(sets.stream().findFirst().get());
        } else {
            Set<IrBlock> currentSet = new HashSet<>(sets.stream().findFirst().get());
            for (Iterator<Set<IrBlock>> it = sets.stream().skip(1).iterator(); it.hasNext(); ) {
                Set<IrBlock> next = it.next();
                currentSet = intersect(currentSet, next);
            }
            return currentSet;
        }
    }

    private Set<IrBlock> intersect(Set<IrBlock> set1, Set<IrBlock> set2) {
        return set1.stream().filter(set2::contains).collect(Collectors.toSet());
    }

    private static boolean setEquals(Set<IrBlock> a, Set<IrBlock> b) {
        return a.size() == b.size() && a.containsAll(b) && b.containsAll(a);
    }

    private HashMap<IrBlock, IrBlock> getImmediateDominators(HashMap<IrBlock, Set<IrBlock>> dominators) {
        HashMap<IrBlock, IrBlock> immDominators = new HashMap<>();

        for (IrBlock block : dominators.keySet()) {
            HashSet<IrBlock> immDominatorCandidates = new HashSet<>(dominators.get(block));
            // Block can not be its own immediate dominator
            immDominatorCandidates.remove(block);

            if (immDominatorCandidates.isEmpty()) {
                continue;
            }

            for (IrBlock candidate : immDominatorCandidates) {

                // Check candidate dominates all other candidates
                boolean isImmediateDominator = true;
                for (IrBlock other : immDominatorCandidates) {
                    if (other == candidate) {
                        continue;
                    }

                    if (dominators.get(other).contains(candidate)) {
                        isImmediateDominator = false;
                        break;
                    }
                }

                if (isImmediateDominator) {
                    immDominators.put(block, candidate);
                    break;
                }
            }
        }

        return immDominators;
    }

    private HashMap<IrBlock, Set<IrBlock>> getDominanceFrontiers(IrFunction function, Map<IrBlock, IrBlock> immDominators) {
        HashMap<IrBlock, Set<IrBlock>> dominanceFrontiers = new HashMap<>();

        for (IrBlock block : function.blocks()) {
            if (block.getPredecessorBlocks().size() < 2) {
                continue;
            }

            for (IrBlock predecessor : block.getPredecessorBlocks()) {
                Optional<IrBlock> runner = Optional.of(predecessor);
                while (runner.isPresent() && immDominators.get(block) != runner.get()) {
                    dominanceFrontiers.computeIfAbsent(runner.get(), __ -> new HashSet<>())
                            .add(block);
                    if (immDominators.containsKey(runner.get())) {
                        runner = Optional.of(immDominators.get(runner.get()));
                    } else {
                        runner = Optional.empty();
                    }
                }
            }
        }

        return dominanceFrontiers;
    }
}
