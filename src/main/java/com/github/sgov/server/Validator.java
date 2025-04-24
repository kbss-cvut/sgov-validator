package com.github.sgov.server;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.progress.NullProgressMonitor;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.rules.RuleUtil;
import org.topbraid.shacl.validation.ResourceValidationReport;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("MissingJavadocType")
public class Validator {

    private final Model shapesModel;
    private final Model mappingModel;

    /**
     * Validator constructor.
     */
    public Validator() {

        // inference rules
        this.shapesModel = ModelFactory.createDefaultModel();
        shapesModel.read(
                com.github.sgov.server.Validator.class.getResourceAsStream("/inference-rules.ttl"),
                null,
                FileUtils.langTurtle);

        // mapping Z-SGoV to UFO
        this.mappingModel = ModelFactory.createDefaultModel();
        mappingModel.read(
                com.github.sgov.server.Validator.class.getResourceAsStream("/z-sgov-mapping.ttl"),
                null,
                FileUtils.langTurtle);
    }

    /**
     * Validates the given model with vocabulary data (glossaries, models) against the built-in ruleset.
     *
     * @param dataModel Model with data to validate
     * @param language  Language of data, used by some rules
     * @return Validation report
     */
    public ValidationReport validate(Model dataModel, String language) {
        final Model shapesModel = getRulesModel(
                Stream.concat(ValidationRules.glossaryRules(language).stream(),
                              Stream.concat(ValidationRules.modelRules(language).stream(),
                                            ValidationRules.vocabularyRules(language).stream()))
                      .map(Rule::content)
                      .collect(Collectors.toSet()));
        return validate(dataModel, shapesModel);
    }

    /**
     * Validates the given model with vocabulary data (glossaries, models) against the given shapes model containing
     * validation rules and the default inference rules provided by this validator.
     *
     * @param dataModel   Model with data to validate
     * @param shapesModel Model with validation rules
     * @return Validation report
     */
    public ValidationReport validate(Model dataModel, Model shapesModel) {
        shapesModel.add(this.shapesModel);

        dataModel.add(mappingModel);

        final Model inferredModel = RuleUtil.executeRules(dataModel, shapesModel, null,
                                                          new NullProgressMonitor());
        dataModel.add(inferredModel);

        final Resource report = ValidationUtil.validateModel(dataModel, shapesModel, true);

        return new ResourceValidationReport(report);
    }

    private static Model getRulesModel(final Collection<String> rules) {
        final Model shapesModel = JenaUtil.createMemoryModel();
        for (String r : rules) {
            shapesModel.read(new ByteArrayInputStream(r.getBytes(StandardCharsets.UTF_8)), null, FileUtils.langTurtle);
        }
        return shapesModel;
    }
}
