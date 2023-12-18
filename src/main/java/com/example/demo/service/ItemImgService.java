package com.example.demo.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.ItemImg;
import com.example.demo.repository.ItemImgRepository;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    @Value("${itemImgLocation}")     // from application.properties
    private String itemImgLocation;

    private final FileService fileService;
    private final ItemImgRepository itemImgRepository;
    
    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws IOException {

        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl  = "";

        // file upload
        if (!StringUtils.isEmpty(oriImgName)) {
            imgName = fileService.UploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
            imgUrl = itemImgLocation + imgName;
        }

        // 상품 이미지 저장
        itemImg.updateItemImg(oriImgName, imgName, imgUrl);
        itemImgRepository.save(itemImg);
    }

    // 상품 이미지 수정
    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile) throws IOException {

        if (!itemImgFile.isEmpty()) {
            ItemImg savedItemImg = itemImgRepository.findById(itemImgId).orElseThrow(EntityNotFoundException::new);

            // 기존 이미지 파일이 존재한다면 삭제
            if (!StringUtils.isEmpty(savedItemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation + "/" + savedItemImg);
            }

            String oriImgName = itemImgFile.getOriginalFilename();
            String imgName    = fileService.UploadFile(oriImgName, oriImgName, itemImgFile.getBytes());
            String imgUrl     = itemImgLocation + imgName;
            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl);
        }
    }
}
