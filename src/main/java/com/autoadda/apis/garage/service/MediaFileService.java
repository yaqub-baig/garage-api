package com.autoadda.apis.garage.service;


import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.MediaFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaFileService {

    List<MediaFile> upload(List<MultipartFile> files) throws IOException;

    GenericResponse delete(Long mediaFileId);

    GenericResponse getMediaFileById(Long mediaFileId);
}
