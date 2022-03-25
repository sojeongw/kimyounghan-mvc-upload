package hello.upload.controller;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ItemForm {

    private Long itemId;
    private String itemName;

    // 요구 사항에서 이미지는 여러 개를 첨부할 수 있으므로 MultipartFile를 리스트로 받는다.
    private List<MultipartFile> imageFiles;
    private MultipartFile attachFile;

}
