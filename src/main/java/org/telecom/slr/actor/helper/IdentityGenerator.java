package org.telecom.slr.actor.helper;

public class IdentityGenerator {
    private static int current = 0;

    public synchronized static int generate() {
        return current++;
    }

    public static void reset() {
        current = 0;
    }

}
