package com.spark.test.system;

import com.spark.bean.system.vo.DepartmentVO;
import com.spark.bean.system.tree.DepartmentTree;
import com.spark.manage.system.IDepartmentService;
import com.spark.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 20:20
 */
@SpringBootTest
public class DepartmentTest {
    @Autowired
    private IDepartmentService departmentService;

    /**
     * 测试创建部门
     */
    @Test
    public void testDepartment() {
        DepartmentVO departmentVO = new DepartmentVO();
        departmentVO.setName("星火科技研发部后端组");
        departmentVO.setPrtId(502L);
        departmentService.createDepartment(departmentVO);
    }

    /**
     * 测试部门树
     */
    @Test
    public void testDpetTree() {
        DepartmentTree o1 = new DepartmentTree();
        o1.setId(102L);
        o1.setPrtId(0L);
        o1.setName("星火科技");

        DepartmentTree o2 = new DepartmentTree();
        o2.setId(202L);
        o2.setPrtId(0L);
        o2.setName("超火科技");

        DepartmentTree o3 = new DepartmentTree();
        o3.setId(302L);
        o3.setPrtId(102L);
        o3.setName("星火科技销售部");

        DepartmentTree o4 = new DepartmentTree();
        o4.setId(402L);
        o4.setPrtId(202L);
        o4.setName("超火科技研发部");

        DepartmentTree o5 = new DepartmentTree();
        o5.setId(502L);
        o5.setPrtId(102L);
        o5.setName("星火科技研发部");

        DepartmentTree o6 = new DepartmentTree();
        o6.setId(602L);
        o6.setPrtId(502L);
        o6.setName("星火科技研发部后端组");

        List<DepartmentTree> nodes = new ArrayList<>();
        nodes.add(o1);
        nodes.add(o2);
        nodes.add(o3);
        nodes.add(o4);
        nodes.add(o5);
        nodes.add(o6);

        List<DepartmentTree> newNodes = new ArrayList<>();
        for (DepartmentTree node : nodes) {
            // 找出父节点
            if (0L == node.getPrtId()) {
                // 调用递归方法填充子节点列表
                recursionDepartment(node, nodes);
                newNodes.add(node);
            }
        }
        System.out.println(JsonUtil.toString(newNodes));
    }

    /**
     * 递归方法
     * @param tree 父节点对象
     * @param trees 所有的List
     * @return
     */
    public void recursionDepartment(DepartmentTree tree, List<DepartmentTree> trees) {
        // 获取tree的子部门
        if (!hasChild(trees, tree)) {
            return;
        }
        List<DepartmentTree> childList =  this.getChildList(tree, trees);
        for (DepartmentTree tChild : childList) {
            recursionDepartment(tChild, trees);
        }
        tree.setChildren(childList);
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<DepartmentTree> list, DepartmentTree t) {
        return getChildList(t, list).size() > 0;
    }

    /**
     * 得到子节点列表
     */
    private List<DepartmentTree> getChildList(DepartmentTree node, List<DepartmentTree> trees) {
        List<DepartmentTree> tlist = new ArrayList<>();
        for (DepartmentTree tree : trees) {
            if (node.getId().equals(tree.getPrtId())) {
                tlist.add(tree);
            }
        }
        return tlist;
    }
}
