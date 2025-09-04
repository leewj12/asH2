package com.inpro.asBoard.auth;

import com.inpro.asBoard.security.JwtTokenProvider;
import com.inpro.asBoard.user.UserAccount;
import com.inpro.asBoard.user.UserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder pw; // ✅ 로그인 시 비번 검증에 사용
    private final JwtTokenProvider jwt;

    private final PasswordEncoder passwordEncoder; // ✅ 회원가입 시 비번 해시에 사용 (같은 빈을 2개 필드로 주입 중)

    /** 로컬: false, 운영(HTTPS): true 로 설정 */
    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    /* ====== 요청/응답 DTO(레코드) ====== */
    record SignupReq(String username, String password, String passwordConfirm) {}
    record SimpleRes(boolean ok, String message) {}

    record LoginReq(String username, String password) {}
    record LoginRes(String username, java.util.List<String> roles) {}

    //========================================================================
    // 1. 로그인 API
    // 2. URL : [POST]{...}/api/auth/login
    // 3. Param : req(body, required, LoginReq)
    //            - username (required, String)
    //            - password (required, String)
    // 4. 설명 : 자격 검증 후 액세스/리프레시 토큰 발급 및 쿠키 설정
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req, HttpServletResponse res) {
        var accOpt = userMapper.findByUsername(req.username());
        if (accOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid");
        var acc = accOpt.get();
        if (!acc.isActive() || !pw.matches(req.password(), acc.getPasswordHash()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid");

        // 권한 CSV → 리스트
        var roles = java.util.List.of(acc.getRoles().split("\\s*,\\s*"));

        // JWT 발급 (만료시간은 JwtTokenProvider 설정 사용)
        String access  = jwt.createAccess(acc.getUsername(), roles);
        String refresh = jwt.createRefresh(acc.getUsername());

        // ⚠️ httpOnly(false) 이면 JS로 쿠키를 읽을 수 있음. 보안상 가능하면 true 권장(프론트가 쿠키를 직접 읽지 않는 구조라면 true로)
        var accessCookie = ResponseCookie.from("accessToken", access)
                .httpOnly(true)                       // ✅ 권장: true
                .secure(cookieSecure)
                .sameSite("Lax")                       // 크로스 사이트 POST가 필요하면 "None; Secure" 고려
                .path("/")                             // ⬅️ Access는 루트 경로
                .maxAge(Duration.ofMinutes(15))        // ⚠️ 토큰 TTL과 일치 권장
                .build();

        // refresh는 리프레시 엔드포인트로만 자동 전송되게 path를 좁힘
        var refreshCookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)                       // ✅ 권장: true
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth/refresh")             // ⬅️ 이 경로에만 붙음
                .maxAge(Duration.ofDays(14))
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 바디로는 최소 정보만
        return ResponseEntity.ok(new LoginRes(acc.getUsername(), roles));
    }

    //========================================================================
    // 1. 액세스 토큰 재발급 API
    // 2. URL : [POST]{...}/api/auth/refresh
    // 3. Param : refreshToken(cookie, required)
    // 4. 설명 : refreshToken 검증 성공 시 새 액세스 토큰 갱신
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name="refreshToken", required=false)
                                         String refresh, HttpServletResponse res) {
        if (refresh == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            // jwt.getUsername(refresh)는 서명/만료 검증을 통과해야만 성공
            String username = jwt.getUsername(refresh);

            // 최신 권한 로드(권한 변경 반영)
            String rolesCsv = userMapper.findByUsername(username)
                    .map(UserAccount::getRoles).orElse("ROLE_USER");


            var roles = java.util.List.of(rolesCsv.split("\\s*,\\s*"));

            // 새 Access 발급 + 쿠키 재세팅
            String newAccess = jwt.createAccess(username, roles);

            var accessCookie = ResponseCookie.from("accessToken", newAccess)
                    .httpOnly(true) // ✅ 권장: true
                    .secure(cookieSecure)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofMinutes(15)) // ⚠️ 토큰 TTL과 일치 권장
                    .build();
            res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            // 파싱/만료 실패 → 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    //========================================================================
    // 1. 로그아웃 API
    // 2. URL : [POST]{...}/api/auth/logout
    // 3. Param :
    // 4. 설명 : 액세스/리프레시 쿠키 제거
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse res) {
        var clearAccess  = ResponseCookie.from("accessToken","")
                .httpOnly(true) // ✅ 권장: true
                .secure(cookieSecure)
                .sameSite("Lax").path("/")
                .maxAge(0)
                .build();

        var clearRefresh = ResponseCookie.from("refreshToken","")
                .httpOnly(true) // ✅ 권장: true
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
        return ResponseEntity.ok().build();
    }

    //========================================================================
    // 1. 로그인 상태 조회 API
    // 2. URL : [GET]{...}/api/auth/me
    // 3. Param :
    // 4. 설명 : 인증 여부/아이디/권한 목록 반환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(a -> a.getAuthority()).toList()
        ));
    }

    //========================================================================
    // 1. 회원가입 API
    // 2. URL : [POST]{...}/api/auth/signup
    // 3. Param : req(body, required, SignupReq)
    //            - username (required, String)
    //            - password (required, String)
    //            - passwordConfirm (required, String)
    // 4. 설명 : 서버 검증/중복 체크 후 사용자 생성(ROLE_USER)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupReq req) {
        // 1) 서버 검증
//        if (req.username() == null || !req.username().matches("^[a-zA-Z0-9._-]{4,30}$"))
//            return ResponseEntity.badRequest().body(new SimpleRes(false,"아이디 형식이 올바르지 않습니다."));
//        if (req.password() == null || req.password().length() < 8)
//            return ResponseEntity.badRequest().body(new SimpleRes(false,"비밀번호는 8자 이상이어야 합니다."));
        if (!req.password().equals(req.passwordConfirm()))
            return ResponseEntity.badRequest().body(new SimpleRes(false,"비밀번호가 일치하지 않습니다."));

        // 2) 중복 체크
        if (userMapper.findByUsername(req.username()).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleRes(false,"이미 사용 중인 아이디입니다."));

        // 3) 저장 (일반 가입자는 ROLE_USER만)
        UserAccount u = UserAccount.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password())) // 해시 저장
                .roles("ROLE_USER")
                .active(true)
                .build();
        userMapper.insert(u);

        return ResponseEntity.ok(new SimpleRes(true,"가입 완료"));
    }
}
