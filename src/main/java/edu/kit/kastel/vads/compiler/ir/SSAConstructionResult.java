package edu.kit.kastel.vads.compiler.ir;

public interface SSAConstructionResult {

    default IrFile asFile() { throw new IllegalStateException("SSAConstructionResult is not an ir file"); }
    default IrFunction asFunction() { throw new IllegalStateException("SSAConstructionResult is not a function"); }
    default SSAValue asSSAValue() { throw new IllegalStateException("SSAConstructionResult is not a SSAValue"); }
    default TerminationType asTerminationType() { throw new IllegalStateException("SSAConstructionResult is not a TerminationType"); }

    static SSAConstructionResult ssaValue(SSAValue value) {
        return new SSAValueResult(value);
    }

    static SSAConstructionResult file(IrFile file) {
        return new FileResult(file);
    }

    static SSAConstructionResult function(IrFunction value) {
        return new FunctionResult(value);
    }

    static SSAConstructionResult statement() {
        return new StatementResult(TerminationType.NONE);
    }

    static SSAConstructionResult statement(TerminationType terminationType) {
        return new StatementResult(terminationType);
    }

    record StatementResult(TerminationType terminationType) implements SSAConstructionResult {
        @Override
        public TerminationType asTerminationType() {
            return terminationType;
        }
    }

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

    enum TerminationType {
        NONE,
        WEAK,
        STRONG;

        TerminationType merge(TerminationType other) {
            return switch (this) {
                case STRONG -> other;
                case WEAK -> other == NONE ? NONE : WEAK;
                case NONE -> NONE;
            };
        }
    }
}
