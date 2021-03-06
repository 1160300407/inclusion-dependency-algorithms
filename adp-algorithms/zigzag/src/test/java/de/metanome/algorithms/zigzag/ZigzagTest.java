package de.metanome.algorithms.zigzag;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithms.zigzag.configuration.ZigzagConfiguration;
import de.metanome.input.ind.AlgorithmType;
import de.metanome.input.ind.InclusionDependencyParameters;
import de.metanome.util.InclusionDependencyResultReceiverStub;
import de.metanome.util.TestDatabase;
import de.metanome.validation.ValidationParameters;
import de.metanome.validation.database.QueryType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.metanome.util.InclusionDependencyUtil.sortIndAttributes;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class ZigzagTest {

  private TestDatabase testDatabase;
  private String relationName = "TEST";
  private List<String> columnNames = asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N");

  @BeforeEach
  public void setup() throws Exception {
    testDatabase = TestDatabase.builder()
            .resourceClass(ZigzagTest.class)
            .relationName(relationName)
            .columnNames(columnNames)
            .csvPath("testPaperExample.csv")
            .build();
    testDatabase.setUp();
  }

  @AfterEach
  public void tearDown() {
    if (testDatabase != null) {
      testDatabase.tearDown();
    }
  }

  @Test
  public void testPaperExample() throws Exception {
    // GIVEN
    List<ColumnIdentifier> ci = new ArrayList<>();
    for (String c : columnNames) {
      ci.add(new ColumnIdentifier(relationName, c));
    }

    List<InclusionDependency> maximumINDs = new ArrayList<>();
    maximumINDs.add(
        new InclusionDependency(
            new ColumnPermutation(ci.get(0), ci.get(1), ci.get(2), ci.get(3), ci.get(4)),
            new ColumnPermutation(ci.get(7), ci.get(8), ci.get(9), ci.get(10), ci.get(11))));
    maximumINDs.add(
        new InclusionDependency(
            new ColumnPermutation(ci.get(3), ci.get(5), ci.get(6)),
            new ColumnPermutation(ci.get(10), ci.get(12), ci.get(13))));
    maximumINDs.add(
        new InclusionDependency(
            new ColumnPermutation(ci.get(4), ci.get(5)),
            new ColumnPermutation(ci.get(11), ci.get(12))));
    maximumINDs.add(
        new InclusionDependency(
            new ColumnPermutation(ci.get(4), ci.get(6)),
            new ColumnPermutation(ci.get(11), ci.get(13))));

    // SETUP
    InclusionDependencyResultReceiverStub resultReceiver = new InclusionDependencyResultReceiverStub();

    ValidationParameters validationParameters = new ValidationParameters();
    validationParameters.setQueryType(QueryType.ERROR_MARGIN);
    validationParameters.setConnectionGenerator(testDatabase.asConnectionGenerator());

    InclusionDependencyParameters inclusionDependencyParameters = new InclusionDependencyParameters();
    inclusionDependencyParameters.setAlgorithmType(AlgorithmType.FILE);
    inclusionDependencyParameters
        .setConfigurationString("inputPath=" + getClass().getResource("ind_input.json").getFile());

    // EXECUTE
    ZigzagConfiguration config =
        ZigzagConfiguration.builder()
            .resultReceiver(resultReceiver)
            .validationParameters(validationParameters)
            .inclusionDependencyParameters(inclusionDependencyParameters)
            .startK(2)
            .epsilon(10000)
            .build();

    Zigzag zigzag = new Zigzag(config);
    zigzag.execute();

    // THEN
    assertEqualMaxInds(resultReceiver.getReceivedResults(), maximumINDs);
  }

  @Disabled // FIXME
  @Test
  void testCalculateOptimisticBorder() {
    Set<InclusionDependency> unsatisfiedINDs = new HashSet<>();

    ColumnIdentifier a1 = new ColumnIdentifier("table", "a");
    ColumnIdentifier a2 = new ColumnIdentifier("table", "a2");
    ColumnIdentifier b1 = new ColumnIdentifier("table", "b");
    ColumnIdentifier b2 = new ColumnIdentifier("table", "b2");
    ColumnIdentifier c1 = new ColumnIdentifier("table", "c");
    ColumnIdentifier c2 = new ColumnIdentifier("table", "c2");
    ColumnIdentifier d1 = new ColumnIdentifier("table", "d");
    ColumnIdentifier d2 = new ColumnIdentifier("table", "d2");
    ColumnIdentifier e1 = new ColumnIdentifier("table", "e");
    ColumnIdentifier e2 = new ColumnIdentifier("table", "e2");

    ColumnPermutation ad1 = new ColumnPermutation(a1, d1);
    ColumnPermutation ad2 = new ColumnPermutation(a2, d2);
    InclusionDependency AD = new InclusionDependency(ad1, ad2);

    ColumnPermutation cd1 = new ColumnPermutation(c1, d1);
    ColumnPermutation cd2 = new ColumnPermutation(c2, d2);
    InclusionDependency CD = new InclusionDependency(cd1, cd2);

    ColumnPermutation de1 = new ColumnPermutation(d1, e1);
    ColumnPermutation de2 = new ColumnPermutation(d2, e2);
    InclusionDependency DE = new InclusionDependency(de1, de2);

    unsatisfiedINDs.add(AD);
    unsatisfiedINDs.add(CD);
    unsatisfiedINDs.add(DE);

    Set<ColumnIdentifier> BD = Sets.newHashSet(b1, d1);
    Set<ColumnIdentifier> ABCE = Sets.newHashSet(a1, b1, c1, e1);
    Set<Set<ColumnIdentifier>> optimisticBorder = ImmutableSet.of(BD, ABCE);

    InclusionDependencyResultReceiverStub resultReceiver = new InclusionDependencyResultReceiverStub();

    ValidationParameters validationParameters = new ValidationParameters();
    validationParameters.setQueryType(QueryType.ERROR_MARGIN);
    validationParameters.setConnectionGenerator(testDatabase.asConnectionGenerator());

    InclusionDependencyParameters inclusionDependencyParameters = new InclusionDependencyParameters();
    inclusionDependencyParameters.setAlgorithmType(AlgorithmType.FILE);
    inclusionDependencyParameters
            .setConfigurationString("inputPath=" + getClass().getResource("ind_input.json").getFile());

    Zigzag zigzag = new Zigzag(ZigzagConfiguration.builder()
            .resultReceiver(resultReceiver)
            .validationParameters(validationParameters)
            .inclusionDependencyParameters(inclusionDependencyParameters)
            .startK(2)
            .epsilon(10000)
            .build());
    System.out.println(zigzag.calculateOptimisticBorder(unsatisfiedINDs));
    assertThat(zigzag.calculateOptimisticBorder(unsatisfiedINDs)).isEqualTo(optimisticBorder);
    // System.out.println(optimisticBorder.equals(zigzag.calculateOptimisticBorder(unsatisfiedINDs)));
  }

  private Set<InclusionDependency> calculateUnaryInds() {
    ColumnIdentifier a1 = new ColumnIdentifier("table", "a");
    ColumnIdentifier a2 = new ColumnIdentifier("table", "a2");
    ColumnIdentifier b1 = new ColumnIdentifier("table", "b");
    ColumnIdentifier b2 = new ColumnIdentifier("table", "b2");
    ColumnIdentifier c1 = new ColumnIdentifier("table", "c");
    ColumnIdentifier c2 = new ColumnIdentifier("table", "c2");
    ColumnIdentifier d1 = new ColumnIdentifier("table", "d");
    ColumnIdentifier d2 = new ColumnIdentifier("table", "d2");
    ColumnIdentifier e1 = new ColumnIdentifier("table", "e");
    ColumnIdentifier e2 = new ColumnIdentifier("table", "e2");

    Set<InclusionDependency> unaryINDs = new HashSet<>();
    unaryINDs.add(toInd(a1, a2));
    unaryINDs.add(toInd(b1, b2));
    unaryINDs.add(toInd(c1, c2));
    unaryINDs.add(toInd(d1, d2));
    unaryINDs.add(toInd(e1, e2));
    return unaryINDs;
  }

  private InclusionDependency toInd(ColumnIdentifier dep, ColumnIdentifier ref) {
    return new InclusionDependency(new ColumnPermutation(dep), new ColumnPermutation(ref));
  }

  private InclusionDependency[] toArray(Set<InclusionDependency> inds) {
    return inds.toArray(new InclusionDependency[0]);
  }

  private void assertEqualMaxInds(Collection<InclusionDependency> indsA, Collection<InclusionDependency> indsB) {
    ImmutableSet<InclusionDependency> sortedIndsA = sortIndAttributes(indsA);
    ImmutableSet<InclusionDependency> sortedIndsB = sortIndAttributes(indsB);
    assertThat(sortedIndsA).containsExactlyInAnyOrder(toArray(sortedIndsB));
  }
}
