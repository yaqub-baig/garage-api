package com.autoadda.apis.garage.util;

import java.util.UUID;

public class GUIDGenerator {
	public static String generateGUID() {
		// Generate a random UUID (GUID)
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

//	public static void main(String[] args) {
//		// Generate and print a GUID
//		String guid = generateGUID();
//		System.out.println("Generated GUID: " + guid);
//	}
}
