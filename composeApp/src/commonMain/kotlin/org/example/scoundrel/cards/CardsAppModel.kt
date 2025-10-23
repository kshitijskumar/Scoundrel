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
    ACE;

    companion object {
        const val JACK_SCOUNDREL_MONSTER_VALUE = 11
        const val QUEEN_SCOUNDREL_MONSTER_VALUE = 12
        const val KING_SCOUNDREL_MONSTER_VALUE = 13
        const val ACE_SCOUNDREL_MONSTER_VALUE = 14
    }
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