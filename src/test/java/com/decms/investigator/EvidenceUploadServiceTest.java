package com.decms.investigator;

import com.decms.common.service.FileStorageService;
import com.decms.common.service.HashService;
import com.decms.forensic.service.CustodyLogService;
import com.decms.investigator.dto.EvidenceUploadRequest;
import com.decms.investigator.dto.EvidenceUploadResult;
import com.decms.investigator.service.EvidenceUploadService;
import com.decms.model.ActionType;
import com.decms.model.Evidence;
import com.decms.model.HashRecord;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.HashRecordRepository;
import com.decms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceUploadServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private HashRecordRepository hashRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private HashService hashService;

    @Mock
    private CustodyLogService custodyLogService;

    private EvidenceUploadService service;

    @BeforeEach
    void setUp() {
        service = new EvidenceUploadService(
                evidenceRepository,
                hashRecordRepository,
                userRepository,
                fileStorageService,
                hashService,
                custodyLogService,
                10_485_760L
        );
    }

    @Test
    void uploadEvidencePersistsEvidenceHashAndCustodyEntry() {
        EvidenceUploadRequest request = new EvidenceUploadRequest();
        request.setCaseId("CASE-01");
        request.setEvidenceType("DOCUMENT");
        request.setDescription("Disk image metadata");
        request.setEvidenceFile(new MockMultipartFile(
                "evidenceFile",
                "doc.pdf",
                "application/pdf",
                "payload".getBytes()
        ));

        User user = new User();
        user.setUserId("inv-001");
        user.setRole(Role.INVESTIGATOR);

        when(userRepository.findById("inv-001")).thenReturn(Optional.of(user));
        when(fileStorageService.store(any(), any())).thenReturn("uploads/CASE-01/file.pdf");
        when(hashService.generateHash(any(MultipartFile.class))).thenReturn("abc123");
        when(hashRecordRepository.existsByHashValue("abc123")).thenReturn(false);
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(invocation -> {
            Evidence e = invocation.getArgument(0);
            e.setEvidenceId("E-100");
            return e;
        });
        when(hashRecordRepository.save(any(HashRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(custodyLogService.createEntry(any(), any(), any(), any(), any())).thenReturn(null);

        EvidenceUploadResult result = service.uploadEvidence(request, "inv-001", "127.0.0.1");

        assertEquals("E-100", result.getEvidenceId());
        assertEquals("abc123", result.getHashValue());
        assertTrue(!result.isDuplicateHash());

        verify(evidenceRepository).save(any(Evidence.class));
        verify(hashRecordRepository).save(any(HashRecord.class));
        verify(custodyLogService).createEntry(
                eq("E-100"),
                eq("inv-001"),
                eq(Role.INVESTIGATOR),
                eq(ActionType.UPLOAD),
                eq("127.0.0.1")
        );
    }

    @Test
    void uploadEvidenceRejectsUnsupportedMimeType() {
        EvidenceUploadRequest request = new EvidenceUploadRequest();
        request.setCaseId("CASE-02");
        request.setEvidenceType("DOCUMENT");
        request.setEvidenceFile(new MockMultipartFile(
                "evidenceFile",
                "payload.exe",
                "application/x-msdownload",
                "bin".getBytes()
        ));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadEvidence(request, "inv-001", "127.0.0.1")
        );

        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }

    @Test
    void uploadEvidenceRejectsNonInvestigatorRole() {
        EvidenceUploadRequest request = new EvidenceUploadRequest();
        request.setCaseId("CASE-03");
        request.setEvidenceType("DOCUMENT");
        request.setEvidenceFile(new MockMultipartFile(
                "evidenceFile",
                "doc.pdf",
                "application/pdf",
                "payload".getBytes()
        ));

        User legalUser = new User();
        legalUser.setUserId("legal-001");
        legalUser.setRole(Role.LEGAL_OFFICER);

        when(userRepository.findById("legal-001")).thenReturn(Optional.of(legalUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.uploadEvidence(request, "legal-001", "127.0.0.1")
        );

        assertEquals("Only investigators can upload evidence", ex.getMessage());
    }
}
