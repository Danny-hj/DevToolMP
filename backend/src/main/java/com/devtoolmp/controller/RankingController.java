package com.devtoolmp.controller;

import com.devtoolmp.dto.response.ToolRankingDTO;
import com.devtoolmp.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tools/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping("/daily")
    public ResponseEntity<List<ToolRankingDTO>> getDailyRanking() {
        List<ToolRankingDTO> ranking = rankingService.getDailyRanking();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<ToolRankingDTO>> getWeeklyRanking() {
        List<ToolRankingDTO> ranking = rankingService.getWeeklyRanking();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/alltime")
    public ResponseEntity<List<ToolRankingDTO>> getAllTimeRanking() {
        List<ToolRankingDTO> ranking = rankingService.getAllTimeRanking();
        return ResponseEntity.ok(ranking);
    }
}
