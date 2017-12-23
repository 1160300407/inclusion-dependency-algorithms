SPIDER
======

## Improvements over the reference implementation

1. Fixed I/O bug in TPMMS merge phase which sneaked nulls into the dataset in case duplicate entries
  were spread across more than one partition.
  In the original `TPMMS.java:139` newlines are generate despite the fact that `tuple.value` has
  never been written (l. 130) (since skipped due to a detected duplicate).

2. Previously the relation was scanned for each attribute individually. Now the relation is scanned
  in one pass while writing all attribute files simultaneously.
  This trades *n* scans of the relation (*n* begin the number of attributes) for one additional pass
  during disk-based TPMMS (since the attribute files are initially unsorted, which would be at least
  the case for relational inputs anyway).

## To-Do

1. Revisit handling of `NULL`-values. Currently they are encoded as `⟂` and subject to the same to
  deduplication and sorting process as all the other attribute values. Likely this is not the
  desired behavior.
2. If the handling of `NULL`-values is fixed, rerun the algorithm with a column containing only
  `NULL`-values. Technically an empty column should participate in at least the inclusion
  dependencies formed by the empty column as left hand side and one column of all available columns
  as the right hand side.
3. Implement IND-pruning by data type. Given a table input (and therefore precise typing
  information) the reference implementation pruned candidates of unequal data type early. This is
  currently missing.
4. Inside `Spider`, the `attributeIndex` could possibly a fixed-size array and not a map, making 
  look-ups from attribute ID to attribute even cheaper.
5. Revisit I/O handling. If a single I/O-Exception occurs, the algorithm execution can be aborted
  immediately. However, if resources are allocated in batch, failure to allocate a single resource
  currently does not free the already open resources. 