package org.example.scoundrel.game.scoundrel

import org.example.scoundrel.cards.CardSuit
import org.example.scoundrel.cards.CardsAppModel
import org.example.scoundrel.game.scoundrel.ScoundrelGameManagerImpl.Companion.MAX_HP
import org.example.scoundrel.utils.addAllAndGet
import org.example.scoundrel.utils.addAndGet
import org.example.scoundrel.utils.removeAndGet

interface ScoundrelMoveManager {

    fun isValidMove(
        currentGameState: ScoundrelGameState,
        move: ScoundrelGameMoves
    ): Boolean

    suspend fun makeMove(
        currentGameState: ScoundrelGameState,
        move: ScoundrelGameMoves
    ): ScoundrelGameState

}

class ScoundrelMoveManagerImpl : ScoundrelMoveManager {

    override fun isValidMove(
        currentGameState: ScoundrelGameState,
        move: ScoundrelGameMoves
    ): Boolean {
        return when(move) {
            is ScoundrelGameMoves.Monster -> {
                isMonsterUsable(
                    currentGameState = currentGameState,
                    card = move.card,
                    fightBareHanded = move.fightBareHanded
                )
            }
            is ScoundrelGameMoves.HealthPotion -> {
                isPotionUsable(
                    currentGameState = currentGameState,
                    card = move.card
                )
            }
            is ScoundrelGameMoves.Weapon -> isWeaponUsable(move.card)
            is ScoundrelGameMoves.DiscardHealthPotion -> move.card.isPotionCard()
        }
    }

    override suspend fun makeMove(
        currentGameState: ScoundrelGameState,
        move: ScoundrelGameMoves
    ): ScoundrelGameState {
        return when(move) {
            is ScoundrelGameMoves.HealthPotion -> makeHealthPotionMove(currentGameState, move)
            is ScoundrelGameMoves.Monster -> makeMonsterMove(currentGameState, move)
            is ScoundrelGameMoves.Weapon -> makeWeaponMove(currentGameState, move)
            is ScoundrelGameMoves.DiscardHealthPotion -> makeDiscardHealthPotionMove(currentGameState, move)
        }
    }

    private fun isWeaponUsable(card: CardsAppModel): Boolean {
        return card.isWeaponCard()
    }

    private fun isPotionUsable(
        currentGameState: ScoundrelGameState,
        card: CardsAppModel
    ): Boolean {
        val isPotion = card.suit == CardSuit.HEARTS
        if (!isPotion) {
            // shouldn't happen, as checked before
            return false
        }

        return when(card) {
            is CardsAppModel.Face -> {
                // potions cannot be face cards
                false
            }
            is CardsAppModel.Value -> {
                val cardsPlayedInCurrentRoom = currentGameState.currentRoomState.cardPlayedInRoom
                val potionPlayedAlreadyInRoom = cardsPlayedInCurrentRoom.any { it.isPotionCard() }
                !potionPlayedAlreadyInRoom
            }
        }
    }

    private fun isMonsterUsable(
        currentGameState: ScoundrelGameState,
        card: CardsAppModel,
        fightBareHanded: Boolean
    ): Boolean {
        if (!card.isMonsterCard()) {
            return false
        }
        if (fightBareHanded) {
            // barehanded any monster can be fought
            return true
        }

        val weaponsDeck = currentGameState.weaponDeck
        return when {
            weaponsDeck.isEmpty() -> false
            weaponsDeck.size == 1 -> {
                val weaponCard = weaponsDeck.first()
                // this should always be true, since first card of the weapons deck should always be a weapon
                weaponCard.isWeaponCard()
            }
            else -> {
                val lastMonsterSlain = weaponsDeck.last()
                val lastMonsterValue = lastMonsterSlain.monsterValue() ?: return false // shouldn't be null
                val currentMonsterValue = card.monsterValue() ?: return false // shouldn't be null
                currentMonsterValue < lastMonsterValue
            }
        }
    }

    private fun makeWeaponMove(
        currentState: ScoundrelGameState,
        move: ScoundrelGameMoves.Weapon
    ): ScoundrelGameState {
        val currentRoom = currentState.currentRoomState

        val updatedRoom = currentRoom.updateRoomAfterMove(move.card)

        return currentState.copy(
            currentRoomState = updatedRoom,
            discardedDeck = currentState.discardedDeck.addAllAndGet(currentState.weaponDeck), // discard current weapon deck to equip new weapon
            weaponDeck = listOf(move.card) // equip new weapon
        )
    }

    private fun makeHealthPotionMove(
        currentState: ScoundrelGameState,
        move: ScoundrelGameMoves.HealthPotion
    ): ScoundrelGameState {
        val currentRoom = currentState.currentRoomState

        // update HP
        val updatedHealth = getUpdatedHealthPoints(
            currentHp = currentState.healthPoints,
            hpToAffect = (move.card.healthValue() ?: 0)
        )

        // update room with card played state
        val updatedRoom = currentRoom.updateRoomAfterMove(move.card)

        return currentState.copy(
            healthPoints = updatedHealth,
            discardedDeck = currentState.discardedDeck.addAndGet(move.card),
            currentRoomState = updatedRoom
        )
    }

    private fun makeDiscardHealthPotionMove(
        currentGameState: ScoundrelGameState,
        move: ScoundrelGameMoves.DiscardHealthPotion
    ): ScoundrelGameState {
        // not updating this card in cardsPlayed in room, coz technically this was not played, but rather discarded
        return currentGameState.copy(
            currentRoomState = currentGameState.currentRoomState.copy(
                roomDeck = currentGameState.currentRoomState.roomDeck.removeAndGet(move.card),
                cardPlayedInRoom = currentGameState.currentRoomState.cardPlayedInRoom.addAndGet(move.card)
            ),
            discardedDeck = currentGameState.discardedDeck.addAndGet(move.card)
        )
    }

    private fun makeMonsterMove(
        currentState: ScoundrelGameState,
        move: ScoundrelGameMoves.Monster
    ): ScoundrelGameState {
        val currentRoom = currentState.currentRoomState

        return if (move.fightBareHanded) {
            fightMonsterBareHanded(
                currentState = currentState,
                currentRoom = currentRoom,
                card = move.card
            )
        } else {
            fightMonsterWithWeapon(
                currentState = currentState,
                currentRoom = currentRoom,
                card = move.card
            )
        }
    }

    private fun fightMonsterBareHanded(
        currentState: ScoundrelGameState,
        currentRoom: ScoundrelRoomState,
        card: CardsAppModel,
    ): ScoundrelGameState {
        val currentHp = currentState.healthPoints
        val monsterValue = card.monsterValue() ?: return currentState

        val updatedHp = getUpdatedHealthPoints(
            currentHp = currentHp,
            hpToAffect = -monsterValue // negative to reduce health
        )

        return currentState.copy(
            healthPoints = updatedHp,
            currentRoomState = currentRoom.updateRoomAfterMove(card),
            discardedDeck = currentState.discardedDeck.addAndGet(card) // after barehanded fight, monster card considered discarded
        )
    }

    private fun fightMonsterWithWeapon(
        currentState: ScoundrelGameState,
        currentRoom: ScoundrelRoomState,
        card: CardsAppModel,
    ): ScoundrelGameState {
        val currentHp = currentState.healthPoints
        val monsterValue = card.monsterValue() ?: return currentState

        val weaponDeck = currentState.weaponDeck

        if (weaponDeck.isEmpty()) {
            // no weapon, shouldn't happen due to initial guardrail
            return currentState
        }

        // not checking for only weapon VS monster already slain check here since its already handled
        // before making the move
        val weaponValue = weaponDeck.first().weaponValue() ?: return currentState // guardrailed before
        val damageTaken = monsterValue - weaponValue
        val updatedHealth = if (damageTaken > 0) {
            // weapon couldn't block all the damage, taken diff as damage
            getUpdatedHealthPoints(currentHp = currentHp, hpToAffect = -damageTaken)
        } else {
            // weapon blocked all the damage, no hp drain
            currentHp
        }

        return currentState.copy(
            healthPoints = updatedHealth,
            currentRoomState = currentRoom.updateRoomAfterMove(card),
            weaponDeck = currentState.weaponDeck.addAndGet(card)
        )
    }

    private fun getUpdatedHealthPoints(
        currentHp: Int,
        hpToAffect: Int
    ): Int {
        val updatedHealth = currentHp + hpToAffect
        return when {
            updatedHealth > MAX_HP -> MAX_HP
            updatedHealth < 0 -> 0
            else -> updatedHealth
        }
    }

    private fun ScoundrelRoomState.updateRoomAfterMove(card: CardsAppModel): ScoundrelRoomState {
        return this.copy(
            roomDeck = this.roomDeck.removeAndGet(card),
            cardPlayedInRoom = this.cardPlayedInRoom.addAndGet(card)
        )
    }
}