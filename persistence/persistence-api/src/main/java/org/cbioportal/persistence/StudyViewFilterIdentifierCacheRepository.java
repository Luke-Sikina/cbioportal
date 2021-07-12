package org.cbioportal.persistence;

import java.util.List;

public interface StudyViewFilterIdentifierCacheRepository {
    public void persist(byte[] hash, List<Integer> sampleIds);
}
