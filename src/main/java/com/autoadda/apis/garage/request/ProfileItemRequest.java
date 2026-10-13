package com.autoadda.apis.garage.request;

/**
 * An item of a list step in the technician CV profile.
 * {@code id == null} creates a new item; a non-null id updates the existing item.
 */
public interface ProfileItemRequest {
    Integer getId();
}
