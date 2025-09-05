package com.inpro.asBoard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt;

    //========================================================================
    // 1. JWT 인증 필터
    // 2. URL : (Filter) 모든 요청
    // 3. Param : Authorization(Bearer <JWT>) 헤더(optional),
    //            accessToken 쿠키(optional)
    // 4. 설명 : JWT 유효 시 SecurityContext에 인증 정보 설정
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        //========================================================================
        // 1. JWT 인증 처리
        // 2. URL : (Filter) 모든 요청
        // 3. Param : Authorization 헤더, accessToken 쿠키
        // 4. 설명 : 헤더/쿠키에서 토큰 추출 → 검증 → SecurityContext 설정 → 체인 진행
        // 5. 작성 : wjlee(25.09.01)
        // 6. 수정 :
        //========================================================================

        String token = null;

        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) token = h.substring(7);

        if (token == null && req.getCookies() != null) {          // ✅ 쿠키 지원
            for (var c : req.getCookies()) {
                if ("accessToken".equals(c.getName())) { token = c.getValue(); break; }
            }
        }

        if (token != null) {
            try {
                String username = jwt.getUsername(token);
                var roles = jwt.getRoles(token).stream()
                        .map(SimpleGrantedAuthority::new).toList();
                var auth = new UsernamePasswordAuthenticationToken(username, null, roles);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignore) {}
        }
        chain.doFilter(req, res);
    }
}

