package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.HirType;

public enum IrType {
    BOOL,
    I32;

    static IrType from(HirType type) {
        return switch (type) {
            case BOOLEAN -> IrType.BOOL;
            case INT -> IrType.I32;
            case INVALID -> throw new IllegalArgumentException("Can not convert invalid type into ir type");
        };
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
