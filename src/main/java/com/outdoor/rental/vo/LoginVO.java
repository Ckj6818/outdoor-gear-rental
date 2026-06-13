package com.outdoor.rental.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String username;
    private Integer role;
}
