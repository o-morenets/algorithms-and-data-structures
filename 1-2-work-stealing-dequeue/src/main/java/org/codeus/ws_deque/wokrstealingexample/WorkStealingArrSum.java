package org.codeus.ws_deque.wokrstealingexample;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static org.codeus.ws_deque.SumArrayUtils.getFilledArr;
import static org.codeus.ws_deque.SumArrayUtils.sum;

public class WorkStealingArrSum {

    public long returnSumUsingWorkStealing(int arraySize) {
        long before = System.currentTimeMillis();

        ForkJoinPool pool = ForkJoinPool.commonPool();
        int[] arrToSum = getFilledArr(arraySize);

        var someTask = new LongRecursiveTask(arrToSum);
        Long sum = pool.invoke(someTask);
        pool.shutdown();

        long after = System.currentTimeMillis();
        int millisecondsTook = (int) (after - before);
        System.out.println("Milliseconds took: " + millisecondsTook);

        return sum;
    }

    private static class LongRecursiveTask extends RecursiveTask<Long> {
        private final int[] arrToSum;

        public LongRecursiveTask(int[] arrToSum) {
            this.arrToSum = arrToSum;
        }

        @Override
        protected Long compute() {
            if (arrToSum.length == 1) {
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
                return (long) arrToSum[0];
            }

            var leftArr = Arrays.copyOfRange(arrToSum, 0, arrToSum.length / 2);
            var rightArr = Arrays.copyOfRange(arrToSum, arrToSum.length / 2, arrToSum.length);

            var leftTask = new LongRecursiveTask(leftArr);
            var rightTask = new LongRecursiveTask(rightArr);

            return rightTask.compute() + leftTask.fork().join();
        }
    }
}
