package org.example.scoundrel.ui.scoundrel

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.example.scoundrel.cards.CardsAppModel
import org.example.scoundrel.game.scoundrel.ScoundrelFinishState
import org.example.scoundrel.game.scoundrel.ScoundrelGameManagerImpl
import org.example.scoundrel.theme.ColorUtils
import org.example.scoundrel.theme.ShapeUtils.cardShape
import org.jetbrains.compose.resources.painterResource
import scoundrel.composeapp.generated.resources.Res
import scoundrel.composeapp.generated.resources.ic_health_heart
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

        Spacer(Modifier.height(24.dp))

        state.selectedCard?.let { selectedCard ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                selectedCard.moveSets.forEach { move ->
                    Button(
                        onClick = {
                            sendIntent(
                                ScoundrelIntent.MakeMoveIntent(selectedCard, move)
                            )
                        },
                        enabled = move.canChoose
                    ) {
                        Text(move.moveName())
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.currentRoomDeck.forEach { card ->
                Button(
                    onClick = {
                        sendIntent(ScoundrelIntent.SelectCardIntent(card))
                    },
                    Modifier.weight(1f)
                ) {
                    Text(card.toString())
                }
            }
        }

        Button(
            onClick = { sendIntent(ScoundrelIntent.CreateNextRoomIntent) },
            enabled = state.canCreateNextRoom()
        ) {
            Text("Next Room")
        }

        Button(
            onClick = { sendIntent(ScoundrelIntent.SkipCurrentRoomIntent) },
            enabled = state.canSkipCurrentRoom
        ) {
            Text("Skip Room")
        }

        Spacer(Modifier.weight(1f))

        CurrentRoomSection(
            currentRoomDeck = state.currentRoomDeck,
            selectedCard = state.selectedCard,
            cardClicked = { sendIntent.invoke(ScoundrelIntent.SelectCardIntent(it)) },
            moveClicked = { set, move -> sendIntent.invoke(ScoundrelIntent.MakeMoveIntent(set, move)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(48.dp))
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
    moveClicked: (CardAndPossibleMoveSet, ScoundrelMoveSet) -> Unit,
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

        currentCardsSlots.forEach { card ->
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
                        .offset(y = if (isCardSelected) (-12).dp else 0.dp)
                        .scale(if (isCardSelected) 1.25f else 1f)
                        .zIndex(if (isCardSelected) 2f else 0f)
                        .clickable(onClick = { cardClicked.invoke(card) })
                )
            }
        }
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
    EmptyCardSlot(
        modifier = modifier
            .background(Color.White, cardShape)
            .border(4.dp, Color.Black, cardShape)
    ) {
        Text(card.toString())
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
                .background(ColorUtils.LightBrown)
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
