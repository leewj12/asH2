package com.inpro.asBoard.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface FileStorageService {

    //========================================================================
    // 1. 공개 URL prefix 조회
    // 2. URL : (Service) 내부 호출
    // 3. Param :
    // 4. 설명 : 설정 기반 공개 경로 prefix 반환(예: /upload)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    String publicBase();

    //========================================================================
    // 1. 공개 디렉토리 경로 조합
    // 2. URL : (Service) 내부 호출
    // 3. Param : parts(vararg, String)  // 하위 경로 세그먼트
    // 4. 설명 : publicBase 하위로 parts를 이어 붙여 공개 경로 생성(예: /upload/as/2025-08-18)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    String buildPublicDir(String... parts);

    //========================================================================
    // 1. 물리 디렉토리 보장 생성
    // 2. URL : (Service) 내부 호출
    // 3. Param : parts(vararg, String)  // 업로드 루트 하위 경로
    // 4. 설명 : 업로드 루트 하위에 디렉토리를 생성하고 절대경로 Path 반환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    Path ensureDir(String... parts);

    //========================================================================
    // 1. 공개경로 → 절대 디렉토리 변환
    // 2. URL : (Service) 내부 호출
    // 3. Param : publicPath(required, String)  // 예: /upload/as/2025-08-18
    // 4. 설명 : 공개 경로를 업로드 루트 기준의 절대 디렉토리 Path로 변환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    Path toAbsoluteDirFromPublic(String publicPath);

    //========================================================================
    // 1. 다중 파일 저장
    // 2. URL : (Service) 내부 호출
    // 3. Param : files(required, List<MultipartFile>)
    //            subDirs(vararg, String) // 하위 디렉토리(예: "as")
    // 4. 설명 : 업로드 루트/subDirs/오늘날짜 에 파일 저장, SavedFile 목록 반환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    List<SavedFile> saveAll(List<MultipartFile> files, String... subDirs);

    //========================================================================
    // 1. 커밋 후 파일 삭제 예약
    // 2. URL : (Service) 내부 호출
    // 3. Param : absoluteFiles(required, Collection<Path>)
    // 4. 설명 : 트랜잭션 커밋 이후 물리 파일 삭제 예약
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    void deleteAfterCommit(Collection<Path> absoluteFiles);
}