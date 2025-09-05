package com.inpro.asBoard.as.dto;

import lombok.Data;

@Data
public class DashAggregateItemDto {
    /** 라벨
     *  - dimension=farm      → FARM_NAME
     *  - dimension=equipment → TABLE_NAME
     *  - dimension=category  → AS_TYPE
     */
    private String label;

    /** 집계 건수 */
    private int count;

    // 선택적으로 내려오는 보조정보(있으면 프론트가 코드까지 사용 가능)
    private String farmCode;
    private String farmName;
    private String tableName;  // 장비 테이블명
    private Long   firstAsId;  // ← cnt==1일 때만 세팅
}