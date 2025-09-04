package com.inpro.asBoard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * JWT 생성/파싱 유틸
 * - HS256 대칭키 기반
 * - Access 토큰: roles 포함
 * - Refresh 토큰: 최소 정보(subject)만
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    /** ⚠️ 최소 32바이트(문자 32자 이상) 권장. Base64 문자열이면 더 안전. */
    @Value("${app.jwt.secret}")
    private String secret;

    /** Access Token 만료(초) */
    @Value("${app.jwt.access-exp-seconds}")
    private long accessExp;

    /** Refresh Token 만료(초) */
    @Value("${app.jwt.refresh-exp-seconds}")
    private long refreshExp;

    //========================================================================
    // 1. 서명키 생성
    // 2. URL : (Provider) 내부 호출
    // 3. Param :
    // 4. 설명 : HS256 서명에 사용할 대칭 키 생성
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    private Key key() {
        // 예) Base64로 관리 시: Decoders.BASE64.decode(secret)
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    //========================================================================
    // 1. Access Token 발급
    // 2. URL : (Provider) 내부 호출
    // 3. Param : username (required, String)
    //            roles (required, List<String>)
    // 4. 설명 : subject/roles/iat/exp 세팅 후 HS256 서명하여 액세스 토큰 생성
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    public String createAccess(String username, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessExp)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    //========================================================================
    // 1. Refresh Token 발급
    // 2. URL : (Provider) 내부 호출
    // 3. Param : username (required, String)
    // 4. 설명 : subject/iat/exp 세팅 후 HS256 서명하여 리프레시 토큰 생성
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    public String createRefresh(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshExp)))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    //========================================================================
    // 1. JWT 파싱/검증
    // 2. URL : (Provider) 내부 호출
    // 3. Param : token (required, String)
    // 4. 설명 : 서명/만료 검증 통과 시 Claims 반환(실패 시 예외)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
    }

    //========================================================================
    // 1. subject(username) 추출
    // 2. URL : (Provider) 내부 호출
    // 3. Param : token (required, String)
    // 4. 설명 : Claims에서 subject를 추출하여 반환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    public String getUsername(String token) { return parse(token).getBody().getSubject(); }

    //========================================================================
    // 1. roles 클레임 추출
    // 2. URL : (Provider) 내부 호출
    // 3. Param : token (required, String)
    // 4. 설명 : Claims의 roles 리스트 반환(없으면 빈 리스트)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object r = parse(token).getBody().get("roles");
        return (r instanceof List) ? (List<String>) r : List.of();
    }
}