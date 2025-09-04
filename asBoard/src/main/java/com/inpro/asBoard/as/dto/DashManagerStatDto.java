package com.inpro.asBoard.as.dto;

import lombok.Data;

@Data
public class DashManagerStatDto {
    private String asManager; // nullable → "미지정" 처리 가능
    private int count;
}