package com.github.sgov.server;

import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shacl.validation.ReportEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidationResultSeverityComparatorTest {

    private ReportEntry mockWithSeverity(final String severityUri) {
        final ReportEntry result1 = ReportEntry.create();
        result1.severity(ResourceFactory.createResource(severityUri).asNode());
        result1.source(ResourceFactory.createResource().asNode());
        return result1;
    }

    private void testEquals(final String severityUri1, final String severityUri2) {
        ReportEntry result1 = mockWithSeverity(severityUri1);
        ReportEntry result2 = mockWithSeverity(severityUri2);
        result2.source(result1.source());
        Assertions.assertEquals( 0, new ValidationResultSeverityComparator().compare(result1,result2) );
    }

    private void testGreater(final String severityUri1, final String severityUri2) {
        ReportEntry result1 = mockWithSeverity(severityUri1);
        ReportEntry result2 = mockWithSeverity(severityUri2);
        Assertions.assertTrue( new ValidationResultSeverityComparator().compare(result1,result2) > 0);
    }

    @Test
    public void compareComparesCorrectlySameValue() {
        testEquals(
            ShaclSeverity.VIOLATION.getUri(),
            ShaclSeverity.VIOLATION.getUri());

        testEquals(
            ShaclSeverity.WARNING.getUri(),
            ShaclSeverity.WARNING.getUri());

        testEquals(
            ShaclSeverity.INFO.getUri(),
            ShaclSeverity.INFO.getUri());
    }

    @Test
    public void compareComparesCorrectlyLessThanValue() {
        testGreater(
            ShaclSeverity.WARNING.getUri(),
            ShaclSeverity.VIOLATION.getUri()
        );

        testGreater(
            ShaclSeverity.INFO.getUri(),
            ShaclSeverity.WARNING.getUri()
        );

        testGreater(
            ShaclSeverity.INFO.getUri(),
            ShaclSeverity.VIOLATION.getUri()
        );
    }

    @Test
    public void compareComparesCorrectlyGreaterThanValue() {
        testGreater(
            ShaclSeverity.WARNING.getUri(),
            ShaclSeverity.VIOLATION.getUri()
        );

        testGreater(
            ShaclSeverity.INFO.getUri(),
            ShaclSeverity.WARNING.getUri()
        );

        testGreater(
            ShaclSeverity.INFO.getUri(),
            ShaclSeverity.VIOLATION.getUri()
        );
    }
}
