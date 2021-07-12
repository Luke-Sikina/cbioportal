package org.cbioportal.service.impl;

import org.cbioportal.persistence.StudyViewFilterIdentifierCacheRepository;
import org.cbioportal.service.StudyViewFilterIdentifierCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StudyViewFilterIdentifierCacheImpl implements StudyViewFilterIdentifierCache {
    private final Map<byte[], Object> hashes = new ConcurrentHashMap<>();
    
    @Autowired
    StudyViewFilterIdentifierCacheRepository repository;

    @Override
    public boolean isCached(byte[] hash) {
        return hashes.containsKey(hash);
    }

    @Override
    public synchronized void persist(byte[] hash, List<Integer> sampleIds) {
        if (isCached(hash)) {
            return;
        }
        repository.persist(hash, sampleIds);
        hashes.put(hash, null);
    }
}
