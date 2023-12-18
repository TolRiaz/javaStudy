package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.OrderDto;
import com.example.demo.dto.OrderHistDto;
import com.example.demo.dto.OrderItemDto;
import com.example.demo.entity.Item;
import com.example.demo.entity.ItemImg;
import com.example.demo.entity.Member;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.repository.ItemImgRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    @Autowired
    private final ItemRepository    itemRepository;
    private final MemberRepository  memberRepository;
    private final OrderRepository   orderRepository;
    private final ItemImgRepository itemImgRepository;

    // 단일 상품 주문
    public Long order(OrderDto orderDto, String email) {

        // OrderItem(List) 객체 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        orderItemList.add(OrderItem.createOrderItem(item, orderDto.getCount()));

        // Order 객체 생성
        Member member = memberRepository.findByEmail(email);
        Order  order  = Order.createOrder(member, orderItemList);

        // Order 객체 DB 저장 (Cascade로 인해 OrderItem 객체도 같이 저장)
        orderRepository.save(order);
        return order.getId();
    }

    // 주문 내역 조회
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {

        List<Order> orders = orderRepository.findOrders(email, pageable);
        long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();

            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }

        return new PageImpl<>(orderHistDtos, pageable, totalCount);
    }

    // 주문한 유저가 맞는지 검증
    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email) {

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        if (StringUtils.equals(order.getMember().getEmail(), email)) {
            return true;
        }

        return false;
    }

    public void orderCancel(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        order.orderCancle();
    }

    // 장바구니 상품(들) 주문
    public Long orders(List<OrderDto> orderDtoList, String email) {

        // 로그인한 유저 조회
        Member member = memberRepository.findByEmail(email);

        // orderDto 객체를 이용하여 item 객체와 count 값을 얻어낸 뒤, 이를 이용하여 OrderItem 객체(들) 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId()).orElseThrow(EntityNotFoundException::new);
            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        // Order Entity 클래스에 존재하는 createOrder 메소드로 Order 생성 및 저장
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }

}
