package edu.kit.kastel.vads.compiler.frontend.parser.type;

public sealed interface Type permits BasicType {
    String asString();
}
