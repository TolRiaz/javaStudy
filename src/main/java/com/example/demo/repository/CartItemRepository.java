package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.dto.CartListDto;
import com.example.demo.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    @Query(
        """
        select new com.example.demo.dto.CartListDto ( ci.id, i.itemName, i.price, ci.count, im.imgUrl )
        from CartItem ci, ItemImg im
        join ci.item i
        where ci.cart.id = :cartId
        and im.item.id = ci.item.id
        and im.repImgYn = 'Y'
        order by ci.regTime desc
        """
    )
    List<CartListDto> findCartListDto(Long cartId);

}
