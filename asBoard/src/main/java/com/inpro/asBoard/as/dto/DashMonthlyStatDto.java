package com.inpro.asBoard.as.dto;

import lombok.Data;

@Data
public class DashMonthlyStatDto {
    private String month; // yyyy-MM
    private int count;
}