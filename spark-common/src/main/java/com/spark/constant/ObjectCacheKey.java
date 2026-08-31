package com.spark.constant;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 19:15
 */
public class ObjectCacheKey {
    /**
     * 序列化主键缓存key
     */
    public final static String SEQUENCE_PRIMARY="s_p_";

    /**
     * 部门code码前缀
     */
    public final static String DEPT_CODE = "d_c_";

    /**
     * 登录后用户sessionId前缀
     */
    public final static String LOGIN_SESSION = "l_s_";

    /**
     * 验证码值前缀
     */
    public final static String VALIDATE_CODE_KEY = "v_c_k_";

    /**
     * 大模型问答缓存key
     */
    public final static String LLM_CHAT_MEMORY = "c_m_";

    /**
     * 防抖缓存key
     */
    public final static String DEBOUNCE_KEY = "d_k_";

    /**
     * 分片上传会话元数据key
     */
    public final static String UPLOAD_CHUNK_META = "u_c_m_";

    /**
     * 知识图谱检索缓存key前缀
     */
    public final static String KG_RETRIEVE_CACHE = "k_g_r";
}
