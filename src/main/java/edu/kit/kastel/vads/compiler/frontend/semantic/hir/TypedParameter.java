package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record TypedParameter(Symbol symbol, HirType type, Span span) implements TypedObject {
}
