package com.inpro.asBoard.as.controller;

import com.inpro.asBoard.as.dto.*;
import com.inpro.asBoard.as.mapper.AsMapper;
import com.inpro.asBoard.storage.FileStorageService;
import com.inpro.asBoard.storage.SavedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/api/as")
@RequiredArgsConstructor
public class AsRestController {

    private final AsMapper asMapper;
    private final FileStorageService storage; // 경로/저장 로직 주입

    //========================================================================
    // 장비 테이블 화이트리스트 (프론트 select와 무관하게 서버에서 최종 검증)
    //========================================================================
    private static final Set<String> ALLOWED_EQUIP_TABLES = Set.of(
            "SF_TBL_AIRCON","SF_TBL_BRUSH","SF_TBL_COOLER","SF_TBL_ENVIRON","SF_TBL_ENVIRON_L",
            "SF_TBL_EQMNT","SF_TBL_FEEDBIN","SF_TBL_FEEDBIN_B","SF_TBL_FEEDBIN_L","SF_TBL_FOG",
            "SF_TBL_LIGHT","SF_TBL_METER","SF_TBL_METER_S","SF_TBL_ROOF","SF_TBL_STER","SF_TBL_STER_Q",
            "SF_TBL_STER_V","SF_TBL_VEHICLE","SF_TBL_VEHICLE_A","SF_TBL_VENTIL","SF_TBL_VENTIL_C",
            "SF_TBL_VENTIL_D","SF_TBL_VENTIL_P","SF_TBL_VENTIL_S","SF_TBL_WIND","SF_TBL_WIND_D"
    );

    //========================================================================
    // 1. 농가 장비 테이블 조회 API
    // 2. Param : farmCode
    // 3. 설명 : 해당 농가에 데이터가 존재하는 장비 테이블명 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    @GetMapping("/categoryList")
    public List<String> getCategoryList(@RequestParam String farmCode) {
        if (farmCode == null || farmCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "farmCode is required");
        }
        return asMapper.selectCategoryList(farmCode);
    }

    //========================================================================
    // 1. 농가 장비 목록 조회 API
    // 2. Param : farmCode, tableName
    // 3. 설명 : 해당 농가에 등록된 장비 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    @GetMapping("/equipmentList")
    public List<Map<String, Object>> getEquipmentList(@RequestParam String farmCode,
                                                      @RequestParam String tableName) {
        if (farmCode == null || farmCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "farmCode is required");
        }
        if (!ALLOWED_EQUIP_TABLES.contains(tableName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported table: " + tableName);
        }
        Map<String, Object> param = new HashMap<>();
        param.put("farmCode", farmCode);
        param.put("tableName", tableName);
        return asMapper.selectEquipmentListByTable(param);
    }

    //========================================================================
    // 1. 농가 목록 검색 API
    // 2. Param : farmCode
    // 3. 설명 : 사용 농가 목록을 이름/코드 부분일치로 검색
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    @GetMapping("/searchFarmList")
    public List<Map<String, String>> searchFarmList(@RequestParam String keyword) {
        return asMapper.searchFarmList(keyword);
    }

    //========================================================================
    // 1. 접수 등록 API
    // 2. URL : [POST]{...}/as/write
    // 3. Param : dto(body, required, AsDto)
    //            - equipList[] (optional, AsEquipDto)
    //            - scheduleList[] (optional, AsScheduleDto)
    // 4. 설명 : AS 접수글 등록
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/write")
    public ResponseEntity<?> insertAs(@RequestBody AsDto dto) {
        dto.setUseFlag(true);
        asMapper.insertAsHeader(dto);

        Long asId = dto.getAsId();
        if (asId == null) throw new IllegalStateException("AS ID 생성 실패");

        if (dto.getEquipList() != null) {
            for (AsEquipDto equip : dto.getEquipList()) {
                equip.setAsId(asId);
                equip.setUseFlag(true);
                asMapper.insertAsEquip(equip);
            }
        }

        if (dto.getScheduleList() != null) {
            for (AsScheduleDto schedule : dto.getScheduleList()) {
                schedule.setAsId(asId);
                schedule.setUseFlag(true);
                asMapper.insertSchedule(schedule);
            }
        }

        return ResponseEntity.ok(asId);
    }

    //========================================================================
    // 1. 접수 수정 API
    // 2. URL : [PUT]{...}/as/edit
    // 3. Param : dto(body, required, AsDto)
    //            - equipList[] (optional, AsEquipDto)
    //            - scheduleList[] (optional, AsScheduleDto)
    //            - deletedFileIds[] (optional)
    // 4. 설명 : AS 접수글 수정
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("/edit")
    public ResponseEntity<?> updateAs(@RequestBody AsDto dto) {
        final List<Path> toDeleteAfterCommit = new ArrayList<>();

        if (dto.getDeletedFileIds() != null && !dto.getDeletedFileIds().isEmpty()) {
            List<AsFileDto> files = asMapper.selectFilesByIds(dto.getDeletedFileIds());
            for (AsFileDto f : files) {
                Path dir = storage.toAbsoluteDirFromPublic(f.getFilePath()); // 공개경로 → 절대경로
                Path absoluteFile = dir.resolve(f.getUuidName()).normalize();
                toDeleteAfterCommit.add(absoluteFile);
            }
        }

        asMapper.updateAsHeader(dto);

        asMapper.deleteEquipHardByAsId(dto.getAsId());
        if (dto.getEquipList() != null) {
            for (AsEquipDto equip : dto.getEquipList()) {
                equip.setAsId(dto.getAsId());
                equip.setUseFlag(true);
                asMapper.insertAsEquip(equip);
            }
        }

        asMapper.deleteScheduleByAsId(dto.getAsId());
        if (dto.getScheduleList() != null) {
            for (AsScheduleDto s : dto.getScheduleList()) {
                s.setAsId(dto.getAsId());
                s.setUseFlag(true);
                asMapper.insertSchedule(s);
            }
        }

        if (dto.getDeletedFileIds() != null && !dto.getDeletedFileIds().isEmpty()) {
            asMapper.deleteFilesByIdsHard(dto.getDeletedFileIds());
            storage.deleteAfterCommit(toDeleteAfterCommit); // ✅ 커밋 후 물리 삭제
        }

        return ResponseEntity.ok().build();
    }

    //========================================================================
    // 1. 접수 삭제 API
    // 2. URL : [DELETE]{...}/as/delete/{asId}
    // 3. Param : asId
    // 4. 설명 : AS 접수글 논리 삭제
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/delete/{asId}")
    public ResponseEntity<?> deleteAs(@PathVariable Long asId) {
        // ⚠️ 현재는 물리 파일은 그대로 남김(주석 설명대로). 필요 시:
        // 1) asMapper.selectFilesByAsId(asId)로 목록 조회
        // 2) toAbsoluteDirFromPublicPath + afterCommit에서 파일 일괄 삭제
        asMapper.deleteAsEquipByAsId(asId);
        asMapper.deleteAsFilesByAsId(asId);
        asMapper.deleteScheduleByAsIdSoft(asId);
        asMapper.deleteAsHeader(asId);
        return ResponseEntity.ok().build();
    }

    //========================================================================
    // 1. 파일 업로드 API
    // 2. URL : [POST]{...}/as/file/upload
    // 3. Param : asId, files
    // 4. 설명 : 파일 업로드
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/file/upload")
    public ResponseEntity<?> uploadAsFiles(@RequestParam("files") List<MultipartFile> files,
                                           @RequestParam("asId") Long asId) {
        if (files == null || files.isEmpty() || asId == null) {
            return ResponseEntity.badRequest().body("파일 또는 AS ID 누락");
        }

        // "as/<오늘>" 구조로 저장되도록 서비스가 날짜 샤딩까지 처리함
        List<SavedFile> saved = storage.saveAll(files, "as");

        for (SavedFile sf : saved) {
            AsFileDto dto = new AsFileDto();
            dto.setAsId(asId);
            dto.setOriginalName(sf.getOriginalName());
            dto.setUuidName(sf.getUuidName());
            dto.setFilePath(sf.getPublicDir());                       // /upload/as/2025-08-18
            dto.setFileSize((int)Math.min(sf.getSize(), Integer.MAX_VALUE)); // ⚠ int DTO라 캐스팅
            dto.setFileType(sf.getContentType());
            dto.setUseFlag(true);
            asMapper.insertAsFile(dto);
        }
        return ResponseEntity.ok("파일 업로드 완료");
    }

    //========================================================================
    // 1. AS 상세 페이지 완료 처리 API
    // 2. URL : [PATCH]{...}/as/schedule/{scheduleId}/complete
    // 3. Param : scheduleId, payload(AsScheduleDto)
    // 4. 설명 : AS 상세 페이지 일정 완료 버튼
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @Transactional(rollbackFor = Exception.class)
    @PatchMapping("/schedule/{scheduleId}/complete")
    public ResponseEntity<?> completeFromClientPayload(
            @PathVariable Long scheduleId,
            @RequestBody AsScheduleDto payload) {

        if (!scheduleId.equals(payload.getScheduleId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("scheduleId 불일치");
        }

        // ✅ 서버 시간으로 강제 — 클라이언트 시계와 무관하게 일관성 유지
        payload.setCompleteDate(java.time.LocalDateTime.now());

        // PLAN/CONTENT/HOLD는 클라이언트 원본 유지
        asMapper.updateSchedule(payload);

        String nowStr = payload.getCompleteDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return ResponseEntity.ok(Map.of("completeDate", nowStr));
    }
}