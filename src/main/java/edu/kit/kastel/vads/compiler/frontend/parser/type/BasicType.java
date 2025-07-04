package edu.kit.kastel.vads.compiler.frontend.parser.type;

import java.util.Locale;

public enum BasicType implements Type {
    INT,
    BOOL;

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
