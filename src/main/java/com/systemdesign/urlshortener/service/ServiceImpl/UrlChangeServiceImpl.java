package com.systemdesign.urlshortener.service.ServiceImpl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.systemdesign.urlshortener.model.UrlMapping;
import com.systemdesign.urlshortener.repository.UrlRepository;
import com.systemdesign.urlshortener.service.UrlChangeService;
import com.systemdesign.urlshortener.utils.UrlNormalizer;
import com.systemdesign.urlshortener.utils.UrlUtils;

@Transactional
@Service
public class UrlChangeServiceImpl implements UrlChangeService{
    
    private static final Logger logger = LoggerFactory.getLogger(UrlChangeServiceImpl.class);

    
    @Autowired
    private UrlRepository urlRepository;

    private final UrlNormalizer normalizer = new UrlNormalizer();

    
    @Override
    public String getOriginalUrl(String shortCode) {
        if (shortCode == null || shortCode.isEmpty() || shortCode.isBlank())  {
            logger.info("Short code {} is not valid", shortCode);
            return null;
         }
        return this.getMappedUrl(shortCode);
     }

     
    @Transactional(readOnly = true)
    private String getMappedUrl(String shortCode) {
        if (this.urlRepository.existsByShortCode(shortCode))
         {
            Optional<UrlMapping> optionalMap = this.urlRepository.findByShortCode(shortCode);
            
            if (optionalMap.isPresent())  {
                UrlMapping mappedUrl = optionalMap.get();
                logger.info("Retrieved URL Mapping for short code {} : {}", shortCode, mappedUrl.toString());
                return mappedUrl.getLongUrl();
             } else {
                 logger.error("No mapping found for short code {} ", shortCode);
            }
        }
        
        return null;
     }

     @Override
    public String getShortCode(String oUrl) {
        if (oUrl == null || oUrl.isEmpty() || oUrl.isBlank()) 
            return null;
        
        String normalizedUrl = normalizer.normalize(oUrl);
        String urlHash = UrlUtils.hashUrl(normalizedUrl);
        String savedShortCode = this.getSavedShortCode(urlHash);
        
        if (savedShortCode == null || savedShortCode.isEmpty() || savedShortCode.isBlank())  
            return this.createUrlMapping(normalizedUrl, urlHash);
        
        return savedShortCode;
    }
    
    @Transactional(readOnly = true)
    private String getSavedShortCode(String urlHash) {
        Optional<String> shortCode = this.urlRepository.findShortCodeByLongUrlHash(urlHash);
        
        if (shortCode.isPresent())  {
            logger.info("Retrieved Short Code {} for URL hash : {}", shortCode.get(), urlHash);    
            return shortCode.get();
        } else {
            logger.info("No Short Code found for URL hash : {}", urlHash);   
        }     
        
        return null;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    private String createUrlMapping(String normalizedUrl, String urlHash) {
        if (normalizedUrl == null || normalizedUrl.isBlank() || normalizedUrl.isEmpty()) 
            return null;
            
        // Generate and try to insert with retry
        int attempts = 0;
        while (attempts < 5) {
            String shortCode = UrlUtils.generateShortCode();
            logger.info("Generated new Short Code {} for URL : {}",shortCode, normalizedUrl);
            try {
                UrlMapping newMapping = new UrlMapping(shortCode, normalizedUrl, urlHash);
                urlRepository.save(newMapping);
                logger.info("New URL Mapping saved : {} ", newMapping.toString());
                return shortCode;
            } catch (DataIntegrityViolationException e) {
                // Short code collision - try again
                attempts++;
                if (attempts >= 5) {
                    throw new RuntimeException("Failed to generate unique short code after 5 attempts");
                }
            }
        }
        throw new RuntimeException("Unexpected error in short code generation");
    }
}
