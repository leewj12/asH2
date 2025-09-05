package com.inpro.asBoard.as.dto;

import lombok.Data;

@Data
public class DashCodeNameDto {
    /** 코드값 (예: FARM_CODE, TABLE_NAME 등) */
    private String code;
    /** 표시명 (예: FARM_NAME, TABLE_NAME 등) */
    private String name;
}