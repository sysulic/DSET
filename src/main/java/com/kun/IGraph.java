package com.kun;

import java.util.Collection;
import java.util.Set;

public interface IGraph<V, W> {
    Collection<V> getAllNodes();

    Collection<V> getSuccessors(V node);

    W getLabel(V node);

    void addNode(V node);

    void addEdge(V from, V to, W label);

    void removeEdge(V from, V to);

    void removeAllEdgesFrom(V from);
}
