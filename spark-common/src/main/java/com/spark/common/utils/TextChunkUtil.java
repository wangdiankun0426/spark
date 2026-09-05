package com.spark.common.utils;

import com.spark.common.enums.ChunkStrategyEnum;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.document.splitter.DocumentByLineSplitter;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentByWordSplitter;
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
public class TextChunkUtil {
    private final static Logger logger = LoggerFactory.getLogger(TextChunkUtil.class);

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
     * 将文本切分为不超过 MAX_CHUNK_LEN 的块（默认按段落分割）
     * @param content 待切分的文本
     * @param size 切块大小
     * @param overlap 重叠大小
     * @return 切分后的字符串列表
     */
    public static List<String> handleChunk(String content, Integer size, Integer overlap) {
        return handleChunk(content, size, overlap, null);
    }

    /**
     * 将文本切分为不超过指定大小的块
     * @param content 待切分的文本
     * @param size 切块大小
     * @param overlap 重叠大小
     * @param strategy 分块策略（null则使用默认的PARAGRAPH）
     * @return 切分后的字符串列表
     */
    public static List<String> handleChunk(String content, Integer size, Integer overlap, String strategy) {
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
            // 获取分块策略
            ChunkStrategyEnum strategyEnum = ChunkStrategyEnum.indexOf(strategy);
            // 创建分割器
            DocumentSplitter splitter = createSplitter(strategyEnum, size, overlap);
            // 执行分割
            Document document = Document.from(content);
            List<TextSegment> segments = splitter.split(document);
            if (CollectionUtil.isEmpty(segments)) {
                return new ArrayList<>();
            }
            // 清洗数据
            List<String> chunks = new ArrayList<>();
            segments.forEach(segment -> {
                String text = segment.text();
                for (int i = 0; i < PATTERNS.length; i++) {
                    text = PATTERNS[i].matcher(text).replaceAll(REPLACEMENTS[i]);
                }
                if (!text.trim().isEmpty()) {
                    chunks.add(text);
                }
            });
            return chunks;
        } catch (Exception e) {
            logger.error("handleChunk error ", e);
            return new ArrayList<>();
        }
    }

    /**
     * 创建分割器
     * @param strategy 分块策略
     * @param size 分块大小
     * @param overlap 重叠大小
     * @return 分割器实例
     */
    private static DocumentSplitter createSplitter(ChunkStrategyEnum strategy, int size, int overlap) {
        return switch (strategy) {
            case LINE -> new DocumentByLineSplitter(size, overlap);
            case SENTENCE -> new DocumentBySentenceSplitter(size, overlap);
            case WORD -> new DocumentByWordSplitter(size, overlap);
            case CHARACTER -> new DocumentByCharacterSplitter(size, overlap);
            default -> new DocumentByParagraphSplitter(size, overlap);
        };
    }

}
