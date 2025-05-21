package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.backend.aasm.CodeGenerator;
import edu.kit.kastel.vads.compiler.ir.IrGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CodeGenerationStep {

    public void run(List<IrGraph> graphs, CompilerPipelineRunInfo runInfo) throws IOException {
        int gccExitCode = 0;
        try {
            String s = new CodeGenerator().generateCode(graphs);

            String fileName = runInfo.sourceFilePath().getFileName().toString() + ".s";
            String assemblyFilePath = runInfo.sourceFilePath().resolveSibling(fileName).toString();
            Path assemblyPath = Path.of(assemblyFilePath);
            Files.writeString(assemblyPath, s);

            gccExitCode = generateExecutable(assemblyPath, runInfo.outputFilePath());
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
