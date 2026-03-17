package com.sprint.findex.domain.indexinfo.controller;

import com.sprint.findex.common.dto.CursorPageResponse;
import com.sprint.findex.domain.indexinfo.dto.*;
import com.sprint.findex.domain.indexinfo.service.IndexInfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController implements IndexInfoApi {

    private final IndexInfoService indexInfoService;

    @Override
    public ResponseEntity<IndexInfoResponse> createIndexInfo(IndexInfoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(indexInfoService.createIndexInfo(request));
    }

    @Override
    public ResponseEntity<IndexInfoResponse> getIndexInfo(Long id) {
        return ResponseEntity.ok(indexInfoService.getIndexInfo(id));
    }

    @Override
    public ResponseEntity<IndexInfoResponse> updateIndexInfo(Long id, IndexInfoUpdateRequest request) {
        return ResponseEntity.ok(indexInfoService.updateIndexInfo(id, request));
    }

    @Override
    public ResponseEntity<Void> deleteIndexInfo(Long id) {
        indexInfoService.deleteIndexInfo(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CursorPageResponse<IndexInfoResponse>> getIndexInfoList(
            IndexInfoSearchCondition condition, Long idAfter, int size) {
        return ResponseEntity.ok(indexInfoService.getIndexInfoList(condition, idAfter, size));
    }

    @Override
    public ResponseEntity<List<IndexInfoSummaryDto>> getIndexInfoSummaries() {
        return ResponseEntity.ok(indexInfoService.getIndexInfoSummaries());
    }
}
