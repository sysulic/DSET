package com.kun;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Data
public class Node {

    // the corresponding QNP state
    private int state;

    // actual cost
    private double cost;

    // heuristic value
    private double heuristic;

    // if this node is a goal node
    private boolean goalNode;

    private boolean deadend;

    private boolean alreadyAskIfGoalNode;

    private Set<Connector> outConnectors;

    private Set<Connector> visitedOutConnectors;

    private Connector markedInConnector;

    private Connector markedOutConnector;

    private List<Action> applicableActions;

    private boolean alreadyCalcOutConnectors;

    public Node(int state) {
        this.state = state;
        outConnectors = new LinkedHashSet<>();
        visitedOutConnectors = new HashSet<>();
    }

    public Connector getNextUnvisitedConnector() {
        if (outConnectors.isEmpty()) {
            return null;
        }
        Connector connector = null;
        Iterator<Connector> iterator = outConnectors.iterator();
        if (iterator.hasNext()) {
            connector = iterator.next();
            iterator.remove();
            visitedOutConnectors.add(connector);
        }
        return connector;
    }

    public void reset() {
        Set<Connector> temp = visitedOutConnectors;
        visitedOutConnectors = outConnectors;
        outConnectors = temp;
        visitedOutConnectors.clear();
        markedOutConnector = null;
    }

    @Override
    public String toString() {
        return String.valueOf(state);
    }

    @Override
    public int hashCode() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (Objects.nonNull(o) && o instanceof Node && ((Node) o).hashCode() == this.hashCode()){
            return true;
        }
        return false;
    }
}
