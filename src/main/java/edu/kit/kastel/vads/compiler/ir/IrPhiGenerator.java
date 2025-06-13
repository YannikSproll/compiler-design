package edu.kit.kastel.vads.compiler.ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class IrPhiGenerator {

    public IrFunction addPhis(IrFunction function) {

        HashMap<IrBlock, Set<IrBlock>> dominators = buildDominatorTree(function);
        HashMap<IrBlock, IrBlock> immDominators = getImmediateDominators(dominators);
        // Compute dominance frontiers
        // Find phi candidates
        // Insert phis

        return function;
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
}
