package com.teco.bolao.controller;

import com.teco.bolao.dto.SeedImportRequestDto;
import com.teco.bolao.dto.SeedImportResultDto;
import com.teco.bolao.service.SeedImportService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/imports")
public class AdminImportController {

    private final SeedImportService seedImportService;

    public AdminImportController(SeedImportService seedImportService) {
        this.seedImportService = seedImportService;
    }

    @PostMapping("/seed")
    public SeedImportResultDto importSeed(@Valid @RequestBody SeedImportRequestDto request) {
        return seedImportService.importSeed(request);
    }
}
