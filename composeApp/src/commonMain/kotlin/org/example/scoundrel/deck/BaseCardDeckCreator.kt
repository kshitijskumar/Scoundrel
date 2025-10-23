package org.example.scoundrel.deck

import org.example.scoundrel.cards.CardFace
import org.example.scoundrel.cards.CardSuit
import org.example.scoundrel.cards.CardsAppModel

abstract class BaseCardDeckCreator {

    open fun createDeck(): List<CardsAppModel> {
        val deck = mutableListOf<CardsAppModel>()
        createSuits().forEach { suit ->
            deck.addAll(createValueCardsForSuit(suit))
            deck.addAll(createFaceCardsForSuit(suit))
        }
        return deck
    }

    fun shuffleDeck(deck: List<CardsAppModel>): List<CardsAppModel> {
        var cards: List<CardsAppModel> = deck
        (1..3).forEach { _ -> cards = cards.shuffled() }
        return cards
    }

    protected open fun createSuits(): List<CardSuit> = CardSuit.entries

    protected open fun createValueCardsForSuit(suit: CardSuit): List<CardsAppModel.Value> {
        return (2..10).map { CardsAppModel.Value(it, suit) }
    }

    protected open fun createFaceCardsForSuit(suit: CardSuit): List<CardsAppModel.Face> {
        return CardFace.entries.map {
            CardsAppModel.Face(it, suit)
        }
    }

}