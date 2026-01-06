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
 * @author LevelX2
 */
public class AminatouVeilPiercerTest extends CardTestPlayerBase {

    private static final String aminatou = "Aminatou, Veil Piercer";

    /**
     * Test that Aminatou's surveil 2 trigger works at beginning of upkeep
     */
    @Test
    public void testSurveil2Trigger() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.LIBRARY, playerA, "Mountain"); // top
        addCard(Zone.LIBRARY, playerA, "Forest"); // second
        addCard(Zone.LIBRARY, playerA, "Plains"); // third
        skipInitShuffling();

        setStopAt(2, PhaseStep.DRAW); // Stop on turn 2, after surveil but before draw
        execute();

        assertPermanentCount(playerA, aminatou, 1);
        // Surveil should have happened (no error checking, just that the game progresses)
    }

    /**
     * Test basic miracle on enchantment - Oblivion Ring
     * Oblivion Ring costs {2}{W}, reduced by {4} = {W}
     * Based on MiracleTest pattern
     */
    @Test
    public void testEnchantmentMiracle_OblivionRing() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1); // For {W} miracle cost
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1); // For Think Twice

        // Add buffer cards so surveil doesn't hit Oblivion Ring
        // Last card added is on top! Surveil sees top 2 (Forest, Mountain)
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring"); // Bottom - will be drawn by Think Twice
        addCard(Zone.LIBRARY, playerA, "Mountain"); // Middle - surveiled
        addCard(Zone.LIBRARY, playerA, "Forest"); // Top - surveiled
        addCard(Zone.HAND, playerA, "Think Twice"); // Draw spell
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears"); // Target for O-Ring
        skipInitShuffling();

        // Cast Think Twice to draw Oblivion Ring as first card
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Think Twice");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // Miracle should auto-cast O-Ring for {W}, exiling Grizzly Bears
        assertPermanentCount(playerA, "Oblivion Ring", 1);
        assertExileCount(playerB, "Grizzly Bears", 1);
    }

    /**
     * Test miracle cost reduction for cheap enchantment
     * Abundant Growth costs {G}, reduced by {4} = {0}
     */
    @Test
    public void testMiracleCostReduction_CheapEnchantment() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1); // For Think Twice
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 1); // Target for Abundant Growth

        // Add buffer cards for surveil (last added is on top)
        addCard(Zone.LIBRARY, playerA, "Abundant Growth"); // Bottom - will be drawn
        addCard(Zone.LIBRARY, playerA, "Mountain"); // Middle - surveiled
        addCard(Zone.LIBRARY, playerA, "Plains"); // Top - surveiled
        addCard(Zone.HAND, playerA, "Think Twice");
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Think Twice");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // Should cast for free ({0})
        assertPermanentCount(playerA, "Abundant Growth", 1);
    }

    /**
     * Test that non-enchantments do NOT get miracle
     */
    @Test
    public void testNonEnchantment_NoMiracle() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1); // For Lightning Bolt if it had miracle

        // Add buffer cards for surveil (last added is on top)
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt"); // Bottom - will be drawn
        addCard(Zone.LIBRARY, playerA, "Forest"); // Middle - surveiled
        addCard(Zone.LIBRARY, playerA, "Plains"); // Top - surveiled
        addCard(Zone.HAND, playerA, "Think Twice");
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Think Twice");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // Lightning Bolt should be in hand, NOT cast
        assertHandCount(playerA, "Lightning Bolt", 1);
    }

    /**
     * Test miracle only works on FIRST card drawn per turn
     * Draw 2 cards, only first should trigger miracle
     */
    @Test
    public void testMiracle_OnlyFirstCardDrawn() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1); // For Divination

        // Add buffer and test cards (last added is on top)
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring"); // 4th from top, first draw - should miracle
        addCard(Zone.LIBRARY, playerA, "Pacifism"); // 3rd from top, second draw - no miracle
        addCard(Zone.LIBRARY, playerA, "Mountain"); // 2nd from top - surveiled
        addCard(Zone.LIBRARY, playerA, "Forest"); // Top - surveiled
        addCard(Zone.HAND, playerA, "Divination"); // Draw 2 cards
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Divination");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // Oblivion Ring should be cast via miracle
        assertPermanentCount(playerA, "Oblivion Ring", 1);
        // Pacifism should be in hand (second draw, no miracle)
        assertHandCount(playerA, "Pacifism", 1);
    }

    /**
     * Test miracle works on opponent's turn
     */
    @Test
    public void testMiracle_OpponentTurn() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);

        // Add buffers - turn 1 surveil (top 2), turn 2 upkeep surveil (next top 2), then Opt draws
        // Last added is on top of library!
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring"); // 5th from top - will be drawn by Opt
        addCard(Zone.LIBRARY, playerA, "Swamp"); // 4th from top - turn 2 surveil
        addCard(Zone.LIBRARY, playerA, "Mountain"); // 3rd from top - turn 2 surveil
        addCard(Zone.LIBRARY, playerA, "Plains"); // 2nd from top - turn 1 surveil
        addCard(Zone.LIBRARY, playerA, "Forest"); // Top - turn 1 surveil
        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        // Cast Opt during opponent's turn (after turn 2 surveil) to draw Oblivion Ring
        castSpell(2, PhaseStep.PRECOMBAT_MAIN, playerA, "Opt");

        setStopAt(2, PhaseStep.BEGIN_COMBAT);
        execute();

        assertPermanentCount(playerA, "Oblivion Ring", 1);
        assertExileCount(playerB, "Grizzly Bears", 1);
    }

    /**
     * Test that miracle only works when Aminatou is on battlefield
     */
    @Test
    public void testNoMiracle_WithoutAminatou() {
        // No Aminatou
        addCard(Zone.BATTLEFIELD, playerA, "Island", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3); // For regular O-Ring cost

        addCard(Zone.LIBRARY, playerA, "Oblivion Ring");
        addCard(Zone.HAND, playerA, "Think Twice");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Think Twice");

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // O-Ring should be in hand (no miracle without Aminatou)
        assertHandCount(playerA, "Oblivion Ring", 1);
        // Grizzly Bears still on battlefield
        assertPermanentCount(playerB, "Grizzly Bears", 1);
    }

    /**
     * Test surveil 2 card selection
     * Based on SurveilTest pattern
     */
    @Test
    public void testSurveil_BothToGraveyard() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.LIBRARY, playerA, "Plains"); // bottom after surveil
        addCard(Zone.LIBRARY, playerA, "Forest"); // top, will surveil to yard
        addCard(Zone.LIBRARY, playerA, "Mountain"); // second, will surveil to yard
        skipInitShuffling();

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // Both surveiled cards should be in graveyard
        assertGraveyardCount(playerA, "Mountain", 1);
        assertGraveyardCount(playerA, "Forest", 1);
    }

    /**
     * Test that enchantment in graveyard doesn't get miracle
     */
    @Test
    public void testMiracle_OnlyFromDraw() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);

        // Oblivion Ring already in graveyard
        addCard(Zone.GRAVEYARD, playerA, "Oblivion Ring");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();

        // O-Ring stays in graveyard (miracle only on draw)
        assertGraveyardCount(playerA, "Oblivion Ring", 1);
        assertPermanentCount(playerB, "Grizzly Bears", 1);
    }
}
