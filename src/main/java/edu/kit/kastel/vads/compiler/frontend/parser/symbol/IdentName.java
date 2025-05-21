package edu.kit.kastel.vads.compiler.frontend.parser.symbol;

record IdentName(String identifier) implements Name {
    @Override
    public String asString() {
        return identifier();
    }
}
