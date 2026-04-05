package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.ArrayList;
import java.util.List;

public class UCCProfiler {

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     * @param relation The relation that should be profiled for unique column combinations.
     * @return The list of all minimal, non-trivial unique column combinations in ths provided relation.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();

        // Calculate all unary UCCs and unary non-UCCs
        for (int attribute = 0; attribute < numAttributes; attribute++) {
            AttributeList attributes = new AttributeList(attribute);
            PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[attribute]);
            if (pli.isUnique())
                uniques.add(new UCC(relation, attributes));
            else
                currentNonUniques.add(pli);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Discover all unique column combinations of size n>1 by traversing the lattice level-wise. Make sure to     //
        // generate only minimal candidates while moving upwards and to prune non-minimal ones. Hint: The class       //
        // AttributeList offers some helpful functions to test for sub- and superset relationships. Use PLI           //
        // intersection to validate the candidates in every lattice level. Advances techniques, such as random walks, //
        // hybrid search strategies, or hitting set reasoning can be used, but are optional to pass the assignment.   //
        List<PositionListIndex> nextLevel = currentNonUniques;
        while (!nextLevel.isEmpty()) {
            List<PositionListIndex> newLevel = new ArrayList<>();
            int size = nextLevel.size();

            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    PositionListIndex pli1 = nextLevel.get(i);
                    PositionListIndex pli2 = nextLevel.get(j);
                    if (pli1.getAttributes().samePrefixAs(pli2.getAttributes())) {
                        AttributeList newAttrList = pli1.getAttributes().union(pli2.getAttributes());

                        boolean isMinimal = true;
                        for (UCC ucc : uniques) {
                            if (newAttrList.supersetOf(ucc.getAttributeList())) {
                                isMinimal = false;
                                break;
                            }
                        }
                        if (!isMinimal) continue;

                        PositionListIndex newPLI = pli1.intersect(pli2);
                        if (newPLI.isUnique()) {
                            uniques.add(new UCC(relation, newAttrList));
                        } else {
                            newLevel.add(newPLI);
                        }
                    }
                }
            }

            nextLevel = newLevel;
        }


        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return uniques;
    }
}
