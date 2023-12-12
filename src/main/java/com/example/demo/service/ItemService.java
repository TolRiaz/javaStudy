package com.example.demo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ItemFormDto;
import com.example.demo.dto.ItemImgDto;
import com.example.demo.dto.ItemSearchDto;
import com.example.demo.dto.MainItemDto;
import com.example.demo.entity.Item;
import com.example.demo.entity.ItemImg;
import com.example.demo.repository.ItemImgRepository;
import com.example.demo.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {

    private final ItemRepository    itemRepository;
    private final ItemImgService    itemImgService;
    private final ItemImgRepository itemImgRepository;

    // 상품 등록
    public Long SaveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        // 상품등록 (1번)
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        // 이미지 등록(2번, 순서중요)
        for (int i = 0; i < itemImgFileList.size(); i++) {
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            if (i==0) {
                itemImg.setRepImgYn("Y");
            } else {
                itemImg.setRepImgYn("N");
            }
            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }

        return item.getId();
    }

    // 상품 관리 페이지 상품 목록 조회
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    // 상품 조회
    @Transactional(readOnly = true)
    public ItemFormDto getItemDetail(Long itemId) {

        // 상품 이미지 엔티티들을 itemImgDto 객체로 변환하여 itemImgDtoList에 담음
        List<ItemImg>    itemImgList    = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();

        for (ItemImg itemImg : itemImgList) {
            ItemImgDto itemImgDto = ItemImgDto.of(itemImg);
            itemImgDtoList.add(itemImgDto);
        }

        // 상품 엔티니를 ItemFormDto 객체로 변환하고, itemImgDtoList 멤버 변수를 초기화
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);

        return itemFormDto;
    }

    // 상품 수정
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws IOException {

        // 상품 수정
        Item item = itemRepository.findById(itemFormDto.getId()).orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);

        // 상품 이미지 수정
        List<Long> itemImgIds = itemFormDto.getItemImgIds();
        for (int i=0; i < itemImgFileList.size(); i++) {
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }

        return item.getId();
    }

    // 메인 페이지 상품 목록 조회
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }
}
