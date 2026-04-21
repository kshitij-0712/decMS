package com.decms.common.decorator;

import com.decms.model.ActionType;
import org.springframework.stereotype.Service;

@Service
public class BaseEvidenceAccessService implements EvidenceAccessService {

    @Override
    public void recordEvidenceAction(String evidenceId,
                                     String actorId,
                                     ActionType actionType,
                                     String ipAddress) {
        // Base behavior intentionally minimal.
    }
}
