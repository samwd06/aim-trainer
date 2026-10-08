package game.input;

import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests for the labels shown in the key rebinding menu. */
class KeybindsTest {

    @Test
    void unboundActionShowsDashes() {
        assertEquals("---", Keybinds.getKeyText(0));
    }

    @Test
    void namedMouseButtonsHaveReadableLabels() {
        assertEquals("Left Click", Keybinds.getKeyText(Keybinds.MOUSE_LEFT));
        assertEquals("Scroll Click", Keybinds.getKeyText(Keybinds.MOUSE_MIDDLE));
        assertEquals("Right Click", Keybinds.getKeyText(Keybinds.MOUSE_RIGHT));
        assertEquals("Mouse 4", Keybinds.getKeyText(Keybinds.MOUSE_SIDE1));
        assertEquals("Mouse 5", Keybinds.getKeyText(Keybinds.MOUSE_SIDE2));
    }

    @Test
    void extraMouseButtonsAreNumbered() {
        assertEquals("Mouse 6", Keybinds.getKeyText(-106));
    }

    @Test
    void keyboardKeysAreShownInUpperCase() {
        assertEquals("A", Keybinds.getKeyText(KeyEvent.VK_A));
        assertEquals("F3", Keybinds.getKeyText(KeyEvent.VK_F3));
        assertEquals("SPACE", Keybinds.getKeyText(KeyEvent.VK_SPACE));
    }

    @Test
    void defaultShootBindingIsLeftClick() {
        assertEquals(Keybinds.MOUSE_LEFT, Keybinds.SHOOT);
    }
}
