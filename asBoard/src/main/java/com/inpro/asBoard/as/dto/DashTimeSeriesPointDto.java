package com.inpro.asBoard.as.dto;

import lombok.Data;

@Data
public class DashTimeSeriesPointDto {
    /** 버킷(yyyy-MM 또는 yyyy-MM-dd 등) */
    private String bucket;
    /** 건수 */
    private int count;

    private Long   firstAsId;  // ← cnt==1일 때만 세팅
    private String fromDate;   // "yyyy-MM-01T00:00" / "yyyy-MM-ddT00:00"
    private String toDate;     // "yyyy-MM-31T23:59" / "yyyy-MM-ddT23:59"
}