package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.*;

final class EntryPointAnalysis {


    void analyze(TypedFile file) {
        long numberOfEntryPoints = file.functions().stream().filter(TypedFunction::isMainFunction).count();

        if (numberOfEntryPoints == 0) {
            throw new SemanticException("No entry points found");
        }

        if (numberOfEntryPoints > 1) {
            throw new SemanticException("Multiple entry points found");
        }
    }
}
