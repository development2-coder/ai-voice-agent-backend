package com.infinitio.aivoiceplatform.aidialer.controller;

import com.infinitio.aivoiceplatform.aidialer.dto.request.CreateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.PauseDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.ResumeDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.StartDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.request.UpdateAiDialerRequest;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerCallResponse;
import com.infinitio.aivoiceplatform.aidialer.dto.response.DialerResponse;
import com.infinitio.aivoiceplatform.aidialer.service.AiDialerService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerSchedulerService;
import com.infinitio.aivoiceplatform.aidialer.service.DialerCallInitiationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-dialers")
@RequiredArgsConstructor
public class AiDialerController {

    private final AiDialerService aiDialerService;

    private final DialerCallService dialerCallService;

    private final DialerSchedulerService dialerSchedulerService;

    private final DialerCallInitiationService
            dialerCallInitiationService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<DialerResponse> create(
            @Valid @RequestBody CreateAiDialerRequest request) {

        DialerResponse response =
                aiDialerService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // GET BY PUBLIC ID
    // =========================================================

    @GetMapping("/{publicId}")
    public ResponseEntity<DialerResponse> getByPublicId(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                aiDialerService.getByPublicId(
                        publicId
                )
        );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<List<DialerResponse>> getAll() {

        return ResponseEntity.ok(
                aiDialerService.getAll()
        );
    }


    // =========================================================
    // GET BY CAMPAIGN
    // =========================================================

    @GetMapping("/campaign/{campaignPublicId}")
    public ResponseEntity<List<DialerResponse>> getByCampaign(
            @PathVariable String campaignPublicId) {

        return ResponseEntity.ok(
                aiDialerService.getByCampaign(
                        campaignPublicId
                )
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{publicId}")
    public ResponseEntity<DialerResponse> update(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateAiDialerRequest request) {

        return ResponseEntity.ok(
                aiDialerService.update(
                        publicId,
                        request
                )
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> delete(
            @PathVariable String publicId) {

        aiDialerService.delete(
                publicId
        );

        return ResponseEntity.noContent()
                .build();
    }


    // =========================================================
    // START
    // =========================================================

    @PostMapping("/start")
    public ResponseEntity<DialerResponse> start(
            @Valid @RequestBody StartDialerRequest request) {

        return ResponseEntity.ok(
                aiDialerService.start(
                        request
                )
        );
    }


    // =========================================================
    // PAUSE
    // =========================================================

    @PostMapping("/pause")
    public ResponseEntity<DialerResponse> pause(
            @Valid @RequestBody PauseDialerRequest request) {

        return ResponseEntity.ok(
                aiDialerService.pause(
                        request
                )
        );
    }


    // =========================================================
    // RESUME
    // =========================================================

    @PostMapping("/resume")
    public ResponseEntity<DialerResponse> resume(
            @Valid @RequestBody ResumeDialerRequest request) {

        return ResponseEntity.ok(
                aiDialerService.resume(
                        request
                )
        );
    }


    // =========================================================
    // STOP
    // =========================================================

    @PostMapping("/{publicId}/stop")
    public ResponseEntity<DialerResponse> stop(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                aiDialerService.stop(
                        publicId
                )
        );
    }


    // =========================================================
    // PROCESS ONE DIALER MANUALLY
    // =========================================================

    @PostMapping("/{publicId}/process")
    public ResponseEntity<List<DialerCallResponse>> processDialer(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                dialerSchedulerService.processDialer(
                        publicId
                )
        );
    }


    // =========================================================
    // GET DIALER CALLS
    // =========================================================

    @GetMapping("/{publicId}/calls")
    public ResponseEntity<List<DialerCallResponse>> getCalls(
            @PathVariable String publicId) {

        return ResponseEntity.ok(
                dialerCallService.getByDialer(
                        publicId
                )
        );
    }


    // =========================================================
    // GET SINGLE CALL
    // =========================================================

    @GetMapping("/calls/{callPublicId}")
    public ResponseEntity<DialerCallResponse> getCall(
            @PathVariable String callPublicId) {

        return ResponseEntity.ok(
                dialerCallService.getByPublicId(
                        callPublicId
                )
        );
    }


    // =========================================================
    // GET CONTACT CALL HISTORY
    // =========================================================

    @GetMapping(
            "/calls/contact/{campaignContactPublicId}"
    )
    public ResponseEntity<List<DialerCallResponse>>
    getContactCallHistory(
            @PathVariable String campaignContactPublicId) {

        return ResponseEntity.ok(
                dialerCallService
                        .getByCampaignContact(
                                campaignContactPublicId
                        )
        );
    }

    @PostMapping("/calls/{callPublicId}/initiate")
    public ResponseEntity<DialerCallResponse> initiateCall(
            @PathVariable String callPublicId) {

        return ResponseEntity.ok(
                dialerCallInitiationService.initiateCall(
                        callPublicId
                )
        );
    }
}