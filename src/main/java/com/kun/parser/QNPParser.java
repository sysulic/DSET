package com.kun.parser;

import com.kun.Action;
import com.kun.QNPProblem;
import com.kun.auxiliary.Pair;
import com.kun.auxiliary.Quadruple;
import lombok.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

@Data
public class QNPParser {

    private String filepath;

    private int types;

    private Map<String, Integer> varOrder;

    private Set<String> booleanVars;

    private Set<String> numericVars;

    private Quadruple<Integer, Integer, Integer, Integer> quadruple;

    /*
    public static void main(String[] args) {
        String separator = File.separator;
        // String domainPath = QNPParser.class.getClassLoader().getResource("").getPath() + separator + "Domain" + separator;
        String domainPath = "D:\\0Files\\202106\\梁艺坤打包\\QNPSolver4.0\\QNPSolver4.0\\src\\main\\resources" +separator + "Domain" + separator;
        String filename = domainPath + "Gripper.qnp";
        QNPParser parser = new QNPParser(filename);
        QNPProblem problem = parser.parse();
        System.out.println(problem.getDomainName());
        System.out.println(problem.getInitState());
        System.out.println(problem.getPosGoalState());
        System.out.println(problem.getNegGoalState());
        for (Map.Entry<String, Action> entry : problem.getActionMap().entrySet()) {
            System.out.println(entry);
        }
    }
    */

    public QNPParser(String filepath) {
        this.filepath = filepath;
    }

    private void init(int varCount) {
        int size = (int) (varCount / 0.75) + 1;
        varOrder = new HashMap<>(size);
        numericVars = new HashSet<>(size);
        booleanVars = new HashSet<>(size);
        quadruple = new Quadruple<>();
    }

    private void handleVariableDefinition(String[] items) {
        int varCount = Integer.parseInt(items[0]);
        init(varCount);
        for (int i = 0; i < varCount; ++i) {
            int j = 2 * i + 1;
            varOrder.put(items[j], i);
            if (items[j + 1].equals("1")) {
                numericVars.add(items[j]);
                types |= (1 << i);
            } else {
                booleanVars.add(items[j]);
            }
        }
    }

    private int handleInitialState(String[] items) {
        int varCount = Integer.parseInt(items[0]);
        int initState = 0;
        for (int i = 0; i < varCount; ++i) {
            int j = 2 * i + 1;
            if (items[j + 1].equals("1")) {
                initState |= 1 << varOrder.get(items[j]);
            }
        }
        return initState;
    }

    private void handleGoalState(String[] items) {
        int varCount = Integer.parseInt(items[0]);
        int positiveState = 0;
        int negativeState = 0;
        for (int i = 0; i < varCount; ++i) {
            int j = 2 * i + 1;
            if (items[j + 1].equals("1")) {
                positiveState |= 1 << varOrder.get(items[j]);
            } else {
                negativeState |= 1 << varOrder.get(items[j]);
            }
        }
        quadruple.setAll(positiveState, negativeState, 0, 0);
    }

    private void handleActionEffect(String[] items) {
        int varCount = Integer.parseInt(items[0]);
        int posNumericEff = 0;
        int posBooleanEff = 0;
        int negNumericEff = 0;
        int negBooleanEff = 0;
        for (int i = 0; i < varCount; ++i) {
            String var = items[2 * i + 1];
            String op = items[2 * i + 2];
            if (op.equals("1")) {
                if (numericVars.contains(var)) {
                    posNumericEff |= 1 << varOrder.get(var);
                } else {
                    posBooleanEff |= 1 << varOrder.get(var);
                }
            } else {
                if (numericVars.contains(var)) {
                    negNumericEff |= 1 << varOrder.get(var);
                } else {
                    negBooleanEff |= 1 << varOrder.get(var);
                }
            }
        }
        quadruple.setAll(posBooleanEff, posNumericEff, negBooleanEff, negNumericEff);
    }

    private Action handleAction(String[] actionExprs) {
        String name = actionExprs[0];
        String[] preItems = actionExprs[1].strip().split(" ");
        String[] effItems = actionExprs[2].strip().split(" ");
        handleGoalState(preItems); // handle preconditions of an action
        int posPre = quadruple.getFirst();
        int negPre = quadruple.getSecond();
        handleActionEffect(effItems); // handle effects of an action
        int posBooleanEff = quadruple.getFirst();
        int posNumericEff = quadruple.getSecond();
        int negBooleanEff = quadruple.getThird();
        int negNumericEff = quadruple.getFourth();
        return new Action(name, posPre, negPre, posBooleanEff, posNumericEff, negBooleanEff, negNumericEff);
    }

    public QNPProblem parse() {
        BufferedReader reader = null;
        QNPProblem problem = null;
        try {
            reader = new BufferedReader(new FileReader(new File(filepath)));
            String domainName = reader.readLine().strip();

            String[] variableLine = reader.readLine().strip().split(" ");
            handleVariableDefinition(variableLine);

            String[] initStateLine = reader.readLine().strip().split(" ");
            int initState = handleInitialState(initStateLine);

            String[] goalStateLine = reader.readLine().strip().split(" ");
            handleGoalState(goalStateLine);
            int posGoal = quadruple.getFirst();
            int negGoal = quadruple.getSecond();

            int numActions = Integer.parseInt(reader.readLine().strip());
            Map<String, Action> actionMap = new HashMap<>();
            for (int i = 0; i < numActions; ++i) {
                String name = reader.readLine();
                String pre = reader.readLine();
                String eff = reader.readLine();
                Action action = handleAction(new String[]{name, pre, eff});
                actionMap.put(name, action);
            }

            Map<Integer, String> index2Var = new HashMap<>();
            for (Map.Entry<String, Integer> entry : varOrder.entrySet()) {
                index2Var.put(entry.getValue(), entry.getKey());
            }

            problem = new QNPProblem(domainName, initState, posGoal, negGoal, actionMap, varOrder.size(), index2Var, booleanVars, types);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return problem;
    }

}