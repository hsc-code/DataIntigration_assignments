package de.di.schema_matching.helper;

import java.util.Arrays;

public class HungarianAlgorithm {
    public static int[] findOptimalAssignment(double[][] costMatrix) {
        int n = costMatrix.length;
        int[] labelByWorker = new int[n];
        int[] labelByJob = new int[n];
        int[] minSlackWorkerByJob = new int[n];
        double[] minSlackValueByJob = new double[n];
        int[] matchJobByWorker = new int[n];
        Arrays.fill(matchJobByWorker, -1);
        int[] matchWorkerByJob = new int[n];
        Arrays.fill(matchWorkerByJob, -1);

        double[] labelWorker = new double[n];
        double[] labelJob = new double[n];

        // Init labels
        for (int w = 0; w < n; w++) {
            labelWorker[w] = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (costMatrix[w][j] < labelWorker[w]) {
                    labelWorker[w] = costMatrix[w][j];
                }
            }
        }

        for (int j = 0; j < n; j++) {
            labelJob[j] = Double.POSITIVE_INFINITY;
            for (int w = 0; w < n; w++) {
                if (costMatrix[w][j] - labelWorker[w] < labelJob[j]) {
                    labelJob[j] = costMatrix[w][j] - labelWorker[w];
                }
            }
        }

        int[] parentWorkerByCommittedJob = new int[n];
        boolean[] committedWorkers = new boolean[n];

        for (int match = 0; match < n; match++) {
            Arrays.fill(committedWorkers, false);
            Arrays.fill(parentWorkerByCommittedJob, -1);
            int[] queue = new int[n];
            int qStart = 0;
            int qEnd = 0;

            int s = -1;
            for (int w = 0; w < n; w++) {
                if (matchJobByWorker[w] == -1) {
                    s = w;
                    break;
                }
            }

            queue[qEnd++] = s;
            committedWorkers[s] = true;

            for (int j = 0; j < n; j++) {
                minSlackValueByJob[j] = costMatrix[s][j] - labelWorker[s] - labelJob[j];
                minSlackWorkerByJob[j] = s;
            }

            int committedJob = -1;
            while (true) {
                while (qStart < qEnd) {
                    int w = queue[qStart++];
                    for (int j = 0; j < n; j++) {
                        if (parentWorkerByCommittedJob[j] == -1) {
                            double slack = costMatrix[w][j] - labelWorker[w] - labelJob[j];
                            if (slack == 0) {
                                parentWorkerByCommittedJob[j] = w;
                                if (matchWorkerByJob[j] == -1) {
                                    committedJob = j;
                                    break;
                                } else {
                                    queue[qEnd++] = matchWorkerByJob[j];
                                    committedWorkers[matchWorkerByJob[j]] = true;
                                }
                            } else if (slack < minSlackValueByJob[j]) {
                                minSlackValueByJob[j] = slack;
                                minSlackWorkerByJob[j] = w;
                            }
                        }
                    }
                    if (committedJob != -1) break;
                }
                if (committedJob != -1) break;

                double delta = Double.POSITIVE_INFINITY;
                for (int j = 0; j < n; j++) {
                    if (parentWorkerByCommittedJob[j] == -1) {
                        delta = Math.min(delta, minSlackValueByJob[j]);
                    }
                }

                for (int w = 0; w < n; w++) {
                    if (committedWorkers[w]) labelWorker[w] += delta;
                }
                for (int j = 0; j < n; j++) {
                    if (parentWorkerByCommittedJob[j] != -1)
                        labelJob[j] -= delta;
                    else
                        minSlackValueByJob[j] -= delta;
                }

                for (int j = 0; j < n; j++) {
                    if (parentWorkerByCommittedJob[j] == -1 && minSlackValueByJob[j] == 0) {
                        int w = minSlackWorkerByJob[j];
                        parentWorkerByCommittedJob[j] = w;
                        if (matchWorkerByJob[j] == -1) {
                            committedJob = j;
                            break;
                        } else {
                            queue[qEnd++] = matchWorkerByJob[j];
                            committedWorkers[matchWorkerByJob[j]] = true;
                        }
                    }
                }
                if (committedJob != -1) break;
            }

            int j = committedJob;
            while (true) {
                int w = parentWorkerByCommittedJob[j];
                int nextJ = matchJobByWorker[w];
                match(w, j, matchJobByWorker, matchWorkerByJob);
                j = nextJ;
                if (j == -1) break;
            }
        }
        return matchJobByWorker;
    }

    private static void match(int w, int j, int[] matchJobByWorker, int[] matchWorkerByJob) {
        matchJobByWorker[w] = j;
        matchWorkerByJob[j] = w;
    }
}
