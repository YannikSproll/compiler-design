package edu.kit.kastel.vads.compiler.ir.ValueProducingInstructions;

import edu.kit.kastel.vads.compiler.ir.IrBlock;
import edu.kit.kastel.vads.compiler.ir.SSAValue;

import java.util.List;
import java.util.Objects;

public record IrPhi(
        SSAValue target,
        List<IrPhiItem> sources) implements IrValueProducingInstruction {

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
