package edu.kit.kastel.vads.compiler.pipeline;

import edu.kit.kastel.vads.compiler.frontend.semantic.hir.TypedFile;
import edu.kit.kastel.vads.compiler.ir.IrFile;
import edu.kit.kastel.vads.compiler.ir.IrFunctionPrinter;
import edu.kit.kastel.vads.compiler.ir.SsaConstruction;

public class IRStep {

    public IrFile run(TypedFile typedFile) {
        SsaConstruction construction = new SsaConstruction();
        IrFile irFile = construction.generateIr(typedFile);
        new IrFunctionPrinter().print(irFile);

        return irFile;
    }
}
