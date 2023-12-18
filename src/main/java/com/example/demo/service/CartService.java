package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CartItemDto;
import com.example.demo.dto.CartListDto;
import com.example.demo.dto.CartOrderDto;
import com.example.demo.dto.OrderDto;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Item;
import com.example.demo.entity.Member;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.MemberRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderService orderService;

    // 장바구니 담기
    public Long addCart(CartItemDto cartItemDto, String email) {

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());

        // 장바구니가 존재하지 않는다면 생성
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        Item item = itemRepository.findById(cartItemDto.getItemId()).orElseThrow(EntityNotFoundException::new);
        CartItem cartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        // 해당 상품이 장바구니에 존재하지 않는다면 생성 후 추가
        if (cartItem == null) {
            cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);

        // 해당 상품이 이미 장바구니에 존재한다면 수량을 추가
        } else {
            cartItem.addCount(cartItemDto.getCount());
        }

        return cartItem.getId();
    }

    // 장바구니 조회
    @Transactional(readOnly = true)
    public List<CartListDto> getCartList(String email) {

        List<CartListDto> cartListDtos = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Cart   cart   = cartRepository.findByMemberId(member.getId());

        if (cart == null) {
            return cartListDtos;
        }

        cartListDtos = cartItemRepository.findCartListDto(cart.getId());
        return cartListDtos;
    }

    // 현재 로그인한 사용자가 장바구니의 주인인지 확인
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {

        // 현재 로그인된 사용자
        Member currMember = memberRepository.findByEmail(email);

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        Member savedMember = cartItem.getCart().getMember();

        if (StringUtils.equals(currMember.getEmail(), savedMember.getEmail())) {
            return true;
        } else {
            return false;
        }

    }

    // 장바구니 상품 수량 변경
    public void updateCartItemCount(Long cartItemId, int count) {

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItem.updateCount(count);

    }

    // 장바구니 상품 삭제
    public void deleteCartItem(Long cartItemId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);

    }

    // 장바구니 상품(들) 주문
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {

        List<OrderDto> orderDtoList = new ArrayList<>();

        for (CartOrderDto cartOrderDto : cartOrderDtoList ) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId()).orElseThrow(EntityNotFoundException::new);
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, email);

        return orderId;
    }

}
