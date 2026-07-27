package com.spark.web.controller.kb;

import com.spark.bean.base.ResultData;
import com.spark.bean.kb.query.DocumentEventQuery;
import com.spark.bean.kb.result.DocumentEventResult;
import com.spark.bean.kb.vo.DocumentEventVO;
import com.spark.kb.service.IDocumentEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/8/18 12:42
 */
@RestController
@RequestMapping("kb/document/event")
public class DocumentEventController {
    @Autowired
    private IDocumentEventService documentEventService;

    /**
     * 查询文档事件详情
     * @param query 查询参数
     * @return 文档事件详情
     */
    @GetMapping("detail")
    public ResultData<DocumentEventResult> queryDocumentEventDetail(DocumentEventQuery query) {
        return documentEventService.queryDocumentEventDetail(query);
    }

    /**
     * 修改文档事件
     * @param documentEventVO 文档事件参数
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateDocumentEvent(DocumentEventVO documentEventVO) {
        return documentEventService.updateDocumentEvent(documentEventVO);
    }
}
