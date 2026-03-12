package org.example.dto;

import lombok.Data;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class CardDTO {
    private Long id;
    private String title;
    private String description;
    private OffsetDateTime createdAt;
    private List<BlockDTO> blocks;
    private BoardColumnDTO currentColumn;
    private Long boardId;
    private String blockReason;
    private String unblockReason;
}