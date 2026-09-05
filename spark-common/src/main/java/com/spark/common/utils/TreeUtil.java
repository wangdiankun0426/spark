package com.spark.common.utils;

import com.spark.common.bean.base.BaseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 11:29
 */
public class TreeUtil<T extends BaseTree> {
    /**
     * 转为树列表
     * @param list
     * @return
     */
    public List<T> convertTreeList(List<T> list) {
        List<T> treeList = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)) {
            return treeList;
        }
        for (T r : list) {
            // 找出父节点
            if (0L == r.getPrtId()) {
                // 调用递归方法填充子节点列表
                recursion(r, list);
                treeList.add(r);
            }
        }
        return treeList;
    }

    /**
     * 递归方法
     * @param tree 父节点对象
     * @param trees 所有的List
     * @return
     */
    private void recursion(T tree, List<T> trees) {
        // 获取tree的子部门
        if (!hasChild(trees, tree)) {
            return;
        }
        List<T> childList =  this.getChildList(tree, trees);
        for (T tChild : childList) {
            recursion(tChild, trees);
        }
        tree.setChildren(childList);
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<T> list, T t) {
        return getChildList(t, list).size() > 0;
    }

    /**
     * 得到子节点列表
     */
    private List<T> getChildList(T node, List<T> trees) {
        List<T> oList = new ArrayList<>();
        for (T tree : trees) {
            if (node.getId().equals(tree.getPrtId())) {
                oList.add(tree);
            }
        }
        return oList;
    }
}
