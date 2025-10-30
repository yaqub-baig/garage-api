package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.service.ReferralService;
import com.autoadda.apis.garage.util.ReferredEntityType;
import com.autoadda.apis.garage.util.ReferrerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

    @Autowired
    private ReferralService referralService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateReferral(@RequestParam ReferrerType type, @RequestParam Integer id) {
        String code = referralService.createReferral ( type, id );
        return ResponseEntity.ok ( code );
    }

    @PostMapping("/redeem")
    public ResponseEntity<Void> redeemReferral(@RequestParam String code, @RequestParam Integer newId,
                                               @RequestParam ReferredEntityType referredType) {
        referralService.redeemReferral ( code, newId, referredType );
        return ResponseEntity.ok ( ).build ( );
    }
}
