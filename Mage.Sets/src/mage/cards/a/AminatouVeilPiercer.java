package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.keyword.SurveilEffect;
import mage.abilities.keyword.MiracleAbility;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.FilterCard;
import mage.filter.predicate.Predicates;
import mage.game.Game;
import mage.players.Player;
import mage.util.CardUtil;
import mage.watchers.common.MiracleWatcher;

import java.util.Collections;
import java.util.UUID;

/**
 * @author YourNameHere
 */
public class AminatouVeilPiercer extends CardImpl {

    public AminatouVeilPiercer(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WIZARD);
        this.power = new MageInt(2);
        this.toughness = new MageInt(4);

        // At the beginning of your upkeep, surveil 2.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                TargetController.YOU, new SurveilEffect(2), false));

        // Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}.
        SimpleStaticAbility miracleAbility = new SimpleStaticAbility(Zone.BATTLEFIELD, new AminatouVeilPiercerMiracleEffect());
        miracleAbility.addWatcher(new MiracleWatcher());
        this.addAbility(miracleAbility);
    }

    public AminatouVeilPiercer(final AminatouVeilPiercer card) {
        super(card);
    }

    @Override
    public AminatouVeilPiercer copy() {
        return new AminatouVeilPiercer(this);
    }
}

class AminatouVeilPiercerMiracleEffect extends ContinuousEffectImpl {

    private static final FilterCard filter = new FilterCard("enchantment cards in your hand");

    static {
        filter.add(Predicates.or(
                CardType.ENCHANTMENT.getPredicate()
        ));
    }

    public AminatouVeilPiercerMiracleEffect() {
        super(Duration.WhileOnBattlefield, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        staticText = "Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}";
    }

    public AminatouVeilPiercerMiracleEffect(final AminatouVeilPiercerMiracleEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }

        // Apply to all enchantment cards owned by the player (so it works when drawn)
        // The card text says "in your hand" but Miracle needs to be on the card when drawn
        for (Card card : game.getCards()) {
            if (card.isOwnedBy(controller.getId()) && filter.match(card, game)) {
                // Calculate the reduced cost using CardUtil
                String miracleCost = CardUtil.reduceCost(card.getManaCost(), 4).getText();

                // Add miracle ability with the reduced cost
                MiracleAbility ability = new MiracleAbility(miracleCost);
                game.getState().addOtherAbility(card, ability);
            }
        }
        return true;
    }

    @Override
    public AminatouVeilPiercerMiracleEffect copy() {
        return new AminatouVeilPiercerMiracleEffect(this);
    }
}