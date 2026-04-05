package de.di.schema_matching;

import de.di.schema_matching.helper.HungarianAlgorithm;
import de.di.schema_matching.structures.CorrespondenceMatrix;
import de.di.schema_matching.structures.SimilarityMatrix;

import java.util.Arrays;

public class SecondLineSchemaMatcher {

    /**
     * Translates the provided similarity matrix into a binary correspondence matrix by selecting possibly optimal
     * attribute correspondences from the similarities.
     * @param similarityMatrix A matrix of pair-wise attribute similarities.
     * @return A CorrespondenceMatrix of pair-wise attribute correspondences.
     */
    public CorrespondenceMatrix match(SimilarityMatrix similarityMatrix) {
        double[][] simMatrix = similarityMatrix.getMatrix();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Translate the similarity matrix into a binary correlation matrix by implementing either the StableMarriage //
        // algorithm or the Hungarian method.                                                                         //
        int n = simMatrix.length;
        int m = simMatrix[0].length;

        // Convert the similarity matrix to a cost matrix by negating values
        // (Hungarian algorithm minimizes cost; we want to maximize similarity)
        double[][] costMatrix = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                costMatrix[i][j] = -simMatrix[i][j];
            }
        }

        // Pad to make the cost matrix square if needed
        int size = Math.max(n, m);
        double[][] squareCost = new double[size][size];
        for (int i = 0; i < size; i++) {
            Arrays.fill(squareCost[i], 0); // fill with 0s by default
            if (i < n) {
                System.arraycopy(costMatrix[i], 0, squareCost[i], 0, m);
            }
        }

        // Perform Hungarian Algorithm on square matrix
        int[] assignment = HungarianAlgorithm.findOptimalAssignment(squareCost);

        // Convert assignment to correspondence matrix
        int[] sourceAssignments = Arrays.copyOf(assignment, n); // ignore padded assignments
        int[][] corrMatrix = assignmentArray2correlationMatrix(sourceAssignments, simMatrix);


        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return new CorrespondenceMatrix(corrMatrix, similarityMatrix.getSourceRelation(), similarityMatrix.getTargetRelation());
    }

    /**
     * Translate an array of source assignments into a correlation matrix. For example, [0,3,2] maps 0->1, 1->3, 2->2
     * and, therefore, translates into [[1,0,0,0][0,0,0,1][0,0,1,0]].
     * @param sourceAssignments The list of source assignments.
     * @param simMatrix The original similarity matrix; just used to determine the number of source and target attributes.
     * @return The correlation matrix extracted form the source assignments.
     */
    private int[][] assignmentArray2correlationMatrix(int[] sourceAssignments, double[][] simMatrix) {
        int[][] corrMatrix = new int[simMatrix.length][];
        for (int i = 0; i < simMatrix.length; i++) {
            corrMatrix[i] = new int[simMatrix[i].length];
            for (int j = 0; j < simMatrix[i].length; j++)
                corrMatrix[i][j] = 0;
        }
        for (int i = 0; i < sourceAssignments.length; i++)
            if (sourceAssignments[i] >= 0 && sourceAssignments[i] < simMatrix[i].length)
                corrMatrix[i][sourceAssignments[i]] = 1;
        return corrMatrix;
    }
}