package edu.kit.kastel.vads.compiler.frontend.semantic.hir;

import edu.kit.kastel.vads.compiler.Span;

public record Symbol(
        String name,
        HirType type,
        Span declaredAt) { }
