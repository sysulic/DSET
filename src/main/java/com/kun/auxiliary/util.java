package com.kun.auxiliary;

public class util {

    public static int countOnes(int n) {
        int count = 0;
        while (n > 0) {
            count++;
            n &= (n - 1);
        }
        return count;
    }
}
