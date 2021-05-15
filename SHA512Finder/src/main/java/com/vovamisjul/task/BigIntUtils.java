package com.vovamisjul.task;

import java.math.BigInteger;

public class BigIntUtils {
    private static final int START_CHAR = 33;
    private static final int END_CHAR = 126;
    private static final BigInteger CHARS_BASE = BigInteger.valueOf(END_CHAR - START_CHAR + 1);

    public static String bigIntToString(BigInteger value) {
        String result = "";

        BigInteger[] divResult = value.divideAndRemainder(CHARS_BASE);
        while (divResult[0].compareTo(BigInteger.ZERO) > 0) {
            result = bigIntToChar(divResult[1]) + result;
            divResult = divResult[0].divideAndRemainder(CHARS_BASE);
        }
        result = bigIntToChar(divResult[1]) + result;
        return result;
    }

    public static char bigIntToChar(BigInteger value) {
        return (char) (value.intValue() + START_CHAR);
    }
}
