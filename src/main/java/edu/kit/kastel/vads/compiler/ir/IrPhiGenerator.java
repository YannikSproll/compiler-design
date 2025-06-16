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
        Map<IrBlock, Set<IrBlock>> dominanceChildren = getDominanceTree(
                new HashSet<>(function.blocks()),
                immDominators);
        Map<IrBlock, Set<IrBlock>> dominanceFrontiers = getDominanceFrontiers(
                function,
                new HashSet<>(function.blocks()),
                dominanceChildren,
                immDominators);

        insertPlaceholderPhis(function, ssaVariables, dominanceFrontiers, ssaValueGenerator);
        //new IrFunctionPrinter().print(function);
        insertPhiOperands(function.startBlock(), ssaVariables);
        //new IrFunctionPrinter().print(function);
        //removeInvalidPhis(function);
        // Insert phis
        iteratePhis(function, dominanceChildren);
        //new IrFunctionPrinter().print(function);
        removeTrivialPhis(function, dominanceChildren);
        //new IrFunctionPrinter().print(function);

        return function;
    }


    private void insertPlaceholderPhis(
            IrFunction function,
            SSAVariableRenameRecording ssaVariables,
            Map<IrBlock, Set<IrBlock>> dominanceFrontiers,
            SSAValueGenerator ssaValueGenerator) {

        Map<Symbol, List<IrBlock>> defSites = new HashMap<>();
        HashSet<IrBlock> visitedBlock = new HashSet<>();
        computeDefSites(function.startBlock(), visitedBlock, defSites, ssaVariables);

        Map<Symbol, Set<IrBlock>> phiLocations = new HashMap<>();
        Map<Symbol, Queue<IrBlock>> workLists = new HashMap<>();
        Map<Symbol, Set<IrBlock>> hasAlready = new HashMap<>();

        for (Symbol symbol : defSites.keySet()) {
            phiLocations.put(symbol, new HashSet<>());
            workLists.put(symbol, new ArrayDeque<>());
            hasAlready.put(symbol, new HashSet<>());

            for (IrBlock block : defSites.get(symbol)) {
                workLists.get(symbol).add(block);
            }
        }

        for (Symbol symbol : defSites.keySet()) {
            Queue<IrBlock> symbolWorkList = workLists.get(symbol);
            while (!symbolWorkList.isEmpty()) {
                IrBlock current = symbolWorkList.poll();
                for (IrBlock frontier : dominanceFrontiers.getOrDefault(current, Set.of())) {

                    if (!hasAlready.get(symbol).contains(frontier)) {
                        phiLocations.get(symbol).add(frontier);
                        symbolWorkList.add(frontier);
                        hasAlready.get(symbol).add(frontier);
                    }
                }
            }
        }
        int x = 0;

        for (Symbol symbol : phiLocations.keySet()) {
            Set<IrBlock> blocksToInsertPhis = phiLocations.get(symbol);
            for (IrBlock block : blocksToInsertPhis) {
                IrPhi phi = new IrPhi(ssaValueGenerator.generateNewSSAValue(Optional.of(symbol)), new ArrayList<>());
                ssaVariables.introduceNewSSAValue(phi.target(), block);
                block.insertInstruction(0, phi);
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
                    HashSet<IrBlock> visitedPred = new HashSet<>();
                    Optional<SSAValue> phiSource = getLatestDefOf(predecessor, visitedPred, phiTargetSymbol, ssaVariables);
                    if (phiSource.isPresent()) {
                        phi.addPhiItem(new IrPhi.IrPhiItem(phiSource.get(), predecessor));
                    }
                }
            }
        }

        for (IrBlock successor : block.getSuccessorBlocks()) {
            insertPhiOperands(successor, visitedBlocks, ssaVariables);
        }
    }

    // TODO: Rewrite
    private Optional<SSAValue> getLatestDefOf(IrBlock block, Set<IrBlock> visitedBlocks, Symbol symbol, SSAVariableRenameRecording ssaVariables) {
        if (!visitedBlocks.add(block)){
            return Optional.empty();
        }

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
            return Optional.of(ssaValues.getLast());
        }

        HashSet<SSAValue> lastPredecessorDefs = new HashSet<>();
        for (IrBlock predecessor : block.getPredecessorBlocks()) {
            Optional<SSAValue> lastPredecessorDef = getLatestDefOf(predecessor, visitedBlocks, symbol, ssaVariables);
            lastPredecessorDef.ifPresent(lastPredecessorDefs::add);
        }

        if (lastPredecessorDefs.size() != 1) {
            //throw new IllegalArgumentException("Unexpected.");
        }

        return lastPredecessorDefs.stream().findFirst();
    }


    private void removeInvalidPhis(IrFunction function) {
        HashSet<IrBlock> visitedBlocks = new HashSet<>();
        removeInvalidPhis(function.startBlock(), visitedBlocks);
    }

    private void removeInvalidPhis(IrBlock block, HashSet<IrBlock> visitedBlocks) {
        if (!visitedBlocks.add(block)) {
            return;
        }

        Set<IrPhi> phisToRemove = new HashSet<>();
        for (IrInstruction instruction : block.getInstructions()) {
            if (instruction instanceof IrPhi phi) {
                if (phi.sources().stream().map(IrPhi.IrPhiItem::value).distinct().count() < 2) {
                    phisToRemove.add(phi);
                }
            }
        }
        phisToRemove.forEach(block::removeInstruction);

        for (IrBlock successor : block.getSuccessorBlocks()) {
            removeInvalidPhis(successor, visitedBlocks);
        }
    }


    private void iteratePhis(IrFunction function, Map<IrBlock, Set<IrBlock>> dominanceChildren) {

        Set<IrBlock> startBlockChildren = dominanceChildren.getOrDefault(function.startBlock(), Set.of());
        for (IrBlock child : startBlockChildren) {
            iteratePhis(child, dominanceChildren);
        }
    }

    private void iteratePhis(IrBlock currentBlock, Map<IrBlock, Set<IrBlock>> dominanceChildren) {

        List<IrPhi> phis = new ArrayList<>();
        for (IrInstruction instruction : currentBlock.getInstructions()) {
            if (instruction instanceof IrPhi phi) {
                phis.add(phi);
            }
        }

        for (IrPhi phi : phis) {
            replacePhiOperands(currentBlock, phi);
        }

        Set<IrBlock> blockChildren = dominanceChildren.getOrDefault(currentBlock, Set.of());
        for (IrBlock child : blockChildren) {
            iteratePhis(child, dominanceChildren);
        }
    }

    private void replacePhiOperands(IrBlock startBlock, IrPhi phi) {
        HashSet<SSAValue> operandsToReplace = phi.sources()
                .stream()
                .map(IrPhi.IrPhiItem::value)
                .collect(Collectors.toCollection(HashSet::new));

        boolean phiFound = false;
        for (IrInstruction instruction : startBlock.getInstructions()) {
            if (!phiFound) {
                 if (instruction == phi) {
                     phiFound = true;
                 }
                continue;
            }


            for (SSAValue operand : operandsToReplace) {
                HashSet<SSAValue> updateOperandsToReplace = new HashSet<>(operandsToReplace);

                if (doesInstructionDefineSSAValue(instruction, operand)) {
                    updateOperandsToReplace.remove(operand);
                } else {
                    replacePhiOperandInInstruction(instruction, phi.target(), operand, Optional.empty());
                }


                operandsToReplace = updateOperandsToReplace;
            }
        }

        HashSet<BlockVisitingPair> visitedBlocks = new HashSet<>();
        visitedBlocks.add(new BlockVisitingPair(startBlock, startBlock));
        for (IrBlock successor : startBlock.getSuccessorBlocks()) {
            replacePhiOperands(successor, startBlock, visitedBlocks, phi.target(), operandsToReplace);
        }
    }

    private void replacePhiOperands(IrBlock block, IrBlock comingFrom, HashSet<BlockVisitingPair> visitedBlocks, SSAValue newOperand, HashSet<SSAValue> operandsToReplace) {
        if (!visitedBlocks.add(new BlockVisitingPair(block, comingFrom))) {
            return;
        }

        HashSet<SSAValue> remainingOperandsToReplace = new HashSet<>(operandsToReplace);

        for (IrInstruction instruction : block.getInstructions()) {

            // This a guard that prevents phis from overriding their own operands
            if (doesInstructionDefineSSAValue(instruction, newOperand)) {
                remainingOperandsToReplace.clear();
                return;
            }

            for (SSAValue operand : remainingOperandsToReplace) {
                HashSet<SSAValue> updateOperandsToReplace = new HashSet<>(remainingOperandsToReplace);

                if (doesInstructionDefineSSAValue(instruction, operand)) {
                    updateOperandsToReplace.remove(operand);
                } else {
                    boolean replaced = replacePhiOperandInInstruction(instruction, newOperand, operand, Optional.of(comingFrom));

                    if (instruction instanceof IrPhi phi) {
                        if (replaced || phi.containsOperand(operand)) {
                            updateOperandsToReplace.remove(operand);
                        }
                    }
                }


                remainingOperandsToReplace = updateOperandsToReplace;
            }
        }

        if (remainingOperandsToReplace.isEmpty()) {
            return;
        }

        for (IrBlock successor : block.getSuccessorBlocks()) {
            replacePhiOperands(successor, block, visitedBlocks, newOperand, remainingOperandsToReplace);
        }
    }

    private boolean replacePhiOperandInInstruction(IrInstruction instruction, SSAValue newOperand, SSAValue operandToReplace, Optional<IrBlock> comingFromFilter) {
        boolean replaced = false;
        switch (instruction) {
            case IrBinaryOperationInstruction binaryOperationInstruction:
                if (operandToReplace == binaryOperationInstruction.leftSrc()) {
                    binaryOperationInstruction.replaceLeftSrc(newOperand);
                    replaced = true;
                }
                if (operandToReplace == binaryOperationInstruction.rightSrc()) {
                    binaryOperationInstruction.replaceRightSrc(newOperand);
                    replaced = true;
                }
                break;
            case IrBranchInstruction irBranchInstruction:
                if (operandToReplace == irBranchInstruction.conditionValue()) {
                    irBranchInstruction.replaceConditionValue(newOperand);
                    replaced = true;
                }
                break;
            case IrJumpInstruction _, IrIntConstantInstruction _, IrBoolConstantInstruction _:
                break;
            case IrReturnInstruction irReturnInstruction:
                if (operandToReplace == irReturnInstruction.src()) {
                    irReturnInstruction.replaceSrc(newOperand);
                    replaced = true;
                }
                break;
            case IrMoveInstruction irMoveInstruction:
                if (operandToReplace == irMoveInstruction.source()) {
                    irMoveInstruction.replaceSource(newOperand);
                    replaced = true;
                }
                break;
            case IrPhi irPhi:
                for (IrPhi.IrPhiItem phiItem : irPhi.sources()) {
                    if (comingFromFilter.isPresent() && phiItem.block() != comingFromFilter.get()) {
                        // Only replaces operand in phi if coming from the correct predecessor
                        continue;
                    }

                    if (operandToReplace == phiItem.value()) {
                        phiItem.changeValue(newOperand);
                        replaced = true;
                    }
                }
                break;
            case IrUnaryOperationInstruction irUnaryOperationInstruction:
                if (operandToReplace == irUnaryOperationInstruction.src()) {
                    irUnaryOperationInstruction.replaceSrc(newOperand);
                    replaced = true;
                }
                break;
        }
        return replaced;
    }

    private boolean doesInstructionDefineSSAValue(IrInstruction instruction, SSAValue ssaValue) {
        if (instruction instanceof IrValueProducingInstruction valueProducingInstruction) {
            return valueProducingInstruction.target() == ssaValue;
        }
        return false;
    }


    record BlockVisitingPair(IrBlock block, IrBlock comingFrom) { }

    private void removeTrivialPhis(IrFunction function, Map<IrBlock, Set<IrBlock>> dominanceChildren) {
        Set<IrBlock> startBlockChildren = dominanceChildren.getOrDefault(function.startBlock(), Set.of());
        for (IrBlock child : startBlockChildren) {
            removeTrivialPhis(child, dominanceChildren);
        }
    }

    private void removeTrivialPhis(IrBlock currentBlock, Map<IrBlock, Set<IrBlock>> dominanceChildren) {
        List<IrPhi> phis = new ArrayList<>();
        for (IrInstruction instruction : currentBlock.getInstructions()) {
            if (instruction instanceof IrPhi phi) {
                phis.add(phi);
            }
        }

        for (IrPhi phi : phis) {
            if (phi.isTrivialPhi()) {
                currentBlock.removeInstruction(phi);
                Optional<SSAValue> singlePhiOperand = phi.getTrivialOperandOrThrow();
                if (singlePhiOperand.isPresent()) {
                    HashSet<IrBlock> visitedBlocks = new HashSet<>();
                    replaceAllOperandOccurrences(currentBlock, visitedBlocks, singlePhiOperand.get(), phi.target());
                }
            }
        }

        Set<IrBlock> blockChildren = dominanceChildren.getOrDefault(currentBlock, Set.of());
        for (IrBlock child : blockChildren) {
            iteratePhis(child, dominanceChildren);
        }
    }

    private void replaceAllOperandOccurrences(IrBlock currentBlock, Set<IrBlock> visitedBlocks, SSAValue newOperand, SSAValue operandToReplace) {
        if (!visitedBlocks.add(currentBlock)) {
            return;
        }

        for (IrInstruction instruction : currentBlock.getInstructions()) {
            replacePhiOperandInInstruction(instruction, newOperand, operandToReplace, Optional.empty());
        }

        for (IrBlock successor : currentBlock.getSuccessorBlocks()) {
            replaceAllOperandOccurrences(successor, visitedBlocks, newOperand, operandToReplace);
        }
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

    private Map<IrBlock, Set<IrBlock>> getDominanceTree(Set<IrBlock> allBlocks, HashMap<IrBlock, IrBlock> immDominators) {
        Map<IrBlock, Set<IrBlock>> domTreeChildren = new HashMap<>();
        for (IrBlock block : allBlocks) { // Assuming you have a list of all blocks
            if (immDominators.containsKey(block)) {
                IrBlock idom = immDominators.get(block); // immDominators is your IDom map
                domTreeChildren.computeIfAbsent(idom, __ -> new HashSet<>()).add(block);
            }
        }
        return domTreeChildren;
    }

    private Map<IrBlock, Set<IrBlock>> getDominanceFrontiers(
            IrFunction function,
            Set<IrBlock> allBlocks,
            Map<IrBlock, Set<IrBlock>> dominatorTree,
            Map<IrBlock, IrBlock> immDominators) {
        Map<IrBlock, Set<IrBlock>> dominanceFrontiers = new HashMap<>();
        for (IrBlock block : allBlocks) {
            dominanceFrontiers.put(block, new HashSet<>());
        }

        // Step 1: Determine the processing order (post-order traversal of the dominator tree).
        // This ensures that when we process a node 'n', the DF of its dominator tree children
        // have already been computed.
        List<IrBlock> orderedBlocks = getPostOrderDominatorTreeTraversal(allBlocks, dominatorTree, immDominators);

        // Step 2: Iterate through blocks in the computed order and apply the two rules.
        for (IrBlock n : orderedBlocks) {

            // Rule 1: Add nodes to DF(n) based on CFG edges.
            // These are successors 'y' of 'n' where 'n's dominance "breaks out".
            for (IrBlock y : n.getSuccessorBlocks()) {
                if (!immDominators.containsKey(y) || !immDominators.get(y).equals(n)) { // If 'n' is NOT the immediate dominator of 'y'
                    dominanceFrontiers.get(n).add(y);
                }
            }

            // Rule 2: Propagate dominance frontiers from children in the dominator tree.
            // For each child 'c' of 'n' in the dominator tree, if 'w' is in DF(c)
            // and 'n' does not strictly dominate 'w', then 'w' is also in DF(n).
            for (IrBlock c : dominatorTree.getOrDefault(n, Collections.emptySet())) {
                for (IrBlock w : dominanceFrontiers.get(c)) {
                    if (!strictlyDominates(n, w, immDominators)) {
                        dominanceFrontiers.get(n).add(w);
                    }
                }
            }
        }
        return dominanceFrontiers;
    }

    private boolean strictlyDominates(IrBlock d, IrBlock n, Map<IrBlock, IrBlock> immDominators) {
        if (d.equals(n)) { // A node does not strictly dominate itself
            return false;
        }
        IrBlock current = n;
        // Traverse up the immediate dominator chain from 'n'
        while (current != null && !current.equals(d)) {
            current = immDominators.get(current);
        }
        // If 'current' became 'd', then 'd' was found on the path from entry to 'n' (excluding 'n')
        return current != null && current.equals(d);
    }

    private List<IrBlock> getPostOrderDominatorTreeTraversal(Set<IrBlock> allBlocks, Map<IrBlock, Set<IrBlock>> domTreeChildren, Map<IrBlock, IrBlock> immDominators) {
        List<IrBlock> order = new ArrayList<>();
        Set<IrBlock> visited = new HashSet<>();

        // Find the entry block (root of the dominator tree)
        IrBlock entryBlock = null;
        for (Map.Entry<IrBlock, IrBlock> entry : immDominators.entrySet()) {
            if (entry.getValue() == null) { // Block with no immediate dominator is the entry
                entryBlock = entry.getKey();
                break;
            }
        }

        // Handle cases where an explicit entry might not be directly linked to null,
        // or for disconnected graphs (though a CFG usually has one entry).
        // Iterate all blocks to ensure all components of the dominator tree are visited.
        for (IrBlock block : allBlocks) {
            if (!visited.contains(block)) {
                postOrderDFS(block, domTreeChildren, visited, order);
            }
        }

        return order;
    }

    private void postOrderDFS(IrBlock node, Map<IrBlock, Set<IrBlock>> domTreeChildren,
                              Set<IrBlock> visited, List<IrBlock> order) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);

        // Recursively visit all children in the dominator tree
        for (IrBlock child : domTreeChildren.getOrDefault(node, Collections.emptySet())) {
            postOrderDFS(child, domTreeChildren, visited, order);
        }
        // Add the node to the order list AFTER visiting all its children (post-order)
        order.add(node);
    }
}
