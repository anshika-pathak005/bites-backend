package com.bites.util;

import java.math.BigDecimal;

/**
 * Pure math helper: works out the straight-line ("as the crow flies")
 * distance in kilometers between two lat/lng points using the Haversine
 * formula. Used to check a customer address actually falls within the
 * restaurant's delivery radius — see DeliveryPricingServiceImpl.
 */
public final class HaversineUtil {

    // Mean radius of the Earth in kilometers — standard constant used by the
    // Haversine formula.
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Utility class — never instantiated.
    private HaversineUtil() {
    }

    // Returns the great-circle distance between two coordinates, in
    // kilometers. Accepts BigDecimal (the type lat/lng are stored as) so
    // callers never need to do their own conversion.
    public static double distanceInKm(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        // Step 1:
        // Haversine works in radians, not degrees — convert every input up front.
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lng1Rad = Math.toRadians(lng1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lng2Rad = Math.toRadians(lng2.doubleValue());

        // Step 2:
        // Difference in latitude/longitude between the two points, in radians.
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLng = lng2Rad - lng1Rad;

        // Step 3:
        // The core Haversine formula: builds the square of half the chord
        // length between the two points on the unit sphere.
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLng / 2), 2);

        // Step 4:
        // Angular distance in radians, then scale by Earth's radius to get
        // an actual distance in kilometers.
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
