package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.duplicate_detection.structures.Duplicate;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.Levenshtein;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class SortedNeighborhood {

    // A Record class that stores the values of a record with its original index. This class helps to remember the
    // original index of a record when this record is being sorted.
    @Data
    @AllArgsConstructor
    private static class Record {
        private int index;
        private String[] values;
    }

    /**
     * Discovers all duplicates in the relation by running the Sorted Neighborhood Method once with every sortingKey.
     * Each run uses one of the specified sortingKeys for the sorting, the windowsSize for the windowing, and
     * the recordComparator for the similarity calculations. A pair of records is classified as a duplicate and the
     * corresponding record indexes are returned as a Duplicate object, if the similarity of the two records w.r.t.
     * the provided recordComparator is equal to or greater than the similarityThreshold.
     * @param relation The relation, in which duplicates should be detected.
     * @param sortingKeys The sorting keys that should be used; a sorting key corresponds to an attribute index, whose
     *                    lexicographical order should determine a sortation; every specificed sorting key korresponds
     *                    to one Sorted Neighborhood run and the union of all duplicates of all runs is the result of
     *                    the call.
     * @param windowSize The window size each Sorted Neighborhood run should use.
     * @param recordComparator The record comparator each Sorted Neighborhood run should use when comparing records.
     * @return The list of discovered duplicate pairs of all Sorted Neighborhood runs.
     */
    public Set<Duplicate> detectDuplicates(Relation relation, int[] sortingKeys, int windowSize, RecordComparator recordComparator) {
        Set<Duplicate> duplicates = new HashSet<>();

        Record[] records = new Record[relation.getRecords().length];
        for (int i = 0; i < relation.getRecords().length; i++)
            records[i] = new Record(i, relation.getRecords()[i]);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Discover all duplicates in the provided relation. A duplicate stores the attribute indexes that refer to   //
        // matching records. Use the provided sortingKeys, windowSize, and recordComparator to implement the Sorted   //
        // Neighborhood Method correctly.                                                                             //

        // For each sorting key, run the Sorted Neighborhood Method
        for (int sortingKey : sortingKeys) {
            // Sort records by the current sorting key (lexicographically)
            Arrays.sort(records, (r1, r2) -> {
                String val1 = r1.getValues()[sortingKey];
                String val2 = r2.getValues()[sortingKey];
                return val1.compareTo(val2);
            });
            
            // Slide a window over the sorted records
            for (int i = 0; i <= records.length - windowSize; i++) {
                // Compare all pairs within the current window
                for (int j = i; j < i + windowSize; j++) {
                    for (int k = j + 1; k < i + windowSize; k++) {
                        Record record1 = records[j];
                        Record record2 = records[k];
                        
                        // Calculate similarity between the two records
                        double similarity = recordComparator.compare(record1.getValues(), record2.getValues());
                        
                        // If similarity is above threshold, it's a duplicate
                        if (recordComparator.isDuplicate(similarity)) {
                            duplicates.add(new Duplicate(record1.getIndex(), record2.getIndex(), similarity, relation));
                        }
                    }
                }
            }
        }

        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return duplicates;
    }

    /**
     * Suggests a RecordComparator instance based on the provided relation for duplicate detection purposes.
     * @param relation The relation a RecordComparator needs to be suggested for.
     * @return A RecordComparator instance for comparing records of the provided relation.
     */
    public static RecordComparator suggestRecordComparatorFor(Relation relation) {
        List<AttrSimWeight> attrSimWeights = new ArrayList<>(relation.getAttributes().length);
        double threshold = 0.0;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Define the AttrSimWeight objects for a RecordComparator that matches the records of the provided relation  //
        // possibly well, i.e., duplicate should receive possibly high similarity scores and non-duplicates should    //
        // receive possibly low scores. In other words, put together a possibly effective ensemble of the already     //
        // implemented similarity functions for duplicate detections runs on the provided relation. Side note: This   //
        // is usually learned by machine learning algorithms, but a creative, heuristics-based solution is sufficient //
        // here.                                                                                                      //

        // Create similarity measures
        Jaccard jaccardWithTokens = new Jaccard(new Tokenizer(2, false), false); // 2-grams, no padding, set semantics
        Jaccard jaccardBag = new Jaccard(new Tokenizer(3, true), true);  // 3-grams, with padding, bag semantics
        Levenshtein levenshteinWithDamerau = new Levenshtein(true);       // With Damerau transposition
        Levenshtein levenshteinStandard = new Levenshtein(false);         // Standard Levenshtein
        
        // Assign similarity measures to attributes with equal weights
        double weight = 1.0 / relation.getAttributes().length;
        
        for (int i = 0; i < relation.getAttributes().length; i++) {
            // Alternate between different similarity measures for variety
            if (i % 4 == 0) {
                // Use Jaccard with 2-grams for every 4th attribute (good for text with spaces)
                attrSimWeights.add(new AttrSimWeight(i, jaccardWithTokens, weight));
            } else if (i % 4 == 1) {
                // Use Levenshtein with Damerau for character-level similarity
                attrSimWeights.add(new AttrSimWeight(i, levenshteinWithDamerau, weight));
            } else if (i % 4 == 2) {
                // Use Jaccard with bag semantics for repeated elements
                attrSimWeights.add(new AttrSimWeight(i, jaccardBag, weight));
            } else {
                // Use standard Levenshtein for the rest
                attrSimWeights.add(new AttrSimWeight(i, levenshteinStandard, weight));
            }
        }
        
        // Set a reasonable threshold for duplicate detection
        // 0.7 means records need to be 70% similar to be considered duplicates
        threshold = 0.7;

        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return new RecordComparator(attrSimWeights, threshold);
    }
}
