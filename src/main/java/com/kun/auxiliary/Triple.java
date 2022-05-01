package com.kun.auxiliary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Triple <A, B, C> {
    private A first;
    private B second;
    private C third;
}