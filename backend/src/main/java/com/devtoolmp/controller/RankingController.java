package com.devtoolmp.controller;

import com.devtoolmp.dto.response.ToolRankingDTO;
import com.devtoolmp.dto.response.PageResponse;
import com.devtoolmp.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webapi/toolmarket/v1/tools/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping("/daily")
    public ResponseEntity<List<ToolRankingDTO>> getDailyRanking() {
        List<ToolRankingDTO> ranking = rankingService.getDailyRanking();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/daily/page")
    public ResponseEntity<PageResponse<ToolRankingDTO>> getDailyRankingPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ToolRankingDTO> ranking = rankingService.getDailyRanking(page, size);
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<ToolRankingDTO>> getWeeklyRanking() {
        List<ToolRankingDTO> ranking = rankingService.getWeeklyRanking();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/weekly/page")
    public ResponseEntity<PageResponse<ToolRankingDTO>> getWeeklyRankingPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ToolRankingDTO> ranking = rankingService.getWeeklyRanking(page, size);
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/alltime")
    public ResponseEntity<List<ToolRankingDTO>> getAllTimeRanking() {
        List<ToolRankingDTO> ranking = rankingService.getAllTimeRanking();
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/alltime/page")
    public ResponseEntity<PageResponse<ToolRankingDTO>> getAllTimeRankingPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ToolRankingDTO> ranking = rankingService.getAllTimeRanking(page, size);
        return ResponseEntity.ok(ranking);
    }
}
