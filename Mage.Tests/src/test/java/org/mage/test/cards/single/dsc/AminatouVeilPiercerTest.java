package org.mage.test.cards.single.dsc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for Aminatou, Veil Piercer
 * {1}{W}{U}{B}
 * Legendary Creature — Human Wizard 2/4
 * At the beginning of your upkeep, surveil 2.
 * Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}.
 *
 * NOTE: PlayerA goes first and skips their draw step on turn 1. All draw-dependent tests run
 * on turn 3 (PlayerA's second turn), requiring 4 buffer cards for two surveil cycles.
 * Library setup comments use "bottom to top" notation: last addCard call = top of library.
 */
public class AminatouVeilPiercerTest extends CardTestPlayerBase {

    private static final String aminatou = "Aminatou, Veil Piercer";

    /**
     * Surveil 2 trigger puts top two library cards in graveyard when chosen.
     * Runs to turn 2 (PlayerB's first turn) to confirm state after PlayerA's turn-1 surveil.
     */
    @Test
    public void testSurveil_BothToGraveyard() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        removeAllCardsFromLibrary(playerA);
        // Library bottom to top (last added = top): Plains, Mountain, Forest
        addCard(Zone.LIBRARY, playerA, "Plains");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerA, "Forest");
        skipInitShuffling();

        // Upkeep surveil 2: put Forest (top) and Mountain (2nd) to graveyard
        addTarget(playerA, "Forest^Mountain");

        setStrictChooseMode(true);
        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertGraveyardCount(playerA, "Forest", 1);
        assertGraveyardCount(playerA, "Mountain", 1);
        assertLibraryCount(playerA, 1); // Plains remains
    }

    /**
     * Drawing an enchantment as the first card this turn triggers miracle.
     * PlayerA skips the draw step on turn 1, so we test on turn 3 (PlayerA's second turn).
     * Two surveil cycles consume 4 buffer cards; Oblivion Ring is drawn on turn 3.
     * Oblivion Ring {2}{W} has miracle cost {W} (generic {2} fully reduced by Aminatou's {4}).
     */
    @Test
    public void testEnchantmentMiracle_OblivionRing() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1); // for {W} miracle cost

        removeAllCardsFromLibrary(playerA);
        // Library bottom to top (last added = top):
        // Oblivion Ring (5th/bottom), Forest (4th), Mountain (3rd), Island (2nd), Plains (1st/top)
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring");
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Plains");        // top

        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        // Turn 1 upkeep: surveil 2 — put Plains (top) and Island (2nd) to graveyard
        addTarget(playerA, "Plains^Island");
        // Turn 1 draw: SKIPPED (PlayerA goes first)
        // Turn 3 (PlayerA's second turn) upkeep: surveil 2 — put Mountain and Forest to graveyard
        addTarget(playerA, "Mountain^Forest");
        // Turn 3 draw: Oblivion Ring drawn (first card this turn), miracle offer
        setChoice(playerA, true); // accept miracle reveal
        setChoice(playerA, true); // accept optional miracle trigger
        // Miracle trigger resolves: cast Oblivion Ring for {W}; target Grizzly Bears
        addTarget(playerA, "Grizzly Bears");

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Oblivion Ring", 1);
        assertExileCount("Grizzly Bears", 1);
    }

    /**
     * Non-enchantment cards drawn do NOT get miracle from Aminatou's ability.
     * Tests on turn 3 (PlayerA's second turn) using 4 buffer cards for two surveil cycles.
     */
    @Test
    public void testNonEnchantment_NoMiracle() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);

        removeAllCardsFromLibrary(playerA);
        // Library bottom to top (last added = top):
        // Lightning Bolt (5th/bottom), Forest (4th), Mountain (3rd), Island (2nd), Plains (1st/top)
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt");
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Plains");        // top
        skipInitShuffling();

        // Turn 1 upkeep: surveil 2 — put Plains and Island to graveyard
        addTarget(playerA, "Plains^Island");
        // Turn 1 draw: SKIPPED (PlayerA goes first)
        // Turn 3 upkeep: surveil 2 — put Mountain and Forest to graveyard
        addTarget(playerA, "Mountain^Forest");
        // Turn 3 draw: Lightning Bolt drawn — NOT an enchantment, no miracle

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // Lightning Bolt is in hand (no miracle, not cast)
        assertHandCount(playerA, "Lightning Bolt", 1);
    }

    /**
     * Miracle only triggers on the FIRST card drawn each turn.
     * Oblivion Ring is drawn first in turn 3's draw step (miracle fires and is accepted).
     * Opt then draws Pacifism as the second card this turn (no miracle).
     */
    @Test
    public void testMiracle_OnlyFirstCardDrawn() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1); // for {U} Opt cost
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1); // for {W} O-Ring miracle cost

        removeAllCardsFromLibrary(playerA);
        // Library bottom to top (last added = top):
        // Pacifism (6th/bottom), Oblivion Ring (5th), Forest (4th), Mountain (3rd), Island-buf (2nd), Plains-buf (1st/top)
        addCard(Zone.LIBRARY, playerA, "Pacifism");
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring");
        addCard(Zone.LIBRARY, playerA, "Forest");
        addCard(Zone.LIBRARY, playerA, "Mountain");
        addCard(Zone.LIBRARY, playerA, "Island");        // buffer, distinct from battlefield Island
        addCard(Zone.LIBRARY, playerA, "Plains");        // top, buffer

        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        // Turn 1 upkeep: surveil 2 — put Plains-buf and Island-buf to graveyard
        addTarget(playerA, "Plains^Island");
        // Turn 1 draw: SKIPPED (PlayerA goes first)
        // Turn 3 upkeep: surveil 2 — put Mountain and Forest to graveyard
        addTarget(playerA, "Mountain^Forest");
        // Turn 3 draw: Oblivion Ring drawn first, miracle offer
        setChoice(playerA, true); // accept miracle reveal
        setChoice(playerA, true); // accept optional miracle trigger
        // Miracle trigger: cast O-Ring for {W}, target Grizzly Bears
        addTarget(playerA, "Grizzly Bears");
        // Turn 3 main: cast Opt ({U}), scry Pacifism (keep on top), draw Pacifism (second draw — no miracle)
        castSpell(3, PhaseStep.PRECOMBAT_MAIN, playerA, "Opt");
        // Scry: skip putting any card on the bottom (keep Pacifism on top)
        addTarget(playerA, org.mage.test.player.TestPlayer.TARGET_SKIP);

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Oblivion Ring", 1);    // cast via miracle (first draw)
        assertHandCount(playerA, "Pacifism", 1);              // second draw: no miracle, stays in hand
        assertExileCount("Grizzly Bears", 1);
    }

    /**
     * Without Aminatou on the battlefield, enchantments do not gain miracle.
     * Tests on turn 3 (PlayerA's second turn) since draw is skipped on turn 1.
     */
    @Test
    public void testNoMiracle_WithoutAminatou() {
        // No Aminatou — no surveil triggers, no miracle granted
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3); // enough for full Oblivion Ring cost {2}{W}

        removeAllCardsFromLibrary(playerA);
        // Library: Oblivion Ring on top (no buffer needed — no surveil)
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        // Turn 1 draw: SKIPPED; Turn 2 (PlayerB): no action
        // Turn 3 draw: Oblivion Ring drawn — no Aminatou so no miracle

        setStrictChooseMode(true);
        setStopAt(3, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // Oblivion Ring is in hand (no miracle without Aminatou)
        assertHandCount(playerA, "Oblivion Ring", 1);
        assertPermanentCount(playerB, "Grizzly Bears", 1);
    }
}
