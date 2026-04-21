package com.decms.common.decorator;

import com.decms.forensic.service.CustodyLogService;
import com.decms.model.ActionType;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class LoggingDecorator implements EvidenceAccessService {

    private final BaseEvidenceAccessService delegate;
    private final CustodyLogService custodyLogService;
    private final UserRepository userRepository;

    public LoggingDecorator(BaseEvidenceAccessService delegate,
                            CustodyLogService custodyLogService,
                            UserRepository userRepository) {
        this.delegate = delegate;
        this.custodyLogService = custodyLogService;
        this.userRepository = userRepository;
    }

    @Override
    public void recordEvidenceAction(String evidenceId,
                                     String actorId,
                                     ActionType actionType,
                                     String ipAddress) {
        delegate.recordEvidenceAction(evidenceId, actorId, actionType, ipAddress);

        Role role = userRepository.findById(actorId)
                .map(User::getRole)
                .orElse(null);
        if (role == null) {
            return;
        }

        custodyLogService.createEntry(evidenceId, actorId, role, actionType, ipAddress);
    }
}
