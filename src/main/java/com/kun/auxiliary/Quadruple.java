package com.kun.auxiliary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quadruple <A, B, C, D> {
    private A first;
    private B second;
    private C third;
    private D fourth;

    public void setAll(A a, B b, C c, D d) {
        first = a;
        second = b;
        third = c;
        fourth = d;
    }
}
