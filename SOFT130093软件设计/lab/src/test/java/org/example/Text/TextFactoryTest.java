package org.example.Text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TextFactoryTest {
    private TextFactory textFactory = new TextFactory();

    @Test
    @DisplayName("Invalid Text")
    public void testInvalidText() {
        assertThrows(RuntimeException.class, () -> textFactory.createText("#Header"));
        assertThrows(RuntimeException.class, () -> textFactory.createText("1.OrderedList"));
        assertThrows(RuntimeException.class, () -> textFactory.createText("+OrderedList"));
        assertThrows(RuntimeException.class, () -> textFactory.createText("Invalid Text"));
    }

    @Test
    @DisplayName("Valid Header Test (level1-6)")
    public void testValidHeader() {
        for (int level = 1; level <= 6; level++) {
            String headerText = "#".repeat(level) + " Header Text";
            Text header = textFactory.createText(headerText);
            assertTrue(header instanceof Header);
            assertEquals("Header Text", header.getContent());
            assertEquals(level, ((Header) header).getType());
        }
    }

    @Test
    @DisplayName("Invalid Header Test (level>6)")
    public void testInvalidHeader() {
        String headerText = "#".repeat(7) + " Header Text";
        assertThrows(RuntimeException.class, () -> textFactory.createText(headerText));
    }

    @Test
    @DisplayName("Valid OrderedList Test")
    public void testOrderedList() {
        Text orderedList = textFactory.createText("1. Item 1");
        assertTrue(orderedList instanceof OrderedList);
        assertEquals("Item 1", orderedList.getContent());
        assertEquals(1, ((OrderedList) orderedList).getLevel());

        orderedList = textFactory.createText("11. Item 1");
        assertTrue(orderedList instanceof OrderedList);
        assertEquals("Item 1", orderedList.getContent());
        assertEquals(11, ((OrderedList) orderedList).getLevel());

        orderedList = textFactory.createText("111. Item 1.2");
        assertTrue(orderedList instanceof OrderedList);
        assertEquals("Item 1.2", orderedList.getContent());
        assertEquals(111, ((OrderedList) orderedList).getLevel());
    }

    @Test
    @DisplayName("Valid UnorderedList Test")
    public void testValidUnorderedList() {
        List<Character> typeList = new ArrayList<>();
        typeList.add('*');
        typeList.add('+');
        typeList.add('-');
        for (Character type:typeList) {
            Text unorderList = textFactory.createText(type + " Item 1");
            assertTrue(unorderList instanceof UnorderedList);
            assertEquals("Item 1", unorderList.getContent());
            assertEquals(type, ((UnorderedList) unorderList).getType());
        }
    }
}