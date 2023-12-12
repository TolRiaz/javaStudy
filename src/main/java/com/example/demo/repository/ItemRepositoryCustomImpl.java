package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.demo.constatnt.ItemSellStatus;
import com.example.demo.dto.ItemSearchDto;
import com.example.demo.dto.MainItemDto;
import com.example.demo.dto.QMainItemDto;
import com.example.demo.entity.Item;
import com.example.demo.entity.QItem;
import com.example.demo.entity.QItemImg;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private JPAQueryFactory queryFactory;

    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if (StringUtils.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if (StringUtils.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        } else if (StringUtils.equals("1w", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        } else if (StringUtils.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        } else if (StringUtils.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(6);
        }

        return QItem.item.regTime.after(dateTime);
    }

    // 상품 상태에 대한 조회 조건
    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    // 상품명 또는 등록자 아이디에 대한 조회 조건 BooleanExpression
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if (StringUtils.equals("itemName", searchQuery)) {
            return QItem.item.itemName.like(getLikeQuery(searchQuery));
        } else if (StringUtils.equals("createdBy", searchBy)) {
            return QItem.item.createdBy.like(getLikeQuery(searchQuery));
        }

        return null;
    }

    private BooleanExpression itemNameLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemName.like(getLikeQuery(searchQuery));
    }

    private String getLikeQuery(String query) {
        return "%" + query + "%";
    }


    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        // queryFactory를 이용하여 쿼리문 생성
        QueryResults<Item> results = queryFactory
            .selectFrom(QItem.item)
            // 위에서 만든 BooleanExpression 함수들을 이용
            .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                    searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                    searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
            .orderBy(QItem.item.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();        // 2번의 select 문 실행; Depreacted 됨

            // 조회 대상 리스트 결과
            List<Item> content = results.getResults();

            // 조회 대상 리스트의 개수(count)
            long total = results.getTotal();

            // Page 인터페이스를 구현한 PageImpl 객체 반환
            return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        QItem    item    = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
            .select(
                new QMainItemDto(
                    item.id,
                    item.itemName,
                    item.itemDetail,
                    itemImg.imgUrl,
                    item.price)
            )
            .from(itemImg)
            .join(itemImg.item, item)
            .where(itemImg.repImgYn.eq("Y"))
            .where(itemNameLike(itemSearchDto.getSearchQuery()))
            .orderBy(item.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

}
