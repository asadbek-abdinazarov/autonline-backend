package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.dto.request.TestFinishRequest;
import uz.javachi.autonline.dto.request.TestStartRequest;
import uz.javachi.autonline.dto.response.FinishResponseDTO;
import uz.javachi.autonline.dto.response.StartedResponseDTO;
import uz.javachi.autonline.dto.response.TestTemplateResponseDTO;
import uz.javachi.autonline.service.TestTemplateService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TestTemplateController {
    private final TestTemplateService testTemplateService;

    @GetMapping
    public ResponseEntity<List<TestTemplateResponseDTO>> getAllTestTemplates() {
        return ResponseEntity.ok(testTemplateService.getAllTestTemplates());
    }

    @PostMapping("/start-test")
    public ResponseEntity<StartedResponseDTO> startTestTemplate(@RequestBody TestStartRequest dto){
        return ResponseEntity.ok(testTemplateService.startTestTemplate(dto));
    }

    @PostMapping("/finish-test")
    public ResponseEntity<FinishResponseDTO> finishTestTemplate(@RequestBody TestFinishRequest dto){
        return ResponseEntity.ok(testTemplateService.finishTestTemplate(dto));
    }
}
