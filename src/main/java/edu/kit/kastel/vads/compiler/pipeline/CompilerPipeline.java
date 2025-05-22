package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;

import java.io.IOException;
import java.util.List;

public final class CompilerPipeline {
    private final ParseAndLexStep parseAndLexStep;
    private final SemanticAnalysisStep semanticAnalysisStep;
    private final IRStep irStep;
    private final CodeGenerationStep codeGenerationStep;

    public CompilerPipeline() {
        parseAndLexStep = new ParseAndLexStep();
        semanticAnalysisStep = new SemanticAnalysisStep();
        irStep = new IRStep();
        codeGenerationStep = new CodeGenerationStep();
    }

    public void run(CompilerPipelineRunInfo runInfo) throws IOException {
        ProgramTree ast = parseAndLexStep.run(runInfo);

        semanticAnalysisStep.run(ast);

        List<IrGraph> irGraphs = irStep.run(ast);

        CodeGenerationContext codeGenerationContext = new CodeGenerationContext(ast, runInfo);
        codeGenerationStep.run(irGraphs, codeGenerationContext);
    }
}
