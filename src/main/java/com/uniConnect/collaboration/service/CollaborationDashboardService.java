package com.uniConnect.collaboration.service;

import com.uniConnect.collaboration.dto.*;
import com.uniConnect.collaboration.entity.*;
import com.uniConnect.collaboration.enums.*;
import com.uniConnect.collaboration.repository.*;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.common.service.FileStorageService;
import com.uniConnect.campaign.repository.MatchingRequestRepository;
import com.uniConnect.member.enums.UserRole;
import com.uniConnect.company.entity.Company;
import com.uniConnect.global.exception.ErrorCode;
import com.uniConnect.global.exception.CustomException;
import com.uniConnect.s3.S3FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaborationDashboardService {

    private final CollaborationRepository collaborationRepository;
    private final CollaborationTaskRepository taskRepository;
    private final ProductInfoRepository productInfoRepository;
    private final ContentUploadRepository contentUploadRepository;
    private final ReceiptConfirmationRepository receiptRepository;
    private final FileStorageService fileStorageService;
    private final S3FileService s3FileService;
    private final ShippingInfoRepository shippingInfoRepository;
    private final StudentReceiveInfoRepository studentReceiveInfoRepository;
    private final MatchingRequestRepository matchingRequestRepository;

    @Value("${app.s3.bucket}")
    private String bucketName;

    // 헬퍼: collaboration 조회
    private Collaboration getCollabOrThrow(Long collaborationId) {
        return collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new IllegalArgumentException("Collaboration not found: " + collaborationId));
    }

    private void validateCompanyOwnership(Collaboration collab, Long companyUserId) {
        if (companyUserId == null) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        // 1) Collaboration → MatchingRequest
        var matching = collab.getMatching();
        if (matching == null || matching.getCampaign() == null) {
            throw new AccessDeniedException("매칭 정보가 올바르지 않습니다.");
        }

        // 2) matching → campaign → company
        var company = matching.getCampaign().getCompany();
        if (company == null) {
            throw new AccessDeniedException("기업 정보가 존재하지 않습니다.");
        }

        // 3) company → users 목록에서 userId 찾기
        boolean isOwner = company.getUsers().stream()
                .anyMatch(u -> u.getUserId().equals(companyUserId));

        if (!isOwner) {
            throw new AccessDeniedException("해당 기업의 사용자만 접근 가능합니다.");
        }
    }

    // 1. 기업: 제품 정보 입력
    public ProductInfoResponse addOrUpdateProductInfo(ProductInfoRequest request, Long companyUserId) {
        Collaboration collab = getCollabOrThrow(request.getCollaborationId());

        // 회사 권한 검증 (matchingRequest → campaign → company → user)
        validateCompanyOwnership(collab, companyUserId);

        ProductInfo info = ProductInfo.builder()
                .collaboration(collab)
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .description(request.getDescription())
                .providedBy(UploaderType.Company)
                .isShipped(false)
                .build();

        ProductInfo saved = productInfoRepository.save(info);

        upsertTaskStatus(collab, TaskType.ProductInfo, TaskStatus.Done, "Company", null);
        collab.setStatus(CollaborationStatus.InProgress);

        return ProductInfoResponse.from(saved);
    }



    // 2. 기업: 배송 정보 입력 (발송일/운송장/발송여부)
    public ProductInfoResponse updateShippingInfo(ShippingInfoRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());

        // 가장 최근 ProductInfo를 꺼내서 배송정보 채운다고 가정 (실제로는 특정 productInfoId를 지정해도 됨)
        ProductInfo latest = productInfoRepository.findByCollaboration(collab)
                .stream()
                .reduce((a,b) -> b) // 마지막 것
                .orElseThrow(() -> new IllegalStateException("No product info yet"));

        latest.setDeliveryDate(req.getShippingDate());
        latest.setIsShipped(req.getIsShipped());
        latest.setTrackingNo(req.getTrackingNo());
        productInfoRepository.save(latest);

        // 태스크 상태: ShippingInfo → Done if shipped == true, else InProgress
        TaskStatus newStatus = Boolean.TRUE.equals(req.getIsShipped()) ? TaskStatus.Done : TaskStatus.InProgress;
        upsertTaskStatus(collab, TaskType.ShippingInfo, newStatus, "Company", null);

        if (Boolean.TRUE.equals(req.getIsShipped())) {
            collab.setStatus(CollaborationStatus.WaitingReceipt);
        }
        return ProductInfoResponse.from(latest);
    }

    // 3. 기업/학생단체: 이미지 업로드
    public ContentUploadResponse uploadContentToS3(Long collaborationId,
                                                   String uploaderTypeStr,
                                                   String caption,
                                                   MultipartFile image) throws Exception {

        Collaboration collab = getCollabOrThrow(collaborationId);

        String key = "collaboration/" + collaborationId + "/uploads/" + image.getOriginalFilename();
        s3FileService.upload(bucketName, key, image);
        String url = "https://" + bucketName + ".s3.amazonaws.com/" + key;

        UploaderType uploaderType;
        if ("Company".equalsIgnoreCase(uploaderTypeStr)) {
            uploaderType = UploaderType.Company;
        } else if ("Admin".equalsIgnoreCase(uploaderTypeStr)) {
            uploaderType = UploaderType.Admin;
        } else {
            uploaderType = UploaderType.StudentOrg;
        }

        ContentUpload upload = ContentUpload.builder()
                .collaboration(collab)
                .imageUrl(url)
                .caption(caption)
                .uploaderType(uploaderType)
                .build();

        contentUploadRepository.save(upload);

        // 태스크 업데이트
        TaskStatus contentStatus = (uploaderType == UploaderType.StudentOrg) ? TaskStatus.Done : TaskStatus.InProgress;
        upsertTaskStatus(collab, TaskType.ContentShare, contentStatus, uploaderType.name(), null);

        return ContentUploadResponse.from(upload);
    }



    // 4. 학생단체: 인수증 제출
    public ReceiptResponse submitReceiptToS3(Long collaborationId,
                                             String receiverName,
                                             String location,
                                             MultipartFile receiptImage) throws Exception {

        Collaboration collab = getCollabOrThrow(collaborationId);

        String key = "collaboration/" + collaborationId + "/receipts/" + receiptImage.getOriginalFilename();
        s3FileService.upload(bucketName, key, receiptImage);
        String url = "https://" + bucketName + ".s3.amazonaws.com/" + key;

        ReceiptConfirmation receipt = ReceiptConfirmation.builder()
                .collaboration(collab)
                .receiverName(receiverName)
                .location(location)
                .receiptImageUrl(url)
                .status(ReceiptStatus.WaitingApproval)
                .submittedAt(LocalDateTime.now())
                .build();

        receiptRepository.save(receipt);

        upsertTaskStatus(collab, TaskType.Receipt, TaskStatus.InProgress, "StudentOrg", null);
        collab.setStatus(CollaborationStatus.WaitingReceipt);

        return ReceiptResponse.from(receipt);
    }

    // 5. 기업: 인수증 승인
    public ReceiptResponse approveReceipt(ReceiptApproveRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());
        ReceiptConfirmation latest = receiptRepository.findTopByCollaborationOrderBySubmittedAtDesc(collab)
                .orElseThrow(() -> new IllegalStateException("No receipt uploaded"));

        latest.setStatus(ReceiptStatus.Approved);
        latest.setApprovedAt(LocalDateTime.now());
        receiptRepository.save(latest);

        upsertTaskStatus(collab, TaskType.Receipt, TaskStatus.Done, "Company", null);
        collab.setStatus(CollaborationStatus.Completed);

        return ReceiptResponse.from(latest);
    }

    // 6. 기업: 날짜 픽스
    public TaskResponse fixEventDate(DateFixRequest req) {
        Collaboration collab = getCollabOrThrow(req.getCollaborationId());

        CollaborationTask task = upsertTaskStatus(
                collab,
                TaskType.EventDateFix,
                TaskStatus.Pending,
                "Company",
                req.getEventDate()
        );

        collab.setStatus(CollaborationStatus.InProgress);

        return TaskResponse.from(task);
    }

    private CollaborationTask upsertTaskStatus(
            Collaboration collab,
            TaskType type,
            TaskStatus status,
            String updatedBy,
            java.time.LocalDate deadline
    ) {
        CollaborationTask task = taskRepository.findByCollaboration(collab).stream()
                .filter(t -> t.getType() == type)
                .findFirst()
                .orElseGet(() -> CollaborationTask.builder()
                        .collaboration(collab)
                        .type(type)
                        .status(TaskStatus.Pending)
                        .build()
                );

        task.setStatus(status);
        task.setUpdatedBy(updatedBy);
        if (deadline != null) {
            task.setDeadline(deadline);
        }
        return taskRepository.save(task);
    }

    // 협업 대시보드 조회(학생단체 & 기업 공용)
    @Transactional(readOnly = true)
    public CollaborationDashboardResponse getDashboard(Long collaborationId, Long userId, String role) {

        Collaboration collab = collaborationRepository.findById(collaborationId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Long matchingId = collab.getMatching().getMatchingId();

        boolean hasAccess = false;

        // 학생단체 접근 검증
        if (role.equals("StudentOrg")) {
            hasAccess = matchingRequestRepository.existsByMatchingIdAndStudentOrgUsers(
                    matchingId,
                    userId
            );
        }

        // 기업 접근 검증
        else if (role.equals("Company")) {
            hasAccess = matchingRequestRepository.existsByMatchingIdAndCompanyUsers(
                    matchingId,
                    userId
            );
        }

        // 그 외 (Admin 등)
        else if (role.equals("Admin")) {
            hasAccess = true; // 필요하면 추가 검증
        }

        if (!hasAccess) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        List<CollaborationTask> tasks = taskRepository.findByCollaboration(collab);
        List<ProductInfo> products = productInfoRepository.findByCollaboration(collab);
        List<ContentUpload> uploads = contentUploadRepository.findByCollaboration(collab);
        List<ReceiptConfirmation> receipts = receiptRepository.findByCollaboration(collab);

        return CollaborationDashboardResponse.from(
                collab, tasks, products, uploads, receipts, role
        );
    }
}