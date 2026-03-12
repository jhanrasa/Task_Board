package org.example.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class BoardColumnDTO {
    private Long id;
    private String name;
    private ColumnKind kind;
    private int order;
}