package org.example.client.core.common.util;

import java.util.prefs.Preferences;

public final class SessionManager {
    private static final String PREFS_NODE = "org.example.client.session";
    private static final String TOKEN_KEY = "jwtToken";

    private static volatile String jwtToken;
    private static volatile boolean sessionInitialized = false;

    static {
        loadSession();
    }

    private SessionManager() {}

    public static synchronized void initSession(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        if (!JwtUtil.isTokenValid(token)) {
            throw new IllegalStateException("Token is expired or invalid");
        }

        jwtToken = token;
        sessionInitialized = true;
        saveToPreferences(token);

        System.out.println("Session initialized with valid token");
    }

    public static boolean isAuthenticated() {
        if (jwtToken == null) {
            return false;
        }
        return JwtUtil.isTokenValid(jwtToken);
    }

    public static synchronized void clearSession() {
        jwtToken = null;
        sessionInitialized = false;
        clearPreferences();
        System.out.println("Session cleared");
    }

    public static String getToken() {
        checkSessionState();
        return jwtToken;
    }

    public static String getUsername() {
        checkSessionState();
        return JwtUtil.getUsernameFromToken(jwtToken);
    }

    public static String getRole() {
        checkSessionState();
        return JwtUtil.getUserRoleFromToken(jwtToken);
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    private static void checkSessionState() {
        if (!sessionInitialized || !isAuthenticated()) {
            throw new SecurityException("Invalid or expired session");
        }
    }

    private static void loadSession() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        String token = prefs.get(TOKEN_KEY, null);

        if (token != null && JwtUtil.isTokenValid(token)) {
            jwtToken = token;
            sessionInitialized = true;
            System.out.println("Session loaded from preferences");
        } else {
            clearPreferences();
        }
    }

    private static void saveToPreferences(String token) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        prefs.put(TOKEN_KEY, token);
        System.out.println("Token saved");
    }

    private static void clearPreferences() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        prefs.remove(TOKEN_KEY);
        System.out.println("Preferences cleared");
    }
    public static Long getUserId() {
        checkSessionState();
        return JwtUtil.getUserIdFromToken(jwtToken);
    }

    public static boolean isCurrentUser(Long userId) {
        return userId != null && userId.equals(getUserId());
    }
}