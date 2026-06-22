package com.outdoor.rental.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiConsultRequestDTO {

    @NotBlank(message = "提问内容不能为空")
    private String question;

    /** 多轮对话历史（不含当前 question），按时间正序，最多保留最近若干轮 */
    @Valid
    @Size(max = 12, message = "对话历史最多 12 条")
    private List<AiChatMessageDTO> history = new ArrayList<>();
}
