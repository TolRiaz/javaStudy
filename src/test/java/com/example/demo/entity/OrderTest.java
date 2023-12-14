package com.example.demo.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.constatnt.ItemSellStatus;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderTest {
    
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @PersistenceContext
    EntityManager em;

    // Create item for Test
    public Item createItem() {
        Item item = new Item();
        item.setItemName("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStock(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        return item;
    }

    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {

        // 1. Order 생성 (초기화 X)
        Order order = new Order();

        for(int i=0; i<3; i++) {
            // 2. Item 생성 및 저장 (초기화 O)
            Item item = this.createItem();
            itemRepository.save(item);

            // 3. OrderItem 생성 및 초기화
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);  // 외래키 값 지정

            // 4. Order에 OrderItem add (총 3개)
            order.getOrderItems().add(orderItem);
        }

        // 5. 3개의 OrderItem 객체를 담고있는 Order 객체 저장
        orderRepository.saveAndFlush(order);

        em.clear();     // clear 는 엔티티는 그대로 두고 내부의 데이터만 비움
                        // close 는 엔티티도 해제하여 더이상 사용할 수 없는 상태로 만듦

        List<OrderItem> savedOrderItems = orderItemRepository.findAll();
        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(3, savedOrder.getOrderItems().size());
    }

    @Autowired
    MemberRepository memberRepository;

    public Order createOrder() {
        Order order = new Order();

        for (int i=0; i<3; i++) {
            Item item = createItem();
            itemRepository.save(item);
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(10);
            orderItem.setOrderPrice(1000);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        Member member = new Member();
        memberRepository.save(member);
        order.setMember(member);
        orderRepository.save(order);

        return order;
    }

    @Test
    @DisplayName("즉시 로딩 테스트")
    public void eagerLoadingTest() {
        Order order = this.createOrder();
        Long orderItem_id = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        OrderItem orderItem = orderItemRepository.findById(orderItem_id)
                .orElseThrow(EntityNotFoundException::new);
        // System.out.println("item" + orderItem.getItem());
    }

    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest() {
        Order order = this.createOrder();
        Long orderItem_id = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        OrderItem orderItem = orderItemRepository.findById(orderItem_id)
                .orElseThrow(EntityNotFoundException::new);
        System.out.println("Order class: " + orderItem.getOrder().getClass());
        System.out.println("============================");
        orderItem.getOrder().getOrderDate();
        System.out.println("============================");

    }

}
