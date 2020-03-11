package org.bmsource.dwh.importer.importer.web;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.*;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bmsource.dwh.common.utils.BeanUtils;

@JsonAutoDetect(getterVisibility = Visibility.NONE)
public class MappingResponse {

  static Source builder() {
    return new Builder();
  }

  public interface Source {

    Destination setSourceFields(List<String> fields, List<Object> values);
  }

  public interface Destination {

    Build setFactModel(Class fact);
  }

  public interface Build {

    Build autoSuggestionMapping();

    MappingResponse build();
  }

  public static class Builder implements Source, Destination, Build {

    private Map<String, Object> inputFields = new LinkedHashMap<>();
    private Map<String, Map<String, Object>> factFields = new LinkedHashMap<>();
    private Map<String, String> mapping = new LinkedHashMap<>();

    static class Score {
      String source;
      String destination;
      Integer similarity;
    }

    @Override
    public Destination setSourceFields(List<String> fields, List<Object> values) {
      for (int i = 0; i < fields.size(); i++) {
        inputFields.put(fields.get(i).toString(), i < values.size() ? values.get(i) : null);
      }
      return this;
    }

    @Override
    public Build setFactModel(Class factModel) {
      Arrays.stream(BeanUtils.getProperties(factModel)).forEach(field -> {
        Map<String, Object> fieldMetadata = new LinkedHashMap<>();
        fieldMetadata.put("type", field.getType().getSimpleName());
        fieldMetadata.put("label", camelCaseToSentence(field.getName()));
        NotNull annotation = field.getAnnotation(NotNull.class);
        if (annotation != null) {
          fieldMetadata.put("required", true);
        }
        factFields.put(field.getName(), fieldMetadata);
      });
      return this;
    }

    @Override
    public Build autoSuggestionMapping() {
      this.mapping = autoSuggest();
      return this;
    }

    private Map<String, String> autoSuggest() {
      Map<String, String> suggestions = new LinkedHashMap<>();
      List<Score> scores = prepareScores();
      List<String> sources = scores.stream().map(score -> score.source).collect(Collectors.toList());
      Set<String> orderedSources = new LinkedHashSet<>(sources);
      for (String column : orderedSources) {
        Score matchingScore = pickFromScores(scores, column);
        if(matchingScore != null && matchingScore.similarity < 10) {
          suggestions.put(matchingScore.source, matchingScore.destination);
        }
      }
      return suggestions;
    }

    @Override
    public MappingResponse build() {
      return new MappingResponse(this);
    }

    private String camelCaseToSentence(String str) {
      return StringUtils.capitalize(StringUtils.join(
          StringUtils.splitByCharacterTypeCamelCase(str),
          ' '
      ));
    }

    private List<Score> prepareScores() {
      List<Score> scores = new LinkedList<>();
      for (String column : inputFields.keySet()) {
        for (String factField : factFields.keySet()) {
          Score score = new Score();
          score.source = column;
          score.destination = factField;
          score.similarity = LevenshteinDistance.getDefaultInstance().apply(factField, camelCaseToSentence(column));
          scores.add(score);
        }
      }
      scores.sort((score1, score2) -> score1.similarity.compareTo(score2.similarity));
      return scores;
    }

    private Score pickFromScores(List<Score> scores, String source) {
      Score score = null;
      ListIterator<Score> iter1 = scores.listIterator();
      while(iter1.hasNext()){
        Score scoreTemp = iter1.next();
        if(scoreTemp.source.equals(source)){
          if (score == null) {
            score = scoreTemp;
          }
          iter1.remove();
        }
      }
      ListIterator<Score> iter2 = scores.listIterator();
      while(iter2.hasNext()){
        Score scoreTemp = iter2.next();
        if(scoreTemp.destination.equals(score.destination)){
          iter2.remove();
        }
      }
      return score;
    }
  }

  private MappingResponse(Builder builder) {
    this.factFields = builder.factFields;
    this.inputFields = builder.inputFields;
    this.mapping = builder.mapping;
  }

  @JsonSerialize
  @JsonProperty("sourceColumns")
  private Map<String, Object> inputFields;

  @JsonSerialize
  @JsonProperty("destinationColumns")
  private Map<String, Map<String, Object>> factFields;

  @JsonSerialize
  @JsonProperty("mapping")
  private Map<String, String> mapping;

  public Map<String, Object> getInputFields() {
    return inputFields;
  }

  public Map<String, Map<String, Object>> getFactFields() {
    return factFields;
  }

  public Map<String, String> getMapping() {
    return mapping;
  }
}
