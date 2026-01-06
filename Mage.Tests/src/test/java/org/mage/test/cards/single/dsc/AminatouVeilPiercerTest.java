package org.mage.test.cards.single.dsc;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * Tests for Aminatou, Veil Piercer
 * {1}{W}{U}{B}
 * Legendary Creature — Human Wizard
 * At the beginning of your upkeep, surveil 2.
 * Each enchantment card in your hand has miracle. Its miracle cost is equal to its mana cost reduced by {4}.
 */
public class AminatouVeilPiercerTest extends CardTestPlayerBase {

    private static final String aminatou = "Aminatou, Veil Piercer";

    /**
     * Test that Aminatou triggers surveil 2 at beginning of upkeep
     */
    @Test
    public void testSurveilTrigger() {
        // Setup: Aminatou on battlefield
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.LIBRARY, playerA, "Plains", 5);
        skipInitShuffling();

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN); // Stop on playerA's second turn after upkeep
        execute();

        // Player should have surveiled (looked at top 2 cards)
        // This test mainly checks that no errors occur during surveil
        assertPermanentCount(playerA, aminatou, 1);
    }

    /**
     * Test that enchantment cards drawn as first card gain and can use miracle
     * Oblivion Ring costs {2}{W}, reduced by {4} = {W} (minimum 0 generic, keep colored)
     *
     * Cast Aminatou on turn 1, then draw Oblivion Ring on turn 3
     */
    @Test
    public void testEnchantmentGainsMiracle() {
        // Setup
        addCard(Zone.HAND, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2); // For Aminatou and miracle cost
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp");

        // Library setup: Turn 1 draw, turn 2 surveil removes 2, turn 3 draws O-Ring
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring"); // Turn 3 draw
        addCard(Zone.LIBRARY, playerA, "Mountain"); // Surveil 2 on turn 2
        addCard(Zone.LIBRARY, playerA, "Forest"); // Surveil 2 on turn 2
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears"); // Target for O-Ring
        skipInitShuffling();

        // Cast Aminatou on turn 1
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, aminatou);

        setStopAt(3, PhaseStep.POSTCOMBAT_MAIN);
        execute();

        // If miracle worked, O-Ring should be on battlefield (cast during draw step via miracle)
        assertPermanentCount(playerA, aminatou, 1);
        assertPermanentCount(playerA, "Oblivion Ring", 1);
        assertExileCount(playerB, "Grizzly Bears", 1);
    }

    /**
     * Test that non-enchantment cards do NOT gain miracle
     */
    @Test
    public void testNonEnchantmentDoesNotGainMiracle() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Lightning Bolt"); // Instant, not enchantment
        skipInitShuffling();

        // Draw Lightning Bolt as first card using Opt
        castSpell(1, PhaseStep.UPKEEP, playerA, "Opt");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // Lightning Bolt should be in hand, not cast via miracle (non-enchantments don't get miracle)
        assertHandCount(playerA, "Lightning Bolt", 1);
    }

    /**
     * Test miracle cost calculation for enchantments costing less than {4}
     * Abundant Growth costs {G}, reduced by {4} should be {0}
     */
    @Test
    public void testMiracleCostReduction() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);

        // Test card costing less than {4} - should cost {0}
        // Abundant Growth costs {G}, reduced by {4} should be {0}
        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.LIBRARY, playerA, "Abundant Growth");
        addCard(Zone.BATTLEFIELD, playerA, "Forest"); // For Abundant Growth target
        skipInitShuffling();

        // Draw Abundant Growth as first card
        castSpell(1, PhaseStep.UPKEEP, playerA, "Opt");

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // Should be able to cast for free (miracle cost {0})
        assertPermanentCount(playerA, "Abundant Growth", 1);
    }

    /**
     * Test that miracle works when drawing as first card on opponent's turn
     */
    @Test
    public void testMiracleOnOpponentTurn() {
        addCard(Zone.BATTLEFIELD, playerA, aminatou);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 1);
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring");
        // Opt to draw on opponent's turn
        addCard(Zone.HAND, playerA, "Opt");
        addCard(Zone.BATTLEFIELD, playerA, "Island");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        skipInitShuffling();

        // Cast Opt during opponent's turn to draw Oblivion Ring
        castSpell(2, PhaseStep.UPKEEP, playerA, "Opt");

        setStopAt(2, PhaseStep.PRECOMBAT_MAIN);
        execute();

        assertPermanentCount(playerA, "Oblivion Ring", 1);
        assertExileCount(playerB, "Grizzly Bears", 1);
    }

    /**
     * Test that enchantment only gets miracle when Aminatou is on battlefield
     */
    @Test
    public void testNoMiracleWithoutAminatou() {
        // No Aminatou on battlefield
        addCard(Zone.LIBRARY, playerA, "Oblivion Ring");
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 3);
        skipInitShuffling();

        setStopAt(1, PhaseStep.PRECOMBAT_MAIN);
        execute();

        // O-Ring should be in hand (no miracle without Aminatou)
        assertHandCount(playerA, "Oblivion Ring", 1);
    }
}
