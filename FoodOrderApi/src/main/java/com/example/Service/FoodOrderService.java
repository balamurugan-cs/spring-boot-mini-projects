package com.example.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.Exception.InvalidOrderException;
import com.example.Exception.OrderNotFoundException;
import com.example.Model.FoodOrder;

@Service
public class FoodOrderService {

    private final ArrayList<FoodOrder> orders = new ArrayList<>();

    private long nextId = 1L;

    // returns a copy, so outside code cannot change the stored order
    private FoodOrder copyOrder(FoodOrder order) {
        return new FoodOrder(
                order.getOrderId(),
                order.getCustomerName(),
                order.getFoodItem(),
                order.getQuantity(),
                order.getPrice()
        );
    }

    private void checkNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderException(message);
        }
    }

    // finds the stored order or throws OrderNotFoundException
    private FoodOrder findOrder(Long id) {
        return orders.stream()
                .filter(order -> order.getOrderId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with ID: " + id
                        ));
    }

    // ---------- CRUD ----------

    public FoodOrder addOrder(FoodOrder request) {

        FoodOrder order = new FoodOrder(
                nextId++,
                request.getCustomerName().trim(),
                request.getFoodItem().trim(),
                request.getQuantity(),
                request.getPrice()
        );

        orders.add(order);

        return copyOrder(order);
    }

    public List<FoodOrder> getAllOrders() {

        return orders.stream()
                .map(this::copyOrder)
                .toList();
    }

    public FoodOrder getOrderById(Long id) {
        return copyOrder(findOrder(id));
    }

    // PUT: replace all fields
    public FoodOrder updateOrder(Long id, FoodOrder request) {

        FoodOrder order = findOrder(id);

        order.setCustomerName(request.getCustomerName().trim());
        order.setFoodItem(request.getFoodItem().trim());
        order.setQuantity(request.getQuantity());
        order.setPrice(request.getPrice());

        return copyOrder(order);
    }

    // PATCH: change only the fields that are sent
    public FoodOrder patchOrder(Long id, FoodOrder request) {

        FoodOrder order = findOrder(id);

        if (request.getCustomerName() != null) {
            checkNotBlank(request.getCustomerName(), "Customer name cannot be blank");
            order.setCustomerName(request.getCustomerName().trim());
        }

        if (request.getFoodItem() != null) {
            checkNotBlank(request.getFoodItem(), "Food item cannot be blank");
            order.setFoodItem(request.getFoodItem().trim());
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity() < 1) {
                throw new InvalidOrderException("Quantity must be at least 1");
            }
            order.setQuantity(request.getQuantity());
        }

        if (request.getPrice() != null) {
            if (request.getPrice().signum() <= 0) {
                throw new InvalidOrderException("Price must be greater than zero");
            }
            order.setPrice(request.getPrice());
        }

        return copyOrder(order);
    }

    public String deleteOrder(Long id) {

        FoodOrder order = findOrder(id);

        orders.remove(order);

        return "Order deleted with ID: " + id;
    }

    // ---------- filter, sort, map ----------

    // filter 1: partial food item match
    public List<FoodOrder> searchByFoodItem(String keyword) {

        checkNotBlank(keyword, "Search keyword must not be blank");

        String searchText = keyword.trim().toLowerCase(Locale.ROOT);

        return orders.stream()
                .filter(order ->
                        order.getFoodItem()
                                .toLowerCase(Locale.ROOT)
                                .contains(searchText))
                .map(this::copyOrder)
                .toList();
    }

    // filter 2: all orders of one customer
    public List<FoodOrder> getByCustomer(String name) {

        checkNotBlank(name, "Customer name must not be blank");

        return orders.stream()
                .filter(order ->
                        order.getCustomerName()
                                .equalsIgnoreCase(name.trim()))
                .map(this::copyOrder)
                .toList();
    }

    // filter 3: price range, using compareTo
    public List<FoodOrder> getByPriceRange(BigDecimal min, BigDecimal max) {

        if (min == null || max == null
                || min.signum() < 0
                || min.compareTo(max) > 0) {
            throw new InvalidOrderException(
                    "Enter a valid price range (min must not be more than max)"
            );
        }

        return orders.stream()
                .filter(order -> order.getPrice().compareTo(min) >= 0)
                .filter(order -> order.getPrice().compareTo(max) <= 0)
                .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .map(this::copyOrder)
                .toList();
    }

    // sorting: by id, customer, item, quantity or price
    public List<FoodOrder> getSortedOrders(String sortBy, String direction) {

        checkNotBlank(sortBy, "Sort field must not be blank");

        Comparator<FoodOrder> comparator =
                switch (sortBy.trim().toLowerCase(Locale.ROOT)) {
                    case "id" -> Comparator.comparing(FoodOrder::getOrderId);
                    case "customer" -> Comparator.comparing(
                            FoodOrder::getCustomerName,
                            String.CASE_INSENSITIVE_ORDER);
                    case "item" -> Comparator.comparing(
                            FoodOrder::getFoodItem,
                            String.CASE_INSENSITIVE_ORDER);
                    case "quantity" -> Comparator.comparing(FoodOrder::getQuantity);
                    case "price" -> (a, b) -> a.getPrice().compareTo(b.getPrice());
                    default -> throw new InvalidOrderException(
                            "Sort field must be id, customer, item, quantity or price");
                };

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        } else if (!"asc".equalsIgnoreCase(direction)) {
            throw new InvalidOrderException("Direction must be asc or desc");
        }

        return orders.stream()
                .sorted(comparator)
                .map(this::copyOrder)
                .toList();
    }

    // map + reduce: total bill of one customer (price x quantity)
    public BigDecimal getTotalBill(String customer) {

        checkNotBlank(customer, "Customer name must not be blank");

        return orders.stream()
                .filter(order ->
                        order.getCustomerName()
                                .equalsIgnoreCase(customer.trim()))
                .map(order ->
                        order.getPrice()
                                .multiply(BigDecimal.valueOf(order.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // map + distinct + sorted: list of food item names
    public List<String> getDistinctFoodItems() {

        return orders.stream()
                .map(FoodOrder::getFoodItem)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

}