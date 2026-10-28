package com.example.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Model.FoodOrder;
import com.example.Service.FoodOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class FoodOrderController {

    @Autowired
    FoodOrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodOrder addOrder(@Valid @RequestBody FoodOrder order) {
        return service.addOrder(order);
    }

    @GetMapping
    public List<FoodOrder> getAllOrders() {
        return service.getAllOrders();
    }

    @GetMapping("/{id}")
    public FoodOrder getOrderById(@PathVariable Long id) {
        return service.getOrderById(id);
    }

    @PutMapping("/{id}")
    public FoodOrder updateOrder(@PathVariable Long id,
                                 @Valid @RequestBody FoodOrder order) {
        return service.updateOrder(id, order);
    }

    @PatchMapping("/{id}")
    public FoodOrder patchOrder(@PathVariable Long id,
                                @RequestBody FoodOrder order) {
        return service.patchOrder(id, order);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteOrder(@PathVariable Long id) {
        return Map.of("message", service.deleteOrder(id));
    }

    @GetMapping("/search")
    public List<FoodOrder> searchByFoodItem(@RequestParam String keyword) {
        return service.searchByFoodItem(keyword);
    }

    @GetMapping("/customer")
    public List<FoodOrder> getByCustomer(@RequestParam String name) {
        return service.getByCustomer(name);
    }

    @GetMapping("/price")
    public List<FoodOrder> getByPriceRange(@RequestParam BigDecimal min,
                                           @RequestParam BigDecimal max) {
        return service.getByPriceRange(min, max);
    }

    @GetMapping("/sorted")
    public List<FoodOrder> getSortedOrders(
            @RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return service.getSortedOrders(sortBy, direction);
    }

    @GetMapping("/total")
    public Map<String, Object> getTotalBill(@RequestParam String customer) {
        return Map.of("customer", customer,
                      "totalBill", service.getTotalBill(customer));
    }

    @GetMapping("/items")
    public List<String> getDistinctFoodItems() {
        return service.getDistinctFoodItems();
    }

}