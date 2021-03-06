package de.metanome.algorithms.bellbrockhausen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithms.bellbrockhausen.accessors.DataAccessObject;
import de.metanome.algorithms.bellbrockhausen.accessors.TableInfo;
import de.metanome.algorithms.bellbrockhausen.configuration.BellBrockhausenConfiguration;
import de.metanome.algorithms.bellbrockhausen.models.Attribute;
import de.metanome.util.InclusionDependencyResultReceiverStub;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BellBrockhausenTest {

    private static final String TABLE_NAME = "table";

    private InclusionDependencyResultReceiverStub resultReceiver;
    private BellBrockhausen bellBrockhausen;

    @Mock
    private BellBrockhausenConfiguration config;
    @Mock
    private DataAccessObject dataAccessObject;

    @BeforeEach
    void setupMocks() {
        MockitoAnnotations.initMocks(this);
        bellBrockhausen = new BellBrockhausen(config, dataAccessObject);
        resultReceiver = new InclusionDependencyResultReceiverStub();
        when(config.getResultReceiver()).thenReturn(resultReceiver);
        when(config.getTableNames()).thenReturn(ImmutableList.of(TABLE_NAME));
    }

    @Test
    void testTableWithTwoInds() throws AlgorithmExecutionException {
        // GIVEN
        Attribute attributeA = new Attribute(new ColumnIdentifier(TABLE_NAME, "a"), Range.closed("1", "3"));
        Attribute attributeB = new Attribute(new ColumnIdentifier(TABLE_NAME, "b"), Range.closed("2", "4"));
        Attribute attributeC = new Attribute(new ColumnIdentifier(TABLE_NAME, "c"), Range.closed("1", "4"));
        ImmutableList<Attribute> attributes = ImmutableList.of(attributeA, attributeB, attributeC);
        TableInfo tableInfo = new TableInfo(TABLE_NAME, attributes);
        InclusionDependency indAC = toInd(attributeA.getColumnIdentifier(), attributeC.getColumnIdentifier());
        InclusionDependency indBC = toInd(attributeB.getColumnIdentifier(), attributeC.getColumnIdentifier());
        ImmutableSet<InclusionDependency> validInds = ImmutableSet.of(indAC, indBC);

        when(dataAccessObject.isValidUIND(any(InclusionDependency.class)))
                .thenAnswer(invocation -> validInds.contains(invocation.<InclusionDependency>getArgument(0)));

        // WHEN
        when(dataAccessObject.getTableInfo(TABLE_NAME)).thenReturn(tableInfo);
        bellBrockhausen.execute();

        // THEN
        assertThat(resultReceiver.getReceivedResults()).containsExactlyInAnyOrder(toArray(validInds));
    }

    @Test
    void testTableWithNoInds() throws AlgorithmExecutionException {
        // GIVEN
        Attribute attributeA = new Attribute(new ColumnIdentifier(TABLE_NAME, "a"), Range.closed("1", "3"));
        Attribute attributeB = new Attribute(new ColumnIdentifier(TABLE_NAME, "b"), Range.closed("2", "4"));
        Attribute attributeC = new Attribute(new ColumnIdentifier(TABLE_NAME, "c"), Range.closed("1", "4"));
        TableInfo tableInfo = new TableInfo(TABLE_NAME, ImmutableList.of(attributeA, attributeB, attributeC));
        ImmutableSet<InclusionDependency> validInds = ImmutableSet.of();

        when(dataAccessObject.isValidUIND(any(InclusionDependency.class))).thenReturn(false);

        // WHEN
        when(dataAccessObject.getTableInfo(TABLE_NAME)).thenReturn(tableInfo);
        bellBrockhausen.execute();

        // THEN
        assertThat(resultReceiver.getReceivedResults()).containsExactlyInAnyOrder(toArray(validInds));
    }

    @Test
    void testTableWithTransitiveInds() throws AlgorithmExecutionException {
        // GIVEN
        Attribute attributeA = new Attribute(new ColumnIdentifier(TABLE_NAME, "a"), Range.closed("1", "3"));
        Attribute attributeB = new Attribute(new ColumnIdentifier(TABLE_NAME, "b"), Range.closed("3", "4"));
        Attribute attributeC = new Attribute(new ColumnIdentifier(TABLE_NAME, "c"), Range.closed("1", "3"));
        Attribute attributeD = new Attribute(new ColumnIdentifier(TABLE_NAME, "d"), Range.closed("1", "4"));
        ImmutableList<Attribute> attributes = ImmutableList.of(attributeA, attributeB, attributeC, attributeD);
        TableInfo tableInfo = new TableInfo(TABLE_NAME, attributes);
        InclusionDependency indAC = toInd(attributeA.getColumnIdentifier(), attributeC.getColumnIdentifier());
        InclusionDependency indAD = toInd(attributeA.getColumnIdentifier(), attributeD.getColumnIdentifier());
        InclusionDependency indCA = toInd(attributeC.getColumnIdentifier(), attributeA.getColumnIdentifier());
        InclusionDependency indCD = toInd(attributeC.getColumnIdentifier(), attributeD.getColumnIdentifier());
        InclusionDependency indBD = toInd(attributeB.getColumnIdentifier(), attributeD.getColumnIdentifier());
        ImmutableSet<InclusionDependency> validInds = ImmutableSet.of(indAC, indAD, indCA, indCD, indBD);

        when(dataAccessObject.isValidUIND(any(InclusionDependency.class)))
                .thenAnswer(invocation -> validInds.contains(invocation.<InclusionDependency>getArgument(0)));

        // WHEN
        when(dataAccessObject.getTableInfo(TABLE_NAME)).thenReturn(tableInfo);
        bellBrockhausen.execute();

        // THEN
        assertThat(resultReceiver.getReceivedResults()).containsExactlyInAnyOrder(toArray(validInds));
    }


    @Test
    void testTableWithEqualValues() throws AlgorithmExecutionException {
        // GIVEN
        Attribute attributeA = new Attribute(new ColumnIdentifier(TABLE_NAME, "a"), Range.closed("1", "3"));
        Attribute attributeB = new Attribute(new ColumnIdentifier(TABLE_NAME, "b"), Range.closed("1", "3"));
        Attribute attributeC = new Attribute(new ColumnIdentifier(TABLE_NAME, "c"), Range.closed("1", "3"));
        ImmutableList<Attribute> attributes = ImmutableList.of(attributeA, attributeB, attributeC);
        TableInfo tableInfo = new TableInfo(TABLE_NAME, attributes);
        InclusionDependency indAB = toInd(attributeA.getColumnIdentifier(), attributeB.getColumnIdentifier());
        InclusionDependency indBA = toInd(attributeB.getColumnIdentifier(), attributeA.getColumnIdentifier());
        InclusionDependency indAC = toInd(attributeA.getColumnIdentifier(), attributeC.getColumnIdentifier());
        InclusionDependency indCA = toInd(attributeC.getColumnIdentifier(), attributeA.getColumnIdentifier());
        InclusionDependency indBC = toInd(attributeB.getColumnIdentifier(), attributeC.getColumnIdentifier());
        InclusionDependency indCB = toInd(attributeC.getColumnIdentifier(), attributeB.getColumnIdentifier());
        ImmutableSet<InclusionDependency> validInds = ImmutableSet.of(indAB, indBA, indAC, indCA, indBC, indCB);

        when(dataAccessObject.isValidUIND(any(InclusionDependency.class)))
                .thenAnswer(invocation -> validInds.contains(invocation.<InclusionDependency>getArgument(0)));

        // WHEN
        when(dataAccessObject.getTableInfo(TABLE_NAME)).thenReturn(tableInfo);
        bellBrockhausen.execute();

        // THEN
        assertThat(resultReceiver.getReceivedResults()).containsExactlyInAnyOrder(toArray(validInds));
    }

    private InclusionDependency toInd(ColumnIdentifier dependant, ColumnIdentifier referenced) {
        return new InclusionDependency(new ColumnPermutation(dependant), new ColumnPermutation(referenced));
    }

    private InclusionDependency[] toArray(Set<InclusionDependency> inds) {
        return inds.stream().toArray(InclusionDependency[]::new);
    }
}
