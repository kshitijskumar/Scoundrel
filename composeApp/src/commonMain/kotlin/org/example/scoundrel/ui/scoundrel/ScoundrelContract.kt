package org.example.scoundrel.ui.scoundrel

import org.example.scoundrel.cards.CardsAppModel
import org.example.scoundrel.game.scoundrel.ScoundrelFinishState

data class ScoundrelState(
    val gameStarted: Boolean = false,
    val healthPoints: Int = 0,
    val dungeonDeck: List<CardsAppModel> = listOf(),
    val currentRoomDeck: List<CardsAppModel> = listOf(),
    val weaponDeck: List<CardsAppModel> = listOf(),
    val selectedCard: CardAndPossibleMoveSet? = null,
    val canSkipCurrentRoom: Boolean = false,
    val finishState: ScoundrelFinishState? = null
)

fun ScoundrelState.canCreateNextRoom(): Boolean = this.currentRoomDeck.size == 1

sealed class ScoundrelIntent {

    data object InitialisationIntent : ScoundrelIntent()
    data object StartGameIntent : ScoundrelIntent()

    data class SelectCardIntent(val card: CardsAppModel) : ScoundrelIntent()

    data class MakeMoveIntent(
        val card: CardAndPossibleMoveSet,
        val move: ScoundrelMoveSet
    ) : ScoundrelIntent()

    data object CreateNextRoomIntent : ScoundrelIntent()

    data object SkipCurrentRoomIntent : ScoundrelIntent()

    data object RetryIntent : ScoundrelIntent()

}

data class CardAndPossibleMoveSet(
    val selectedCard: CardsAppModel,
    val moveSets: List<ScoundrelMoveSet>
)

sealed class ScoundrelMoveSet {
    abstract val canChoose: Boolean
    sealed class MonsterCard : ScoundrelMoveSet() {
        data class FightWithWeapon(override val canChoose: Boolean) : MonsterCard()
        data class FightBareHanded(override val canChoose: Boolean) : MonsterCard()
    }

    sealed class HealthCard : ScoundrelMoveSet() {
        data class UsePotion(override val canChoose: Boolean) : HealthCard()
        data class Discard(override val canChoose: Boolean) : HealthCard()
    }

    data class EquipWeapon(override val canChoose: Boolean) : ScoundrelMoveSet()
}

class ScoundrelEffect