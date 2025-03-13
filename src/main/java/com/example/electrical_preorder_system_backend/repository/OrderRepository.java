package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.Order;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    /**
     * Find all orders, order by status and then created_at desc
     *
     * @param pageable  pageable
     * @return          page of orders
     */
    @Nonnull
    @Query(value = "SELECT * FROM \"order\" " +
            "ORDER BY CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END, created_at DESC",
            countQuery = "SELECT count(*) FROM \"order\"",
            nativeQuery = true)
    Page<Order> findAll(@NonNull Pageable pageable);

    /**
     * Find all orders by status, order by status and then created_at desc
     *
     * @param status    status
     * @param pageable  pageable
     * @return          page of orders
     */
    @Query(value = "SELECT * " +
            "FROM \"order\" " +
            "WHERE status = ?1 AND is_deleted = false " +
            "ORDER BY CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END, created_at DESC",
            countQuery = "SELECT count(*) " +
                    "FROM \"order\" " +
                    "WHERE status = ?1",
            nativeQuery = true)
    Page<Order> findAllByStatus(String status, Pageable pageable);

    /**
     * Find all orders by user id and status, order by status and then created_at desc
     *
     * @param userId    user id
     * @param status    status
     * @param pageable  pageable
     * @return          page of orders
     */
    @Query(value = "SELECT * " +
            "FROM \"order\" " +
            "WHERE user_id = ?1 AND status = ?2 AND is_deleted = false " +
            "ORDER BY CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END, created_at DESC",
            countQuery = "SELECT count(*) " +
                    "FROM \"order\" " +
                    "WHERE user_id = ?1 AND status = ?2",
            nativeQuery = true)
    Page<Order> findAllByUserIdAndStatus(UUID userId, String status, Pageable pageable);

    /**
     * Find all orders by user id, order by status and then created_at desc
     *
     * @param userId    user id
     * @param pageable  pageable
     * @return          page of orders
     */
    @Query(value = "SELECT * " +
            "FROM \"order\" " +
            "WHERE user_id = ?1 AND is_deleted = false " +
            "ORDER BY CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END, created_at DESC",
            countQuery = "SELECT count(*) " +
                    "FROM \"order\" " +
                    "WHERE user_id = ?1 AND is_deleted = false",
            nativeQuery = true)
    Page<Order> findAllByUserId(UUID userId, Pageable pageable);

    /** Find order by user id and campaign id
     *
     * @param userId UUID of user
     * @param campaignId UUID of campaign
     * @return Order
     */
    List<Order> findByUserIdAndCampaignId(UUID userId, UUID campaignId);

    @Query("SELECT COUNT(o) " +
            "FROM Order o JOIN o.campaign c " +
            "WHERE c.product.id = :productId AND o.isDeleted = false AND o.status = 'PENDING'")
    long countPendingOrdersByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.campaign.id = :campaignId AND o.isDeleted = false")
    long countByCampaignIdAndIsDeletedFalse(@Param("campaignId") UUID campaignId);
}