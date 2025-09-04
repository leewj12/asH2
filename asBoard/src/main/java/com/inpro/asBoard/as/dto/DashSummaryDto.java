package com.inpro.asBoard.as.dto;

import lombok.Data;

@Data
public class DashSummaryDto {
    private String period;        // "전체", "이번달", "이번주", "오늘"
    private String statusLabel;   // "대기", "진행", "완료", "경고", "보류"
    private int count;
}
