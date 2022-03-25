package hello.upload.domain;

import lombok.Data;

@Data
public class UploadFile {

    // 업로드 한 파일 이름
    // 나중에 고객이 업로드 한 파일 리스트를 보여줄 때 출력한다.
    private String uploadFileName;

    // 시스템에 올린 파일 이름
    // 사용자들이 같은 이름으로 파일을 올릴 수 있기 때문에 둘을 구분한다.
    private String storeFileName;

    public UploadFile(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
}
