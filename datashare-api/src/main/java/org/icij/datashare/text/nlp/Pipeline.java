package org.icij.datashare.text.nlp;

import org.icij.datashare.reflect.EnumTypeToken;
import org.icij.datashare.text.Language;
import org.icij.datashare.text.NamedEntity;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static org.icij.datashare.function.ThrowingFunctions.joinComma;
import static org.icij.datashare.text.NamedEntity.Category.*;
import static org.icij.datashare.text.nlp.NlpStage.NER;


public interface Pipeline {
    enum Type implements EnumTypeToken {
        CORENLP,
        GATENLP,
        IXAPIPE,
        MITIE,
        OPENNLP;

        private final String className;

        Type() { className = buildClassName(Pipeline.class, this); }

        @Override
        public String getClassName() { return className; }

        public static Optional<Type> parse(final String valueName) {
            return EnumTypeToken.parse(Type.class, valueName);
        }

        public static Optional<Type> fromClassName(final String className) {
            return EnumTypeToken.parseClassName(Pipeline.class, Type.class, className);
        }

        public static Pipeline.Type[] parseAll(final String comaSeparatedTypes) {
            return stream(comaSeparatedTypes.split(",")).map(Type::valueOf).toArray(Type[]::new);
        }
    }

    enum Property {
        STAGES,
        ENTITIES,
        CACHING,
        LANGUAGE,
        ENCODING;

        public String getName() {
            return name().toLowerCase().replace('_', '-');
        }

        public static Function<List<NlpStage>, Function<List<NamedEntity.Category>, Function<Boolean, Properties>>>
                build =
                nlpStages -> entityCategories -> enableCaching -> {
                    Properties properties = new Properties();
                    properties.setProperty(STAGES.getName(),   joinComma.apply(nlpStages));
                    properties.setProperty(ENTITIES.getName(), joinComma.apply(entityCategories));
                    properties.setProperty(CACHING.getName(),  String.valueOf(enableCaching));
                    return properties;
                };
    }

    Charset DEFAULT_ENCODING = UTF_8;
    List<NlpStage> DEFAULT_TARGET_STAGES = singletonList(NER);
    List<NamedEntity.Category> DEFAULT_ENTITIES = asList(PERSON, ORGANIZATION, LOCATION);
    boolean DEFAULT_CACHING = true;

    Type getType();

    boolean initialize(Language language) throws InterruptedException;
    Annotations process(String content, String docId, Language language) throws InterruptedException;
    void terminate(Language language) throws InterruptedException ;

    /**
     * Is stage supported for language?
     *
     * @param stage     the stage to test for support
     * @param language  the language on which stage is tested
     * @return true if stage supports language; false otherwise
     */
    boolean supports(NlpStage stage, Language language);

    /**
     * @return the list of all targeted named entity categories
     */
    List<NamedEntity.Category> getTargetEntities();

    /**
     * @return the list of all involved stages
     */
    List<NlpStage> getStages();

    /**
     * @return true if pipeline is caching annotators; false otherwise
     */
    boolean isCaching();

    /**
     * @return the list of all involved stages
     */
    Charset getEncoding();

    /**
     * @return the tagset used by the part-of-speech tagger
     */
    Optional<String> getPosTagSet(Language language);

}
