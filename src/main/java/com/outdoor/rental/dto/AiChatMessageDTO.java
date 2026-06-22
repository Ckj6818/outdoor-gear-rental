package com.outdoor.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * AI 导购多轮对话中的单条消息。
 */
@Data
public class AiChatMessageDTO {

    @NotBlank(message = "消息角色不能为空")
    @Pattern(regexp = "user|assistant", message = "角色只能是 user 或 assistant")
    private String role;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
