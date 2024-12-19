package com.example.service;

import com.example.entity.dto.resp.AuthorizeRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;

/**
 * @author Nruonan
 * @description
 */
public interface AuthService {

    AuthorizeRespDTO login(String username, String password, HttpServletRequest request);
}
