package com.inpro.asBoard.as.mapper;

import com.inpro.asBoard.as.dto.*;
import com.inpro.asBoard.as.dto.DashMonthlyStatDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashMapper {

    //========================================================================
    // 1. 월별 통계 조회
    // 2. Param : -
    // 3. 설명 : 최근 월 단위 통계 목록 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashMonthlyStatDto> selectMonthlyStats();

    //========================================================================
    // 1. 임박/예정 목록 조회
    // 2. Param : limit (required, int)
    // 3. 설명 : 예정 일정 상위 N건 반환(정렬/상한은 SQL 기준)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashUpcomingDto> selectUpcomingList(@Param("limit") int limit);

    //========================================================================
    // 1. 기간별 상태 카운트
    // 2. Param(map) :
    //      - fromDate : LocalDate (optional, 포함)
    //      - toDate   : LocalDate (optional, 포함)
    //      // 빈 맵이면 전체 기간
    // 3. 설명 : 기간 조건으로 상태별 건수 집계(요약 카드용)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashSummaryRow> selectStatusCountByPeriod(Map<String, Object> periodParam);

    //========================================================================
    // 1. 범용 집계(TopN)
    // 2. Param :
    //      - dimension (required, string)  // farm|equipment|category|region|project|asType
    //      - period    (required, string)  // all|1d|1w|1m|custom...
    //      - topN      (required, int)
    // 3. 설명 : dimension 기준 TopN 집계 결과 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashAggregateItemDto> selectAggregate(@Param("dimension") String dimension,
                                                @Param("period") String period,
                                                @Param("topN") int topN);

    //========================================================================
    // 1. 필터용 농가 목록
    // 2. Param : -
    // 3. 설명 : code=farmCode, name=farmName 페어 목록 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashCodeNameDto> selectFarmList();       // code=farmCode, name=farmName

    //========================================================================
    // 1. 필터용 장비 목록
    // 2. Param : -
    // 3. 설명 : code=tableName, name=tableName 페어 목록 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashCodeNameDto> selectEquipmentList();  // code=tableName, name=tableName

    //========================================================================
    // 1. 농가→장비 의존 목록
    // 2. Param : farmCode (required, string)
    // 3. 설명 : 특정 농가에 실제 존재하는 장비 목록
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashCodeNameDto> selectEquipmentListByFarm(@Param("farmCode") String farmCode);

    //========================================================================
    // 1. 장비→농가 의존 목록
    // 2. Param : tableName (required, string)
    // 3. 설명 : 특정 장비를 보유한 농가 목록
    // 4. 주의 : tableName 값 화이트리스트 검증 권장
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    List<DashCodeNameDto> selectFarmListByEquipment(@Param("tableName") String tableName);

    //========================================================================
    // 1. 농가 기준 장비 집계
    // 2. Param :
    //      - period   (required, string)
    //      - farmCode (required, string)
    // 3. 설명 : 특정 농가에서 장비별 집계 결과 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashAggregateItemDto> selectEquipmentByFarm(@Param("period") String period,
                                                      @Param("farmCode") String farmCode);

    //========================================================================
    // 1. 농가 기준 카테고리 집계
    // 2. Param :
    //      - period   (required, string)
    //      - farmCode (required, string)
    // 3. 설명 : 특정 농가에서 카테고리별 집계 결과 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashAggregateItemDto> selectCategoryByFarm(@Param("period") String period,
                                                     @Param("farmCode") String farmCode);

    //========================================================================
    // 1. 장비 기준 농가 집계
    // 2. Param :
    //      - period    (required, string)
    //      - tableName (required, string)
    // 3. 설명 : 특정 장비를 보유한 농가별 집계 결과 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashAggregateItemDto> selectFarmByEquipment(@Param("period") String period,
                                                      @Param("tableName") String tableName);

    //========================================================================
    // 1. 장비 기준 카테고리 집계
    // 2. Param :
    //      - period    (required, string)
    //      - tableName (required, string)
    // 3. 설명 : 특정 장비에서 카테고리별 집계 결과 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashAggregateItemDto> selectCategoryByEquipment(@Param("period") String period,
                                                          @Param("tableName") String tableName);

    //========================================================================
    // 1. 시계열 데이터 조회
    // 2. Param :
    //      - period    (required, string)  // all|1d|1w|1m|custom...
    //      - bucket    (required, string)  // day|month
    //      - farmCode  (optional, string)
    //      - tableName (optional, string)
    // 3. 설명 : 기간/bucket 기준의 시계열 포인트 목록 반환(필터 적용)
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashTimeSeriesPointDto> selectTimeSeries(@Param("period") String period,
                                                   @Param("bucket") String bucket,        // "day" | "month"
                                                   @Param("farmCode") String farmCode,    // nullable
                                                   @Param("tableName") String tableName); // nullable

    //========================================================================
    // 1. 교차 필터 집계(농가+장비)
    // 2. Param :
    //      - period    (required, string)
    //      - farmCode  (optional, string)
    //      - tableName (optional, string)
    // 3. 설명 : 농가/장비 동시 필터링된 카테고리 집계 결과 반환
    // 4. 작성 : wjlee(25.09.01)
    // 5. 수정 :
    //========================================================================
    List<DashAggregateItemDto> selectCategoryByFarmAndEquipment(@Param("period") String period,
                                                                 @Param("farmCode") String farmCode,
                                                                 @Param("tableName") String tableName);

    //========================================================================
    // 1. 지역별 AS 건수 조회
    // 2. Param :
    // 3. 설명 : 지역별 AS 건수 조회
    // 4. 작성 : wjlee(25.09.02)
    // 5. 수정 :
    //========================================================================
    List<Map<String, Object>> selectSidoCounts();
}