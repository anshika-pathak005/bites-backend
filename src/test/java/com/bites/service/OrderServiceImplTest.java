package com.bites.service;

import com.bites.entity.Order;
import com.bites.entity.Restaurant;
import com.bites.entity.enums.OrderStatus;
import com.bites.exception.BadRequestException;
import com.bites.exception.ResourceNotFoundException;
import com.bites.repository.*;
import com.bites.service.impl.OrderServiceImpl;
import com.bites.util.OrderNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private CartService cartService;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private DeliveryPricingService deliveryPricingService;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                paymentRepository,
                cartRepository,
                cartItemRepository,
                cartService,
                menuItemRepository,
                deliveryPricingService,
                restaurantRepository,
                customerService,
                orderNumberGenerator
        );
    }

    @Test
    void shouldCancelPendingOrderWithinWindow() {
        String orderNumber = "BTS-2026-0001";
        Order order = Order.builder()
                .id(1L)
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .placedAt(LocalDateTime.now().minusMinutes(2))
                .build();

        Restaurant restaurant = Restaurant.builder()
                .cancellationWindowMinutes(5)
                .build();

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var payment = mock(com.bites.entity.Payment.class);
        when(payment.getMethod()).thenReturn(com.bites.entity.enums.PaymentMethod.COD);
        when(payment.getStatus()).thenReturn(com.bites.entity.enums.PaymentStatus.PENDING);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        var response = orderService.cancel(orderNumber);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectCancellationWhenWindowExpired() {
        String orderNumber = "BTS-2026-0002";
        Order order = Order.builder()
                .id(2L)
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .placedAt(LocalDateTime.now().minusMinutes(10))
                .build();

        Restaurant restaurant = Restaurant.builder()
                .cancellationWindowMinutes(5)
                .build();

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderService.cancel(orderNumber));
        assertTrue(ex.getMessage().contains("cancellation window"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRejectCancellationWhenAlreadyAccepted() {
        String orderNumber = "BTS-2026-0003";
        Order order = Order.builder()
                .id(3L)
                .orderNumber(orderNumber)
                .status(OrderStatus.ACCEPTED)
                .placedAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderService.cancel(orderNumber));
        assertTrue(ex.getMessage().contains("can no longer be cancelled"));
    }
}
