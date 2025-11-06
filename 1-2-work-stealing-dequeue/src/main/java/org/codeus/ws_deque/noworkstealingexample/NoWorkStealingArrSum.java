package org.codeus.ws_deque.noworkstealingexample;

import java.util.function.Supplier;

import static org.codeus.ws_deque.SumArrayUtils.getFilledArr;
import static org.codeus.ws_deque.SumArrayUtils.sum;

public class NoWorkStealingArrSum {
    private static long benchmarkExecutionTimeAndExecute(Supplier<Long> sumFunc) {
        long before = System.currentTimeMillis();
        Long sum = sumFunc.get();
        long after = System.currentTimeMillis();
        int millisecondsTook = (int) (after - before);
        System.out.println("Milliseconds took: " + millisecondsTook);
        return sum;
    }

    public long returnSumWithoutWorkStealing(int arraySize) {
        int[] arrayToSum = getFilledArr(arraySize);
        Supplier<Long> sumFunc = () -> sum(arrayToSum);

        return benchmarkExecutionTimeAndExecute(sumFunc);
    }

}
