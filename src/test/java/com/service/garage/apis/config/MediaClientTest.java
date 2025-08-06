//package com.service.garage.apis.config;
//
//import com.service.garage.apis.response.GenericResponse;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class MediaClientTest {
//
//    @Mock
//    private MediaClient mediaClient;
//
//    @Test
//    void getMediaFile_shouldReturnMediaFile() {
//        // Arrange
//        Long mediaFileId = 1L;
//        GenericResponse expectedResponse = new GenericResponse ( );
//        expectedResponse.setStatus ( "SUCCESS" );
//        expectedResponse.setMessage ( "Media file retrieved successfully" );
//        expectedResponse.setBody ( "Sample media data" );
//
//        when ( mediaClient.get ( anyLong ( ) ) ).thenReturn ( expectedResponse );
//
//        // Act
//        GenericResponse actualResponse = mediaClient.get ( mediaFileId );
//
//        // Assert
//        assertNotNull ( actualResponse );
//        assertEquals ( "SUCCESS", actualResponse.getStatus ( ) );
//        assertEquals ( "Media file retrieved successfully", actualResponse.getMessage ( ) );
//        assertEquals ( "Sample media data", actualResponse.getBody ( ) );
//    }
//
//    @Test
//    void getMediaFile_withInvalidId_shouldReturnError() {
//        // Arrange
//        Long invalidId = - 1L;
//        when ( mediaClient.get ( invalidId ) ).thenThrow ( new RuntimeException ( "Media file not found" ) );
//
//        // Act & Assert
//        assertThrows ( RuntimeException.class, () -> mediaClient.get ( invalidId ) );
//    }
//}