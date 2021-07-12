package org.cbioportal.persistence.mybatis;

import org.cbioportal.persistence.StudyViewFilterIdentifierCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudyViewFilterIdentifierCacheMyBatisRepository implements StudyViewFilterIdentifierCacheRepository {
    @Autowired
    StudyViewFilterIdentifierCacheMapper mapper;
    
    @Override
    public void persist(byte[] hash, List<Integer> sampleIds) {
        mapper.persist(hash, sampleIds);
    }
}
