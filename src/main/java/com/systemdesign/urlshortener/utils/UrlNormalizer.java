package com.systemdesign.urlshortener.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class UrlNormalizer {
    
    private final NormalizationStrategy strategy;
    
    // Default constructor uses AGGRESSIVE strategy (recommended for URL shorteners)
    public UrlNormalizer() {
        this(NormalizationStrategy.AGGRESSIVE);
    }
    
    public UrlNormalizer(NormalizationStrategy strategy) {
        this.strategy = strategy;
    }
    
    public String normalize(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        try {
            String urlWithScheme = ensureScheme(url);
            URI uri = new URI(urlWithScheme);
            
            // Apply security checks if strategy requires
            if (strategy.removeUserInfo() && uri.getRawUserInfo() != null) {
                throw new IllegalArgumentException("URL contains user information which is not allowed");
            }
            
            if (strategy.getAllowedSchemes() != null && 
                !strategy.getAllowedSchemes().contains(uri.getScheme().toLowerCase())) {
                throw new IllegalArgumentException("URL scheme not allowed: " + uri.getScheme());
            }
            
            String scheme = normalizeScheme(uri.getScheme());
            String host = normalizeHost(uri.getHost());
            int port = normalizePort(uri.getPort(), scheme);
            String path = normalizePath(uri.getPath());
            String query = normalizeQuery(uri.getQuery());
            String fragment = normalizeFragment(uri.getFragment());
            
            return buildNormalizedUri(scheme, host, port, path, query, fragment);
            
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
    
    private String ensureScheme(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        
        // Try to parse without scheme, default to https
        if (url.contains("://")) {
            return url; // Has some other scheme, leave it
        }
        
        // Check if it looks like a domain
        if (url.contains(".") && !url.contains(" ")) {
            return "https://" + url;
        }
        
        return url; // Let URI parsing handle the error
    }
    
    private String normalizeScheme(String scheme) {
        String normalized = scheme != null ? scheme.toLowerCase() : "https";
        
        // Force HTTPS if strategy requires
        if (strategy.forceHttps() && "http".equals(normalized)) {
            return "https";
        }
        
        return normalized;
    }
    
    private String normalizeHost(String host) {
        if (host == null) {
            throw new IllegalArgumentException("URL must have a valid host");
        }
        
        String normalized = host.toLowerCase();
        
        if (strategy.removeWwwSubdomain() && normalized.startsWith("www.")) {
            normalized = normalized.substring(4);
        }
        
        return normalized;
    }
    
    private int normalizePort(int port, String scheme) {
        // Return -1 for default ports (will be omitted in final URL)
        if (port == -1) return -1;
        
        // Check if port matches scheme default
        if ("http".equals(scheme) && port == 80) return -1;
        if ("https".equals(scheme) && port == 443) return -1;
        
        return port;
    }
    
    private String normalizePath(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "";
        }
        
        // Remove trailing slash
        String normalized = path;
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        
        // Ensure starts with slash
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        
        // Decode URL-encoded characters for consistency
        if (strategy.decodeUrlEncoded()) {
            normalized = decodeUrlEncoded(normalized);
        }
        
        // Remove duplicate slashes
        normalized = normalized.replaceAll("/+", "/");
        
        return normalized;
    }
    
    private String normalizeQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        // Parse query parameters
        String[] pairs = query.split("&");
        Map<String, List<String>> params = new TreeMap<>(); // TreeMap for sorted keys
        
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = decodeUrlEncoded(keyValue[0]);
            String value = keyValue.length > 1 ? decodeUrlEncoded(keyValue[1]) : "";
            
            // Skip tracking parameters if strategy requires
            if (strategy.removeTrackingParams() && strategy.getTrackingParameters().contains(key)) {
                continue;
            }
            
            // Add to map (allow multiple values for same key)
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        
        // Rebuild query string
        if (params.isEmpty()) {
            return null;
        }
        
        if (strategy.sortQueryParameters()) {
            return buildSortedQueryString(params);
        } else {
            return buildQueryString(params);
        }
    }
    
    private String buildSortedQueryString(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .flatMap(entry -> {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();
                    // Sort values for consistency
                    if (strategy.sortQueryParameters()) {
                        Collections.sort(values);
                    }
                    return values.stream()
                            .map(value -> encodeUrlComponent(key) + "=" + encodeUrlComponent(value));
                })
                .collect(Collectors.joining("&"));
    }
    
    private String buildQueryString(Map<String, List<String>> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = encodeUrlComponent(entry.getKey());
            for (String value : entry.getValue()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(key).append("=").append(encodeUrlComponent(value));
            }
        }
        return sb.toString();
    }
    
    private String normalizeFragment(String fragment) {
        if (!strategy.removeFragments()) {
            return normalizeFragmentContent(fragment);
        }
        
        // For SEO strategy, preserve hashbang fragments
        if (strategy.preserveHashbangFragments() && fragment != null && fragment.startsWith("!")) {
            return normalizeFragmentContent(fragment);
        }
        
        return null;
    }
    
    private String normalizeFragmentContent(String fragment) {
        if (fragment == null) return null;
        
        String normalized = fragment.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        
        if (strategy.decodeUrlEncoded()) {
            normalized = decodeUrlEncoded(normalized);
        }
        
        return encodeUrlComponent(normalized);
    }
    
    private String buildNormalizedUri(String scheme, String host, int port, 
                                   String path, String query, String fragment) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(scheme).append("://").append(host);
        
        if (port != -1) {
            sb.append(":").append(port);
        }
        
        if (path != null && !path.isEmpty()) {
            sb.append(path);
        }
        
        if (query != null && !query.isEmpty()) {
            sb.append("?").append(query);
        }
        
        // Add fragment only if not null and not empty
        if (fragment != null && !fragment.isEmpty()) {
            sb.append("#").append(fragment);
        }
        
        return sb.toString();
    }
    
    // Utility methods
    
    private String decodeUrlEncoded(String encoded) {
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return encoded; // Return original if decoding fails
        }
    }
    
    private String encodeUrlComponent(String component) {
        try {
            return URLEncoder.encode(component, StandardCharsets.UTF_8.name())
                    .replace("+", "%20") // Use %20 instead of + for spaces
                    .replace("%7E", "~") // Keep tilde unencoded
                    .replace("*", "%2A"); // Encode asterisk
        } catch (Exception e) {
            return component;
        }
    }
    
    // Static utility methods for convenience
    
    public static String normalizeForShortener(String url) {
        return new UrlNormalizer(NormalizationStrategy.AGGRESSIVE).normalize(url);
    }
    
    public static boolean areEquivalent(String url1, String url2) {
        try {
            String normalized1 = normalizeForShortener(url1);
            String normalized2 = normalizeForShortener(url2);
            return normalized1.equals(normalized2);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Getters
    
    public NormalizationStrategy getStrategy() {
        return strategy;
    }
}