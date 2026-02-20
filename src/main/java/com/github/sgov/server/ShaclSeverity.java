package com.github.sgov.server;

import lombok.Getter;
import org.apache.jena.shacl.vocabulary.SHACL;

/**
 * SHACL severity.
 */
@Getter
public enum ShaclSeverity {
    VIOLATION(SHACL.Violation.getURI()),
    WARNING(SHACL.Warning.getURI()),
    INFO(SHACL.Info.getURI());

    private final String uri;

    ShaclSeverity(final String uri) {
        this.uri = uri;
    }
}
