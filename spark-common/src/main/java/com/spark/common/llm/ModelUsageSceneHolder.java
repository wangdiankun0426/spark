package com.spark.common.llm;

import com.spark.common.enums.ModelUsageSceneEnum;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/10/27 10:00:00
 * 模型调用场景线程栈，内层场景覆盖外层、退出时恢复外层
 */
public class ModelUsageSceneHolder {

    private static final ThreadLocal<Deque<ModelUsageSceneEnum>> SCENE_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private ModelUsageSceneHolder() {
    }

    /**
     * 入栈当前调用场景
     * @param scene 调用场景
     */
    public static void push(ModelUsageSceneEnum scene) {
        if (scene != null) {
            SCENE_STACK.get().push(scene);
        }
    }

    /**
     * 出栈恢复外层场景
     */
    public static void pop() {
        Deque<ModelUsageSceneEnum> deque = SCENE_STACK.get();
        if (!deque.isEmpty()) {
            deque.pop();
        }
    }

    /**
     * 获取当前场景，无场景返回OTHER
     * @return 当前场景
     */
    public static ModelUsageSceneEnum current() {
        Deque<ModelUsageSceneEnum> deque = SCENE_STACK.get();
        ModelUsageSceneEnum scene = deque.peek();
        return scene == null ? ModelUsageSceneEnum.OTHER : scene;
    }

    /**
     * 清空线程场景，线程池回收时兜底
     */
    public static void clear() {
        SCENE_STACK.remove();
    }
}
