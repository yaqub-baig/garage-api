package com.autoadda.apis.garage.util;

/**
 * Steps of the technician CV profile form, in the order the UI presents them.
 */
public enum ProfileStep {
    PERSONAL(1),
    QUALIFICATIONS(2),
    CERTIFICATIONS(3),
    SKILLS(4),
    EMPLOYMENT(5),
    LANGUAGES(6),
    DOCUMENTS(7),
    REFERENCES(8),
    SOCIAL_LINKS(9);

    private final int order;

    ProfileStep(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
