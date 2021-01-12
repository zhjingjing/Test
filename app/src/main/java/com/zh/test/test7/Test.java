package com.zh.test.test7;

/**
 * @Author: Administrator
 * @Time 2021/1/12 001219:47
 * @Email: zhaohang@zhizhangyi.com
 * @Describe: 1.class1跟class2是同一类型 则返回true
 * 2.class1是class2的超类或超接口 则返回true
 */
public class Test implements TestInterface {
    public static void main(String[] args) {
        System.out.println(Test.class.isAssignableFrom(TestInterface.class));
        System.out.println(TestInterface.class.isAssignableFrom(Test.class));
        System.out.println(Test.class.isAssignableFrom(ChildTest.class));
        System.out.println(TestInterface.class.isAssignableFrom(ChildTest.class));
    }

    @Override
    public void doTest() {

    }

    public void doTest1() {

    }
}
