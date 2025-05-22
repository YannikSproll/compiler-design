package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.backend.aasm.*;
import edu.kit.kastel.vads.compiler.ir.IrGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CodeGenerationStep {

    public void run(List<IrGraph> graphs, CodeGenerationContext codeGenerationContext) throws IOException {
        int gccExitCode = 0;
        try {
            StringBuilder codeBuilder = new StringBuilder();
            X86InstructionGenerator instructionGenerator = new X86InstructionGenerator(codeBuilder);
            CodeGenerator codeGenerator = new DebugCodeGeneratorDecorator(new X86Bit64CodeGenerator(instructionGenerator));

            DefaultNodeSequenceAnalysis sequenceAnalysis = new DefaultNodeSequenceAnalysis();
            InstructionSelector instructionSelector = new InstructionSelector(sequenceAnalysis);
            instructionSelector.generateCode(graphs, codeGenerator, codeGenerationContext.runInfo().sourceFilePath().toString());
            String s = codeBuilder.toString();

            String fileName = codeGenerationContext.runInfo().sourceFilePath().getFileName().toString() + ".s";
            String assemblyFilePath = codeGenerationContext.runInfo().sourceFilePath().resolveSibling(fileName).toString();
            Path assemblyPath = Path.of(assemblyFilePath);
            Files.writeString(assemblyPath, s);

            gccExitCode = generateExecutable(assemblyPath, codeGenerationContext.runInfo().outputFilePath());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        if (gccExitCode != 0) {
            System.exit(gccExitCode);
        }
    }

    private static int generateExecutable(Path assemblyFilePath, Path outputFilePath) throws IOException, InterruptedException {
        Process gccProcess = new ProcessBuilder(new String[] {"gcc", "-g", "-o", outputFilePath.toString(), assemblyFilePath.toString()})
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

        gccProcess.waitFor();
        return gccProcess.exitValue();
    }
}
