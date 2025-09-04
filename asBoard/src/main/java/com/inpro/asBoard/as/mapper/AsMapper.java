package com.inpro.asBoard.as.mapper;

import com.inpro.asBoard.as.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AsMapper {

    //========================================================================
    // 1. 농가 목록 조회
    // 2. Param :
    // 3. 설명 : 사용 농가 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<Map<String, String>> selectFarmList();

    //========================================================================
    // 1. 농가 목록 검색
    // 2. Param : keyword
    // 3. 설명 : 사용 농가 목록을 이름/코드 부분일치로 검색
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<Map<String, String>> searchFarmList(@Param("keyword") String keyword);

    //========================================================================
    // 1. 농가 장비 테이블 조회
    // 2. Param : farmCode
    // 3. 설명 : 해당 농가에 데이터가 존재하는 장비 테이블명을 문자열로 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<String> selectCategoryList(String farmCode);

    //========================================================================
    // 1. 농가 등록 장비 목록 조회
    // 2. Param : farmCode
    // 3. 설명 : 해당 농가에 등록된 장비 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<Map<String, Object>> selectEquipmentListByTable(Map<String, Object> param);

    //========================================================================
    // 1. AS 접수 등록
    // 2. Param : dto(body, required, AsDto)
    // 3. 설명 : AS 접수글 등록
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void insertAsHeader(AsDto dto);

    //========================================================================
    // 1. AS 장비 등록
    // 2. Param : equipList[] (optional, AsEquipDto)
    // 3. 설명 : AS 장비 등록
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void insertAsEquip(AsEquipDto equip);

    //========================================================================
    // 1. AS 접수 수정
    // 2. Param : dto(body, required, AsDto)
    // 3. 설명 : AS 접수글 수정
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void updateAsHeader(AsDto dto);

    //========================================================================
    // 1. AS 접수 내역 상세 조회
    // 2. Param : asId
    // 3. 설명 : AS 접수 내역 상세 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    AsDto selectAsDetail(Long asId);

    //========================================================================
    // 1. AS 장비 목록 조회
    // 2. Param : asId
    // 3. 설명 : AS 장비 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<AsEquipDto> selectEquipListByAsId(Long asId);

    //========================================================================
    // 1. 접수 내역 삭제
    // 2. Param : asId
    // 3. 설명 : 접수 내역 논리 삭제
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void deleteAsHeader(Long asId);         // USE_FLAG = '0'

    //========================================================================
    // 1. AS 장비 삭제
    // 2. Param : asId
    // 3. 설명 : AS 장비 논리 삭제
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void deleteAsEquipByAsId(Long asId);    // USE_FLAG = '0' for all equip rows

    //========================================================================
    // 1. AS 장비 삭제
    // 2. Param : asId
    // 3. 설명 : AS 장비 데이터 삭제(수정 시 활용)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void deleteEquipHardByAsId(Long asId);

    //========================================================================
    // 1. AS 일정 삭제
    // 2. Param : asId
    // 3. 설명 : AS 일정 논리 삭제
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void deleteScheduleByAsIdSoft(Long asId);

    //========================================================================
    // 1. AS 일정 삭제
    // 2. Param : asId
    // 3. 설명 : AS 일정 데이터 삭제(수정 시 활용)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void deleteScheduleByAsId(Long asId);

    //========================================================================
    // 1. AS 일정 등록
    // 2. Param : schedule(AsScheduleDto)
    // 3. 설명 : AS 일정 등록
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void insertSchedule(AsScheduleDto schedule);

    //========================================================================
    // 1. AS 일정 수정
    // 2. Param : schedule(AsScheduleDto)
    // 3. 설명 : AS 일정 수정
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void updateSchedule(AsScheduleDto schedule);

    //========================================================================
    // 1. 접수 일정 목록 조회
    // 2. Param : asId
    // 3. 설명 : 특정 접수 일정 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<AsScheduleDto> selectScheduleListByAsId(Long asId);

    //========================================================================
    // 1. 전체 일정 목록 조회
    // 2. Param :
    // 3. 설명 : 전체 일정 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<AsScheduleDto> selectAllForSchedule();

    //========================================================================
    // 1. 첨부파일 등록
    // 2. Param : file(AsFileDto)
    // 3. 설명 : 첨부파일 등록
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void insertAsFile(AsFileDto file);

    //========================================================================
    // 1. 접수 글 첨부파일 목록 조회
    // 2. Param : asId
    // 3. 설명 : 접수 글 첨부파일 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<AsFileDto> selectFileListByAsId(Long asId);

    //========================================================================
    // 1. fileId로로 파일 목록 조회
    // 2. Param : fileIds
    // 3. 설명 : fileId로 UUID, 경로 검증 후 목록 생성
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<AsFileDto> selectFilesByIds(@Param("fileIds") List<Long> fileIds);

    //========================================================================
    // 1. 첨부파일 삭제
    // 2. Param : asId
    // 3. 설명 : 첨부파일 논리 삭제
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void deleteAsFilesByAsId(Long asId);

    //========================================================================
    // 1. 첨부파일 삭제
    // 2. Param : fileIds
    // 3. 설명 : 첨부파일 삭제(수정 시 활용)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    void deleteFilesByIdsHard(@Param("fileIds") List<Long> fileIds);

    //========================================================================
    // 1. AS 접수 목록 조회
    // 2. Param(map):
    //    //      - farms       : List<String>
    //    //      - types       : List<String>           // "기타" 제외된 목록
    //    //      - includeEtc  : boolean                // types에 "기타" 포함 여부
    //    //      - statuses    : List<String>
    //    //      - includeHold : boolean                // "보류" 포함 여부(상태 계산용)
    //    //      - tables      : List<String>
    //    //      - equipName   : String
    //    //      - period      : String (today|1w|1m)
    //    //      - q           : String                 // 자유검색어
    //    //      - dateBy      : 'plan' | 'reg'         // PLAN_DATE or REG_DATE 기준
    //    //      - startDate   : LocalDateTime
    //    //      - endDate     : LocalDateTime
    //    //      - size        : int
    //    //      - offset      : int
    //    //      - sortField   : String (화이트리스트: asId|planDate|regDate)
    //    //      - sortDir     : 'asc' | 'desc'
    // 3. 설명 : AS 접수 목록 조회(검색, 기간, 장비, 농장 필터 적용)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<AsDto> selectAsListFiltered(Map<String, Object> params);

    //========================================================================
    // 1. AS 접수 목록 페이징 계산
    // 2. Param(map):
    //    //      - farms       : List<String>
    //    //      - types       : List<String>           // "기타" 제외된 목록
    //    //      - includeEtc  : boolean                // types에 "기타" 포함 여부
    //    //      - statuses    : List<String>
    //    //      - includeHold : boolean                // "보류" 포함 여부(상태 계산용)
    //    //      - tables      : List<String>
    //    //      - equipName   : String
    //    //      - period      : String (today|1w|1m)
    //    //      - q           : String                 // 자유검색어
    //    //      - dateBy      : 'plan' | 'reg'         // PLAN_DATE or REG_DATE 기준
    //    //      - startDate   : LocalDateTime
    //    //      - endDate     : LocalDateTime
    //    //      - size        : int
    //    //      - offset      : int
    //    //      - sortField   : String (화이트리스트: asId|planDate|regDate)
    //    //      - sortDir     : 'asc' | 'desc'
    // 3. 설명 : AS 접수 목록 페이징 계산
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    int countAsListFiltered(Map<String, Object> params);

    //========================================================================
    // 1. AS 장비 테이블 목록 조회
    // 2. Param:
    // 3. 설명 : AS 장비 테이블 목록 조회
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<String> selectDistinctEquipTablesForFilter();

    //========================================================================
    // 1. AS 일정 목록 조회(필터/정렬/페이징)
    // 2. Param(map):
    //      - q           : String (optional)
    //      - farms       : List<String> (optional)
    //      - types       : List<String> (optional)       // "기타" 제외된 목록
    //      - includeEtc  : boolean (optional)            // "기타" 포함 여부
    //      - tables      : List<String> (optional)
    //      - equipName   : String (optional)
    //      - completion  : String (optional)
    //      - period      : String (optional)             // today|1w|1m
    //      - startDate   : LocalDateTime (optional)
    //      - endDate     : LocalDateTime (optional)
    //      - sortField   : String (default=planDate)     // 화이트리스트만 허용
    //      - sortDir     : String (default=asc)
    //      - offset      : int (required)
    //      - size        : int (required)
    // 3. 설명 : 컨트롤러에서 CSV→List 정규화된 map을 그대로 사용.
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    List<AsScheduleRowDto> selectScheduleRowListWithPaging(@Param("params") Map<String, Object> params);

    //========================================================================
    // 1. AS 일정 목록 페이지 계산
    // 2. Param(map):
    //      - q           : String (optional)
    //      - farms       : List<String> (optional)
    //      - types       : List<String> (optional)       // "기타" 제외된 목록
    //      - includeEtc  : boolean (optional)            // "기타" 포함 여부
    //      - tables      : List<String> (optional)
    //      - equipName   : String (optional)
    //      - completion  : String (optional)
    //      - period      : String (optional)             // today|1w|1m
    //      - startDate   : LocalDateTime (optional)
    //      - endDate     : LocalDateTime (optional)
    //      - sortField   : String (default=planDate)     // 화이트리스트만 허용
    //      - sortDir     : String (default=asc)
    //      - offset      : int (required)
    //      - size        : int (required)
    // 3. 설명 : AS 일정 목록 페이지 계산
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    int countScheduleRowList(@Param("params") Map<String, Object> params);

    //========================================================================
    // 1. 엑셀 출력 목록 조회
    // 2. Param(map):
    //      - q           : String (optional)
    //      - farms       : List<String> (optional)
    //      - types       : List<String> (optional)       // "기타" 제외된 목록
    //      - includeEtc  : boolean (optional)            // "기타" 포함 여부
    //      - tables      : List<String> (optional)
    //      - equipName   : String (optional)
    //      - completion  : String (optional)
    //      - period      : String (optional)             // today|1w|1m
    //      - startDate   : LocalDateTime (optional)
    //      - endDate     : LocalDateTime (optional)
    //      - sortField   : String (default=planDate)     // 화이트리스트만 허용
    //      - sortDir     : String (default=asc)
    //      - offset      : int (required)
    //      - size        : int (required)
    // 3. 설명 : 엑셀 출력 목록 조회
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    List<AsScheduleRowDto> selectScheduleRowListForExport(@Param("params") Map<String, Object> params);

}
