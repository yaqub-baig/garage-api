package com.autoadda.apis.garage.util;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class SKUGenerator {

    private static final Map<String, String> categoryCodes = new HashMap<>();
    private static final Map<String, String> subCategoryCodes = new HashMap<>();
    private static final Map<String, String> subSubCategoryCodes = new HashMap<>();
    private static final Set<String> existingSKUs = new HashSet<>();
    private static final Random random = new Random();

    static {
        // Initialize category codes
        categoryCodes.put("Engine", "ENG");
        categoryCodes.put("Transmission", "TRN");
        categoryCodes.put("Brakes", "BRK");
        categoryCodes.put("Suspension", "SUS");
        categoryCodes.put("Electrical", "ELE");
        categoryCodes.put("Body", "BDY");
        categoryCodes.put("Interior", "INT");

        // Initialize subcategory codes for Engine
        subCategoryCodes.put("Fuel System", "FLS");
        subCategoryCodes.put("Air Intake System", "AIS");
        subCategoryCodes.put("Cooling System", "CLS");
        subCategoryCodes.put("Ignition System", "IGN");

        // Initialize sub-subcategory codes for Engine > Fuel System
        subSubCategoryCodes.put("Fuel Injectors", "FIJ");
        subSubCategoryCodes.put("Fuel Pumps", "FPM");
        subSubCategoryCodes.put("Fuel Filters", "FFL");

        // Initialize subcategory codes for Transmission
        subCategoryCodes.put("Manual Transmission", "MTN");
        subCategoryCodes.put("Automatic Transmission", "ATN");
        subCategoryCodes.put("Differential", "DFL");

        // Initialize sub-subcategory codes for Transmission > Manual Transmission
        subSubCategoryCodes.put("Clutch Kits", "CLK");
        subSubCategoryCodes.put("Gear Sets", "GRS");
        subSubCategoryCodes.put("Shift Cables", "SFC");

        // Initialize sub-subcategory codes for Transmission > Automatic Transmission
        subSubCategoryCodes.put("Transmission Fluid", "TFL");
        subSubCategoryCodes.put("Torque Converters", "TQC");
        subSubCategoryCodes.put("Solenoids", "SLD");

        // Initialize sub-subcategory codes for Transmission > Differential
        subSubCategoryCodes.put("Axle Shafts", "AXS");
        subSubCategoryCodes.put("Differential Gears", "DGR");
        subSubCategoryCodes.put("Bearings and Seals", "BAS");

        // Initialize subcategory codes for Brakes
        subCategoryCodes.put("Brake System", "BKS");
        subCategoryCodes.put("ABS Components", "ABS");
        subCategoryCodes.put("Brake Lines and Hoses", "BLH");

        // Initialize sub-subcategory codes for Brakes > Brake System
        subSubCategoryCodes.put("Brake Pads", "BPD");
        subSubCategoryCodes.put("Brake Rotors", "BRT");
        subSubCategoryCodes.put("Brake Calipers", "BCL");

        // Initialize sub-subcategory codes for Brakes > ABS Components
        subSubCategoryCodes.put("ABS Sensors", "ABS");
        subSubCategoryCodes.put("ABS Pumps", "ABP");
        subSubCategoryCodes.put("Control Modules", "CMO");

        // Initialize sub-subcategory codes for Brakes > Brake Lines and Hoses
        subSubCategoryCodes.put("Brake Lines", "BLI");
        subSubCategoryCodes.put("Brake Hoses", "BHO");
        subSubCategoryCodes.put("Fittings and Adapters", "FAD");

        // Initialize subcategory codes for Suspension
        subCategoryCodes.put("Shocks and Struts", "SAS");
        subCategoryCodes.put("Control Arms and Ball Joints", "CAB");
        subCategoryCodes.put("Springs", "SPR");

        // Initialize sub-subcategory codes for Suspension > Shocks and Struts
        subSubCategoryCodes.put("Shock Absorbers", "SHO");
        subSubCategoryCodes.put("Strut Assemblies", "STA");
        subSubCategoryCodes.put("Mounting Kits", "MKI");

        // Initialize sub-subcategory codes for Suspension > Control Arms and Ball
        // Joints
        subSubCategoryCodes.put("Control Arms", "CAR");
        subSubCategoryCodes.put("Ball Joints", "BAJ");
        subSubCategoryCodes.put("Bushings", "BUI");

        // Initialize sub-subcategory codes for Suspension > Springs
        subSubCategoryCodes.put("Coil Springs", "COS");
        subSubCategoryCodes.put("Leaf Springs", "LES");
        subSubCategoryCodes.put("Air Springs", "AIS");

        // Initialize subcategory codes for Electrical
        subCategoryCodes.put("Lighting", "LGT");
        subCategoryCodes.put("Batteries and Charging", "BAC");
        subCategoryCodes.put("Wiring and Connectors", "WAC");

        // Initialize sub-subcategory codes for Electrical > Lighting
        subSubCategoryCodes.put("Headlights", "HDL");
        subSubCategoryCodes.put("Tail Lights", "TAL");
        subSubCategoryCodes.put("Fog Lights", "FOL");

        // Initialize sub-subcategory codes for Electrical > Batteries and Charging
        subSubCategoryCodes.put("Car Batteries", "CAB");
        subSubCategoryCodes.put("Alternators", "ALT");
        subSubCategoryCodes.put("Voltage Regulators", "VOR");

        // Initialize sub-subcategory codes for Electrical > Wiring and Connectors
        subSubCategoryCodes.put("Wire Harnesses", "WIH");
        subSubCategoryCodes.put("Connectors", "CON");
        subSubCategoryCodes.put("Relays", "REL");

        // Initialize subcategory codes for Body
        subCategoryCodes.put("Exterior Parts", "EXP");
        subCategoryCodes.put("Interior Parts", "INP");
        subCategoryCodes.put("Glass and Mirrors", "GAM");

        // Initialize sub-subcategory codes for Body > Exterior Parts
        subSubCategoryCodes.put("Bumpers", "BUM");
        subSubCategoryCodes.put("Grilles", "GRI");
        subSubCategoryCodes.put("Side Mirrors", "SIM");

        // Initialize sub-subcategory codes for Body > Interior Parts
        subSubCategoryCodes.put("Dashboard Components", "DAC");
        subSubCategoryCodes.put("Seat Covers", "SEC");
        subSubCategoryCodes.put("Floor Mats", "FLM");

        // Initialize sub-subcategory codes for Body > Glass and Mirrors
        subSubCategoryCodes.put("Windshields", "WIN");
        subSubCategoryCodes.put("Side Windows", "SIW");
        subSubCategoryCodes.put("Rear Windows", "REW");

        // Initialize subcategory codes for Interior
        subCategoryCodes.put("Comfort and Convenience", "CAC");
        subCategoryCodes.put("Audio and Infotainment", "AAI");
        subCategoryCodes.put("Safety and Security", "SAS");

        // Initialize sub-subcategory codes for Interior > Comfort and Convenience
        subSubCategoryCodes.put("Air Conditioning Components", "ACC");
        subSubCategoryCodes.put("Heater Cores", "HEC");
        subSubCategoryCodes.put("Control Panels", "COP");

        // Initialize sub-subcategory codes for Interior > Audio and Infotainment
        subSubCategoryCodes.put("Car Stereos", "CAS");
        subSubCategoryCodes.put("Speakers", "SPE");
        subSubCategoryCodes.put("Navigation Systems", "NAS");

        // Initialize sub-subcategory codes for Interior > Safety and Security
        subSubCategoryCodes.put("Airbags", "AIR");
        subSubCategoryCodes.put("Seat Belts", "SEB");
        subSubCategoryCodes.put("Alarm Systems", "ALS");
    }

    private static String generateUniqueSerial(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder serial = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            serial.append(characters.charAt(random.nextInt(characters.length())));
        }

        String serialString = serial.toString();
        while (existingSKUs.contains(serialString)) {
            serial.setLength(0);
            for (int i = 0; i < length; i++) {
                serial.append(characters.charAt(random.nextInt(characters.length())));
            }
            serialString = serial.toString();
        }

        existingSKUs.add(serialString);
        return serialString;
    }

    public static String generateSKU(String category, String subCategory, String subSubCategory,
                                     String partIdentifier) {

        String serialNumber = generateUniqueSerial(6);
        return category + "-" + subCategory + "-" + subSubCategory + "-" + partIdentifier + "-" + serialNumber;
    }

    public static void main(String[] args) {
        String category = "Engine";
        String subCategory = "Fuel System";
        String subSubCategory = "Fuel Injectors";
        String partIdentifier = "12345";

        String sku = generateSKU(category, subCategory, subSubCategory, partIdentifier);
        log.info("Generated SKU: " + sku);
    }
}
