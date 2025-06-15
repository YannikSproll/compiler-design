package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.Symbol;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class SSAValue {
    private final String name;
    private final @Nullable Symbol symbol;

    public SSAValue(String name, Optional<Symbol> symbol) {
        this.name = name;
        this.symbol = symbol.orElse(null);
    }

    public String name() {
        return name;
    }

    public Optional<Symbol> symbol() {
        return Optional.ofNullable(symbol);
    }
    public String formatName() {
        if (symbol == null) {
            return name;
        } else {
            return name + " (" + symbol.name() + ")";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SSAValue) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
