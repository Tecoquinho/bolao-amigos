package com.teco.bolao.controller;

import com.teco.bolao.dto.RankingEntryResponseDto;
import com.teco.bolao.service.RankingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public List<RankingEntryResponseDto> getRanking() {
        return rankingService.getRanking();
    }
}
