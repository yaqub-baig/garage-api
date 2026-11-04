package com.autoadda.apis.garage.util;

import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.MediaFileMapper;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.response.MediaFileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Shared support for voice notes attached to note / comment / remark fields.
 * <p>
 * Flow: the client uploads the recording via {@code POST /mediafiles/upload}, then sends the
 * returned media file id as {@code ...VoiceNoteId} alongside the text note.
 */
@Component
@RequiredArgsConstructor
public class VoiceNoteHelper {

    private static final String AUDIO_FOLDER = "audios/";

    private final MediaFileRepository mediaFileRepository;
    private final MediaFileMapper mediaFileMapper;

    @Value("${file.base-url}")
    private String baseUrl;

    /**
     * Loads the uploaded audio file for the given id.
     *
     * @return the media file, or {@code null} when {@code voiceNoteId} is null
     * @throws ResourceNotFoundException when no file exists with that id
     * @throws BadRequestException       when the file is not an audio recording
     */
    public MediaFile resolve(Long voiceNoteId) {
        if (voiceNoteId == null) {
            return null;
        }
        MediaFile file = mediaFileRepository.findById(voiceNoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Voice note not found with id: " + voiceNoteId));
        if (file.getFilePath() == null || !file.getFilePath().startsWith(AUDIO_FOLDER)) {
            throw new BadRequestException("Media file " + voiceNoteId + " is not an audio recording");
        }
        return file;
    }

    public MediaFileResponse toResponse(MediaFile voiceNote) {
        if (voiceNote == null) {
            return null;
        }
        return mediaFileMapper.toResponse(voiceNote, baseUrl + "/uploads/" + voiceNote.getFilePath());
    }
}
