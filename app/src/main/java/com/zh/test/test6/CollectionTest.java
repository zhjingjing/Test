package com.zh.test.test6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @Author: zh
 * @Time 2021/1/8 000817:43
 * @Email: zhaohang@zhizhangyi.com
 * @Describe:
 */
public class CollectionTest {
    public static void main(String[] args) {
        Collection collection=getCollection(new String[]{"1","3","5"});
        Collection collection2=getCollection(new String[]{"1","4","2"});
        System.out.println(collection.retainAll(collection2));
        System.out.println(collection);
    }

    public static Collection<String> getCollection(String[] arrays){
        Collection collection=new ArrayList();
        collection.addAll(Arrays.asList(arrays));
        return collection;
    }
}
