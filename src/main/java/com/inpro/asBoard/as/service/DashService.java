package com.inpro.asBoard.as.service;

import com.inpro.asBoard.as.dto.*;
import com.inpro.asBoard.as.mapper.DashMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashService {

    private final DashMapper dashMapper;

    //========================================================================
    // 1. 월별 통계
    // Param :
    // 설명  : 최근 월 단위 통계 목록 조회
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashMonthlyStatDto> getMonthlyStats() {
        return dashMapper.selectMonthlyStats();
    }

    //========================================================================
    // 1. 임박/예정 목록
    // Param : limit (required, int)
    // 설명  : 예정 일정 상위 N건 조회 (정렬/상한은 Mapper 쿼리 기준)
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashUpcomingDto> getUpcomingList(int limit) {
        return dashMapper.selectUpcomingList(limit);
    }

    //========================================================================
    // 1. 핵심 요약 지표
    // Param :
    // 설명  : [전체/이번달/이번주/오늘] 4구간에 대한 상태별 카운트 집계
    //        내부적으로 buildPeriodParam → selectStatusCountByPeriod
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashSummaryDto> getSummary() {
        List<DashSummaryDto> out = new ArrayList<>();
        out.addAll(collectSummary("전체", new HashMap<>()));
        out.addAll(collectSummary("이번달", buildPeriodParam("1m")));
        out.addAll(collectSummary("이번주", buildPeriodParam("1w")));
        out.addAll(collectSummary("오늘",   buildPeriodParam("1d")));
        return out;
    }

    //========================================================================
    // 1. 범용 집계(TopN)
    // Param : dimension (required, string)   // 예: farm|equipment|category|region|project|asType
    //         period    (required, string)   // all|1d|1w|1m|custom...
    //         topN      (required, int)
    // 설명  : dimension 기준 TopN 집계 결과 반환
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashAggregateItemDto> aggregate(String dimension, String period, int topN) {
        return dashMapper.selectAggregate(dimension, period, topN);
    }

    //========================================================================
    // 1. 필터용 농가 목록
    // Param :
    // 설명  : 코드/이름 페어 목록
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashCodeNameDto> getFarmList() {
        return dashMapper.selectFarmList();
    }

    //========================================================================
    // 1. 필터용 장비 목록
    // Param :
    // 설명  : 장비 테이블/명 페어 목록
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashCodeNameDto> getEquipmentList() {
        return dashMapper.selectEquipmentList();
    }

    //========================================================================
    // 1. 농가→장비 의존 목록
    // Param : farmCode (required, string)
    // 설명  : 특정 농가에 실제 존재하는 장비 목록
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashCodeNameDto> getEquipmentListByFarm(String farmCode) {
        if (farmCode == null || farmCode.isBlank()) return List.of();
        return dashMapper.selectEquipmentListByFarm(farmCode);
    }

    //========================================================================
    // 1. 장비→농가 의존 목록
    // Param : tableName (required, string)
    // 설명  : 특정 장비를 보유한 농가 목록
    // 주의  : tableName 화이트리스트 검증은 Controller/Service 한 곳에서 수행 권장
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashCodeNameDto> getFarmListByEquipment(String tableName) {
        if (tableName == null || tableName.isBlank()) return List.of();
        return dashMapper.selectFarmListByEquipment(tableName);
    }

    //========================================================================
    // 1. 농가 기준 장비 집계
    // Param : period (required, string), farmCode (required, string)
    // 설명  : 특정 농가에서 장비별 집계
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashAggregateItemDto> getEquipmentByFarm(String period, String farmCode) {
        return dashMapper.selectEquipmentByFarm(period, farmCode);
    }

    //========================================================================
    // 1. 농가 기준 카테고리 집계
    // Param : period (required, string), farmCode (required, string)
    // 설명  : 특정 농가에서 카테고리별 집계
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashAggregateItemDto> getCategoryByFarm(String period, String farmCode) {
        return dashMapper.selectCategoryByFarm(period, farmCode);
    }

    //========================================================================
    // 1. 장비 기준 농가 집계
    // Param : period (required, string), tableName (required, string)
    // 설명  : 특정 장비를 보유한 농가별 집계
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashAggregateItemDto> getFarmByEquipment(String period, String tableName) {
        return dashMapper.selectFarmByEquipment(period, tableName);
    }

    //========================================================================
    // 1. 장비 기준 카테고리 집계
    // Param : period (required, string), tableName (required, string)
    // 설명  : 특정 장비에서 카테고리별 집계
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashAggregateItemDto> getCategoryByEquipment(String period, String tableName) {
        return dashMapper.selectCategoryByEquipment(period, tableName);
    }

    //========================================================================
    // 1. 교차 필터 집계(농가+장비)
    // Param : period (required, string), farmCode (optional, string), tableName (optional, string)
    // 설명  : 농가/장비 동시 필터링된 카테고리 집계
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashAggregateItemDto> getCategoryByFarmAndEquipment(String period, String farmCode, String tableName) {
        return dashMapper.selectCategoryByFarmAndEquipment(period, farmCode, tableName);
    }

    //========================================================================
    // 1. 시계열 데이터
    // Param : period (required, string)  // all|1d|1w|1m|custom...
    //         bucket (required, string)  // day|month
    //         farmCode (optional, string)
    //         tableName(optional, string)
    // 설명  : 기간/bucket 기준의 시계열 포인트 조회(필터 적용)
    // 작성  : wjlee(25.09.01)
    //========================================================================
    public List<DashTimeSeriesPointDto> getTimeSeries(String period, String bucket, String farmCode, String tableName) {
        return dashMapper.selectTimeSeries(period, bucket, farmCode, tableName);
    }

    //========================================================================
    // 1. 기간 파라미터 빌더
    // Param : code (required, string)  // '1d' | '1w' | '1m' | 'all'
    // Return: Map<String,Object> { fromDate(LocalDate), toDate(LocalDate) } // all은 빈 맵
    // 설명  : 오늘/이번주(월~일)/이번달(1일~말일) 경계를 산출하여 Mapper에 전달할 파라미터 구성
    // 작성  : wjlee(25.09.01)
    //========================================================================
    private Map<String, Object> buildPeriodParam(String code) {
        Map<String, Object> p = new HashMap<>();
        LocalDate today = LocalDate.now();

        switch (code) {
            case "1d" -> {
                p.put("fromDate", today);
                p.put("toDate", today);
            }
            case "1w" -> {
                LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                p.put("fromDate", monday);
                p.put("toDate", sunday);
            }
            case "1m" -> {
                LocalDate first = today.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate last  = today.with(TemporalAdjusters.lastDayOfMonth());
                p.put("fromDate", first);
                p.put("toDate", last);
            }
            default -> { /* all: 빈 맵 */ }
        }
        return p;
    }

    //========================================================================
    // 1. 요약 행 수집기
    // Param : periodLabel (required, string),
    //         param       (required, map: fromDate/toDate 또는 빈 맵)
    // Return: List<DashSummaryDto>
    // 설명  : 기간 조건으로 상태 카운트를 조회하여 DTO로 변환
    // 작성  : wjlee(25.09.01)
    //========================================================================
    private List<DashSummaryDto> collectSummary(String periodLabel, Map<String, Object> param) {
        List<DashSummaryRow> rows = dashMapper.selectStatusCountByPeriod(param);
        List<DashSummaryDto> out = new ArrayList<>();
        if (rows == null) return out;

        for (DashSummaryRow r : rows) {
            DashSummaryDto dto = new DashSummaryDto();
            dto.setPeriod(periodLabel);
            dto.setStatusLabel(r.statusLabel()); // 실제로는 카테고리 4분류
            dto.setCount(r.cnt());
            out.add(dto);
        }
        return out;
    }
}