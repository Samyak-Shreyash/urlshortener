package com.systemdesign.urlshortener.utils;

import java.util.Set;

/**
 * Defines different strategies for URL normalization
 * Each strategy represents a different level of aggressiveness in normalization
 */
public enum NormalizationStrategy {
    
    /**
     * MINIMAL normalization - only basic safety cleanup
     * - Ensures valid URL format
     * - Lowercase scheme and host
     * - No removal of any components
     * Use case: When you need to preserve the original URL as much as possible
     */
    MINIMAL {
        @Override
        public boolean removeFragments() { return false; }
        
        @Override
        public boolean removeTrackingParams() { return false; }
        
        @Override
        public boolean removeDefaultPorts() { return false; }
        
        @Override
        public boolean removeWwwSubdomain() { return false; }
        
        @Override
        public boolean sortQueryParameters() { return false; }
        
        @Override
        public boolean decodeUrlEncoded() { return false; }
    },
    
    /**
     * BASIC normalization - common cleanup without being too aggressive
     * - Lowercase scheme and host
     * - Remove default ports
     * - Remove fragments
     * - Basic path normalization
     * Use case: General purpose URL normalization
     */
    BASIC {
        @Override
        public boolean removeFragments() { return true; }
        
        @Override
        public boolean removeTrackingParams() { return false; }
        
        @Override
        public boolean removeDefaultPorts() { return true; }
        
        @Override
        public boolean removeWwwSubdomain() { return false; }
        
        @Override
        public boolean sortQueryParameters() { return false; }
        
        @Override
        public boolean decodeUrlEncoded() { return true; }
    },
    
    /**
     * AGGRESSIVE normalization - maximum deduplication
     * - Everything in BASIC plus:
     * - Remove tracking parameters
     * - Remove www subdomain
     * - Sort query parameters
     * - Comprehensive path cleanup
     * Use case: URL shorteners, deduplication systems, web crawlers
     */
    AGGRESSIVE {
        @Override
        public boolean removeFragments() { return true; }
        
        @Override
        public boolean removeTrackingParams() { return true; }
        
        @Override
        public boolean removeDefaultPorts() { return true; }
        
        @Override
        public boolean removeWwwSubdomain() { return true; }
        
        @Override
        public boolean sortQueryParameters() { return true; }
        
        @Override
        public boolean decodeUrlEncoded() { return true; }
    },
    
    /**
     * SEO_OPTIMIZED normalization - preserves SEO-friendly components
     * - Like AGGRESSIVE but preserves fragments and some parameters
     * - Keeps hashbang fragments (#!) for single-page apps
     * - Preserves certain SEO parameters
     * Use case: SEO tools, analytics systems
     */
    SEO_OPTIMIZED {
        @Override
        public boolean removeFragments() { return false; }
        
        @Override
        public boolean removeTrackingParams() { return true; }
        
        @Override
        public boolean removeDefaultPorts() { return true; }
        
        @Override
        public boolean removeWwwSubdomain() { return false; }
        
        @Override
        public boolean sortQueryParameters() { return true; }
        
        @Override
        public boolean decodeUrlEncoded() { return true; }
        
        @Override
        public boolean preserveHashbangFragments() { return true; }
    },
    
    /**
     * SECURITY_FOCUSED normalization - removes potentially dangerous components
     * - Removes userinfo (username:password@)
     * - Validates scheme
     * - Removes suspicious parameters
     * Use case: Security scanners, input validation
     */
    SECURITY_FOCUSED {
        @Override
        public boolean removeFragments() { return true; }
        
        @Override
        public boolean removeTrackingParams() { return true; }
        
        @Override
        public boolean removeDefaultPorts() { return true; }
        
        @Override
        public boolean removeWwwSubdomain() { return false; }
        
        @Override
        public boolean sortQueryParameters() { return false; }
        
        @Override
        public boolean decodeUrlEncoded() { return false; }
        
        @Override
        public boolean removeUserInfo() { return true; }
        
        @Override
        public Set<String> getAllowedSchemes() { 
            return Set.of("http", "https", "ftp"); 
        }
    },

    /**
     * COMPLETE_REMOVAL normalization - maximum deduplication for URL shorteners
     * - Removes all fragments (client-side only)
     * - Removes tracking parameters
     * - Removes default ports and www subdomain
     * - Sorts query parameters for consistency
     * - Comprehensive normalization for optimal deduplication
     * Use case: URL shorteners, deduplication systems
     */
    COMPLETE_REMOVAL {
        @Override
        public boolean removeFragments() { return true; }
        
        @Override
        public boolean removeTrackingParams() { return true; }
        
        @Override
        public boolean removeDefaultPorts() { return true; }
        
        @Override
        public boolean removeWwwSubdomain() { return true; }
        
        @Override
        public boolean sortQueryParameters() { return true; }
        
        @Override
        public boolean decodeUrlEncoded() { return true; }
        
        @Override
        public boolean preserveHashbangFragments() { return false; }
        
        @Override
        public boolean removeUserInfo() { return true; }
        
        @Override
        public boolean normalizePathCase() { return false; }
        
        @Override
        public boolean forceHttps() { return false; }
    };

    // Core normalization behaviors
    public abstract boolean removeFragments();
    public abstract boolean removeTrackingParams();
    public abstract boolean removeDefaultPorts();
    public abstract boolean removeWwwSubdomain();
    public abstract boolean sortQueryParameters();
    public abstract boolean decodeUrlEncoded();
    
    // Extended behaviors with default implementations
    public boolean preserveHashbangFragments() { return false; }
    public boolean removeUserInfo() { return false; }
    public boolean normalizePathCase() { return false; }
    public boolean forceHttps() { return false; }
    
    // Security-related defaults
    public Set<String> getAllowedSchemes() { return null; } // null means all schemes allowed
    public boolean validateHost() { return false; }
    
    /**
     * Gets the set of tracking parameters to remove for this strategy
     */
    public Set<String> getTrackingParameters() {
        if (!removeTrackingParams()) {
            return Set.of();
        }
        
        return Set.of(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
            "fbclid", "gclid", "msclkid", "yclid", "igshid",
            "twclid", "li_fat_id", "mc_cid", "mc_eid",
            "_hsenc", "_hsmi", "hmb_campaign", "hmb_medium", "hmb_source"
        );
    }
    
    /**
     * Gets parameters that should always be preserved (even in aggressive mode)
     */
    public Set<String> getPreservedParameters() {
        return Set.of(
            "id", "page", "view", "action", "type",
            "category", "sort", "filter", "q", "query",
            "search", "code", "token", "key", "ref"
        );
    }
    
    /**
     * Helper method to check if this strategy includes specific normalization
     */
    public boolean includes(NormalizationFeature feature) {
        return switch (feature) {
            case FRAGMENT_REMOVAL -> removeFragments();
            case TRACKING_PARAM_REMOVAL -> removeTrackingParams();
            case DEFAULT_PORT_REMOVAL -> removeDefaultPorts();
            case WWW_REMOVAL -> removeWwwSubdomain();
            case QUERY_SORTING -> sortQueryParameters();
            case URL_DECODING -> decodeUrlEncoded();
            case USERINFO_REMOVAL -> removeUserInfo();
            case HTTPS_FORCING -> forceHttps();
            case PATH_CASE_NORMALIZATION -> normalizePathCase();
            case HASHBANG_PRESERVATION -> preserveHashbangFragments();
        };
    }
    
    /**
     * Gets a description of this normalization strategy
     */
    public String getDescription() {
        return switch (this) {
            case MINIMAL -> "Minimal normalization - only basic safety cleanup";
            case BASIC -> "Basic normalization - common cleanup without being too aggressive";
            case AGGRESSIVE -> "Aggressive normalization - maximum deduplication";
            case SEO_OPTIMIZED -> "SEO-optimized - preserves SEO-friendly components";
            case SECURITY_FOCUSED -> "Security-focused - removes potentially dangerous components";
            case COMPLETE_REMOVAL -> "Complete removal normalization - maximum deduplication for URL shorteners";
        };
    }
    
    /**
     * Features that can be included in normalization
     */
    public enum NormalizationFeature {
        FRAGMENT_REMOVAL,
        TRACKING_PARAM_REMOVAL,
        DEFAULT_PORT_REMOVAL,
        WWW_REMOVAL,
        QUERY_SORTING,
        URL_DECODING,
        USERINFO_REMOVAL,
        HTTPS_FORCING,
        PATH_CASE_NORMALIZATION,
        HASHBANG_PRESERVATION
    }
}