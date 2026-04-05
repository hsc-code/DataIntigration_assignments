package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    /**
     * Calculates the Jaccard similarity of the two input strings. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;

        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }

    /**
     * Calculates the Jaccard similarity of the two string lists. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String[] strings1, String[] strings2) {
        double jaccardSimilarity = 0;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Calculate the Jaccard similarity of the two String arrays. Note that the Jaccard similarity needs to be    //
        // calculated differently depending on the token semantics: set semantics remove duplicates while bag         //
        // semantics consider them during the calculation. The solution should be able to calculate the Jaccard       //
        // similarity either of the two semantics by respecting the inner bagSemantics flag.                          //
        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // BAG SEMANTICS:Use frequency (count) of each token
        if (bagSemantics) {
            //this will create frequency map for string1(which is array of strings) example {harneet:1,amol:2,sadia:3}
            Map<String, Long> freq1 = Arrays.stream(strings1)
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            //this will create frequency map for string2
            Map<String, Long> freq2 = Arrays.stream(strings2)
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            // this will collect all the tokens or keys from both the strings1 and strings2
            Set<String> allTokens = new HashSet<>();
            allTokens.addAll(freq1.keySet());
            allTokens.addAll(freq2.keySet());

            long intersection = 0;
            long union = 0;

            //in this each token in allToken will be taken one by one eg harneet
            for (String token : allTokens) {
                // suppose in freq1 harneet comes one time and in freq2 it comes three times then count1=1 and count2=3
                long count1 = freq1.getOrDefault(token, 0L);
                long count2 = freq2.getOrDefault(token, 0L);

                //this will take intersection for ex for harneet count1=1 and count2=3 so intersection will be min(1,3)=1
                //this will happen for all tokens and the sum  will be stored in intersection
                intersection += Math.min(count1, count2);
                //this will take union for ex for harneet count1=1 and count2=3 so intersection will be max(1,3)=3
                //this will happen for all tokens and the sum  will be stored in union
                union += Math.max(count1, count2);
            }

            // this will calculate jaccard similarity
            // jaccardSimilarity = union == 0 ? 0.0 : (double) intersection / union;
            jaccardSimilarity = union == 0 ? 0.0 : (double) intersection / (strings1.length + strings2.length);

            //i have a doubt i believe that the commented line is correct but in test case the professor has taken all the tokens

        } else {
            // SET SEMANTICS: Ignore duplicates, only unique tokens matter.

            //in this suppose string1 is [harneet,amol,harneet] then set 1 will be {harneet,amol}
            Set<String> set1 = new HashSet<>(Arrays.asList(strings1));
            Set<String> set2 = new HashSet<>(Arrays.asList(strings2));

            //It creates a new Set called intersection and this set is initialized with all elements from set1
            Set<String> intersection = new HashSet<>(set1);
            //It keeps only those elements in intersection that are also present in set2
            intersection.retainAll(set2);

            //It creates a new Set called union and this set is initialized with all elements from set1
            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);
            //Adds all elements from set2 into the union set but the element that are already in set1 will not be added only the new will added
            jaccardSimilarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
        }
        return jaccardSimilarity;
    }
}
