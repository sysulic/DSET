package com.kun;

import java.util.*;

public class Tarjan<V, W> {
    private Map<V, Integer> DFN;

    private Map<V, Integer> LOW;

    private Stack<V> stack;

    private Set<V> inStack;

    private int timeStamp;

    private IGraph<V, W> graph;

    private List<IGraph<V, W>> SCCs;

    public Tarjan() {
    }

    public void setGraph(IGraph<V, W> graph) {
        this.graph = graph;
    }

    public void init() {
        Collection<V> allNodes = graph.getAllNodes();
        int size = (int) (allNodes.size() / 0.75) + 1;
        DFN = new HashMap<>(size);
        LOW = new HashMap<>(size);
        for (V node : allNodes) {
            DFN.put(node, 0);
            LOW.put(node, 0);
        }
        stack = new Stack<>();
        inStack = new HashSet<>(size);
        timeStamp = 0;
        SCCs = new LinkedList<>();
    }

    public List<IGraph<V, W>> run() {
        init();
        for (V node : graph.getAllNodes()) {
            if (DFN.get(node) == 0) {
                tarjan(node);
            }
        }
        return SCCs;
    }

    private void tarjan(V u) {
        DFN.put(u, ++timeStamp);
        LOW.put(u, timeStamp);
        inStack.add(u);
        stack.push(u);

        for (V v : graph.getSuccessors(u)) {
            if (DFN.get(v) == 0) {
                tarjan(v);
                LOW.put(u, Math.min(LOW.get(u), LOW.get(v)));
            } else {
                if (inStack.contains(v)) {
                    LOW.put(u, Math.min(LOW.get(u), DFN.get(v)));
                }
            }
        }

        if (DFN.get(u).equals(LOW.get(u))) {
            Set<V> nodeSet = new HashSet<>();
            V t = null;
            while (!u.equals(t)) {
                t = stack.pop();
                nodeSet.add(t);
                inStack.remove(t);
            }
            SCCs.add(constructSCC(nodeSet));
        }
    }

    private IGraph<V, W> constructSCC(Collection<V> nodeSet) {
        Graph<V, W> scc = new Graph<>();
        for (V node : nodeSet) {
            W label = graph.getLabel(node);
            for (V successor : graph.getSuccessors(node)) {
                if (nodeSet.contains(successor)) {
                    scc.addEdge(node, successor, label);
                }
            }
        }
        return scc;
    }
}
