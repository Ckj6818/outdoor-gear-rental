package com.outdoor.rental.controller;

import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.AiConsultRequestDTO;
import com.outdoor.rental.service.AiConsultantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 户外装备导购接口。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiConsultantController {

    private final AiConsultantService aiConsultantService;

    /**
     * AI 导购对话
     * POST /api/ai/consult
     * <p>
     * 直接返回大模型生成的 JSON 字符串，前端自行解析 reply 与 recommend_gears。
     * </p>
     */
    @PostMapping("/consult")
    public Result<String> consult(@RequestBody @Valid AiConsultRequestDTO dto) {
        return Result.success(aiConsultantService.consult(dto));
    }
}
