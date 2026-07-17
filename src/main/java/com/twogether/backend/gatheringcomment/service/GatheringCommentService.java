package com.twogether.backend.gatheringcomment.service;

import com.twogether.backend.gathering.domain.Gathering;
import com.twogether.backend.gathering.repository.GatheringRepository;
import com.twogether.backend.gatheringcomment.domain.GatheringComment;
import com.twogether.backend.gatheringcomment.dto.request.CommentCreateRequest;
import com.twogether.backend.gatheringcomment.dto.response.CommentResponse;
import com.twogether.backend.gatheringcomment.repository.GatheringCommentRepository;
import com.twogether.backend.global.exception.BusinessException;
import com.twogether.backend.global.exception.ErrorCode;
import com.twogether.backend.user.domain.User;
import com.twogether.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GatheringCommentService {

    private final GatheringCommentRepository gatheringCommentRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;

    public GatheringCommentService(
            GatheringCommentRepository gatheringCommentRepository,
            GatheringRepository gatheringRepository,
            UserRepository userRepository
    ) {
        this.gatheringCommentRepository = gatheringCommentRepository;
        this.gatheringRepository = gatheringRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CommentResponse create(
            String authUserId,
            Long gatheringId,
            CommentCreateRequest request
    ) {
        User me = getUserByAuthId(authUserId);
        Gathering gathering = getGatheringById(gatheringId);

        GatheringComment parent = null;
        if (request.parentId() != null) {
            parent = gatheringCommentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
            if (!parent.getGathering().getId().equals(gatheringId)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
        }

        // 만약 대댓글(답글)인데 부모글이 비밀글이면, 자식글도 자동으로 비밀글 처리되거나 부모의 비밀 설정을 따르게 안전 설정할 수 있음
        boolean isSecret = request.isSecret();
        if (parent != null && parent.isSecret()) {
            isSecret = true; // 부모가 비밀글이면 대댓글도 무조건 비밀글로 보호
        }

        GatheringComment comment = new GatheringComment(
                gathering,
                me,
                parent,
                request.content(),
                isSecret
        );

        GatheringComment saved = gatheringCommentRepository.save(comment);
        return CommentResponse.of(saved, true, true, List.of());
    }

    @Transactional
    public void delete(
            String authUserId,
            Long gatheringId,
            Long commentId
    ) {
        User me = getUserByAuthId(authUserId);
        GatheringComment comment = gatheringCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getGathering().getId().equals(gatheringId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        boolean isHost = comment.getGathering().isHost(me.getId());
        boolean isWriter = comment.getUser().getId().equals(me.getId());

        if (!isHost && !isWriter) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        gatheringCommentRepository.delete(comment);
    }

    public List<CommentResponse> getComments(
            String authUserId,
            Long gatheringId
    ) {
        Gathering gathering = getGatheringById(gatheringId);
        Long myUserId = (authUserId != null)
                ? userRepository.findByAuthUserId(authUserId).map(User::getId).orElse(null)
                : null;

        List<GatheringComment> allComments = gatheringCommentRepository.findAllByGatheringIdWithUser(gatheringId);

        // Group by parent ID to assemble trees efficiently in memory
        Map<Long, List<GatheringComment>> childrenByParentId = allComments.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<GatheringComment> rootComments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .toList();

        return rootComments.stream()
                .map(root -> convertToResponse(root, myUserId, gathering, childrenByParentId))
                .toList();
    }

    private CommentResponse convertToResponse(
            GatheringComment comment,
            Long myUserId,
            Gathering gathering,
            Map<Long, List<GatheringComment>> childrenByParentId
    ) {
        boolean showContent = canViewCommentContent(comment, myUserId, gathering);

        List<GatheringComment> children = childrenByParentId.getOrDefault(comment.getId(), List.of());
        List<CommentResponse> childResponses = children.stream()
                .map(child -> convertToResponse(child, myUserId, gathering, childrenByParentId))
                .toList();

        boolean isMine = myUserId != null && comment.getUser().getId().equals(myUserId);
        return CommentResponse.of(comment, showContent, isMine, childResponses);
    }

    private boolean canViewCommentContent(GatheringComment comment, Long myUserId, Gathering gathering) {
        if (!comment.isSecret()) {
            return true; // 비밀글이 아니면 누구나 열람 가능
        }
        if (myUserId == null) {
            return false; // 비로그인 유저는 비밀글 열람 불가
        }

        boolean isHost = gathering.isHost(myUserId);
        boolean isWriter = comment.getUser().getId().equals(myUserId);

        // 대댓글인 경우 부모 원글 작성자도 열람 가능해야 함
        boolean isParentWriter = false;
        if (comment.getParent() != null) {
            isParentWriter = comment.getParent().getUser().getId().equals(myUserId);
        }

        return isHost || isWriter || isParentWriter;
    }

    private User getUserByAuthId(String authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Gathering getGatheringById(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));
    }
}
