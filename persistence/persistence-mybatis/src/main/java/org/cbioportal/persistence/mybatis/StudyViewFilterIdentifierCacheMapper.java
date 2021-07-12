package org.cbioportal.persistence.mybatis;

import java.util.List;

public interface StudyViewFilterIdentifierCacheMapper {
    public void persist(byte[] hash, List<Integer> sampleIds);
}
