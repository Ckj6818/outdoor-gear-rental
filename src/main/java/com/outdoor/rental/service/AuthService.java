package com.outdoor.rental.service;

import com.outdoor.rental.dto.LoginDTO;
import com.outdoor.rental.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);
}
