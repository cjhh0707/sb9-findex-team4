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
public class IndexInfoController implements IndexInfoApi{

    private final IndexInfoService indexInfoService;

    @Override
    public ResponseEntity<IndexInfoResponse> createIndexInfo(IndexInfoCreateRequest request){
        IndexInfoResponse response = indexInfoService.createIndexInfo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<IndexInfoResponse> updateIndexInfo(Long id, IndexInfoUpdateRequest request){
        IndexInfoResponse response = indexInfoService.updateIndexInfo(id, request);
        return ResponseEntity.ok(response);
    }

    @Override
    public  ResponseEntity<Void> deleteIndexInfo(Long id){
        indexInfoService.deleteIndexInfo(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CursorPageResponse<IndexInfoResponse>> getIndexInfoList(
            IndexInfoSearchCondition condition, Long idAfter, int size){
        // Service도 CursorPageResponse를 반환하도록 수정 필요
        CursorPageResponse<IndexInfoResponse> responses = indexInfoService.getIndexInfoList(condition, idAfter, size);
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<List<IndexInfoSummaryDto>> getIndexInfoSummaries() {
        List<IndexInfoSummaryDto> summaries = indexInfoService.getIndexInfoSummaries();
        return ResponseEntity.ok(summaries);
    }


}