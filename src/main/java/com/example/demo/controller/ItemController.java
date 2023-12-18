package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.demo.dto.ItemFormDto;
import com.example.demo.dto.ItemSearchDto;
import com.example.demo.entity.Item;
import com.example.demo.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;




@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 상품 등록 페이지
    @GetMapping(value = "/admin/item/new")
    public String itemForm(ItemFormDto itemFormDto, Model model) {

        model.addAttribute("itemFormDto", itemFormDto);

        return "item/itemForm";
    }

    // 상품 등록
    @PostMapping(value = "/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                          @RequestParam(name = "itemImgFile") List<MultipartFile> itemImgFileList) {

        // Vaild check
        if (bindingResult.hasErrors()) { return "item/itemForm"; }

        // 첫번째 이미지 존재 여부 확인
        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");

            return "item/itemForm";
        }

        try {
            itemService.SaveItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }

    // 상품 관리 페이지
    @GetMapping(value = {"/admin/items", "/admin/items/{page}"})
    public String itemManage(ItemSearchDto itemSearchDto, @PathVariable(name = "page") Optional<Integer> page, Model model) {

        // PageRequest.of() 메소드를 통해 Pageable 객체 생성
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 3);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        return "item/itemMng";
    }
    
    // 상품 수정 페이지
    @GetMapping(value = "/admin/item/{itemId}")
    public String itemDetail(@PathVariable(name = "itemId") Long itemId, Model model) {

        try {
            ItemFormDto itemFormDto = itemService.getItemDetail(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
            model.addAttribute("itemFormDto", new ItemFormDto());
        }

        return "item/itemForm";
    }

    // 상품 수정
    @PostMapping(value = "/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model,
                             @RequestParam(name = "itemImgFile") List<MultipartFile> itemImgFileList) {
        
        if (bindingResult.hasErrors()) { return "item/itemForm"; }

        if (itemImgFileList.get(0).isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");

            return "item/itemForm";
        }

        try {
            itemService.updateItem(itemFormDto, itemImgFileList);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }

    // 상품 상세 페이지
    @GetMapping(value = "/item/{itemId}")
    public String itemDetail(Model model, @PathVariable("itemId") Long itemId) {
        ItemFormDto itemFormDto = itemService.getItemDetail(itemId);
        model.addAttribute("item", itemFormDto);

        return "item/itemDetail";
    }
    

}
