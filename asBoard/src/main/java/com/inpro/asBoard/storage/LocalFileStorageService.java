package com.inpro.asBoard.storage;

import com.inpro.asBoard.config.FileStorageProps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageProps props;
    private final TransactionalFileOps txOps;

    //========================================================================
    // 1. 공개 URL prefix 조회
    // 2. URL : (Service) 내부 호출
    // 3. Param :
    // 4. 설명 : 설정값 기반 공개 prefix 정규화하여 반환(선행 '/', 말미 '/' 제거)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public String publicBase() {
        String base = props.getPublicBase();
        if (base == null || base.isBlank()) base = "/upload";
        if (!base.startsWith("/")) base = "/" + base;
        if (base.endsWith("/")) base = base.substring(0, base.length()-1);
        return base;
    }

    //========================================================================
    // 1. 공개 디렉토리 경로 조합
    // 2. URL : (Service) 내부 호출
    // 3. Param : parts(vararg, String)
    // 4. 설명 : publicBase 하위로 parts를 이어붙여 공개 경로 생성
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public String buildPublicDir(String... parts) {
        String rest = String.join("/", parts).replaceAll("^/+", "");
        return publicBase() + "/" + rest;
    }

    //========================================================================
    // 1. 물리 디렉토리 보장 생성
    // 2. URL : (Service) 내부 호출
    // 3. Param : parts(vararg, String)
    // 4. 설명 : 업로드 루트 하위로 디렉토리를 생성하고 절대경로 Path 반환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public Path ensureDir(String... parts) {
        Path root = Paths.get(props.getUploadRoot()).toAbsolutePath().normalize();
        if (!root.isAbsolute()) throw new IllegalStateException("upload-root는 절대경로여야 합니다: " + root);
        Path dir = root.resolve(Paths.get("", parts)).normalize();
        if (!dir.startsWith(root)) throw new IllegalStateException("비정상 디렉토리 요청: " + dir);
        try { Files.createDirectories(dir); } catch (Exception e) {
            throw new IllegalStateException("업로드 디렉토리 생성 실패: " + dir, e);
        }
        return dir;
    }

    //========================================================================
    // 1. 공개경로 → 절대 디렉토리 변환
    // 2. URL : (Service) 내부 호출
    // 3. Param : publicPath(required, String)
    // 4. 설명 : 공개 경로를 업로드 루트 기준 절대 디렉토리 Path로 변환
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public Path toAbsoluteDirFromPublic(String publicPath) {
        String under = publicPath == null ? "" : publicPath;
        String base = publicBase();
        if (under.startsWith(base)) under = under.substring(base.length());
        under = under.replaceAll("^/+", "");
        Path root = Paths.get(props.getUploadRoot()).toAbsolutePath().normalize();
        Path dir = root.resolve(under).normalize();
        if (!dir.startsWith(root)) throw new IllegalStateException("비정상 경로 접근: " + dir);
        return dir;
    }

    //========================================================================
    // 1. 다중 파일 저장
    // 2. URL : (Service) 내부 호출
    // 3. Param : files(required, List<MultipartFile>)
    //            subDirs(vararg, String)
    // 4. 설명 : 업로드 루트/subDirs/오늘날짜 에 저장 후 SavedFile 목록 반환(롤백 시 보상삭제 등록)
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public List<SavedFile> saveAll(List<MultipartFile> files, String... subDirs) {
        if (files == null || files.isEmpty()) return List.of();

        String today = LocalDate.now().toString();
        String publicDir = buildPublicDir(concat(subDirs, today)); // 예: /upload/as/2025-08-18
        Path absDir = ensureDir(concat(subDirs, today));           // 예: <root>/as/2025-08-18

        List<File> created = new ArrayList<>();
        List<SavedFile> results = new ArrayList<>();
        txOps.registerRollbackCleanup(created);                    // 롤백 보상삭제

        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;

            String original = mf.getOriginalFilename();
            String ext = "";
            int dot = (original == null) ? -1 : original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String uuid = UUID.randomUUID().toString() + ext;

            Path target = absDir.resolve(uuid).normalize();
            try { mf.transferTo(target.toFile()); }
            catch (Exception e) { throw new RuntimeException("파일 저장 실패: " + original, e); }

            created.add(target.toFile());

            results.add(SavedFile.builder()
                    .originalName(original)
                    .uuidName(uuid)
                    .publicDir(publicDir)
                    .size(mf.getSize())
                    .contentType(mf.getContentType())
                    .absolutePath(target)
                    .build());
        }
        return results;
    }

    //========================================================================
    // 1. 커밋 후 파일 삭제 예약
    // 2. URL : (Service) 내부 호출
    // 3. Param : absoluteFiles(required, Collection<Path>)
    // 4. 설명 : 트랜잭션 커밋 이후 물리 파일 일괄 삭제 예약
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Override
    public void deleteAfterCommit(Collection<Path> absoluteFiles) {
        if (absoluteFiles == null || absoluteFiles.isEmpty()) return;
        List<File> files = absoluteFiles.stream().map(Path::toFile).toList();
        txOps.registerDeleteAfterCommit(files);
    }

    // (내부) 배열 결합 유틸
    private static String[] concat(String[] arr, String extra) {
        String[] r = Arrays.copyOf(arr, arr.length + 1);
        r[arr.length] = extra;
        return r;
    }
}