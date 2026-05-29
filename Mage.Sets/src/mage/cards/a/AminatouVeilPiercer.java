package mage.cards.a;

import mage.ApprovingObject;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.SpellAbility;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.costs.mana.ManaCost;
import mage.abilities.costs.mana.ManaCosts;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;
import mage.util.CardUtil;
import mage.watchers.Watcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author JustinHolt
 */
public final class AminatouVeilPiercer extends CardImpl {

    public AminatouVeilPiercer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // At the beginning of your upkeep, surveil 2.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(TargetController.YOU, new SurveilEffect(2), false));

        // Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}.
        this.addAbility(new AminatouVeilPiercerMiracleTriggeredAbility());
    }

    private AminatouVeilPiercer(final AminatouVeilPiercer card) {
        super(card);
    }

    @Override
    public AminatouVeilPiercer copy() {
        return new AminatouVeilPiercer(this);
    }
}

class AminatouVeilPiercerMiracleTriggeredAbility extends TriggeredAbilityImpl {

    AminatouVeilPiercerMiracleTriggeredAbility() {
        super(Zone.BATTLEFIELD, new AminatouVeilPiercerMiracleEffect(), true);
        addWatcher(new AminatouVeilPiercerWatcher());
    }

    private AminatouVeilPiercerMiracleTriggeredAbility(final AminatouVeilPiercerMiracleTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public AminatouVeilPiercerMiracleTriggeredAbility copy() {
        return new AminatouVeilPiercerMiracleTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.MIRACLE_CARD_REVEALED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        Card card = game.getCard(event.getTargetId());
        if (card == null) {
            return false;
        }
        if (!card.isOwnedBy(this.getControllerId())) {
            return false;
        }
        if (!card.isEnchantment(game)) {
            return false;
        }
        if (game.getState().getZone(card.getId()) != Zone.HAND) {
            return false;
        }
        getEffects().setTargetPointer(new FixedTarget(card.getId()));
        return true;
    }

    @Override
    public String getRule() {
        return "Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}.";
    }
}

class AminatouVeilPiercerMiracleEffect extends OneShotEffect {

    AminatouVeilPiercerMiracleEffect() {
        super(Outcome.Benefit);
    }

    private AminatouVeilPiercerMiracleEffect(final AminatouVeilPiercerMiracleEffect effect) {
        super(effect);
    }

    @Override
    public AminatouVeilPiercerMiracleEffect copy() {
        return new AminatouVeilPiercerMiracleEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (controller == null || card == null) {
            return false;
        }
        SpellAbility abilityToCast = card.getSpellAbility().copy();
        ManaCosts<ManaCost> miracleCost = CardUtil.reduceCost(card.getManaCost(), 4);
        ManaCosts<ManaCost> costRef = abilityToCast.getManaCostsToPay();
        costRef.clear();
        for (ManaCost cost : miracleCost) {
            costRef.add(cost.copy());
        }
        controller.cast(abilityToCast, game, false, new ApprovingObject(source, game));
        return true;
    }
}

class AminatouVeilPiercerWatcher extends Watcher {

    private final Map<UUID, Integer> cardsDrawnThisTurn = new HashMap<>();

    AminatouVeilPiercerWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        if (event.getType() == GameEvent.EventType.UNTAP_STEP_PRE) {
            cardsDrawnThisTurn.clear();
            return;
        }
        if (game.getPhase() == null) {
            return;
        }
        if (event.getType() != GameEvent.EventType.DREW_CARD) {
            return;
        }
        UUID playerId = event.getPlayerId();
        if (playerId == null) {
            return;
        }
        int amount = 1 + cardsDrawnThisTurn.getOrDefault(playerId, 0);
        cardsDrawnThisTurn.put(playerId, amount);
        if (amount != 1) {
            return;
        }
        Player player = game.getPlayer(playerId);
        if (player == null) {
            return;
        }
        Card card = game.getCard(event.getTargetId());
        if (card == null
                || !card.isOwnedBy(playerId)
                || !card.isEnchantment(game)
                || game.getState().getZone(card.getId()) != Zone.HAND) {
            return;
        }
        boolean aminatouPresent = false;
        for (Permanent perm : game.getBattlefield().getAllActivePermanents(playerId)) {
            if (perm.getName().equals("Aminatou, Veil Piercer")) {
                aminatouPresent = true;
                break;
            }
        }
        if (!aminatouPresent) {
            return;
        }
        Cards cards = new CardsImpl(card);
        player.lookAtCards("Miracle", cards, game);
        if (player.chooseUse(Outcome.Benefit, "Reveal " + card.getLogName() + " to be able to use Miracle?", null, game)) {
            player.revealCards("Miracle", cards, game);
            game.fireEvent(GameEvent.getEvent(GameEvent.EventType.MIRACLE_CARD_REVEALED, card.getId(), null, playerId));
        }
    }

    @Override
    public void reset() {
        super.reset();
        cardsDrawnThisTurn.clear();
    }
}
