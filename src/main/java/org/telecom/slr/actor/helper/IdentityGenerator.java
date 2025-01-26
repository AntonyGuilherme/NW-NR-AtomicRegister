package org.telecom.slr.actor.helper;

public class IdentityGenerator {
    public static int current = 0;

    public synchronized static int generate() {
        return current++;
    }
}
