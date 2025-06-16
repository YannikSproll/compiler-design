package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.IrBlock;
import edu.kit.kastel.vads.compiler.ir.SSAValue;

import java.util.*;
import java.util.stream.Collectors;

public record IrPhi(
        SSAValue target,
        List<IrPhiItem> sources) implements IrValueProducingInstruction {

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }

    public boolean isTrivialPhi() {
        return !sources.isEmpty()
                && sources.stream().map(s -> s.value).distinct().count() == 1;
    }

    public Optional<SSAValue> getTrivialOperandOrThrow() {
        if (sources.isEmpty()) {
            return Optional.empty();
        }

        Set<SSAValue> values = sources.stream().map(s -> s.value).distinct().collect(Collectors.toSet());
        if (values.size() >= 2) {
            throw new IllegalStateException("Can not determine single phi operand of non-trivial phi");
        }

        return values.stream().findAny();
    }

    public boolean containsOperand(SSAValue ssaValue) {
        return sources.stream().anyMatch(x -> x.value() == ssaValue);
    }

    public static final class IrPhiItem {
        private SSAValue value;
        private final IrBlock block;

        public IrPhiItem(SSAValue value, IrBlock block) {
            this.value = value;
            this.block = block;
        }

        public SSAValue value() {
            return value;
        }

        public void changeValue(SSAValue value) {
            this.value = value;
        }

        public IrBlock block() {
            return block;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (IrPhiItem) obj;
            return Objects.equals(this.value, that.value) &&
                    Objects.equals(this.block, that.block);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, block);
        }

        @Override
        public String toString() {
            return "IrPhiItem[" +
                    "value=" + value + ", " +
                    "block=" + block + ']';
        }

        }

    public void addPhiItem(IrPhiItem item) {
        sources.add(item);
    }
}
