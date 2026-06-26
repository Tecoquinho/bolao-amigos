package com.teco.bolao.controller;

import com.teco.bolao.dto.RankingEntryResponseDto;
import com.teco.bolao.service.PublicSnapshotService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final PublicSnapshotService publicSnapshotService;

    public RankingController(PublicSnapshotService publicSnapshotService) {
        this.publicSnapshotService = publicSnapshotService;
    }

    @GetMapping
    public List<RankingEntryResponseDto> getRanking() {
        return publicSnapshotService.getRanking();
    }
}
