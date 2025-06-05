package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.TypeChecker;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.TypedFile;

public class SemanticAnalysis {

    private final ProgramTree program;

    public SemanticAnalysis(ProgramTree program) {
        this.program = program;
    }

    public void analyze() {
        //this.program.accept(new RecursivePostorderVisitor<>(new IntegerLiteralRangeAnalysis()), new Namespace<>());
        //this.program.accept(new RecursivePostorderVisitor<>(new VariableStatusAnalysis()), new Namespace<>());
        //this.program.accept(new RecursivePostorderVisitor<>(new ReturnAnalysis()), new ReturnAnalysis.ReturnState());
        TypeChecker typeChecker = new TypeChecker();
        Elaborator elaborator = new Elaborator(typeChecker);
        TypedFile typedFile = elaborator.elaborate(program);
        typedFile.accept(new TypedPostorderVisitor<>(new ReturnAnalysis()), new ReturnAnalysis.ReturnState());
        typedFile.accept(new VariableDefinitionAnalysis(), new VariableDefinitionAnalysis.VariableDefinitionContext());
    }
}
