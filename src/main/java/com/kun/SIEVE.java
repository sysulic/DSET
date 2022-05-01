package com.kun;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class SIEVE {
    private Tarjan<Integer, Action> tarjan;
    private int numBits;

    public boolean isTerminatingGraph(IGraph<Integer, Action> graph) {
        tarjan.setGraph(graph);
        for (IGraph<Integer, Action> scc : tarjan.run()) {
            if (!isTerminatingSCC(scc)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTerminatingSCC(IGraph<Integer, Action> scc) {
        if (Objects.isNull(scc) || scc.getAllNodes().size() <= 1) {
            return true;
        }

        int forbiddenBits = 0;
        boolean hasEdgesToDelete = false;
        Map<Integer, Set<Integer>> decreaseEdges = new HashMap<>();

        for (Integer node : scc.getAllNodes()) {
            Action action = scc.getLabel(node);

            int posNumericEff = action.getPosNumericEff();
            if (posNumericEff != 0) {
                for (int i = 0; i < numBits; ++i) {
                    int bitMask = 1 << i;
                    if ((posNumericEff & bitMask) != 0) {
                        forbiddenBits |= bitMask;
                    }
                }
            }

            int negNumericEff = action.getNegNumericEff() & (~forbiddenBits);
            if (negNumericEff != 0) {
                for (int i = 0; i < numBits; ++i) {
                    int bitMask = 1 << i;
                    if ((negNumericEff & bitMask) != 0) {
                        decreaseEdges.computeIfAbsent(i, k -> new HashSet<>()).add(node);
                    }
                }
            }
        }

        for (Map.Entry<Integer, Set<Integer>> entry : decreaseEdges.entrySet()) {
            int key = entry.getKey();
            if ((forbiddenBits & (1 << key)) == 0) {
                hasEdgesToDelete = true;
                for (Integer source : entry.getValue()) {
                    scc.removeAllEdgesFrom(source);
                }
            }
        }

        if (hasEdgesToDelete) {
            return isTerminatingGraph(scc);
        }
        return false;
    }
}
