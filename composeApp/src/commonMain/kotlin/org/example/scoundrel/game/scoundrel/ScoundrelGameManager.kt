package org.example.scoundrel.game.scoundrel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.scoundrel.cards.CardFace
import org.example.scoundrel.cards.CardSuit
import org.example.scoundrel.cards.CardsAppModel
import org.example.scoundrel.deck.scoundrel.ScoundrelDeckCreator
import org.example.scoundrel.game.scoundrel.ScoundrelGameManagerImpl.Companion.MAX_HP

interface ScoundrelGameManager {

    val gameState: StateFlow<ScoundrelGameState?>

    fun initialise()

    fun isValidMove(move: ScoundrelGameMoves): Boolean

    suspend fun makeMove(move: ScoundrelGameMoves): Boolean

    fun clear()

}

class ScoundrelGameManagerImpl(
    private val deckCreator: ScoundrelDeckCreator = ScoundrelDeckCreator(),
    private val scoundrelMoveManager: ScoundrelMoveManager = ScoundrelMoveManagerImpl()
): ScoundrelGameManager {

    private val _gameState = MutableStateFlow<ScoundrelGameState?>(null)
    private val moveMutex = Mutex()
    override val gameState get() = _gameState.asStateFlow()

    override fun initialise() {
        val deck = deckCreator.createDeck()
        val shuffleDeck = deckCreator.shuffleDeck(deck)

        val roomAndRemainingDeck = getRoomAndRemainingDungeon(shuffleDeck)
        if (roomAndRemainingDeck == null) {
            // shouldn't happen
            return
        }
        _gameState.update {
            ScoundrelGameState(
                healthPoints = MAX_HP,
                dungeonDeck = roomAndRemainingDeck.second,
                currentRoomState = ScoundrelRoomState(
                    roomDeck = roomAndRemainingDeck.first,
                    cardPlayedInRoom = listOf()
                ),
                discardedDeck = listOf(),
                weaponDeck = listOf()
            )
        }
    }

    /**
     * @return first contains room cards and second contains updated deck
     */
    private fun getRoomAndRemainingDungeon(currentDeck: List<CardsAppModel>): Pair<List<CardsAppModel>, List<CardsAppModel>>? {
        val isRequiredCardPresent = currentDeck.size >= ROOM_SIZE
        if (!isRequiredCardPresent) {
            return null
        }

        val updatedDeck = currentDeck.dropLast(ROOM_SIZE)
        val roomCards = currentDeck.subtract(updatedDeck).toList()

        return (roomCards to updatedDeck)
    }

    override fun isValidMove(move: ScoundrelGameMoves): Boolean {
        val currentState = gameState.value ?: return false
        return scoundrelMoveManager.isValidMove(currentState, move)
    }

    override suspend fun makeMove(move: ScoundrelGameMoves): Boolean {
        return moveMutex.withLock {
            if (!isValidMove(move)) {
                return false
            }

            val currentRoomState = gameState.value ?: return false

            _gameState.update {
                scoundrelMoveManager.makeMove(
                    currentGameState = currentRoomState,
                    move = move
                )
            }
            true
        }
    }

    override fun clear() {
        _gameState.update { null }
    }

    companion object {
        const val MAX_HP = 20
        const val ROOM_SIZE = 4
    }

}

data class ScoundrelGameState(
    val healthPoints: Int = MAX_HP,
    val dungeonDeck: List<CardsAppModel>,
    val currentRoomState: ScoundrelRoomState,
    val discardedDeck: List<CardsAppModel>,
    val weaponDeck: List<CardsAppModel>,
)

data class ScoundrelRoomState(
    val roomDeck: List<CardsAppModel>,
    val cardPlayedInRoom: List<CardsAppModel>
)

sealed class ScoundrelGameMoves {

    data class Weapon(val card: CardsAppModel) : ScoundrelGameMoves()
    data class Monster(val card: CardsAppModel, val fightBareHanded: Boolean) : ScoundrelGameMoves()
    data class HealthPotion(val card: CardsAppModel) : ScoundrelGameMoves()

    data class DiscardHealthPotion(val card: CardsAppModel) : ScoundrelGameMoves()

}

fun CardsAppModel.isWeaponCard(): Boolean {
    val isWeaponSuit = this.suit == CardSuit.DIAMONDS

    if (!isWeaponSuit) {
        return false
    }

    return when(this) {
        is CardsAppModel.Face -> {
            // weapons cannot be face cards
            false
        }
        is CardsAppModel.Value -> {
            true
        }
    }
}

fun CardsAppModel.isPotionCard(): Boolean {
    val isPotionSuit = this.suit == CardSuit.HEARTS
    if (!isPotionSuit) {
        return false
    }

    return when(this) {
        is CardsAppModel.Face -> {
            // potions cannot be face cards
            false
        }
        is CardsAppModel.Value -> {
            true
        }
    }
}

fun CardsAppModel.isMonsterCard(): Boolean {
    return this.suit == CardSuit.CLUBS || this.suit == CardSuit.SPADES
}

fun CardsAppModel.weaponValue(): Int? {
    return when(this.suit) {
        CardSuit.DIAMONDS -> {
            when(this) {
                is CardsAppModel.Face -> null // weapons cannot be face card
                is CardsAppModel.Value -> this.number
            }
        }
        CardSuit.HEARTS,
        CardSuit.SPADES,
        CardSuit.CLUBS -> {
            null
        }
    }
}

fun CardsAppModel.monsterValue(): Int? {
    return when(this.suit) {
        CardSuit.SPADES,
        CardSuit.CLUBS -> {
            when(this) {
                is CardsAppModel.Face -> {
                    this.face.monsterValue()
                }
                is CardsAppModel.Value -> this.number
            }
        }
        CardSuit.DIAMONDS,
        CardSuit.HEARTS -> null
    }
}

fun CardsAppModel.healthValue(): Int? {
    return when(this.suit) {
        CardSuit.HEARTS -> {
            when(this) {
                is CardsAppModel.Face -> null
                is CardsAppModel.Value -> this.number
            }
        }
        CardSuit.DIAMONDS,
        CardSuit.SPADES,
        CardSuit.CLUBS -> null
    }
}

fun CardFace.monsterValue(): Int {
    return when(this) {
        CardFace.JACK -> CardFace.JACK_SCOUNDREL_MONSTER_VALUE
        CardFace.QUEEN -> CardFace.QUEEN_SCOUNDREL_MONSTER_VALUE
        CardFace.KING -> CardFace.KING_SCOUNDREL_MONSTER_VALUE
        CardFace.ACE -> CardFace.ACE_SCOUNDREL_MONSTER_VALUE
    }
}