package com.kun;

import com.kun.parser.QNPParser;

import java.io.File;
import java.util.*;

public class Solver {

    private QNPProblem qnpProblem;

    private Node initNode;

    private LinkedList<Node> unexpandedNodes;

    private Set<Node> unexpandedNodeSet;

    private Set<Node> expandedNodes;

    LinkedList<Node> expandedNodeHistoryList;

    private Map<Integer, Node> nodeMap;

    private Node nodeBeingExpanded;

    private boolean solutionFound;

    private Map<Integer, Integer> increaseCount;

    private Map<Integer, Integer> decreaseCount;

    private SIEVE sieve;

    public static void main(String[] args) throws Exception {
        String domainName = "ShovelingSnow";
        String separator = File.separator;
        String domainPath = QNPParser.class.getClassLoader().getResource("").getPath() + separator + "Domain" + separator;
        String filename = domainPath + domainName + ".qnp";

        System.out.println(filename);


        QNPParser parser = new QNPParser(filename);
        QNPProblem problem = parser.parse();
        long startTime = System.nanoTime();
        Solver solver = new Solver(problem);
        solver.search();
        long endTime = System.nanoTime();
        double elapsedTimeInSecond = (double) (endTime - startTime) / 1_000_000_000;
        System.out.println("Is solvable: " + solver.solutionFound);
        System.out.println("Total running time: " + elapsedTimeInSecond + " seconds");
        if (solver.solutionFound) {
            System.out.println("The solution is shown as follows:");
            solver.printSolution();
        }



    }

    public Solver(QNPProblem qnpProblem) {
        this.qnpProblem = qnpProblem;
        int initState = qnpProblem.getInitState();
        initNode = new Node(initState);
        initNode.setGoalNode(false);
        initNode.setDeadend(false);
        initNode.setApplicableActions(qnpProblem.getApplicableActions(initState));
        nodeMap = new HashMap<>();
        unexpandedNodes = new LinkedList<>();
        unexpandedNodeSet = new HashSet<>();
        expandedNodes = new HashSet<>();
        expandedNodeHistoryList = new LinkedList<>();
        solutionFound = false;
        increaseCount = new HashMap<>();
        decreaseCount = new HashMap<>();
        sieve = new SIEVE(new Tarjan<>(), qnpProblem.getNumBits());
    }

    public void search() throws Exception {
        unexpandedNodes.add(initNode);
        unexpandedNodeSet.add(initNode);
        nodeMap.put(initNode.getState(), initNode);

        while (true) {
            if (unexpandedNodes.isEmpty()) {
                if (expandedNodeHistoryList.isEmpty()) {
                    solutionFound = false;
                    break;
                }
                if (checkContainsGoal()) {
                    solutionFound = true;
                    break;
                }

                Node lastExpandedNode = expandedNodeHistoryList.removeLast();
                expandedNodes.remove(lastExpandedNode);
                updateIncreaseAndDecreaseCount(lastExpandedNode, false);
                removeConnectorFromSolutionGraph(lastExpandedNode.getMarkedOutConnector());
                addUnexpandedNode(lastExpandedNode);
            }

            Node node = unexpandedNodes.removeFirst();  // The node to be expanded in this round
            unexpandedNodeSet.remove(node);
            nodeBeingExpanded = node;

            boolean success = expandNode(node);
            if (success) {
                if (!node.isGoalNode()) {
                    updateIncreaseAndDecreaseCount(node, true);
                }
                if (!expandedNodes.contains(node)) {
                    addExpandedNode(node);
                }
            } else {
                node.reset();
                if (node.equals(initNode)) {
                    break;
                } else {
                    Node lastExpandedNode = expandedNodeHistoryList.removeLast();
                    if (!lastExpandedNode.equals(initNode) && lastExpandedNode.getMarkedInConnector() == null) {
                        throw new Exception("Marked inConnector is null");
                    }
                    expandedNodes.remove(lastExpandedNode);
                    Node father = node.getMarkedInConnector().getParent();
                    if (lastExpandedNode.equals(father)) {
                        updateIncreaseAndDecreaseCount(father, false);
                        for (Node child : father.getMarkedOutConnector().getChildren()) {
                            if (unexpandedNodeSet.contains(child)) {
                                unexpandedNodeSet.remove(child);
                                unexpandedNodes.remove(child);
                            }
                        }
                        removeConnectorFromSolutionGraph(father.getMarkedOutConnector());
                    } else {
                        addUnexpandedNode(node);
                    }
                    addUnexpandedNode(lastExpandedNode);
                }
            }

        }
    }

    private boolean checkContainsGoal() {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visitedNodes = new HashSet<>();
        queue.add(initNode);
        visitedNodes.add(initNode);
        boolean containsGoal = false;
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (qnpProblem.isGoalState(node.getState())) {
                containsGoal = true;
                break;
            }
            Connector markedOutConnector = node.getMarkedOutConnector();
            if (Objects.nonNull(markedOutConnector)) {
//                System.out.println(node.getState() + ": " + markedOutConnector.getAction().getName());
                for (Node child : node.getMarkedOutConnector().getChildren()) {
                    if (!visitedNodes.contains(child)) {
                        visitedNodes.add(child);
                        queue.offer(child);
                    }
                }
            }
        }
        return containsGoal;
    }

    private void calculateOutConnectors(Node node) {
        if (node.isAlreadyCalcOutConnectors()) {
            return;
        }
        if (node.isGoalNode() || node.isDeadend()) {
            node.setAlreadyCalcOutConnectors(true);
            return;
        }
        int curState = node.getState();
        Set<Connector> outConnectors = node.getOutConnectors();
        for (Action action : node.getApplicableActions()) {
            List<Integer> successorStates = qnpProblem.applyActionInState(action, curState);
            List<Node> children = new ArrayList<>(successorStates.size());
            for (int state : successorStates) {
                Node child = getNode(state);
                if (qnpProblem.isGoalState(state)) {
                    child.setGoalNode(true);
                } else {
                    List<Action> applicableActionsList = qnpProblem.getApplicableActions(state);
                    if (applicableActionsList.isEmpty()) {
                        child.setDeadend(true);
                    } else {
                        child.setApplicableActions(applicableActionsList);
                    }
                }
                children.add(child);
            }
            outConnectors.add(new Connector(action, node, children));
        }
        node.setAlreadyCalcOutConnectors(true);
        node.setApplicableActions(null);
    }

    public boolean expandNode(Node node) throws Exception {
        calculateOutConnectors(node);
        if (!node.equals(initNode) && node.getMarkedInConnector() == null) {
            throw new Exception("Marked inConnector is null");
        }

        // No need to check whether the current node is a deadend because our algorithm guarantee that a deadend will never be expanded.
        if (node.isGoalNode()) {
            return true;
        }

        boolean flag = false; // whether node is successfully expanded
        Connector connector;
        while ((connector = node.getNextUnvisitedConnector()) != null) {
            if (checkDeadend(connector) && node.getVisitedOutConnectors().remove(connector)) {
                continue;
            }

            if (checkTermination(connector)) {
                List<Node> children = connector.getChildren();
                for (int i = children.size() - 1; i >= 0; --i) {
                    Node child = children.get(i);
                    if (!child.equals(nodeBeingExpanded) && !expandedNodes.contains(child) && !unexpandedNodeSet.contains(child)) {
                        addUnexpandedNode(children.get(i));
                    }
                }
                flag = true;
                break;
            }
        }
        return flag;
    }

    private boolean checkDeadend(Connector connector) {
        boolean deadendFound = false;
        for (Node child : connector.getChildren()) {
            if (child.isDeadend()) {
                deadendFound = true;
                break;
            }
        }
        return deadendFound;
    }

    private boolean checkTermination(Connector connector) {
        Action action = connector.getAction();
//        if (!action.hasNumericEffect()) {
//            updateSolutionGraph(connector);
//            return true;
//        }

        boolean needTerminationTest = checkPotentialNumericContradiction(increaseCount, action.getNegNumericEff());
        if (!needTerminationTest) {
            needTerminationTest = checkPotentialNumericContradiction(decreaseCount, action.getPosNumericEff());
        }

        if (!needTerminationTest) {
            updateSolutionGraph(connector);
            return true;
        }

        updateSolutionGraph(connector);
        boolean isTerminating = sieve.isTerminatingGraph(constructGraph());
        if (!isTerminating) {
            removeConnectorFromSolutionGraph(connector);
        }
        return isTerminating;
    }

    private boolean checkPotentialNumericContradiction(Map<Integer, Integer> numericEffectCount, int numericEffect) {
        boolean contradictionFound = false;
        for (Map.Entry<Integer, Integer> entry : numericEffectCount.entrySet()) {
            int key = entry.getKey();
            int val = entry.getValue();
            if ((numericEffect & (1 << key)) != 0 && val > 0) {
                contradictionFound = true;
                break;
            }
        }
        return contradictionFound;
    }

    private void updateSolutionGraph(Connector connector) {
        Node father = connector.getParent();
        father.setMarkedOutConnector(connector);
        for (Node child : connector.getChildren()) {
            if (child.getMarkedInConnector() == null) {
                child.setMarkedInConnector(connector);
            }
        }
    }

    private void removeConnectorFromSolutionGraph(Connector connector) {
        // connector == null implies the corresponding node is a goal node
        if (connector == null) {
            return;
        }
        Node father = connector.getParent();
        father.setMarkedOutConnector(null);
        for (Node child : connector.getChildren()) {
            assert nodeMap.containsKey(child);
            if (child.getMarkedInConnector().getParent().equals(father)) {
                child.setMarkedInConnector(null);
            }
        }
    }

    private Graph<Integer, Action> constructGraph() {
        Graph<Integer, Action> graph = new Graph<>();
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visitedNodes = new HashSet<>();
        queue.add(initNode);
        visitedNodes.add(initNode);
        while (!queue.isEmpty()) {
            Node curNode = queue.poll();
            int curState = curNode.getState();
            Connector markedOutConnector = curNode.getMarkedOutConnector();
            if (Objects.nonNull(markedOutConnector)) {
                Action action = markedOutConnector.getAction();
                for (Node child : markedOutConnector.getChildren()) {
                    graph.addEdge(curState, child.getState(), action);
                    if (!visitedNodes.contains(child)) {
                        queue.offer(child);
                        visitedNodes.add(child);
                    }
                }
            }
        }
        return graph;
    }

    private void updateIncreaseAndDecreaseCount(Node node, boolean flag) {
        if (node.getMarkedOutConnector() == null) {
            return;
        }
        Action action = node.getMarkedOutConnector().getAction();
        int posNumericEff = action.getPosNumericEff();
        int negNumericEff = action.getNegNumericEff();
        if (posNumericEff == 0 && negNumericEff == 0) {
            return;
        }
        int update = (flag ? 1 : -1) * node.getMarkedOutConnector().getChildren().size();
        for (int i = 0; i < qnpProblem.getNumBits(); ++i) {
            int bitMask = 1 << i;
            if ((posNumericEff & bitMask) != 0) {
                increaseCount.put(i, increaseCount.computeIfAbsent(i, k -> 0) + update);
            }
            if ((negNumericEff & bitMask) != 0) {
                decreaseCount.put(i, decreaseCount.computeIfAbsent(i, k -> 0) + update);
            }
        }
    }

    public void printSolution() {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visitedNodes = new HashSet<>();
        queue.add(initNode);
        visitedNodes.add(initNode);
        int solutionSize = 0;
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            Connector markedOutConnector = node.getMarkedOutConnector();
            if (Objects.nonNull(markedOutConnector)) {
//                System.out.println(node.getState());
                System.out.println(qnpProblem.getStateAsString(node.getState()) + ": " + markedOutConnector.getAction().getName());
                solutionSize++;
                for (Node child : node.getMarkedOutConnector().getChildren()) {
                    if (!visitedNodes.contains(child)) {
                        visitedNodes.add(child);
                        queue.offer(child);
                    }
                }
            }
        }
        System.out.println("Solution size: " + solutionSize);
    }

    private Node getNode(int state) {
        return nodeMap.computeIfAbsent(state, k -> new Node(state));
    }

    private void addUnexpandedNode(Node node) throws Exception {
//        if (node.getMarkedInConnector() == null) {
//            throw new Exception("Marked inConnector is null");
//        }
        unexpandedNodes.addFirst(node);
        unexpandedNodeSet.add(node);
    }

    private void addExpandedNode(Node node) throws Exception {
//        if (!node.equals(initNode) && node.getMarkedInConnector() == null) {
//            throw new Exception("Marked inConnector is null");
//        }
        expandedNodeHistoryList.add(node);
        expandedNodes.add(node);
    }
}
