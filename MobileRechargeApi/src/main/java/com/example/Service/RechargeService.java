package com.example.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.Exception.InvalidRechargeException;
import com.example.Exception.RechargeNotFoundException;
import com.example.Model.Recharge;

@Service
public class RechargeService {

    private static final int MAX_AMOUNT = 5000;

    private final ArrayList<Recharge> recharges = new ArrayList<>();

    private long nextId = 1L;

    // returns a copy, so outside code cannot change the stored recharge
    private Recharge copyRecharge(Recharge recharge) {
        return new Recharge(
                recharge.getRechargeId(),
                recharge.getMobileNumber(),
                recharge.getOperator(),
                recharge.getAmount(),
                recharge.getPlanType()
        );
    }

    private void checkNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidRechargeException(message);
        }
    }

    // business rule: maximum amount
    private void checkMaxAmount(Integer amount) {
        if (amount > MAX_AMOUNT) {
            throw new InvalidRechargeException(
                    "Maximum recharge amount is " + MAX_AMOUNT
            );
        }
    }

    private Recharge findRecharge(Long id) {
        return recharges.stream()
                .filter(recharge -> recharge.getRechargeId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new RechargeNotFoundException(
                                "Recharge not found with ID: " + id
                        ));
    }

    // ---------- CRUD ----------

    public Recharge addRecharge(Recharge request) {

        checkMaxAmount(request.getAmount());

        Recharge recharge = new Recharge(
                nextId++,
                request.getMobileNumber(),
                request.getOperator().trim(),
                request.getAmount(),
                request.getPlanType().trim()
        );

        recharges.add(recharge);

        return copyRecharge(recharge);
    }

    public List<Recharge> getAllRecharges() {

        return recharges.stream()
                .map(this::copyRecharge)
                .toList();
    }

    public Recharge getRechargeById(Long id) {
        return copyRecharge(findRecharge(id));
    }

    public Recharge updateRecharge(Long id, Recharge request) {

        Recharge recharge = findRecharge(id);

        checkMaxAmount(request.getAmount());

        recharge.setMobileNumber(request.getMobileNumber());
        recharge.setOperator(request.getOperator().trim());
        recharge.setAmount(request.getAmount());
        recharge.setPlanType(request.getPlanType().trim());

        return copyRecharge(recharge);
    }

    public String deleteRecharge(Long id) {

        Recharge recharge = findRecharge(id);

        recharges.remove(recharge);

        return "Recharge deleted with ID: " + id;
    }

    // ---------- filter ----------

    // filter 1: by operator
    public List<Recharge> getByOperator(String operator) {

        checkNotBlank(operator, "Operator must not be blank");

        return recharges.stream()
                .filter(recharge ->
                        recharge.getOperator()
                                .equalsIgnoreCase(operator.trim()))
                .map(this::copyRecharge)
                .toList();
    }

    // filter 2: by plan type
    public List<Recharge> getByPlanType(String planType) {

        checkNotBlank(planType, "Plan type must not be blank");

        return recharges.stream()
                .filter(recharge ->
                        recharge.getPlanType()
                                .equalsIgnoreCase(planType.trim()))
                .map(this::copyRecharge)
                .toList();
    }

    // filter 3: partial mobile number match
    public List<Recharge> searchByMobile(String digits) {

        checkNotBlank(digits, "Search text must not be blank");

        if (!digits.trim().matches("[0-9]+")) {
            throw new InvalidRechargeException(
                    "Search text must contain digits only"
            );
        }

        return recharges.stream()
                .filter(recharge ->
                        recharge.getMobileNumber().contains(digits.trim()))
                .map(this::copyRecharge)
                .toList();
    }

    // filter 4: amount range, using compareTo
    public List<Recharge> getByAmountRange(Integer min, Integer max) {

        if (min == null || max == null
                || min < 0
                || min.compareTo(max) > 0) {
            throw new InvalidRechargeException(
                    "Enter a valid amount range (min must not be more than max)"
            );
        }

        return recharges.stream()
                .filter(recharge -> recharge.getAmount().compareTo(min) >= 0)
                .filter(recharge -> recharge.getAmount().compareTo(max) <= 0)
                .sorted((a, b) -> a.getAmount().compareTo(b.getAmount()))
                .map(this::copyRecharge)
                .toList();
    }

    // ---------- sorting ----------

    public List<Recharge> getSortedRecharges(String sortBy, String direction) {

        checkNotBlank(sortBy, "Sort field must not be blank");

        Comparator<Recharge> comparator =
                switch (sortBy.trim().toLowerCase(Locale.ROOT)) {
                    case "id" -> Comparator.comparing(Recharge::getRechargeId);
                    case "mobile" -> Comparator.comparing(Recharge::getMobileNumber);
                    case "operator" -> Comparator.comparing(
                            Recharge::getOperator,
                            String.CASE_INSENSITIVE_ORDER);
                    case "plan" -> Comparator.comparing(
                            Recharge::getPlanType,
                            String.CASE_INSENSITIVE_ORDER);
                    case "amount" -> (a, b) -> a.getAmount().compareTo(b.getAmount());
                    default -> throw new InvalidRechargeException(
                            "Sort field must be id, mobile, operator, plan or amount");
                };

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        } else if (!"asc".equalsIgnoreCase(direction)) {
            throw new InvalidRechargeException("Direction must be asc or desc");
        }

        return recharges.stream()
                .sorted(comparator)
                .map(this::copyRecharge)
                .toList();
    }

    // ---------- map and other Java APIs ----------

    // mapToInt + sum: total amount recharged for one number
    public int getTotalAmount(String mobile) {

        if (mobile == null || !mobile.trim().matches("[0-9]{10}")) {
            throw new InvalidRechargeException(
                    "Mobile number must contain 10 digits"
            );
        }

        return recharges.stream()
                .filter(recharge ->
                        recharge.getMobileNumber().equals(mobile.trim()))
                .mapToInt(Recharge::getAmount)
                .sum();
    }

    // groupingBy + counting: number of recharges for each operator
    public Map<String, Long> getOperatorSummary() {

        return recharges.stream()
                .collect(Collectors.groupingBy(
                        recharge -> recharge.getOperator().toUpperCase(Locale.ROOT),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    // max + Comparator + Optional: the biggest recharge
    public Recharge getHighestRecharge() {

        return recharges.stream()
                .max(Comparator.comparing(Recharge::getAmount))
                .map(this::copyRecharge)
                .orElseThrow(() ->
                        new RechargeNotFoundException("No recharges available"));
    }

    // map + distinct + sorted: all recharged numbers
    public List<String> getAllMobileNumbers() {

        return recharges.stream()
                .map(Recharge::getMobileNumber)
                .distinct()
                .sorted()
                .toList();
    }

}