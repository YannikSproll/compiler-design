package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.backend.aasm.LivenessAnalysis;
import edu.kit.kastel.vads.compiler.frontend.semantic.hir.TypedFile;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.SsaTranslation;
import edu.kit.kastel.vads.compiler.ir.data.*;
import edu.kit.kastel.vads.compiler.ir.optimize.LocalValueNumbering;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.frontend.parser.ast.ProgramTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IRStep {
    public List<IrGraph> run(ProgramTree ast) {
        List<IrGraph> graphs = new ArrayList<>();
        for (FunctionTree function : ast.topLevelTrees()) {
            SsaTranslation translation = new SsaTranslation(function, new LocalValueNumbering());
            graphs.add(translation.translate());
        }

        return Collections.unmodifiableList(graphs);
    }

    public void run(TypedFile typedFile) {
        SsaConstruction construction = new SsaConstruction();
        IrFile irFile = construction.generateIr(typedFile);
        new IrFunctionPrinter().print(irFile);

        LivenessAnalysis la = new LivenessAnalysis();
        for (IrFunction function : irFile.functions()) {
            la.run(function);
        }
    }
}
