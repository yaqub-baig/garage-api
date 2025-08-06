package com.service.garage.apis.service;


import com.service.garage.apis.entity.MediaFile;
import com.service.garage.apis.response.GenericResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaFileService {

    List<MediaFile> upload(List<MultipartFile> files) throws IOException;

    GenericResponse delete(Long mediaFileId);

    GenericResponse getMediaFileById(Long mediaFileId);
}
