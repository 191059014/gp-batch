package com.hb.batch;

/**
 * ========== Description ==========
 *
 * @author Mr.huang
 * @version com.hb.batch.OrderQueryTaskTest.java, v1.0
 * @date 2019年08月28日 14时37分
 */
public class OrderQueryTaskTest {

    public static void main(String[] args) throws InterruptedException {
        Long lastQueryTime = System.currentTimeMillis();
        System.out.println(lastQueryTime);
        test(lastQueryTime);
        System.out.println(lastQueryTime);
    }

    private static void test(Long lastQueryTime) throws InterruptedException {
        Thread.sleep(100);
        lastQueryTime = System.currentTimeMillis();
        System.out.println(lastQueryTime);
    }

}
