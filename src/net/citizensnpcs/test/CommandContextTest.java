package net.citizensnpcs.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.citizensnpcs.command.CommandContext;

import org.junit.Test;

public class CommandContextTest {
    @Test(expected = NumberFormatException.class)
    public void testIllegalInteger() {
        getContext("notInt").getInteger(0);
    }

    @Test
    public void testParsing() {
        assertTrue(0.0 == getContext("0").getDouble(0));
        assertTrue(0 == getContext("0").getInteger(0));
    }

    @Test
    public void testJoining() {
        assertTrue(getContext("join strings").getJoinedStrings(0).equals("join strings"));
    }

    @Test
    public void testValueFlags() {
        assertTrue(getContext("--test values").getFlag("test").equals("values"));
        assertTrue(getContext("--t 0").getFlagInteger("t") == 0);
        assertTrue(getContext("--test 'extended quotes' afterwards").getFlag("test").equals("extended quotes"));
        assertFalse(getContext("--t").hasFlag('t'));
    }

    @Test
    public void testFlags() {
        assertTrue(getContext("-f").getFlags().contains('f'));
        Set<Character> multi = getContext("-f -mm test -ghl").getFlags();
        List<Character> shouldContain = Arrays.asList('f', 'm', 'g', 'h', 'l');
        assertTrue(multi.containsAll(shouldContain));
    }

    @Test
    public void testQuotes() {
        assertTrue(getContext("'this is a quote'").getString(0).equals("this is a quote"));
        assertTrue(getContext("'this is unclosed\"").getString(0).equals("'this"));
        assertTrue(getContext("\"test double quotes\"").getString(0).equals("test double quotes"));
        assertTrue(getContext("'this      is     a    quote'").getString(0).equals("this is a quote"));
    }

    private static CommandContext getContext(String cmd) {
        return new CommandContext("dummy " + cmd);
    }
}