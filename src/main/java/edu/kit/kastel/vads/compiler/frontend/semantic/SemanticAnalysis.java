package edu.kit.kastel.vads.compiler.frontend.semantic;

import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.RecursivePostorderPreVisitor;
import edu.kit.kastel.vads.compiler.frontend.parser.visitor.RecursivePostorderVisitor;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.TypedFile;

public class SemanticAnalysis {

    private final ProgramTree program;

    public SemanticAnalysis(ProgramTree program) {
        this.program = program;
    }

    public void analyze() {
        this.program.accept(new RecursivePostorderVisitor<>(new IntegerLiteralRangeAnalysis()), new Namespace<>());
        //this.program.accept(new RecursivePostorderVisitor<>(new VariableStatusAnalysis()), new Namespace<>());
        this.program.accept(new RecursivePostorderVisitor<>(new ReturnAnalysis()), new ReturnAnalysis.ReturnState());
        Elaborator elaborator = new Elaborator();
        TypedFile typedFile = elaborator.elaborate(program);
        //this.program.accept(new RecursivePostorderPreVisitor<>(new TypeCheckingAnalysis()), new TypeCheckingAnalysis.ScopeStack());
    }

}
