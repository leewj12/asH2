package com.inpro.asBoard.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // @PreAuthorize 등 메서드 단위 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter; // ✅ JWT를 읽어 인증 객체를 주입하는 커스텀 필터

    //========================================================================
    //  * 운영(HTTPS)에서는 반드시 true.
    //  * - accessToken/refreshToken 삭제/발급 모두 동일 설정을 써야 함.
    //  * - application.yml: app.jwt.cookie-secure: true(운영), false(로컬)
    //========================================================================
    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;


    //========================================================================
    // 1. 전역 보안 체인 설정
    // 2. URL : (Security) 전역 필터 체인
    // 3. Param :
    // 4. 설명 : Stateless / URL 인가 / 예외 처리 / 로그아웃 / JWT 필터 배치
    //    * 전역 보안 체인
    //     * - 세션을 쓰지 않는 완전 Stateless 구성
    //     * - HTML(SSR)과 API를 Accept 헤더로 구분해 401/403 vs 리다이렉트 처리
    //     * - GET /logout 지원(주소창으로도 로그아웃)하며 쿠키 즉시 무효화
    //     * - UsernamePasswordAuthenticationFilter 이전에 JWT 필터 연결
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                /* 1) CSRF 비활성화
                 * - 순수 API + JWT 쿠키 사용 시 일반적으로 비활성화.
                 * - 만약 폼 POST(쿠키 기반) 사용 시에는 CSRF 토큰 고려 필요.
                 */
                .csrf(csrf -> csrf.disable())

                /* 2) 세션 완전 차단 (JWT만으로 인증 유지) */
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* 3) URL 인가 규칙 */
                .authorizeHttpRequests(auth -> auth
                        // 🔓 뷰/정적 + 로그인/가입 + 에러 + 로그아웃은 누구나 접근
                        .requestMatchers("/", "/login", "/signup", "/error", "/logout",
                                "/favicon.ico", "/css/**", "/js/**", "/images/**", "/403"
                                , "/**"
                        ).permitAll()

                        // 🔓 인증/토큰 관련 API는 공개
                        .requestMatchers("/api/auth/**").permitAll()

                        // 🔐 AS 도메인: ADMIN 또는 USER
//                        .requestMatchers("/as/**").hasRole("ADMIN")
//                        .requestMatchers("/as/**").hasAnyRole("ADMIN", "USER")

                        // 🔐 이외 전부(SSR 포함) 인증 필요
                        .anyRequest().authenticated()    // 나머지(SSR 포함) 보호
                )

                /* 4) 예외 처리(인증 실패/인가 실패)
                 * - API(/api/**) 또는 Accept: text/html 이 아닌 경우 → 순수 401/403 반환
                 * - 그 외(브라우저 HTML 요청) → 적절한 페이지로 리다이렉트
                 */
                .exceptionHandling(e -> e
                        // 인증 실패(익명) → 401 또는 /login 리다이렉트(+redirect 파라미터)
                        .authenticationEntryPoint((req, res, ex) -> {
                            String uri = req.getRequestURI();
                            String accept = req.getHeader("Accept");
                            boolean wantsHtml = accept != null && accept.contains("text/html");

                            // 🚫 /api/* 는 JSON 401
                            if (uri.startsWith("/api/") || !wantsHtml) {
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                return;
                            }
                            // 🚫 /login 또는 /error로 들어온 건 다시 /login만 (redirect 파라미터 금지)
                            if (uri.equals("/login") || uri.startsWith("/error")
                                    || req.getDispatcherType() == jakarta.servlet.DispatcherType.ERROR) {
                                res.sendRedirect("/login");
                                return;
                            }
                            // ✅ 최초 요청 경로를 redirect 파라미터로 1회 전달(길이 제한)
                            String qs = req.getQueryString();
                            String dest = uri + (qs != null ? "?" + qs : "");
                            if (dest.length() > 1800) dest = "/";
                            String redir = java.net.URLEncoder.encode(dest, java.nio.charset.StandardCharsets.UTF_8);
                            res.sendRedirect("/login?redirect=" + redir);
                        })

                        // 인가 실패(권한 부족) → 403 또는 /403 리다이렉트
                        .accessDeniedHandler((req, res, ex) -> {
                            String uri = req.getRequestURI();
                            String accept = req.getHeader("Accept");
                            boolean wantsHtml = accept != null && accept.contains("text/html");
                            if (uri.startsWith("/api/") || !wantsHtml) {
                                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                            } else {
                                res.sendRedirect("/403");
                            }
                        })
                )

                /* 5) 로그아웃
                 * - 주소창 GET /logout 으로도 로그아웃 허용
                 * - accessToken(/) & refreshToken(/api/auth/refresh) 각각의 경로로 쿠키 무효화
                 * - SameSite=Lax, Secure=프로퍼티 기반
                 */
                .logout(logout -> logout
                        .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout", "GET"))
                        .addLogoutHandler((req, res, auth) -> {
                            // accessToken: path "/"
                            var clearAccess = org.springframework.http.ResponseCookie.from("accessToken","")
                                    .httpOnly(true)
                                    .secure(cookieSecure) // 운영은 true(HTTPS 필수), 로컬은 false
                                    .sameSite("Lax")
                                    .path("/") // ⬅️ Access 토큰은 path "/"
                                    .maxAge(0).build();

                            // refreshToken: path "/api/auth/refresh"
                            var clearRefresh = org.springframework.http.ResponseCookie.from("refreshToken","")
                                    .httpOnly(true).secure(cookieSecure).sameSite("Lax")
                                    .path("/api/auth/refresh") // ⬅️ Refresh 토큰은 이 path
                                    .maxAge(0) // 즉시 만료
                                    .build();
                            res.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, clearAccess.toString());
                            res.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, clearRefresh.toString());
                        })
                        // 성공 시 홈으로. (?logout 같은 쿼리 문자열을 붙이지 않음)
                        .logoutSuccessHandler((req, res, auth) -> res.sendRedirect("/")) // ?logout 안 붙임
                        .permitAll()
                )
                /* 6) 필터 체인: UsernamePasswordAuthenticationFilter 이전에 JWT 인증 필터 배치
                 * - 요청마다 쿠키의 JWT를 검증, SecurityContext에 Authentication 주입
                 */
                .addFilterBefore(jwtAuthFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //========================================================================
    // 1. PasswordEncoder 빈
    // 2. URL : (Bean) 내부 주입
    // 3. Param :
    // 4. 설명 : BCryptPasswordEncoder 제공
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Bean PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //========================================================================
    // 1. AuthenticationManager 빈
    // 2. URL : (Bean) 내부 주입
    // 3. Param : conf(AuthenticationConfiguration)
    // 4. 설명 : AuthenticationConfiguration로부터 AuthenticationManager 획득
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration conf) throws Exception {
        return conf.getAuthenticationManager();
    }
}


