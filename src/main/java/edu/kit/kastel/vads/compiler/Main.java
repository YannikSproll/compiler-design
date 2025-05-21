package edu.kit.kastel.vads.compiler;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.util.YCompPrinter;
import edu.kit.kastel.vads.compiler.frontend.parser.ParseException;
import edu.kit.kastel.vads.compiler.pipeline.CompilerPipeline;
import edu.kit.kastel.vads.compiler.pipeline.CompilerPipelineRunInfo;
import edu.kit.kastel.vads.compiler.frontend.semantic.SemanticException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Invalid arguments: Expected one input file and one output file");
            System.exit(3);
        }
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);

        CompilerPipeline pipeline = new CompilerPipeline();
        CompilerPipelineRunInfo runInfo = new CompilerPipelineRunInfo(input, output);
        try {
            pipeline.run(runInfo);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(42);
            throw new AssertionError("unreachable");
        } catch (SemanticException e) {
            e.printStackTrace();
            System.exit(7);
            throw new AssertionError("unreachable");
        }
    }

    private static void dumpGraph(IrGraph graph, Path path, String key) throws IOException {
        Files.writeString(
            path.resolve(graph.name() + "-" + key + ".vcg"),
            YCompPrinter.print(graph)
        );
    }
}
