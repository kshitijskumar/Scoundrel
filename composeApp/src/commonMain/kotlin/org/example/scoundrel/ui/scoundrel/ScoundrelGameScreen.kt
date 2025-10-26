package org.example.scoundrel.ui.scoundrel

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.example.scoundrel.cards.CardColor
import org.example.scoundrel.cards.CardFace
import org.example.scoundrel.cards.CardSuit
import org.example.scoundrel.cards.CardsAppModel
import org.example.scoundrel.cards.color
import org.example.scoundrel.game.scoundrel.ScoundrelFinishState
import org.example.scoundrel.game.scoundrel.ScoundrelGameManagerImpl
import org.example.scoundrel.game.scoundrel.healthValue
import org.example.scoundrel.game.scoundrel.monsterValue
import org.example.scoundrel.theme.ColorUtils
import org.example.scoundrel.theme.ShapeUtils.cardShape
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import scoundrel.composeapp.generated.resources.Res
import scoundrel.composeapp.generated.resources.ic_card_border
import scoundrel.composeapp.generated.resources.ic_health_heart
import scoundrel.composeapp.generated.resources.ic_suit_clubs
import scoundrel.composeapp.generated.resources.ic_suit_diamond
import scoundrel.composeapp.generated.resources.ic_suit_hearts
import scoundrel.composeapp.generated.resources.ic_suit_spades
import kotlin.collections.indexOf
import kotlin.math.max

@Composable
fun ScoundrelGameScreen(vm: ScoundrelViewModel) {
    val state by vm.state.collectAsState()

    when {
        !state.gameStarted -> {
            ScoundrelStartScreen(
                onStartClick = { vm.processIntent(ScoundrelIntent.StartGameIntent) }
            )
        }
        state.finishState != null -> {
            state.finishState?.let { finishState ->
                ScoundrelGameFinish(
                    finishState = finishState,
                    replayClick = {  }
                )
            }
        }
        else -> {
            ScoundrelGameScreen(
                state = state,
                sendIntent = vm::processIntent
            )
        }
    }
}

@Composable
private fun ScoundrelStartScreen(
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onStartClick) {
            Text("Start game")
        }
    }
}

@Composable
private fun ScoundrelGameFinish(
    finishState: ScoundrelFinishState,
    replayClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (finishState.finalScore > 0) "You Won!" else "You Lost!"
        )

        Button(onClick = replayClick) {
            Text("Replay")
        }
    }
}

@Composable
private fun ScoundrelGameScreen(
    state: ScoundrelState,
    sendIntent: (ScoundrelIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .gameBackground()
            .padding(16.dp),
    ) {
        ScoundrelHealthBar(
            health = state.healthPoints,
            maxHealth = ScoundrelGameManagerImpl.MAX_HP,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.End)
        )

        Spacer(Modifier.height(36.dp))

        WeaponsDeckOption(
            weaponsDeck = state.weaponDeck,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            DungeonDeckOption(
                dungeonDeck = state.dungeonDeck,
                modifier = Modifier
            )

            RoomMoveOption(
                canCreateRoom = state.canCreateNextRoom(),
                canSkipRoom = state.canSkipCurrentRoom,
                createRoomClick = { sendIntent.invoke(ScoundrelIntent.CreateNextRoomIntent) },
                skipRoomClick = { sendIntent.invoke(ScoundrelIntent.SkipCurrentRoomIntent) },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
            )
        }

        Spacer(Modifier.weight(1f))


        if (state.canCreateNextRoom()) {
            Text(
                text = "Cannot play the next card, create next room to continue",
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(32.dp))
        }

        state.selectedCard?.let {
            SelectedCardMoveSets(
                cardAndPossibleMoveSet = it,
                moveClicked = { moveSet, move ->
                    sendIntent.invoke(ScoundrelIntent.MakeMoveIntent(moveSet, move))
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }

        CurrentRoomSection(
            currentRoomDeck = state.currentRoomDeck,
            selectedCard = state.selectedCard,
            cardClicked = { sendIntent.invoke(ScoundrelIntent.SelectCardIntent(it)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun WeaponsDeckOption(
    weaponsDeck: List<CardsAppModel>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        when {
            weaponsDeck.isEmpty() -> {
                // nothing present in weapons deck
                NoCardPlaceholder(
                    modifier = Modifier.fillMaxHeight(0.33f)
                )
            }
            weaponsDeck.size == 1 -> {
                // no monster slain yet
                val weapon = weaponsDeck.first()
                CardComponent(
                    card = weapon,
                    modifier = Modifier.fillMaxHeight(0.33f)
                )
            }
            else -> {
                // weapon selected and monster slain
                val weapon = weaponsDeck.first()
                val lastMonsterSlain = weaponsDeck.last()
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CardComponent(
                        card = weapon,
                        modifier = Modifier
                            .fillMaxHeight(0.33f)
                    )

                    CardComponent(
                        card = lastMonsterSlain,
                        modifier = Modifier
                            .fillMaxHeight(0.33f)
                            .offset(
                                x = 28.dp,
                                y = 28.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun DungeonDeckOption(
    dungeonDeck: List<CardsAppModel>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.25f),
    ) {
        Text(
            text = "Dungeon",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = modifier,
            contentAlignment = Alignment.BottomEnd
        ) {
            NoCardPlaceholder(
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(Color.Black, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dungeonDeck.size.toString(),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun RoomMoveOption(
    canCreateRoom: Boolean,
    canSkipRoom: Boolean,
    createRoomClick: () -> Unit,
    skipRoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        GameOptionButton(
            text = "Next room",
            enabled = canCreateRoom,
            onClick = createRoomClick,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        GameOptionButton(
            text = "Skip room",
            enabled = canSkipRoom,
            onClick = skipRoomClick,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ScoundrelHealthBar(
    health: Int,
    maxHealth: Int,
    modifier: Modifier = Modifier
) {
    val animatedHealth by animateIntAsState(
        targetValue = health,
        animationSpec = tween(500)
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_health_heart),
            modifier = Modifier.size(32.dp),
            contentDescription = null
        )

        LinearProgressIndicator(
            progress = { (animatedHealth.toFloat() / maxHealth) },
            color = Color.Red,
            trackColor = Color.LightGray,
            drawStopIndicator = {},
            modifier = Modifier
                .height(8.dp)
                .weight(1f)
                .padding(horizontal = 12.dp)
                .rotate(180f)
        )

        Text(
            text = animatedHealth.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White
        )
    }
}

@Composable
private fun CurrentRoomSection(
    currentRoomDeck: List<CardsAppModel>,
    selectedCard: CardAndPossibleMoveSet?,
    cardClicked: (CardsAppModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        var previousCards by remember {
            mutableStateOf<List<CardsAppModel?>>(listOf())
        }
        val currentCardsSlots = remember(currentRoomDeck) {
            val final = createCurrentDeckWithSlots(
                currentRoomDeck = currentRoomDeck,
                previousCardSlots = previousCards
            )
            final.also { previousCards = final }
        }

        currentCardsSlots.forEachIndexed { index, card ->
            if (card == null) {
                NoCardPlaceholder(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .weight(1f)
                )
            } else {
                val isCardSelected = card == selectedCard?.selectedCard

                CardComponent(
                    card = card,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .weight(1f)
                        .scale(if (isCardSelected) 1.25f else 1f)
                        .zIndex(if (isCardSelected) 2f else 0f)
                        .clickable(onClick = { cardClicked.invoke(card) })
                )
            }
        }
    }
}

@Composable
private fun SelectedCardMoveSets(
    cardAndPossibleMoveSet: CardAndPossibleMoveSet,
    moveClicked: (CardAndPossibleMoveSet, ScoundrelMoveSet) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        cardAndPossibleMoveSet.moveSets.forEach {
            MoveComponent(
                move = it,
                onClick = { moveClicked.invoke(cardAndPossibleMoveSet, it) },
                modifier = Modifier
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun MoveComponent(
    move: ScoundrelMoveSet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GameOptionButton(
        text = move.moveName(),
        onClick = onClick,
        enabled = move.canChoose,
        modifier = modifier
    )
}

@Composable
private fun GameOptionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
    Box(
        modifier = modifier
            .background(
                color = ColorUtils.TanBrown,
                shape = shape
            )
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(2.dp)
            .border(2.dp, Color.Black, shape)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

private fun ScoundrelMoveSet.moveName(): String {
    return when(this) {
        is ScoundrelMoveSet.EquipWeapon -> "Equip Weapon"
        is ScoundrelMoveSet.HealthCard.Discard -> "Discard"
        is ScoundrelMoveSet.HealthCard.UsePotion -> "Heal"
        is ScoundrelMoveSet.MonsterCard.FightBareHanded -> "Barehanded"
        is ScoundrelMoveSet.MonsterCard.FightWithWeapon -> "With weapon"
    }
}

private fun createCurrentDeckWithSlots(
    currentRoomDeck: List<CardsAppModel>,
    previousCardSlots: List<CardsAppModel?>
): List<CardsAppModel?> {
    return if (previousCardSlots.isEmpty()) {
        // initial state, consider current deck as truth
        currentRoomDeck
    } else if (previousCardSlots.size == currentRoomDeck.size) {
        // new room got created, consider current deck as truth
        currentRoomDeck
    } else {
        // some card got played
        previousCardSlots.mapIndexed { idx, prevCard ->
            /*
            prev - C1, C2, C3, C4
            card - C1, C2, C3, C4

            play C3

            prev - C1, C2, C3, C4
            card - C1, C2, C4
            res  - C1, C2, null, C4

            play C4

            prev - C1, C2, null, C4
            card - C1, C2
            res  - C1, C2, null, null
             */
            val idxInCurrentDeck = currentRoomDeck.indexOf(prevCard)
            when {
                idxInCurrentDeck == -1 -> {
                    // card not in current deck, empty slot here
                    null
                }
                idx == idxInCurrentDeck -> {
                    // card still in same place
                    currentRoomDeck[idx]
                }
                else -> {
                    // card not in same place, but still in deck
                    prevCard
                }
            }
        }
    }
}

private fun Modifier.gameBackground(): Modifier {
    val gradient = Brush.radialGradient(
        listOf(
            ColorUtils.MediumBlue.copy(alpha = 0.5f),
            Color.Transparent
        ),
    )

    return this
        .background(ColorUtils.DarkBlue)
        .background(gradient)
}

@Composable
private fun CardComponent(
    card: CardsAppModel,
    modifier: Modifier = Modifier
) {
    var cardHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    EmptyCardSlot(
        modifier = modifier
            .border(1.dp, Color.Black, cardShape)
            .background(ColorUtils.LightBrown, cardShape)
            .border(1.dp, ColorUtils.LightBrown, cardShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    cardHeight = with(density) { it.size.height.toDp() }
                }
                .padding(4.dp)
                .border(1.dp, Color.Black, cardShape),
            contentAlignment = Alignment.Center
        ) {
            val suitHeight = (cardHeight.value / 4).dp
            if (suitHeight != 0.dp) {
                Image(
                    painter = painterResource(card.getSuitImg()),
                    contentDescription = card.suit.name,
                    modifier = Modifier
                        .height(suitHeight)
                        .aspectRatio(1f)
                        .animateContentSize()
                )
            }
        }

        Text(
            text = card.getValue(),
            color = card.getColor(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(ColorUtils.LightBrown)
        )

        Text(
            text = card.getValue(),
            color = card.getColor(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .background(ColorUtils.LightBrown)
        )
    }
}

fun CardsAppModel.getValue(): String {
    return when(this) {
        is CardsAppModel.Face -> {
            when(this.face) {
                CardFace.JACK -> "J"
                CardFace.QUEEN -> "Q"
                CardFace.KING -> "K"
                CardFace.ACE -> "A"
            }
        }
        is CardsAppModel.Value -> this.number.toString()
    }
}

fun CardsAppModel.getSuitImg(): DrawableResource {
    return when(this.suit) {
        CardSuit.DIAMONDS -> Res.drawable.ic_suit_diamond
        CardSuit.HEARTS -> Res.drawable.ic_suit_hearts
        CardSuit.SPADES -> Res.drawable.ic_suit_spades
        CardSuit.CLUBS -> Res.drawable.ic_suit_clubs
    }
}

fun CardsAppModel.getColor(): Color {
    return when(this.color) {
        CardColor.RED -> Color.Red
        CardColor.BLACK -> Color.Black
    }
}

@Composable
private fun NoCardPlaceholder(
    modifier: Modifier = Modifier
) {
    EmptyCardSlot(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorUtils.MediumRed.copy(alpha = 0.75f))
        )
    }
}

@Composable
private fun EmptyCardSlot(
    modifier: Modifier = Modifier,
    cardHeight: Dp? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val screenHeight = LocalWindowInfo.current.containerSize.height

    val cardHeightToUse = cardHeight ?: run {
        val maxCardHeightInPixels = screenHeight / 6
        with(density) { maxCardHeightInPixels.toDp() }
    }

    Box(
        modifier = modifier
            .heightIn(max = cardHeightToUse)
            .aspectRatio(7f/10)
            .clip(cardShape),
        contentAlignment = Alignment.Center,
        content = content
    )
}
