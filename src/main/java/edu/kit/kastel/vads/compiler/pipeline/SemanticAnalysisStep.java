package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.frontend.semantic.SemanticAnalysis;

public class SemanticAnalysisStep {

    public void run(ProgramTree ast) {

        new SemanticAnalysis(ast).analyze();
    }
}
