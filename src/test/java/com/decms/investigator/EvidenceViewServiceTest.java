package com.decms.investigator;

import com.decms.common.decorator.EvidenceAccessService;
import com.decms.investigator.dto.EvidenceDetailsResponse;
import com.decms.investigator.dto.EvidenceListItemResponse;
import com.decms.investigator.service.EvidenceViewService;
import com.decms.model.ActionType;
import com.decms.model.CustodyLog;
import com.decms.model.Evidence;
import com.decms.model.EvidenceStatus;
import com.decms.model.HashRecord;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.CustodyLogRepository;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.HashRecordRepository;
import com.decms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceViewServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private HashRecordRepository hashRecordRepository;

    @Mock
    private CustodyLogRepository custodyLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EvidenceAccessService evidenceAccessService;

    @InjectMocks
    private EvidenceViewService service;

    @Test
    void listEvidenceReturnsItemsForInvestigatorScope() {
        User user = new User();
        user.setUserId("inv-001");

        Evidence evidence = new Evidence();
        evidence.setEvidenceId("E-11");
        evidence.setCaseId("CASE-X");
        evidence.setFileType("DOCUMENT");
        evidence.setFileSize(1024);
        evidence.setStatus(EvidenceStatus.SEALED);
        evidence.setUploadTimestamp(LocalDateTime.now());
        evidence.setUploadedBy(user);

        when(evidenceRepository.findByUploadedBy_UserIdOrderByUploadTimestampDesc("inv-001"))
                .thenReturn(List.of(evidence));

        List<EvidenceListItemResponse> items = service.listEvidence(null, null, "inv-001");
        assertEquals(1, items.size());
        assertEquals("E-11", items.get(0).getEvidenceId());
    }

    @Test
    void getEvidenceDetailsBlocksDifferentInvestigatorOwner() {
        User owner = new User();
        owner.setUserId("inv-001");
        owner.setName("Owner");
        owner.setRole(Role.INVESTIGATOR);

        Evidence evidence = new Evidence();
        evidence.setEvidenceId("E-77");
        evidence.setUploadedBy(owner);

        User requester = new User();
        requester.setUserId("inv-002");
        requester.setRole(Role.INVESTIGATOR);

        when(evidenceRepository.findById("E-77")).thenReturn(Optional.of(evidence));
        when(userRepository.findById("inv-002")).thenReturn(Optional.of(requester));

        assertThrows(IllegalArgumentException.class,
                () -> service.getEvidenceDetails("E-77", "inv-002", "127.0.0.1", true));
    }

    @Test
    void getEvidenceDetailsIncludesHashAndCustodySummary() {
        User user = new User();
        user.setUserId("inv-001");
        user.setRole(Role.INVESTIGATOR);
        user.setName("Investigator One");

        Evidence evidence = new Evidence();
        evidence.setEvidenceId("E-88");
        evidence.setCaseId("CASE-88");
        evidence.setDescription("Evidence note");
        evidence.setFileType("IMAGE");
        evidence.setFileSize(2048);
        evidence.setStatus(EvidenceStatus.SEALED);
        evidence.setUploadTimestamp(LocalDateTime.now());
        evidence.setUploadedBy(user);

        HashRecord hash = new HashRecord();
        hash.setHashValue("deadbeef");
        hash.setAlgorithm("SHA-256");

        CustodyLog log = new CustodyLog();
        log.setActionType(ActionType.VIEW);
        log.setActor(user);
        log.setActorRole(Role.INVESTIGATOR);
        log.setIpAddress("127.0.0.1");
        log.setTimestamp(LocalDateTime.now());

        when(evidenceRepository.findById("E-88")).thenReturn(Optional.of(evidence));
        when(userRepository.findById("inv-001")).thenReturn(Optional.of(user));
        when(hashRecordRepository.findByEvidenceEvidenceId("E-88")).thenReturn(Optional.of(hash));
        when(custodyLogRepository.findTop10ByEvidenceEvidenceIdOrderByTimestampDesc("E-88")).thenReturn(List.of(log));

        EvidenceDetailsResponse response = service.getEvidenceDetails("E-88", "inv-001", "127.0.0.1", true);
        assertEquals("deadbeef", response.getHashValue());
        assertEquals(1, response.getCustodyEvents().size());
    }

    @Test
    void getEvidenceDetailsRejectsNonInvestigatorRole() {
        User owner = new User();
        owner.setUserId("inv-001");
        owner.setRole(Role.INVESTIGATOR);

        Evidence evidence = new Evidence();
        evidence.setEvidenceId("E-99");
        evidence.setUploadedBy(owner);

        User admin = new User();
        admin.setUserId("admin-001");
        admin.setRole(Role.ADMINISTRATOR);

        when(evidenceRepository.findById("E-99")).thenReturn(Optional.of(evidence));
        when(userRepository.findById("admin-001")).thenReturn(Optional.of(admin));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getEvidenceDetails("E-99", "admin-001", "127.0.0.1", true)
        );

        assertEquals("Only investigators can view investigator evidence details", ex.getMessage());
    }
}
