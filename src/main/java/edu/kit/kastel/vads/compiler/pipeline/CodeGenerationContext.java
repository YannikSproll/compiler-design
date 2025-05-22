package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;

public record CodeGenerationContext(ProgramTree ast, CompilerPipelineRunInfo runInfo) { }
