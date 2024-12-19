package com.example.service;

import com.example.entity.dto.resp.AuthorizeRespDTO;

/**
 * @author Nruonan
 * @description
 */
public interface AuthService {

    AuthorizeRespDTO login(String username, String password);
}
