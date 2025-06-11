package edu.kit.kastel.vads.compiler.ir;

public interface SSAConstructionResult {

    default IrFile asFile() { throw new IllegalStateException("SSAConstructionResult is not an ir file"); }
    default IrFunction asFunction() { throw new IllegalStateException("SSAConstructionResult is not a function"); }
    default SSAValue asSSAValue() { throw new IllegalStateException("SSAConstructionResult is not a SSAValue"); }

    static SSAConstructionResult ssaValue(SSAValue value) {
        return new SSAValueResult(value);
    }

    static SSAConstructionResult file(IrFile file) {
        return new FileResult(file);
    }

    static SSAConstructionResult function(IrFunction value) {
        return new FunctionResult(value);
    }

    static SSAConstructionResult empty() {
        return new EmptyResult();
    }

    record EmptyResult () implements SSAConstructionResult { }

    record FileResult(IrFile file) implements SSAConstructionResult {
        @Override
        public IrFile asFile() {
            return file;
        }
    }

    record FunctionResult(IrFunction value) implements SSAConstructionResult {
        @Override
        public IrFunction asFunction() {
            return value;
        }
    }

    record SSAValueResult(SSAValue value) implements SSAConstructionResult {
        @Override
        public SSAValue asSSAValue() {
            return value;
        }
    }
}
