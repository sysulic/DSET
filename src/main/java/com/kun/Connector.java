package com.kun;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Connector {

    private Node parent;

    private List<Node> children;

    private int nextChildIndex;

    private Action action;

    public Connector(Action action, Node parent, List<Node> children) {
        this.action = action;
        this.parent = parent;
        this.children = children;
    }

    public void updateNextChildIndex() {
        nextChildIndex++;
    }

    public double getAverageChildrenEstimate() {
        if (children == null || children.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Node child : children) {
            sum += child.getCost();
        }
        return sum / children.size();
    }

    @Override
    public int hashCode() {
        return action.hashCode();
    }
}
