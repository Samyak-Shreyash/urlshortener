package com.systemdesign.urlshortener.service.ServiceImpl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.systemdesign.urlshortener.model.UrlMapping;
import com.systemdesign.urlshortener.repository.UrlRepository;
import com.systemdesign.urlshortener.service.UrlChangeService;
import com.systemdesign.urlshortener.utils.UrlUtils;

@Transactional
@Service
public class UrlChangeServiceImpl implements UrlChangeService{
    
    private static final Logger logger = LoggerFactory.getLogger(UrlChangeServiceImpl.class);

    @Autowired
    private UrlRepository urlRepository;

    @Override
    public String getOriginalUrl(String shortCode) {
        if (shortCode == null || shortCode.isEmpty() || shortCode.isBlank())  {
            logger.info("Short code {} is not valid", shortCode);
            return null;
         }
        return this.getMappedUrl(shortCode);
     }

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
            
        String urlHash = UrlUtils.hashUrl(oUrl);
        String savedShortCode = this.getSavedShortCode(urlHash);
        
        if (savedShortCode == null || savedShortCode.isEmpty() || savedShortCode.isBlank())  
            return this.createUrlMapping(oUrl, urlHash);
        
        return savedShortCode;
    }
    
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
    private String createUrlMapping(String url, String urlHash) {
        if (url == null || url.isBlank() || url.isEmpty()) 
            return null;
            
        String shortCode = UrlUtils.generateShortCode();
        logger.info("Generated new Short Code {} for URL : {}",shortCode, url);
        
        UrlMapping newUrlMap = new UrlMapping(shortCode, url, urlHash);
        
        this.urlRepository.save(newUrlMap);
        
        logger.info("New URL Mapping saved : {} ", newUrlMap.toString());
        
        return newUrlMap.getShortCode();
     }
}
