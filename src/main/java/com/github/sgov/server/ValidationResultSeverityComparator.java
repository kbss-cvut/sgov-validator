package com.github.sgov.server;

import org.apache.jena.shacl.validation.ReportEntry;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@SuppressWarnings("MissingJavadocType")
public class ValidationResultSeverityComparator implements Comparator<ReportEntry> {

    private static ShaclSeverity of(final ReportEntry result) {
        final Optional<ShaclSeverity> severity
            = Arrays.stream(ShaclSeverity.values()).filter(s ->
            s.getUri().equals(result.severity().level().getURI())
        ).findFirst();
        return severity.orElse(null);
    }

    /**
     * Compares results based on the severity.
     *
     * @param res1 first validation result
     * @param res2 second validation result
     * @return negative, 0, positive as per comparison contract
     */
    public int compare(ReportEntry res1, ReportEntry res2) {
        return ValidationResultSeverityComparator.of(res1).compareTo(
            ValidationResultSeverityComparator.of(res2)
        );
    }
}
