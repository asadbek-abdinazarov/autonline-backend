package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javachi.autonline.dto.response.StatisticResponseDTO;
import uz.javachi.autonline.service.StatisticService;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/statistic")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping
    public ResponseEntity<StatisticResponseDTO> getStatistic() throws Exception {
        StatisticResponseDTO dto = statisticService.getStatistic().get();
        return ResponseEntity.ok(dto);
    }


}
