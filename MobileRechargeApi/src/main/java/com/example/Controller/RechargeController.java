package com.example.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Model.Recharge;
import com.example.Service.RechargeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/recharges")
public class RechargeController {

    @Autowired
    RechargeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Recharge addRecharge(@Valid @RequestBody Recharge recharge) {
        return service.addRecharge(recharge);
    }

    @GetMapping
    public List<Recharge> getAllRecharges() {
        return service.getAllRecharges();
    }

    @GetMapping("/{id}")
    public Recharge getRechargeById(@PathVariable Long id) {
        return service.getRechargeById(id);
    }

    @PutMapping("/{id}")
    public Recharge updateRecharge(@PathVariable Long id,
                                   @Valid @RequestBody Recharge recharge) {
        return service.updateRecharge(id, recharge);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteRecharge(@PathVariable Long id) {
        return Map.of("message", service.deleteRecharge(id));
    }

    @GetMapping("/operator")
    public List<Recharge> getByOperator(@RequestParam String name) {
        return service.getByOperator(name);
    }

    @GetMapping("/plan")
    public List<Recharge> getByPlanType(@RequestParam String type) {
        return service.getByPlanType(type);
    }

    @GetMapping("/search")
    public List<Recharge> searchByMobile(@RequestParam String digits) {
        return service.searchByMobile(digits);
    }

    @GetMapping("/amount")
    public List<Recharge> getByAmountRange(@RequestParam Integer min,
                                           @RequestParam Integer max) {
        return service.getByAmountRange(min, max);
    }

    @GetMapping("/sorted")
    public List<Recharge> getSortedRecharges(
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.getSortedRecharges(sortBy, direction);
    }

    @GetMapping("/total")
    public Map<String, Object> getTotalAmount(@RequestParam String mobile) {
        return Map.of("mobileNumber", mobile,
                      "totalAmount", service.getTotalAmount(mobile));
    }

    @GetMapping("/summary")
    public Map<String, Long> getOperatorSummary() {
        return service.getOperatorSummary();
    }

    @GetMapping("/highest")
    public Recharge getHighestRecharge() {
        return service.getHighestRecharge();
    }

    @GetMapping("/numbers")
    public List<String> getAllMobileNumbers() {
        return service.getAllMobileNumbers();
    }

}