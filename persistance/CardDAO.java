package org.example.persistence;

import org.example.config.ConnectionFactory;
import org.example.dto.BoardColumnDTO;
import org.example.dto.CardDTO;
import org.example.dto.ColumnKind;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDAO {

    //Opens the connection and sends the object data to the table columns.
    public void insert(CardDTO card) throws SQLException {

        Long initialColumnId = getOrCreateInitialColumn(card.getBoardId());

        String sql = "INSERT INTO CARDS (title, description, board_column_id, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, card.getTitle());
            stmt.setString(2, card.getDescription());
            stmt.setLong(3, initialColumnId);
            // Grava o timestamp atual do Java para o banco
            stmt.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) card.setId(rs.getLong(1));
            }
        }
    }

    //A technical lock that ensures that if you create a card on a new board, the "To Do", "Doing", and "Done" columns are created.
    private Long getOrCreateInitialColumn(Long boardId) throws SQLException {
        String sqlCheck = "SELECT id FROM BOARDS_COLUMNS WHERE board_id = ? AND kind = 'INITIAL'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setLong(1, boardId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }

        createDefaultColumns(boardId);

        return getOrCreateInitialColumn(boardId);
    }
    //Creates the default columns
    private void createDefaultColumns(Long boardId) throws SQLException {
        String sql = "INSERT INTO BOARDS_COLUMNS (board_id, name, kind, column_order) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Object[][] defaults = {
                    {boardId, "A Fazer", "INITIAL", 1},
                    {boardId, "Fazendo", "PENDING", 2},
                    {boardId, "Concluído", "FINAL", 3}
            };

            for (Object[] col : defaults) {
                stmt.setLong(1, (Long) col[0]);
                stmt.setString(2, (String) col[1]);
                stmt.setString(3, (String) col[2]);
                stmt.setInt(4, (Integer) col[3]);
                stmt.executeUpdate();
            }
        }
    }


    public void updateColumn(Long cardId, Long columnId) throws SQLException {
        String sql = "UPDATE CARDS SET board_column_id = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, columnId);
            stmt.setLong(2, cardId);
            stmt.executeUpdate();
        }
    }

    public List<CardDTO> findAll() throws SQLException {
        List<CardDTO> cards = new ArrayList<>();
        String sql = "SELECT c.id, c.title, c.description, c.created_at, " +
                "bc.name as column_name, bc.kind as column_kind " +
                "FROM CARDS c " +
                "INNER JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                BoardColumnDTO column = BoardColumnDTO.builder()
                        .name(rs.getString("column_name"))
                        .kind(ColumnKind.valueOf(rs.getString("column_kind")))
                        .build();

                CardDTO card = CardDTO.builder()
                        .id(rs.getLong("id"))
                        .title(rs.getString("title"))
                        .description(rs.getString("description")) // USE O NOME REAL DA COLUNA
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC))
                        .build();

                cards.add(card);
            }
        }
        return cards;
    }

    public void block(Long cardId, String reason) throws SQLException {
        String sql = "INSERT INTO CARDS_BLOCKS (card_id, reason) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            stmt.setString(2, reason);
            stmt.executeUpdate();
        }
    }

    public boolean hasActiveBlock(Long cardId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CARDS_BLOCKS WHERE card_id = ? AND unblocked_at IS NULL";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public CardDTO findById(Long cardId) throws SQLException {
        String sql = "SELECT c.id, c.title, bc.board_id, bc.id as column_id, bc.column_order " +
                "FROM CARDS c " +
                "INNER JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id " +
                "WHERE c.id = ?";

        try (var conn = ConnectionFactory.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cardId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {

                    BoardColumnDTO currentColumn = BoardColumnDTO.builder()
                            .id(rs.getLong("column_id"))
                            .order(rs.getInt("column_order"))
                            .build();

                    return CardDTO.builder()
                            .id(rs.getLong("id"))
                            .title(rs.getString("title"))
                            .boardId(rs.getLong("board_id"))
                            .currentColumn(currentColumn)
                            .build();
                }
            }
        }
        return null;
    }

    public List<CardDTO> findByBoardId(Long boardId) throws SQLException {
        List<CardDTO> cards = new ArrayList<>();

        String sql = "SELECT c.id, c.title, c.description, c.created_at, bc.name as column_name " +
                "FROM CARDS c " +
                "INNER JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id " +
                "WHERE bc.board_id = ? AND bc.kind <> 'FINAL'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, boardId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CardDTO card = CardDTO.builder()
                            .id(rs.getLong("id"))
                            .title(rs.getString("title"))
                            .description(rs.getString("description"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime().atOffset(java.time.ZoneOffset.UTC))
                            .build();

                    cards.add(card);
                }
            }
        }
        return cards;
    }
    public void unblock(Long cardId, String reason) throws SQLException {

        String sql = "UPDATE CARDS_BLOCKS SET unblocked_at = CURRENT_TIMESTAMP, unblock_reason = ? " +
                "WHERE card_id = ? AND unblocked_at IS NULL";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reason);
            stmt.setLong(2, cardId);
            stmt.executeUpdate();
        }
    }
    public BoardColumnDTO findColumnByOrder(Long boardId, int order) throws SQLException {
        String sql = "SELECT id, name, kind, column_order FROM BOARDS_COLUMNS WHERE board_id = ? AND column_order = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, boardId);
            stmt.setInt(2, order);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return BoardColumnDTO.builder()
                            .id(rs.getLong("id"))
                            .name(rs.getString("name"))
                            .kind(ColumnKind.valueOf(rs.getString("kind")))
                            .order(rs.getInt("column_order"))
                            .build();
                }
            }
        }
        return null;
    }
}