package org.example.scoundrel.cards

sealed class CardsAppModel {

    abstract val suit: CardSuit

    data class Value(val number: Int, override val suit: CardSuit) : CardsAppModel()
    data class Face(val face: CardFace, override val suit: CardSuit) : CardsAppModel()

}

enum class CardSuit {
    DIAMONDS,
    HEARTS,
    SPADES,
    CLUBS
}

enum class CardFace {
    JACK,
    QUEEN,
    KING,
    ACE
}

enum class CardColor {
    RED,
    BLACK
}

val CardSuit.color: CardColor
    get() {
        return when(this) {
            CardSuit.DIAMONDS,
            CardSuit.HEARTS -> CardColor.RED
            CardSuit.SPADES,
            CardSuit.CLUBS -> CardColor.BLACK
        }
    }

val CardsAppModel.color: CardColor get() = this.suit.color