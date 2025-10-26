package org.example.scoundrel.ui.scoundrel

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.example.scoundrel.base.BaseViewModel
import org.example.scoundrel.cards.CardsAppModel
import org.example.scoundrel.game.scoundrel.ScoundrelGameManager
import org.example.scoundrel.game.scoundrel.ScoundrelGameManagerImpl
import org.example.scoundrel.game.scoundrel.ScoundrelGameMoves
import org.example.scoundrel.game.scoundrel.isMonsterCard
import org.example.scoundrel.game.scoundrel.isPotionCard
import org.example.scoundrel.game.scoundrel.isWeaponCard

class ScoundrelViewModel(
    private val gameManager: ScoundrelGameManager = ScoundrelGameManagerImpl()
) : BaseViewModel<ScoundrelState, ScoundrelIntent, ScoundrelEffect>() {

    private var gameStateJob: Job? = null
    override fun initialState() = ScoundrelState()

    override fun processIntent(intent: ScoundrelIntent) {
        when(intent) {
            ScoundrelIntent.InitialisationIntent -> handleInitialisationIntent()
            ScoundrelIntent.StartGameIntent -> handleStartGameIntent()
            is ScoundrelIntent.SelectCardIntent -> handleSelectCardIntent(intent)
            is ScoundrelIntent.MakeMoveIntent -> handleMakeMoveIntent(intent)
            ScoundrelIntent.CreateNextRoomIntent -> handleCreateNextRoom()
            ScoundrelIntent.SkipCurrentRoomIntent -> handleSkipCurrentRoomIntent()
        }
    }

    private fun handleInitialisationIntent() {

    }

    private fun handleStartGameIntent() {
        gameStateJob?.cancel()
        gameManager.initialise()
        startCollectingGameState()
        updateState {
            it.copy(
                gameStarted = true
            )
        }
    }

    private fun handleSelectCardIntent(intent: ScoundrelIntent.SelectCardIntent) {
        val isCardPresentInRoom = state.value.currentRoomDeck.contains(intent.card)
        if (!isCardPresentInRoom) {
            // shouldn't happen
            return
        }

        val possibleMoveSets = getPossibleMoveSetsForCard(intent.card)

        if (possibleMoveSets.isEmpty()) {
            // ideally wont be empty
            return
        }

        updateState {
            it.copy(
                selectedCard = CardAndPossibleMoveSet(
                    selectedCard = intent.card,
                    moveSets = possibleMoveSets
                )
            )
        }
    }

    private fun handleMakeMoveIntent(intent: ScoundrelIntent.MakeMoveIntent) {
        val isValidMove = intent.card.moveSets.contains(intent.move)
        if (!isValidMove) {
            // ideally should not happen, but if it does, reset the move
            updateState {
                it.copy(
                    selectedCard = null
                )
            }
            return
        }

        viewModelScope.launch {
            val gameMove = intent.move.toGameMove(intent.card.selectedCard)
            updateState {
                it.copy(
                    selectedCard = null
                )
            }
            gameManager.makeMove(gameMove)
        }
    }

    private fun handleCreateNextRoom() {
        if (!state.value.canCreateNextRoom()) {
            // shouldn't happen
            return
        }

        viewModelScope.launch {
            gameManager.createNextRoom()
        }
    }

    private fun handleSkipCurrentRoomIntent() {
        if (!state.value.canSkipCurrentRoom) {
            // shouldn't happen
            return
        }

        viewModelScope.launch {
            gameManager.skipCurrentRoom()
        }
    }

    private fun getPossibleMoveSetsForCard(card: CardsAppModel): List<ScoundrelMoveSet> {
        return when {
            card.isWeaponCard() -> {
                mutableListOf<ScoundrelMoveSet>().apply {
                    val canEquip = gameManager.isValidMove(ScoundrelGameMoves.Weapon(card))
                    add(ScoundrelMoveSet.EquipWeapon(canEquip))
                }
            }
            card.isMonsterCard() -> {
                mutableListOf<ScoundrelMoveSet>().apply {
                    val canFightWithWeapon = gameManager.isValidMove(ScoundrelGameMoves.Monster(card, false))
                    val canFightWithBareHanded = gameManager.isValidMove(ScoundrelGameMoves.Monster(card, true))

                    add(ScoundrelMoveSet.MonsterCard.FightBareHanded(canFightWithBareHanded))
                    add(ScoundrelMoveSet.MonsterCard.FightWithWeapon(canFightWithWeapon))
                }
            }
            card.isPotionCard() -> {
                mutableListOf<ScoundrelMoveSet>().apply {
                    val canDiscard = gameManager.isValidMove(ScoundrelGameMoves.DiscardHealthPotion(card))
                    val canUsePotion = gameManager.isValidMove(ScoundrelGameMoves.HealthPotion(card))

                    add(ScoundrelMoveSet.HealthCard.Discard(canDiscard))
                    add(ScoundrelMoveSet.HealthCard.UsePotion(canUsePotion))
                }
            }
            else -> listOf() // shouldn't happen since cards are always one of these types
        }
    }

    private fun startCollectingGameState() {
        gameStateJob = viewModelScope.launch {
            gameManager.gameState.collect { state ->
                state ?: return@collect
                updateState {
                    it.copy(
                        healthPoints = state.healthPoints,
                        dungeonDeck = state.dungeonDeck,
                        currentRoomDeck = state.currentRoomState.roomDeck,
                        weaponDeck = state.weaponDeck,
                        canSkipCurrentRoom = state.canSkipCurrentRoom,
                        finishState = state.finishState
                    )
                }
            }
        }
    }
}

fun ScoundrelMoveSet.toGameMove(card: CardsAppModel): ScoundrelGameMoves {
    return when(this) {
        is ScoundrelMoveSet.EquipWeapon -> {
            ScoundrelGameMoves.Weapon(card)
        }
        is ScoundrelMoveSet.HealthCard.Discard -> {
            ScoundrelGameMoves.DiscardHealthPotion(card)
        }
        is ScoundrelMoveSet.HealthCard.UsePotion -> {
            ScoundrelGameMoves.HealthPotion(card)
        }
        is ScoundrelMoveSet.MonsterCard.FightBareHanded -> {
            ScoundrelGameMoves.Monster(card, true)
        }
        is ScoundrelMoveSet.MonsterCard.FightWithWeapon -> {
            ScoundrelGameMoves.Monster(card, false)
        }
    }
}