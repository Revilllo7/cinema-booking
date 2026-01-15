package com.cinema.repository;

import com.cinema.dto.AdminBookingDTO;
import com.cinema.dto.AdminBookingFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class AdminBookingRepository {

    private static final String SELECT_CLAUSE =
        "SELECT new com.cinema.dto.AdminBookingDTO(" +
            "b.id, b.bookingNumber, b.createdAt, b.status, b.totalPrice, b.paymentMethod," +
            "b.customerEmail, b.customerPhone," +
            "u.id, u.username, u.email," +
            "m.id, m.title," +
            "s.id, s.startTime, s.endTime, h.name," +
            "(SELECT COUNT(bs.id) FROM BookingSeat bs WHERE bs.booking.id = b.id)" +
        ") ";

    private static final String FROM_CLAUSE =
        "FROM Booking b " +
        "JOIN b.user u " +
        "JOIN b.screening s " +
        "JOIN s.movie m " +
        "JOIN s.hall h ";

    private static final Map<String, String> SORT_COLUMNS = Map.of(
        "createdAt", "b.createdAt",
        "status", "b.status",
        "totalPrice", "b.totalPrice",
        "screeningStartTime", "s.startTime",
        "movieTitle", "m.title",
        "username", "u.username"
    );

    @PersistenceContext
    private EntityManager entityManager;

    public Page<AdminBookingDTO> findBookings(AdminBookingFilter filter, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        String whereClause = buildWhereClause(filter, params);
        String orderClause = buildOrderClause(pageable.getSort());

        String queryString = SELECT_CLAUSE + FROM_CLAUSE + whereClause + orderClause;
        TypedQuery<AdminBookingDTO> query = entityManager.createQuery(queryString, AdminBookingDTO.class);
        applyParameters(query, params);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<AdminBookingDTO> content = query.getResultList();

        String countQueryString = "SELECT COUNT(b) " + FROM_CLAUSE + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryString, Long.class);
        applyParameters(countQuery, params);
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private String buildWhereClause(AdminBookingFilter filter, Map<String, Object> params) {
        List<String> clauses = new ArrayList<>();

        if (filter != null) {
            if (filter.getStatus() != null) {
                clauses.add("b.status = :status");
                params.put("status", filter.getStatus());
            }
            if (filter.getStartDate() != null) {
                clauses.add("b.createdAt >= :startDate");
                params.put("startDate", filter.getStartDate());
            }
            if (filter.getEndDate() != null) {
                clauses.add("b.createdAt <= :endDate");
                params.put("endDate", filter.getEndDate());
            }
            if (StringUtils.hasText(filter.getSearch())) {
                String search = filter.getSearch().trim().toLowerCase(Locale.ROOT);
                clauses.add("(LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search OR LOWER(m.title) LIKE :search OR LOWER(b.bookingNumber) LIKE :search)");
                params.put("search", "%" + search + "%");
            }
        }

        if (clauses.isEmpty()) {
            return "";
        }
        return " WHERE " + String.join(" AND ", clauses) + " ";
    }

    private String buildOrderClause(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return " ORDER BY b.createdAt DESC";
        }

        String clause = sort.stream()
            .map(order -> {
                String column = SORT_COLUMNS.getOrDefault(order.getProperty(), "b.createdAt");
                return column + (order.isAscending() ? " ASC" : " DESC");
            })
            .collect(Collectors.joining(", "));

        return " ORDER BY " + clause;
    }

    private void applyParameters(TypedQuery<?> query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }
}
