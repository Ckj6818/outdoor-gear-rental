package com.outdoor.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiConsultRequestDTO {

    @NotBlank(message = "提问内容不能为空")
    private String question;
}
