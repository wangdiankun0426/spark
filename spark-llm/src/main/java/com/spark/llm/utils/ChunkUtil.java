package com.spark.llm.utils;

import com.spark.utils.CollectionUtil;
import com.spark.utils.StringUtil;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/7/25 20:47
 */
public class ChunkUtil {
    private final static Logger logger = LoggerFactory.getLogger(ChunkUtil.class);

    // 切块大小
    private static final int MAX_CHUNK_LEN = 250;
    // 重叠大小
    private static final int MAX_OVERLAP_LEN = 20;

    // 合并多个换行、空格、井号、制表符
    private static final Pattern[] PATTERNS = {
            Pattern.compile("\\n+"),
            Pattern.compile(" +"),
            Pattern.compile("#+"),
            Pattern.compile("\\t+")
    };

    private static final String[] REPLACEMENTS = {
            "\n", " ", "", ""
    };

    /**
     * 将文本切分为不超过 MAX_CHUNK_LEN 的块
     * @param content 待切分的文本
     * @param size 切块大小
     * @param overlap 重叠大小
     * @return 切分后的字符串列表
     */
    public static List<String> handleChunk(String content, Integer size, Integer overlap) {
        try {
            if (StringUtil.isBlank(content)) {
                return new ArrayList<>();
            }
            if (size == null) {
                size = MAX_CHUNK_LEN;
            }
            if (overlap == null) {
                overlap = MAX_OVERLAP_LEN;
            }
            //按段落拆分
            dev.langchain4j.data.document.Document document = dev.langchain4j.data.document.Document.from(content);
            DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(size, overlap);
            List<TextSegment> segments = splitter.split(document);
            if (CollectionUtil.isEmpty(segments)) {
                return new ArrayList<>();
            }
            //清洗数据
            List<String> chunks = new ArrayList<>();
            segments.forEach(segment -> {
                String text = segment.text();
                for (int i = 0; i < PATTERNS.length; i++) {
                    text = PATTERNS[i].matcher(text).replaceAll(REPLACEMENTS[i]);
                }
                chunks.add(text);
            });
            return chunks;
        } catch (Exception e) {
            logger.error("handleChunk error ", e);
            return new ArrayList<>();
        }
    }

}
