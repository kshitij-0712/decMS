package com.decms.common.decorator;

import com.decms.model.ActionType;

public interface EvidenceAccessService {

    void recordEvidenceAction(String evidenceId,
                              String actorId,
                              ActionType actionType,
                              String ipAddress);
}
