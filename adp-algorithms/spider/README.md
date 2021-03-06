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

3. Implement IND-pruning by data type. Given a table input (and therefore precise typing
  information) the reference implementation pruned candidates of unequal data type early. This is
  currently missing.
5. Revisit I/O handling. If a single I/O-Exception occurs, the algorithm execution can be aborted
  immediately. However, if resources are allocated in batch, failure to allocate a single resource
  currently does not free the already open resources. 
