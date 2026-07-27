package com.spark.test;

import java.util.function.Function;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/16 16:58
 */
public class FunctionTest {

    public static void main(String[] args) {
        String ss = "123";
//      1.  Function<String, Integer> function = new Function<String, Integer>() {
//            @Override
//            public Integer apply(String s) {
//                return Integer.parseInt(ss);
//            }
//        };
//        Integer integer = toInteger(ss, function);
//      2.  Function<String, Integer> function = new Function<String, Integer>() {
//            @Override
//            public Integer apply(String s) {
//                return Integer.parseInt(ss);
//            }
//        };
//        Integer integer = toInteger(ss, function);
//     3.  Integer integer = toInteger(ss, (s) -> Integer.parseInt(s));
        Integer integer = toInteger(ss, Integer::parseInt);
        System.out.println(integer);
    }

    private static Integer toInteger(String ss, Function<String, Integer> function) {
        return function.apply(ss);
    }
}
