package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class SSAValue {
    private final String name;
    private final IrType type;
    private final @Nullable Symbol symbol;

    public SSAValue(String name, IrType type, Optional<Symbol> symbol) {
        this.name = name;
        this.type = type;
        this.symbol = symbol.orElse(null);
    }

    public String name() {
        return name;
    }

    public Optional<Symbol> symbol() {
        return Optional.ofNullable(symbol);
    }

    public IrType type() { return type; }

    public String formatName() {
        if (symbol == null) {
            return name + ":" + type;
        } else {
            return name + ":" + type + " (" + symbol.name() + ")";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SSAValue) obj;
        return Objects.equals(this.name, that.name)
                && Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        if (symbol == null) {
            return "SSAValue[" +
                    "name=" + name + ']';
        } else {
            return "SSAValue[" +
                    "name=" + name + "; symbol=" + symbol.name() + ']';
        }

    }

}
