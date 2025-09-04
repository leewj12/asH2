package com.inpro.asBoard.as.controller;

import com.inpro.asBoard.as.dto.*;
import com.inpro.asBoard.as.mapper.AsMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/as")
@RequiredArgsConstructor
public class AsController {

    private final AsMapper asMapper;
    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    //========================================================================
    // 1. 접수 등록 페이지
    // 2. URL : [GET]{...}/as/write
    // 3. Param :
    // 4. 설명 : AS 접수글 등록
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/write")
    public String showWritePage(Model model) {
        List<Map<String, String>> farmList = asMapper.selectFarmList();
        model.addAttribute("farmList", farmList);
        return "as/write";
    }

    //========================================================================
    // 1. AS 접수 목록 조회 페이지
    // 2. URL : [GET]{...}/as/list
    // 3. Param : q (optional, string)             // alias: keyword
    //            statuses (optional, CSV string)  // alias: asStatus (단일도 허용)
    //            farms (optional, CSV string)
    //            types (optional, CSV string)     // "기타" 포함 가능
    //            tables (optional, CSV string)
    //            equipName (optional, string)
    //            period (optional, today|1w|1m)
    //            dateBy (optional, plan|reg, default=plan)
    //            startDate (optional, ISO-8601 datetime)
    //            endDate   (optional, ISO-8601 datetime)
    //            sortField (optional, asId|planDate|regDate, default=asId)
    //            sortDir   (optional, asc|desc, default=desc)
    //            page (optional, int, default=1)
    //            size (optional, int, default=10)
    // 4. 설명 : AS 접수글 목록 조회 및 필터링
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("list") // 클래스 레벨 매핑이 있으면 "/list"로 바꿔도 됨
    public String showListPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String asStatus,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDir,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int size,

            // 고급필터
            @RequestParam(required = false) String farms,     // CSV
            @RequestParam(required = false) String types,     // CSV
            @RequestParam(required = false) String statuses,  // CSV
            @RequestParam(required = false) String tables,    // CSV
            @RequestParam(required = false) String equipName,
            @RequestParam(required = false) String period,    // today|1w|1m
            @RequestParam(required = false) String q,         // 자유검색

            // 날짜 기준
            @RequestParam(defaultValue = "plan") String dateBy,

            Model model
    ) {
        final int offset = (page - 1) * size;

        // 레거시 → 신규 흡수
        if ((q == null || q.isBlank()) && keyword != null && !keyword.isBlank()) {
            q = keyword.trim();
        }
        if ((statuses == null || statuses.isBlank()) && asStatus != null && !asStatus.isBlank()) {
            statuses = asStatus; // 단일도 CSV처럼 처리
        }
        if (sortField == null || sortField.isBlank()) sortField = "asId";
        if (sortDir   == null || sortDir.isBlank())   sortDir   = "desc";

        // CSV → List
        List<String> farmList   = csvToList(farms);
        List<String> typeList   = csvToList(types);
        List<String> statusList = csvToList(statuses);
        List<String> tableList  = csvToList(tables);

        boolean includeEtc  = typeList.stream().anyMatch("기타"::equals);
        List<String> typesNonEtc = typeList.stream().filter(t -> !"기타".equals(t)).toList();
        boolean includeHold = statusList.contains("보류");

        // ✅ dateBy 정규화 (예외값 방지)
        String dateByNorm = "reg".equalsIgnoreCase(dateBy) ? "reg" : "plan";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("farms",       farmList);
        params.put("types",       typesNonEtc);
        params.put("includeEtc",  includeEtc);
        params.put("statuses",    statusList);
        params.put("includeHold", includeHold);
        params.put("tables",      tableList);
        params.put("equipName",   equipName);
        params.put("period",      period);
        params.put("q",           (q != null && !q.isBlank()) ? q.trim() : null);

        // ✅ 기간 기준 + 기간 값 전달 (XML에서 dateBy=plan→PLAN_DATE, reg→REG_DATE 기준으로 분기)
        params.put("dateBy",      dateByNorm);
        params.put("startDate",   startDate);
        params.put("endDate",     endDate);

        // 페이징/정렬
        params.put("size",        size);
        params.put("offset",      offset);
        params.put("sortField",   sortField);
        params.put("sortDir",     sortDir);

        // 단일 쿼리
        List<AsDto> list   = asMapper.selectAsListFiltered(params);
        int totalCount       = asMapper.countAsListFiltered(params);
        int totalPages       = (int) Math.ceil((double) totalCount / size);

        model.addAttribute("asList",     list);
        model.addAttribute("page",       page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("asStatusList",
                List.of("보류", "대기", "임박", "진행중", "지연", "완료"));

        // 장비 테이블 목록(동적)
        model.addAttribute("tableNames", asMapper.selectDistinctEquipTablesForFilter());

        // 파라미터 보존(뷰에서 그대로 씀)
        params.put("page", page);
        params.put("size", size);
        params.put("typesRaw",  types);
        params.put("tablesRaw", tables);
        // ✅ dateBy도 보존
        params.put("dateBy", dateByNorm);
        model.addAttribute("param", params);

        return "as/list";
    }

    private static List<String> csvToList(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .distinct()
                .toList();
    }

    //========================================================================
    // 1. 일정 관리 페이지
    // 2. URL : [GET]{...}/as/calendar
    // 3. Param :
    // 4. 설명 : 월간 캘린더 및 주간 일정표 조회
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/calendar")
    public String showCalendarPage(Model model) {

        /* 1) 전체 일정 */
        List<AsScheduleDto> calendarScheduleList =
                asMapper.selectAllForSchedule();

        /* 2) 이번 주(월~일) 중 표시할 날짜 계산 */
        LocalDate monday = LocalDate.now()
                .with(DayOfWeek.MONDAY);
        List<LocalDate> fullWeek = IntStream.range(0, 7)
                .mapToObj(monday::plusDays)
                .toList();

        List<LocalDate> visibleWeekList = fullWeek.stream()
                .filter(day -> {
                    int dow = day.getDayOfWeek().getValue();
                    if (dow < 6) return true;  // 월~금은 항상
                    return calendarScheduleList.stream()
                            .anyMatch(s ->
                                    s.getPlanDate().toLocalDate().equals(day));
                })
                .toList();

        /* 3) 농장별 색상 팔레트 (고유코드 순차 할당 + 문자열 키 정규화) */
        Map<String, Integer> farmColorMap = new LinkedHashMap<>();

        List<String> codes = calendarScheduleList.stream()
                .map(AsScheduleDto::getFarmCode)            // String/Number 어떤 타입이어도 OK
                .filter(Objects::nonNull)
                .map(c -> String.valueOf(c).trim())          // ★ 문자열 통일 + TRIM
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        int colorIdx = 0;
        for (String code : codes) {
            farmColorMap.put(code, colorIdx++ % 20);         // 고유코드만 0..110 순차 할당
        }


        /* 4) 뷰 모델 */
        model.addAttribute("calendarScheduleList",
                calendarScheduleList);
        model.addAttribute("visibleWeekList", visibleWeekList);
        model.addAttribute("farmColorMap", farmColorMap);

        return "as/calendar";
    }

    //========================================================================
    // 1. AS 접수 내역 상세 페이지
    // 2. URL : [GET]{...}/as/detail/{asId}
    // 3. Param : asId
    // 4. 설명 : AS 접수 내역 상세 조회
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/detail/{asId}")
    public String showDetail(@PathVariable Long asId, Model model) {

        // 1. 헤더 + STATUS_LABEL
        AsDto as = asMapper.selectAsDetail(asId);

        // 2. 서브 목록들
        as.setEquipList(asMapper.selectEquipListByAsId(asId));
        as.setFileList(asMapper.selectFileListByAsId(asId));
        as.setScheduleList(asMapper.selectScheduleListByAsId(asId));

        model.addAttribute("as", as);
        return "as/detail";
    }

    //========================================================================
    // 1. AS 접수 내역 수정 페이지
    // 2. URL : [GET]{...}/as/edit/{asId}
    // 3. Param : asId
    // 4. 설명 : AS 접수 내역 수정
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/edit/{asId}")
    public String showEditPage(@PathVariable Long asId, Model model) {
        AsDto as = asMapper.selectAsDetail(asId);
        as.setEquipList(asMapper.selectEquipListByAsId(asId));
        as.setFileList(asMapper.selectFileListByAsId(asId));
        as.setScheduleList(asMapper.selectScheduleListByAsId(asId));
        model.addAttribute("as", as);
        model.addAttribute("farmList", asMapper.selectFarmList());
        return "as/edit";
    }

    //========================================================================
    // 1. AS 일정 내역 페이지
    // 2. URL : [GET]{...}/as/schedule
    // 3. 3. Param : q (optional, string)               // 자유검색어
    //            farms (optional, CSV string)          // 농가코드 목록
    //            types (optional, CSV string)          // 장비/네트워크/모듈/기타
    //            tables (optional, CSV string)         // 장비 테이블명 목록
    //            equipName (optional, string)          // 장비명 LIKE
    //            completion (optional, string)         // all|done|undone
    //            period (optional, string)             // today|1w|1m
    //            startDate (optional, ISO-8601 datetime)
    //            endDate   (optional, ISO-8601 datetime)
    //            sortField (optional, string, default=planDate)
    //            sortDir   (optional, string, default=asc)
    //            page      (optional, int, default=1)
    //            size      (optional, int, default=10)
    // 4. 설명 : AS 일정 내역 조회/필터/정렬/페이징.
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/schedule")
    public String scheduleList(
            // 상단 자유검색
            @RequestParam(required = false) String q,

            // 고급 필터
            @RequestParam(required = false) String farms,     // CSV
            @RequestParam(required = false) String types,     // CSV (장비/네트워크/모듈/기타)
            @RequestParam(required = false) String tables,    // CSV (장비 카테고리)
            @RequestParam(required = false) String equipName, // 장비명 LIKE
            @RequestParam(required = false) String completion, // 전체/완료/미완료
            @RequestParam(required = false) String period,     // today|1w|1m
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            // 정렬(드로어 내부)
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDir,

            // 페이징
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int size,

            Model model
    ) {
        int offset = (page - 1) * size;

        if (sortField == null || sortField.isBlank()) sortField = "planDate"; // 일정 특성상 예정일 기본
        if (sortDir   == null || sortDir.isBlank())   sortDir   = "asc";

        // CSV → List
        List<String> farmList   = csvToList2(farms);
        List<String> typeList   = csvToList2(types);
        List<String> tableList  = csvToList2(tables);

        boolean includeEtc  = typeList.stream().anyMatch("기타"::equals);
        List<String> typesNonEtc = typeList.stream().filter(t -> !"기타".equals(t)).toList();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("q",           (q != null && !q.isBlank()) ? q.trim() : null);
        params.put("farms",       farmList);
        params.put("types",       typesNonEtc);
        params.put("includeEtc",  includeEtc);
        params.put("tables",      tableList);
        params.put("equipName",   equipName);
        params.put("completion",  completion);
        params.put("period",      period);
        params.put("startDate",   startDate);
        params.put("endDate",     endDate);
        params.put("sortField",   sortField);
        params.put("sortDir",     sortDir);
        params.put("offset",      offset);
        params.put("size",        size);

        List<AsScheduleRowDto> list = asMapper.selectScheduleRowListWithPaging(params);
        int total = asMapper.countScheduleRowList(params);
        int totalPages = (int) Math.ceil(total / (double) size);

        model.addAttribute("rowList", list);
        model.addAttribute("total", total);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);

        // 장비 테이블(카테고리) 목록
        model.addAttribute("tableNames", asMapper.selectDistinctEquipTablesForFilter());

        // 파라미터 보존(뷰에서 그대로 사용)
        params.put("page", page);
        params.put("size", size);
        params.put("typesRaw",  types);
        params.put("tablesRaw", tables);
        model.addAttribute("param", params);

        return "as/schedule";
    }

    //========================================================================
    // 1. AS 일정 내역 내보내기
    // 2. URL : [GET]{...}/as/schedule/export
    // 3. Param : q, farms, types, tables, equipName, completion, period,
    //          startDate, endDate, sortField, sortDir, page, size,
    // 4. 설명 : 필터링된 일정 내역 Excel 파일로 내보내기
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    @GetMapping("/schedule/export")
    public ResponseEntity<byte[]> exportExcel(
            // 필터들 동일
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String farms,
            @RequestParam(required = false) String types,
            @RequestParam(required = false) String tables,
            @RequestParam(required = false) String equipName,
            @RequestParam(required = false) String completion,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDir,

            // th 체크박스로 고른 컬럼 전달 (CSV)
            @RequestParam(required = false) String cols
    ) throws Exception {

        if (sortField == null || sortField.isBlank()) sortField = "planDate";
        if (sortDir   == null || sortDir.isBlank())   sortDir   = "asc";

        List<String> farmList   = csvToList2(farms);
        List<String> typeList   = csvToList2(types);
        List<String> tableList  = csvToList2(tables);
        boolean includeEtc      = typeList.stream().anyMatch("기타"::equals);
        List<String> typesNonEtc= typeList.stream().filter(t -> !"기타".equals(t)).toList();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("q",           (q != null && !q.isBlank()) ? q.trim() : null);
        params.put("farms",       farmList);
        params.put("types",       typesNonEtc);
        params.put("includeEtc",  includeEtc);
        params.put("tables",      tableList);
        params.put("equipName",   equipName);
        params.put("completion",  completion);
        params.put("period",      period);
        params.put("startDate",   startDate);
        params.put("endDate",     endDate);
        params.put("sortField",   sortField);
        params.put("sortDir",     sortDir);

        List<AsScheduleRowDto> rows = asMapper.selectScheduleRowListForExport(params);

        // 선택 컬럼 파싱 (기본: 전부)
        List<String> defaultCols = List.of(
                "asId","farmName","farmCode","regionName","projectName",
                "asType","reqDate","planDate","completeDate","completedMark"
        );
        Set<String> allowed = new LinkedHashSet<>(defaultCols);
        List<String> colKeys = (cols == null || cols.isBlank())
                ? defaultCols
                : Arrays.stream(cols.split(",")).map(String::trim)
                .filter(allowed::contains).distinct().toList();
        if (colKeys.isEmpty()) colKeys = defaultCols;

        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("일정내역");
            CreationHelper ch = wb.getCreationHelper();
            CellStyle dateTimeStyle = wb.createCellStyle();
            dateTimeStyle.setDataFormat(ch.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

            Map<String, String> headerMap = Map.of(
                    "asId","AS_ID", "farmName","농가명", "farmCode","농가코드", "regionName","지역",
                    "projectName","사업명", "asType","문제유형", "reqDate","접수일", "planDate","예정일",
                    "completeDate","완료일", "completedMark","완료"
            );

            // 헤더
            Row head = sh.createRow(0);
            for (int i=0; i<colKeys.size(); i++) head.createCell(i).setCellValue(headerMap.get(colKeys.get(i)));

            // 데이터
            int r = 1;
            for (AsScheduleRowDto row : rows) {
                Row rr = sh.createRow(r++);
                int c = 0;

                for (String k : colKeys) {
                    switch (k) {
                        case "asId"        -> rr.createCell(c++).setCellValue(row.getAsId() == null ? 0 : row.getAsId());
                        case "farmName"    -> rr.createCell(c++).setCellValue(nn(row.getFarmName()));
                        case "farmCode"    -> rr.createCell(c++).setCellValue(nn(row.getFarmCode()));
                        case "regionName"  -> rr.createCell(c++).setCellValue(nn(row.getRegionName()));
                        case "projectName" -> rr.createCell(c++).setCellValue(nn(row.getProjectName()));
                        case "asType"      -> rr.createCell(c++).setCellValue(nn(row.getAsType()));
                        case "reqDate"     -> writeDateCell(rr, c++, row.getReqDate(), dateTimeStyle);
                        case "planDate"    -> writeDateCell(rr, c++, row.getPlanDate(), dateTimeStyle);
                        case "completeDate"-> writeDateCell(rr, c++, row.getCompleteDate(), dateTimeStyle);
                        case "completedMark" -> {
                            String mark = "완료".equals(row.getCompletionLabel()) ? "O" : "X";
                            rr.createCell(c++).setCellValue(mark);
                        }
                    }
                }
            }

            for (int i=0; i<colKeys.size(); i++) sh.autoSizeColumn(i);
            wb.write(bos);
            String fname = URLEncoder.encode("일정내역.xlsx", StandardCharsets.UTF_8).replace("+","%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fname)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bos.toByteArray());
        }
    }

    private static String nn(Object o){ return o==null? "" : String.valueOf(o); }

    // ===== 일정 전용 CSV 파서 =====
    private static List<String> csvToList2(String s) {
        if (s == null || s.isBlank()) return Collections.emptyList();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    // ===== ZONE_SEOUL 시간대 설정 =====
    private static void writeDateCell(Row row, int col, LocalDateTime ldt, CellStyle style) {
        Cell cell = row.createCell(col);
        if (ldt == null) { cell.setBlank(); return; }
        cell.setCellStyle(style);
        Date d = Date.from(ldt.atZone(ZONE_SEOUL).toInstant());
        cell.setCellValue(d);
    }

}