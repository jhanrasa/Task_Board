package org.example.service;

import org.example.dto.CardDTO;
import org.example.dto.BoardColumnDTO;
import org.example.dto.ColumnKind;
import org.example.persistence.BoardDAO;
import org.example.persistence.CardDAO;
import lombok.RequiredArgsConstructor;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class CardService {
    private final CardDAO cardDAO = new CardDAO();
    private final BoardDAO boardDAO = new BoardDAO();

    //Validates two things before authorizing the column switch: that the card isn't locked, and that the destination column is the correct logical sequence.
    public void moveCardToNextColumn(Long cardId) throws SQLException {
        CardDTO card = cardDAO.findById(cardId);
        if (card == null) throw new IllegalArgumentException("Card não encontrado!");

        if (cardDAO.hasActiveBlock(cardId)) {
            throw new IllegalStateException("O card está bloqueado e não pode ser movido!");
        }

        int nextOrder = card.getCurrentColumn().getOrder() + 1;
        BoardColumnDTO nextColumn = cardDAO.findColumnByOrder(card.getBoardId(), nextOrder);

        if (nextColumn == null) {
            throw new IllegalStateException("O card já está na última coluna!");
        }

        cardDAO.updateColumn(cardId, nextColumn.getId());
    }

    public List<CardDTO> listAllCards() throws SQLException {
        return cardDAO.findAll();
    }

    //Ensures that no card starts "in the middle" of the board; it should always start in the INITIAL column of type.
    public void createCard(CardDTO card) throws SQLException {

        if (card.getCurrentColumn().getKind() != ColumnKind.INITIAL) {
            throw new IllegalArgumentException("Cards novos devem começar em uma coluna inicial!");
        }

        cardDAO.insert(card);
    }

    public void blockCard(Long cardId, String reason) throws SQLException {

        CardDTO card = cardDAO.findById(cardId);
        if (card == null) {
            throw new IllegalArgumentException("Card com ID " + cardId + " não encontrado!");
        }

        if (cardDAO.hasActiveBlock(cardId)) {
            throw new IllegalStateException("Este card já possui um bloqueio ativo!");
        }

        cardDAO.block(cardId, reason);
    }

    public List<CardDTO> listCardsByBoardId(Long boardId) throws SQLException {
        return cardDAO.findByBoardId(boardId);
    }

    //Um atalho que busca a coluna FINAL do board atual e move o card para lá de uma vez, desde que ele esteja desbloqueado
    public void finishCard(Long cardId) throws SQLException {
        CardDTO card = cardDAO.findById(cardId);
        if (card == null) throw new IllegalArgumentException("Card não encontrado!");

        if (cardDAO.hasActiveBlock(cardId)) {
            throw new IllegalStateException("Não é possível concluir uma tarefa bloqueada!");
        }

        BoardColumnDTO finalColumn = boardDAO.findColumnByKind(card.getBoardId(), ColumnKind.FINAL);
        cardDAO.updateColumn(cardId, finalColumn.getId());
    }

    public void unblockCard(Long cardId, String reason) throws SQLException {
        if (!cardDAO.hasActiveBlock(cardId)) {
            throw new IllegalStateException("Este card não possui nenhum bloqueio ativo para ser removido!");
        }

        cardDAO.unblock(cardId, reason);
        System.out.println("Card #" + cardId + " desbloqueado com sucesso.");
    }
}