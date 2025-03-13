package com.example.electrical_preorder_system_backend.service.order;

import com.example.electrical_preorder_system_backend.dto.request.CreateOrderRequest;
import com.example.electrical_preorder_system_backend.dto.request.UpdateOrderRequest;
import com.example.electrical_preorder_system_backend.dto.response.OrderDTO;
import com.example.electrical_preorder_system_backend.dto.response.OrderListDTO;
import com.example.electrical_preorder_system_backend.entity.User;

import java.util.UUID;

public interface IOrderService {

    /** Create order with user and createOrderRequest
     * If order already exists, update quantity and total amount
     * If order not exists, create new order
     *
     * @param user Authenticated user
     * @param createOrderRequest CreateOrderRequest
     * @return OrderDTO
     */
    OrderDTO createOrder(User user, CreateOrderRequest createOrderRequest);

    /** Get orders with status, page and size, only accessible by admin
     *
     * @param status Order status
     * @param page Page number
     * @param size Page size
     * @return OrderListDTO
     */
    OrderListDTO getOrders(String status, int page, int size);

    /** Update order with user, orderId and updateOrderRequest
     * Now only update quantity
     * Available for owner and admin
     *
     * @param user Authenticated user
     * @param orderId Order id
     * @param updateOrderRequest UpdateOrderRequest
     */
    void update(User user, UUID orderId, UpdateOrderRequest updateOrderRequest);

    /** Delete order with user and orderId
     *
     * @param user Authenticated user
     * @param orderId Order id
     */
    void delete(User user, UUID orderId);
}
