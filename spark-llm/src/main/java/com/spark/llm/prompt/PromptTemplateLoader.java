package com.spark.prompt;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/9/24 17:51
 */
public class PromptTemplateLoader {

    /**
     * 从 classpath 下的文件加载 PromptTemplate
     */
    public static PromptTemplate fromClasspath(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return PromptTemplate.from(template);
        } catch (IOException e) {
            throw new RuntimeException("failed to load prompt template from: " + filePath, e);
        }
    }

    /**
     * 从文件路径创建 Prompt 并填充变量
     */
    public static Prompt create(String filePath, Map<String, Object> variables) {
        PromptTemplate template = fromClasspath(filePath);
        return template.apply(variables);
    }
}
