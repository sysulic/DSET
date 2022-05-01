package com.kun;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class Action {
    /**
     * Name of this action
     */
    private String name;

    /**
     * Positive precondition, i.e., literals of the form 'x > 0' or 'x = true'
     */
    private int posPre;

    /**
     * Negative precondition, i.e., literals of the form 'x = 0' or 'x = false'
     */
    private int negPre;

    /**
     * Positive boolean effect, i.e., expressions of the form x = true'
     */
    private int posBooleanEff;

    /**
     * Positive numeric effect, i.e., expressions of the form 'x++'
     */
    private int posNumericEff;

    /**
     * Negative boolean effect, i.e., expressions of the form 'x = false'
     */
    private int negBooleanEff;

    /**
     * Negative numerical effect, i.e., expressions of the form 'x--'
     */
    private int negNumericEff;

//    /**
//     * @param node: the node that wraps the original qnp state, actually represented as a bitmap (an Integer in this project)
//     * @param abstractionMask: the binary mask that only concerns the remaining variables after the abstraction
//     * @return
//     */
//    public boolean isApplicableIn(Node node, int abstractionMask) {
//        int state = node.getState() & abstractionMask;
//        int posPre = this.posPre & abstractionMask;
//        int negPre = this.negPre & abstractionMask;
//        return (state & posPre) == posPre && ((~state) & negPre) == negPre;
//    }

    public boolean isApplicableIn(int state) {
        return (state & posPre) == posPre && ((~state) & negPre) == negPre;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Action && ((Action) obj).getName().equals(name));
    }

    public boolean hasNumericEffect() {
        return posNumericEff != 0 || negNumericEff != 0;
    }
}
