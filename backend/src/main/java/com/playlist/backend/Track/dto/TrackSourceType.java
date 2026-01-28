package com.playlist.backend.Track.dto;

public enum TrackSourceType {
    YOUTUBE,
    SPOTIFY,
    FILE;

    public static boolean isSupported(String value) {
        if (value == null) return false;
        try {
            TrackSourceType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}