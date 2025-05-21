package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.frontend.lexer.Lexer;
import edu.kit.kastel.vads.compiler.frontend.parser.ParseException;
import edu.kit.kastel.vads.compiler.frontend.parser.Parser;
import edu.kit.kastel.vads.compiler.frontend.parser.TokenSource;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;

import java.io.IOException;
import java.nio.file.Files;

public final class ParseAndLexStep {

    public ProgramTree run(CompilerPipelineRunInfo runInfo) throws IOException {
        try {
            Lexer lexer = Lexer.forString(Files.readString(runInfo.sourceFilePath()));
            TokenSource tokenSource = new TokenSource(lexer);
            Parser parser = new Parser(tokenSource);
            return parser.parseProgram();
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(42);
            throw new AssertionError("unreachable");
        }
    }
}
