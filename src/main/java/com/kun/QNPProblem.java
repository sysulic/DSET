package com.kun;

import com.kun.auxiliary.util;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class QNPProblem {
    private String domainName;

    private int initState;

    private int posGoalState;

    private int negGoalState;

    private Map<String, Action> actionMap;

    private int numBits;

    private Map<Integer, String> indexToVar;

    private Set<String> booleanVars;

    private int types;

    public boolean isGoalState(int state) {
        return (state & posGoalState) == posGoalState && ((~state) & negGoalState) == negGoalState;
    }

    public List<Action> getApplicableActions(int state) {
        List<Action> applicableActionList = new ArrayList<>();
        for (Action action : actionMap.values()) {
            if (action.isApplicableIn(state)) {
                applicableActionList.add(action);
            }
        }
//        Collections.sort(applicableActionList, (o1, o2) -> o1.getName().compareTo(o2.getName()));
//        Collections.shuffle(applicableActionList);
        return applicableActionList;
    }

    public List<Integer> applyActionInState(Action action, int state) {
        int posBooleanEff = action.getPosBooleanEff();
        int negBooleanEff = action.getNegBooleanEff();
        int posNumericEff = action.getPosNumericEff();
        int negNumericEff = action.getNegNumericEff();
        List<Integer> successorStates = new ArrayList<>(util.countOnes(negNumericEff));
        int newState = (state | posBooleanEff | posNumericEff) & (~negBooleanEff);
        successorStates.add(newState);
        for (int k = 0; k < numBits; ++k) {
            int bitMask = 1 << k;
            if ((negNumericEff & bitMask) == 0) {
                continue;
            }
            int length = successorStates.size();
            for (int i = 0; i < length; ++i) {
                successorStates.add(successorStates.get(i) & (~bitMask));
            }
        }
        return successorStates;
    }

    public String getStateAsString(int state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numBits; ++i) {
            int bitMask = 1 << i;
            String var = indexToVar.get(i);
            if (booleanVars.contains(var)) {
                sb.append(var).append(" = ");
                sb.append((state & bitMask) == 0 ? "false" : "true");
            } else {
                sb.append(var);
                sb.append((state & bitMask) == 0 ? " = 0" : " > 0");
            }
            if (i < numBits - 1)
                sb.append(", ");
        }
        return sb.toString();
    }
}
