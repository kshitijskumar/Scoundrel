package org.example.scoundrel.deck.scoundrel

import org.example.scoundrel.cards.CardSuit
import org.example.scoundrel.cards.CardsAppModel
import org.example.scoundrel.deck.BaseCardDeckCreator

class ScoundrelDeckCreator : BaseCardDeckCreator() {

    override fun createFaceCardsForSuit(suit: CardSuit): List<CardsAppModel.Face> {
        return when(suit) {
            CardSuit.SPADES,
            CardSuit.CLUBS -> {
                super.createFaceCardsForSuit(suit)
            }
            CardSuit.DIAMONDS,
            CardSuit.HEARTS -> {
                // we do not need face cards for the following suits
                listOf()
            }
        }
    }

}