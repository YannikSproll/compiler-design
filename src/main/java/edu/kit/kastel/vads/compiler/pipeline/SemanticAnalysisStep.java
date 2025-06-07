package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.frontend.semantic.SemanticAnalysis;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.TypedFile;

public class SemanticAnalysisStep {

    public TypedFile run(ProgramTree ast) {

        return new SemanticAnalysis(ast).analyze();
    }
}
