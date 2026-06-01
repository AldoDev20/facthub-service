package com.facthub.billing.company.presentation.rest;

import com.facthub.billing.company.application.usecase.RegisterCompanyUseCase;
import com.facthub.billing.company.domain.model.Company;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final RegisterCompanyUseCase registerCompanyUseCase;

    public CompanyController(RegisterCompanyUseCase registerCompanyUseCase) {
        this.registerCompanyUseCase = registerCompanyUseCase;
    }

    /**
     * Endpoint to register or update an Automotive Workshop (Tenant) and its digital certificate.
     * Consumes: multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> registerCompany(
            @RequestParam("ruc") String ruc,
            @RequestParam("businessName") String businessName,
            @RequestParam("sunatSolUsername") String sunatSolUsername,
            @RequestParam("sunatSolPassword") String sunatSolPassword,
            @RequestParam("certificatePassword") String certificatePassword,
            @RequestPart("certificateFile") MultipartFile certificateFile) {

        try {
            Company company = registerCompanyUseCase.execute(
                    ruc,
                    businessName,
                    sunatSolUsername,
                    sunatSolPassword,
                    certificatePassword,
                    certificateFile
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Company registered successfully");
            response.put("ruc", company.getRuc());
            response.put("businessName", company.getBusinessName());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Internal error registering company: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
