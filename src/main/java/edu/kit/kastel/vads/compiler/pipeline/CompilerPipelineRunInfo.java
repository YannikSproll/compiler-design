package edu.kit.kastel.vads.compiler.pipeline;

import java.nio.file.Path;

public record CompilerPipelineRunInfo(Path sourceFilePath, Path outputFilePath) {  }
