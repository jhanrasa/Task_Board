package org.example.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BoardDTO {
    private Long id;
    private String name;
    private List<BoardColumnDTO> columns;
    private List<CardDTO> cards;
}