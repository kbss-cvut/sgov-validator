package com.github.sgov.server;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Derivation;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.util.FileUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("MissingJavadocType")
public class Validator {

    private final List<org.apache.jena.reasoner.rulesys.Rule> rules;
    private final Model mappingModel;

    /**
     * Validator constructor.
     */
    public Validator() {
        this.rules = org.apache.jena.reasoner.rulesys.Rule.parseRules(readRules());
        // mapping Z-SGoV to UFO
        mappingModel = ModelFactory.createDefaultModel();
        mappingModel.read(
                com.github.sgov.server.Validator.class.getResourceAsStream("/z-sgov-mapping.ttl"),
                null,
                FileUtils.langTurtle);
    }

    private static String readRules() {
        try (InputStream is = Validator.class.getClassLoader()
                                             .getResourceAsStream("jena-inference-rules.rules")) {
            assert is != null;
            return new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read inference rules.", e);
        }
    }

    /**
     * Validates the given model with vocabulary data (glossaries, models) against the built-in ruleset.
     *
     * @param dataModel Model with data to validate
     * @param language  Language of data, used by some rules
     * @return Validation report
     */
    public ValidationReport validate(Model dataModel, String language) {
        final Model shapesModel = getRulesModel(ValidationRules.rules(language).stream().map(Rule::content)
                                                               .collect(Collectors.toSet()));
        final ValidationReport result = validate(dataModel, shapesModel);
        shapesModel.close();
        return result;
    }

    /**
     * Validates the given model with vocabulary data (glossaries, models) against the given shapes model containing
     * validation rules and the default inference rules provided by this validator.
     *
     * @param dataModel   model with data to validate
     * @param shapesModel model with validation rules
     * @return validation report
     */
    public ValidationReport validate(final Model dataModel, final Model shapesModel) {
        final Shapes shapes = Shapes.parse(shapesModel);

        dataModel.add(mappingModel);

        final GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        final Model inferredModel = ModelFactory.createInfModel(reasoner, dataModel);

        final ValidationReport report = ShaclValidator.get().validate(shapes, inferredModel.getGraph());
        inferredModel.close();
        return report;
    }

    /**
     * Gets the derivation of the specified statement in the specified data model, under the inference rules applied by
     * this validator.
     *
     * @param dataModel Model with base data
     * @param statement Statement whose derivation trace to get
     * @return String representation of the derivation trace
     */
    public String getDerivation(final Model dataModel, final Statement statement) {
        dataModel.add(mappingModel);

        final GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setDerivationLogging(true);
        final InfModel inferredModel = ModelFactory.createInfModel(reasoner, dataModel);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final PrintWriter out = new PrintWriter(bos);
        final Iterator<Derivation> it = inferredModel.getDerivation(statement);
        while (it.hasNext()) {
            final Derivation d = it.next();
            d.printTrace(out, true);
        }
        out.flush();
        inferredModel.close();
        return bos.toString(StandardCharsets.UTF_8);
    }

    private static Model getRulesModel(final Collection<String> rules) {
        final Model shapesModel = ModelFactory.createDefaultModel();
        for (String r : rules) {
            shapesModel.read(new ByteArrayInputStream(r.getBytes(StandardCharsets.UTF_8)), null, FileUtils.langTurtle);
        }
        return shapesModel;
    }
}
