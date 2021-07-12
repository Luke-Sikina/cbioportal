package org.cbioportal.service;


import java.util.List;

public interface StudyViewFilterIdentifierCache {
    public boolean isCached(byte[] hash);
    public void persist(byte[] hash, List<Integer> sampleIds);
}
