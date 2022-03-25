package hello.upload.controller;

import hello.upload.domain.FileStore;
import hello.upload.domain.Item;
import hello.upload.domain.ItemRepository;
import hello.upload.domain.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form,
                           RedirectAttributes redirectAttributes) throws IOException {
        // 업로드 요청 한 파일과 이미지를 가져온다.
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        // 도메인 객체로 변환해 DB에 저장한다.
        Item item = new Item();
        item.setItemName(form.getItemName());
        // 사실 파일은 DB가 아니라 스토어 서비스에 저장한다. DB에 저장하는 건 파일을 저장한 곳의 상대 경로다.
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);

        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());
        return "redirect:/items/{itemId}";
    }

    // 고객이 업로드 한 파일 리스트를 보여준다.
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);

        return "item-view";
    }

    // 파일 리스트에서 이미지를 보여줄 떄 HTML img 태그에 넣을 이미지 주소를 반환한다.
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        // UrlResource로 파일을 읽어서 @ResponseBody로 이미지 바이너리를 반환한다.
        // fullPath로 전체 경로 /Users/... 를 가져온다.
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    // 파일을 다운로드 할 때 사용한다.
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        // itemId로 요청을 받으면 접근 권한을 체크하는 등의 로직을 추가할 수 있다.
        Item item = itemRepository.findById(itemId);

        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        // 실제 다운로드 받으려면 서버에 저장된 이름으로 가져와야 한다.
        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));
        log.info("uploadFileName={}", uploadFileName);

        // 화면에 파일 이름을 노출할 때는 고객이 업로드 할 때 사용했던 이름을 사용한다.
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        // content-disposition 헤더를 추가하지 않으면 다운로드 하려고 파일명을 누르면
        // 다운로드가 되지 않고 열기로 작동해 파일 내용이 화면에 노출된다.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
