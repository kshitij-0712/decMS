package com.decms.investigator.service;

import com.decms.common.service.FileStorageService;
import com.decms.common.service.HashService;
import com.decms.investigator.dto.EvidenceUploadRequest;
import com.decms.investigator.dto.EvidenceUploadResult;
import com.decms.model.ActionType;
import com.decms.model.Evidence;
import com.decms.model.EvidenceStatus;
import com.decms.model.HashRecord;
import com.decms.model.Role;
import com.decms.model.User;
import com.decms.forensic.service.CustodyLogService;
import com.decms.repository.EvidenceRepository;
import com.decms.repository.HashRecordRepository;
import com.decms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class EvidenceUploadService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "text/plain",
            "application/zip"
    );

    private final EvidenceRepository evidenceRepository;
    private final HashRecordRepository hashRecordRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final HashService hashService;
    private final CustodyLogService custodyLogService;
    private final long maxFileSize;

    public EvidenceUploadService(EvidenceRepository evidenceRepository,
                                 HashRecordRepository hashRecordRepository,
                                 UserRepository userRepository,
                                 FileStorageService fileStorageService,
                                 HashService hashService,
                                 CustodyLogService custodyLogService,
                                 @Value("${app.evidence.max-file-size-bytes:10485760}") long maxFileSize) {
        this.evidenceRepository = evidenceRepository;
        this.hashRecordRepository = hashRecordRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.hashService = hashService;
        this.custodyLogService = custodyLogService;
        this.maxFileSize = maxFileSize;
    }

    @Transactional
    public EvidenceUploadResult uploadEvidence(EvidenceUploadRequest request,
                                               String actorUserId,
                                               String ipAddress) {
        MultipartFile file = request.getEvidenceFile();
        validateRequest(request, file);

        User uploader = userRepository.findById(actorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Investigator user not found"));
        if (uploader.getRole() != Role.INVESTIGATOR) {
            throw new IllegalArgumentException("Only investigators can upload evidence");
        }

        String filePath = fileStorageService.store(file, request.getCaseId());
        try {
            String hash = hashService.generateHash(file);
            boolean duplicateHash = hashRecordRepository.existsByHashValue(hash);

            Evidence evidence = new Evidence();
            evidence.setCaseId(request.getCaseId().trim());
            evidence.setDescription(request.getDescription());
            evidence.setFileType(request.getEvidenceType().trim());
            evidence.setFileSize(file.getSize());
            evidence.setFilePath(filePath);
            evidence.setStatus(EvidenceStatus.SEALED);
            evidence.setUploadedBy(uploader);

            Evidence savedEvidence = evidenceRepository.save(evidence);

            HashRecord hashRecord = new HashRecord();
            hashRecord.setEvidence(savedEvidence);
            hashRecord.setHashValue(hash);
            hashRecord.setAlgorithm("SHA-256");
            hashRecordRepository.save(hashRecord);

            custodyLogService.createEntry(
                    savedEvidence.getEvidenceId(),
                    uploader.getUserId(),
                    Role.INVESTIGATOR,
                    ActionType.UPLOAD,
                    ipAddress
            );

            return new EvidenceUploadResult(
                    savedEvidence.getEvidenceId(),
                    hash,
                    savedEvidence.getStatus().name(),
                    duplicateHash
            );
        } catch (RuntimeException ex) {
            fileStorageService.deleteIfExists(filePath);
            throw ex;
        }
    }

    private void validateRequest(EvidenceUploadRequest request, MultipartFile file) {
        if (request == null) {
            throw new IllegalArgumentException("Upload request is required");
        }
        if (request.getCaseId() == null || request.getCaseId().isBlank()) {
            throw new IllegalArgumentException("Case ID is required");
        }
        if (request.getEvidenceType() == null || request.getEvidenceType().isBlank()) {
            throw new IllegalArgumentException("Evidence type is required");
        }
        if (request.getCaseId().trim().length() > 50) {
            throw new IllegalArgumentException("Case ID cannot exceed 50 characters");
        }
        if (request.getDescription() != null && request.getDescription().length() > 2000) {
            throw new IllegalArgumentException("Description cannot exceed 2000 characters");
        }
        if (request.getEvidenceType().trim().length() > 50) {
            throw new IllegalArgumentException("Evidence type cannot exceed 50 characters");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Evidence file is required");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }
    }
}
