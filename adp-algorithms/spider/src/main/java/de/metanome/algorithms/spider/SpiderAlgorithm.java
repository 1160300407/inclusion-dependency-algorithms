package de.metanome.algorithms.spider;

import static de.metanome.algorithms.spider.ConfigurationKey.INPUT_ROW_LIMIT;
import static de.metanome.algorithms.spider.ConfigurationKey.MAX_MEMORY_USAGE;
import static de.metanome.algorithms.spider.ConfigurationKey.MEMORY_CHECK_INTERVAL;
import static java.util.Arrays.asList;

import com.google.common.base.Joiner;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_execution.FileGenerator;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.TempFileAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.spider.SpiderConfiguration.SpiderConfigurationBuilder;
import java.util.ArrayList;
import java.util.List;

abstract class SpiderAlgorithm implements InclusionDependencyAlgorithm,
    IntegerParameterAlgorithm,
    TempFileAlgorithm {

  final SpiderConfigurationBuilder builder;
  final Spider spider;

  SpiderAlgorithm() {
    builder = SpiderConfiguration.builder();
    spider = new Spider();
  }

  List<ConfigurationRequirement<?>> common() {
    final List<ConfigurationRequirement<?>> requirements = new ArrayList<>();
    requirements.addAll(tpmms());
    return requirements;
  }

  private List<ConfigurationRequirement<?>> tpmms() {
    final ConfigurationRequirementInteger inputRowLimit = new ConfigurationRequirementInteger(
        INPUT_ROW_LIMIT.name());
    inputRowLimit.setDefaultValues(new Integer[]{-1});

    final ConfigurationRequirementInteger maxMemoryUsage = new ConfigurationRequirementInteger(
        MAX_MEMORY_USAGE.name());
    maxMemoryUsage.setDefaultValues(new Integer[]{2048 * 1024});

    final ConfigurationRequirementInteger memoryCheckInterval = new ConfigurationRequirementInteger(
        MEMORY_CHECK_INTERVAL.name());
    memoryCheckInterval.setDefaultValues(new Integer[]{500});

    return asList(inputRowLimit, maxMemoryUsage, memoryCheckInterval);
  }

  <T> T get(final String identifier, final T[] values, final int index)
      throws AlgorithmConfigurationException {

    if (index >= values.length) {
      final String message = String
          .format("Expected at least %d items width identifier %s", index + 1, identifier);
      throw new AlgorithmConfigurationException(message);
    }
    return values[index];
  }

  @SafeVarargs
  final <T> void handleUnknownConfiguration(final String identifier, final T... values)
      throws AlgorithmConfigurationException {

    final String formattedValues = Joiner.on(", ").join(values);
    final String message = String
        .format("Unknown configuration '%s', values: '%s'", identifier, formattedValues);
    throw new AlgorithmConfigurationException(message);
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    final SpiderConfiguration configuration = builder.build();
    spider.execute(configuration);
  }

  @Override
  public void setResultReceiver(
      InclusionDependencyResultReceiver inclusionDependencyResultReceiver) {
    builder.resultReceiver(inclusionDependencyResultReceiver);
  }

  @Override
  public void setIntegerConfigurationValue(final String identifier, final Integer... values)
      throws AlgorithmConfigurationException {

    final int value = get(identifier, values, 0);
    if (identifier.equals(INPUT_ROW_LIMIT.name())) {
      builder.inputRowLimit(value);
    } else if (identifier.equals(MAX_MEMORY_USAGE.name())) {
      builder.maxMemoryUsage(value);
    } else if (identifier.equals(MEMORY_CHECK_INTERVAL.name())) {
      builder.memoryCheckInterval(value);
    } else {
      handleUnknownConfiguration(identifier, values);
    }
  }

  @Override
  public void setTempFileGenerator(final FileGenerator tempFileGenerator) {
    builder.tempFileGenerator(tempFileGenerator);
  }

  @Override
  public String getAuthors() {
    return "Falco Dürsch, Tim Friedrich";
  }

  @Override
  public String getDescription() {
    return "Sort-Merge-Join IND discovery";
  }
}
