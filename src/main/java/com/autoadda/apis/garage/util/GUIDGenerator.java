package com.autoadda.apis.garage.util;

import java.util.UUID;

public class GUIDGenerator {
    private GUIDGenerator() {
        throw new UnsupportedOperationException ( "Utility class cannot be instantiated" );
    }

    public static String generateGUID() {
        // Generate a random UUID (GUID)
        UUID uuid = UUID.randomUUID ( );
        return uuid.toString ( );
    }

}
