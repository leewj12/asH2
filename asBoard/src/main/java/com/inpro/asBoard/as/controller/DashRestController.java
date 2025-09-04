package com.inpro.asBoard.as.controller;

import com.inpro.asBoard.as.dto.*;
import com.inpro.asBoard.as.mapper.DashMapper;
import com.inpro.asBoard.as.service.DashService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dash")
@RequiredArgsConstructor
public class DashRestController {

    private final DashMapper dashMapper;
    private final DashService dashService;

    //========================================================================
    // 1. 문제유형 요약 지표
    // 2. URL   : [GET] /api/dash/summary
    // 3. Param : -
    // 4. 설명  : 대시보드 카드용 문제유형 요약 지표 반환
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/summary")
    public List<DashSummaryDto> getSummary() {
        return dashService.getSummary();
    }

    //========================================================================
    // 1. 월별 통계
    // 2. URL   : [GET] /api/dash/monthly
    // 3. Param : -
    // 4. 설명  : 월 단위 추이/합계 반환
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/monthly")
    public List<DashMonthlyStatDto> getMonthly() {
        return dashService.getMonthlyStats();
    }

    //========================================================================
    // 1. 임박 목록
    // 2. URL   : [GET] /api/dash/upcoming
    // 3. Param : limit (optional, int, default=10)
    // 4. 설명  : 예정 일정 상위 10건 반환(예정일 오름차순)
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/upcoming")
    public List<DashUpcomingDto> getUpcoming(@RequestParam(defaultValue = "10") int limit) {
        return dashService.getUpcomingList(limit);
    }

    //========================================================================
    // 1. 범용 집계(TopN)
    // 2. URL   : [GET] /api/dash/aggregate
    // 3. Param : dimension (required, string)  // 예: farm|equipment|category|region|project|asType
    //            period    (optional, string, default=all) // all|today|1w|1m|custom...
    //            topN      (optional, int, default=5)
    // 4. 설명  : dimension 기준 TopN 집계(대시보드 차트/표 공용)
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/aggregate")
    public List<DashAggregateItemDto> aggregate(@RequestParam String dimension,
                                                 @RequestParam(defaultValue = "all") String period,
                                                 @RequestParam(name = "topN", defaultValue = "5") int topN) {
        return dashService.aggregate(dimension, period, topN);
    }

    //========================================================================
    // 1. 필터용 농가 목록
    // 2. URL   : [GET] /api/dash/farms
    // 3. Param : -
    // 4. 설명  : 대시보드 필터 셀렉트용 농가 코드/이름 목록
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/farms")
    public List<DashCodeNameDto> farmList() {
        return dashService.getFarmList();
    }

    //========================================================================
    // 1. 필터용 장비 목록
    // 2. URL   : [GET] /api/dash/equipments
    // 3. Param : -
    // 4. 설명  : 대시보드 필터 셀렉트용 장비 테이블 목록
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/equipments")
    public List<DashCodeNameDto> equipmentList() {
        return dashService.getEquipmentList();
    }

    //========================================================================
    // 1. 농가→장비 의존 목록
    // 2. URL   : [GET] /api/dash/equipments/by-farm
    // 3. Param : farmCode (required, string)
    // 4. 설명  : 특정 농가에 실제 존재하는 장비 목록
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/equipments/by-farm")
    public List<DashCodeNameDto> equipmentsByFarm(@RequestParam String farmCode) {
        return dashService.getEquipmentListByFarm(farmCode);
    }

    //========================================================================
    // 1. 장비→농가 의존 목록
    // 2. URL   : [GET] /api/dash/farms/by-equipment
    // 3. Param : tableName (required, string)
    // 4. 설명  : 특정 장비를 보유한 농가 목록
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/farms/by-equipment")
    public List<DashCodeNameDto> farmsByEquipment(@RequestParam String tableName) {
        return dashService.getFarmListByEquipment(tableName);
    }

    //========================================================================
    // 1. 농가 기준 장비 집계
    // 2. URL   : [GET] /api/dash/farm/equipment
    // 3. Param : farmCode (required, string)
    //            period   (optional, string, default=all)
    // 4. 설명  : 특정 농가에서 장비별 집계 TopN/분포
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/farm/equipment")
    public List<DashAggregateItemDto> equipmentByFarm(@RequestParam String farmCode,
                                                       @RequestParam(defaultValue = "all") String period) {
        return dashService.getEquipmentByFarm(period, farmCode);
    }

    //========================================================================
    // 1. 농가 기준 카테고리 집계
    // 2. URL   : [GET] /api/dash/farm/category
    // 3. Param : farmCode (required, string)
    //            period   (optional, string, default=all)
    // 4. 설명  : 특정 농가에서 카테고리별 집계
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/farm/category")
    public List<DashAggregateItemDto> categoryByFarm(@RequestParam String farmCode,
                                                      @RequestParam(defaultValue = "all") String period) {
        return dashService.getCategoryByFarm(period, farmCode);
    }

    //========================================================================
    // 1. 장비 기준 농가 집계
    // 2. URL   : [GET] /api/dash/equipment/farm
    // 3. Param : tableName (required, string)
    //            period    (optional, string, default=all)
    // 4. 설명  : 특정 장비를 보유한 농가별 집계
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/equipment/farm")
    public List<DashAggregateItemDto> farmByEquipment(@RequestParam String tableName,
                                                       @RequestParam(defaultValue = "all") String period) {
        return dashService.getFarmByEquipment(period, tableName);
    }

    //========================================================================
    // 1. 장비 기준 카테고리 집계
    // 2. URL   : [GET] /api/dash/equipment/category
    // 3. Param : tableName (required, string)
    //            period    (optional, string, default=all)
    // 4. 설명  : 특정 장비에서 카테고리별 집계
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/equipment/category")
    public List<DashAggregateItemDto> categoryByEquipment(@RequestParam String tableName,
                                                           @RequestParam(defaultValue = "all") String period) {
        return dashService.getCategoryByEquipment(period, tableName);
    }

    //========================================================================
    // 1. 교차 필터 집계(농가+장비)
    // 2. URL   : [GET] /api/dash/category
    // 3. Param : period   (optional, string, default=all)
    //            farmCode (optional, string)
    //            tableName(optional, string)
    // 4. 설명  : 농가와 장비 동시 필터링한 카테고리 집계
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping("/category")
    public List<DashAggregateItemDto> category(@RequestParam(defaultValue = "all") String period,
                                                @RequestParam(required = false) String farmCode,
                                                @RequestParam(required = false) String tableName) {
        return dashService.getCategoryByFarmAndEquipment(period, farmCode, tableName);
    }

    //========================================================================
    // 1. 시계열 데이터
    // 2. URL   : [GET] /api/dash/timeSeries  | /api/dash/time-series
    // 3. Param : period   (required, string)           // all|today|1w|1m|custom...
    //            bucket   (required, string)           // day | month
    //            farmCode (optional, string)
    //            tableName(optional, string)
    // 4. 설명  : 기간/bucket 기준의 시계열 포인트 목록(필터 적용)
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @GetMapping({"/timeSeries", "/time-series"})
    public List<DashTimeSeriesPointDto> timeSeries(@RequestParam String period,
                                                    @RequestParam String bucket, // "day" | "month"
                                                    @RequestParam(required = false) String farmCode,
                                                    @RequestParam(required = false) String tableName) {
        return dashService.getTimeSeries(period, bucket, farmCode, tableName);
    }

    //========================================================================
    // 1. 지역별 AS 건수 조회
    // 2. URL   : [GET] /api/dash/geo/sido
    // 3. Param :
    // 4. 설명  : 지역별 AS 건수 조회
    // 5. 작성  : wjlee(25.09.01)
    // 6. 수정  :
    //========================================================================
    @ResponseBody
    @GetMapping("/geo/sido")
    public List<Map<String,Object>> sidoCounts() {
        return dashMapper.selectSidoCounts(); // [{sido:"경기", as_cnt:123}, ...]
    }
}