package com.kun;

import com.kun.auxiliary.Pair;

import java.util.*;

public class Graph<V, W> implements IGraph<V, W> {
    private Map<V, W> nodeToLabel;

    private Map<V, Set<V>> nodeToSuccessors;

    public Graph() {
        nodeToLabel = new HashMap<>();
        nodeToSuccessors = new HashMap<>();
    }

    @Override
    public Collection<V> getAllNodes() {
        return nodeToLabel.keySet();
    }

    @Override
    public void addNode(V node) {
        nodeToLabel.putIfAbsent(node, null);
        nodeToSuccessors.putIfAbsent(node, new HashSet<>());
    }

    @Override
    public void addEdge(V from, V to, W label) {
        addNode(from);
        addNode(to);
        Set<V> successors = nodeToSuccessors.get(from);
        if (successors.isEmpty()) {
            nodeToLabel.put(from, label);
        }
        successors.add(to);
    }

    @Override
    public void removeEdge(V from, V to) {
        Set<V> successors = nodeToSuccessors.get(from);
        successors.remove(to);
        if (successors.isEmpty()) {
            nodeToLabel.put(from, null);
        }
    }

    @Override
    public void removeAllEdgesFrom(V from) {
        nodeToSuccessors.get(from).clear();
        nodeToLabel.put(from, null);
    }

    @Override
    public Collection<V> getSuccessors(V node) {
        return nodeToSuccessors.get(node);
    }

    @Override
    public W getLabel(V node) {
        return nodeToLabel.get(node);
    }

}
