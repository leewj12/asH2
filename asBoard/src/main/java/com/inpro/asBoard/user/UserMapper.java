package com.inpro.asBoard.user;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    //========================================================================
    // 1. 사용자 조회(아이디로)
    // 2. Param : username
    // 3. 설명 : USERNAME으로 단일 사용자 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    Optional<UserAccount> findByUsername(@Param("username") String username);

    //========================================================================
    // 1. 사용자 등록
    // 2. Param : u(body, required, UserAccount)
    // 3. 설명 : 신규 사용자 INSERT (useGeneratedKeys=true)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    int insert(UserAccount u);

    //========================================================================
    // 1. 전체 사용자 수 조회
    // 2. Param :
    // 3. 설명 : SF_TBL_USERS 테이블의 전체 행 수 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    int countAll();
}
