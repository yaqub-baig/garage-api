package com.autoadda.apis.garage.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.service.VehicleReceivedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.entity.VehicleReceived;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.VehicleRecievedMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.repository.VehicleReceivedRepository;
import com.autoadda.apis.garage.request.VehicleReceivedRequest;
import com.autoadda.apis.garage.response.VehicleRecievedResponse;

@ExtendWith(MockitoExtension.class)
class VehicleReceivedServiceTest {

	@InjectMocks
	private VehicleReceivedService vehicleReceivedService;

	@Mock
	private VehicleReceivedRepository vehicleReceivedRepository;

	@Mock
	private AppointmentDetailsRepository appointmentRepository;

	@Mock
	private TechnicianRepository technicianRepository;

	@Mock
	private MediaFileRepository mediaFileRepository;

	@Mock
	private VehicleRecievedMapper vehicleRecievedMapper;

	private VehicleReceivedRequest request;
	private AppointmentDetails appointment;
	private Technician technician;
	private VehicleReceived vehicleReceived;
	private VehicleRecievedResponse response;
	private List<MediaFile> mediaFiles;

	@BeforeEach
	void setup() {
		appointment = new AppointmentDetails ();
		appointment.setId(1);

		technician = new Technician();
		technician.setId(1);

		MediaFile mediaFile1 = new MediaFile();
		mediaFile1.setId(1L);
		MediaFile mediaFile2 = new MediaFile();
		mediaFile2.setId(2L);
		mediaFiles = Arrays.asList(mediaFile1, mediaFile2);

		request = new VehicleReceivedRequest();
		request.setAppointmentId(1);
		request.setReceiverId(1);
		request.setComment("Test comment");
		request.setVehicleImages( Arrays.asList(1L, 2L));
		request.setIsMainKeyReceived(true);
		request.setIsSpareKeyReceived(false);
		request.setIsToolKitReceived(true);
		request.setIsJackReceived(null);
		request.setIsWheelSpannerReceived(false);
		request.setIsSpareTyreReceived(true);
		request.setIsFireExtinguisherReceived(null);
		request.setIsFirstAidKitReceived(false);
		request.setIsCentralLockRemoteReceived(true);
		request.setIsRCBookReceived(null);
		request.setIsInsuranceCopyReceived(false);
		request.setIsPUCReceived(true);

		vehicleReceived = new VehicleReceived();
		vehicleReceived.setId(1);
		vehicleReceived.setAppointment(appointment);
		vehicleReceived.setTechnician(technician);
		vehicleReceived.setComment("Test comment");
		vehicleReceived.setVehiclePhotos(mediaFiles);

		response = new VehicleRecievedResponse();
		response.setId(1);
		response.setComment("Test comment");
	}

	@Test
	void receiveVehicle_shouldReturnSavedVehicleReceived() {
		when(appointmentRepository.findById(1)).thenReturn(Optional.of(appointment));
		when(technicianRepository.findById(1)).thenReturn(Optional.of(technician));
		when(mediaFileRepository.findAllById(anyList())).thenReturn(mediaFiles);
		when(vehicleReceivedRepository.save(any(VehicleReceived.class))).thenReturn(vehicleReceived);
		when(vehicleRecievedMapper.entityToResponse(any(VehicleReceived.class))).thenReturn(response);

		VehicleRecievedResponse result = vehicleReceivedService.receiveVehicle(request);

		assertNotNull(result);
		assertEquals(1, result.getId());
		assertEquals("Test comment", result.getComment());
		verify(appointmentRepository, times(1)).findById(1);
		verify(technicianRepository, times(1)).findById(1);
		verify(mediaFileRepository, times(1)).findAllById(Arrays.asList(1L, 2L));
		verify(vehicleReceivedRepository, times(1)).save(any(VehicleReceived.class));
	}

	@Test
	void receiveVehicle_appointmentNotFound() {
		when(appointmentRepository.findById(1)).thenReturn(Optional.empty());

		Exception ex = assertThrows(ResourceNotFoundException.class,
				() -> vehicleReceivedService.receiveVehicle(request));
		assertEquals("appointment not found", ex.getMessage());

		verify(appointmentRepository, times(1)).findById(1);
		verify(technicianRepository, never()).findById(anyInt());
	}

	@Test
	void receiveVehicle_technicianNotFound() {
		when(appointmentRepository.findById(1)).thenReturn(Optional.of(appointment));
		when(technicianRepository.findById(1)).thenReturn(Optional.empty());

		Exception ex = assertThrows(ResourceNotFoundException.class,
				() -> vehicleReceivedService.receiveVehicle(request));
		assertEquals("technician not found", ex.getMessage());

		verify(appointmentRepository, times(1)).findById(1);
		verify(technicianRepository, times(1)).findById(1);
	}

	@Test
	void receiveVehicle_withoutVehicleImages() {
		request.setVehicleImages(null);

		when(appointmentRepository.findById(1)).thenReturn(Optional.of(appointment));
		when(technicianRepository.findById(1)).thenReturn(Optional.of(technician));
		when(vehicleReceivedRepository.save(any(VehicleReceived.class))).thenReturn(vehicleReceived);
		when(vehicleRecievedMapper.entityToResponse(any(VehicleReceived.class))).thenReturn(response);

		VehicleRecievedResponse result = vehicleReceivedService.receiveVehicle(request);

		assertNotNull(result);
		verify(mediaFileRepository, never()).findAllById(anyList());
	}

	@Test
	void receiveVehicle_withEmptyVehicleImages() {
		request.setVehicleImages(Arrays.asList());

		when(appointmentRepository.findById(1)).thenReturn(Optional.of(appointment));
		when(technicianRepository.findById(1)).thenReturn(Optional.of(technician));
		when(vehicleReceivedRepository.save(any(VehicleReceived.class))).thenReturn(vehicleReceived);
		when(vehicleRecievedMapper.entityToResponse(any(VehicleReceived.class))).thenReturn(response);

		VehicleRecievedResponse result = vehicleReceivedService.receiveVehicle(request);

		assertNotNull(result);
		verify(mediaFileRepository, never()).findAllById(anyList());
	}

	@Test
	void getVehicleReceivedDetails_validId() {
		when(vehicleReceivedRepository.findById(1)).thenReturn(Optional.of(vehicleReceived));
		when(vehicleRecievedMapper.entityToResponse(vehicleReceived)).thenReturn(response);

		VehicleRecievedResponse result = vehicleReceivedService.getVehicleReceivedDetails(1);

		assertNotNull(result);
		assertEquals(1, result.getId());
		verify(vehicleReceivedRepository, times(1)).findById(1);
	}

	@Test
	void getVehicleReceivedDetails_notFound() {
		when(vehicleReceivedRepository.findById(1)).thenReturn(Optional.empty());

		Exception ex = assertThrows(ResourceNotFoundException.class,
				() -> vehicleReceivedService.getVehicleReceivedDetails(1));
		assertEquals("Vehicle received details not found for this id: 1", ex.getMessage());

		verify(vehicleReceivedRepository, times(1)).findById(1);
	}

	@Test
	void getAllRecievedCarByGarageId_validGarageId() {
		List<VehicleReceived> vehicleReceivedList = Arrays.asList(vehicleReceived);
		when(vehicleReceivedRepository.findByAppointmentGarageId(1)).thenReturn(vehicleReceivedList);
		when(vehicleRecievedMapper.entityToResponse(any(VehicleReceived.class))).thenReturn(response);

		List<VehicleRecievedResponse> result = vehicleReceivedService.getAllRecievedCarByGarageId(1);

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(vehicleReceivedRepository, times(1)).findByAppointmentGarageId(1);
	}

	@Test
	void updateVehicleReceived_validId() {
		when(vehicleReceivedRepository.findById(1)).thenReturn(Optional.of(vehicleReceived));
		when(technicianRepository.findById(1)).thenReturn(Optional.of(technician));
		when(mediaFileRepository.findAllById(anyList())).thenReturn(mediaFiles);
		when(vehicleReceivedRepository.save(any(VehicleReceived.class))).thenReturn(vehicleReceived);
		when(vehicleRecievedMapper.entityToResponse(any(VehicleReceived.class))).thenReturn(response);

		VehicleRecievedResponse result = vehicleReceivedService.updateVehicleReceived(1, request);

		assertNotNull(result);
		verify(vehicleReceivedRepository, times(1)).findById(1);
		verify(technicianRepository, times(1)).findById(1);
		verify(vehicleReceivedRepository, times(1)).save(any(VehicleReceived.class));
	}

	@Test
	void updateVehicleReceived_notFound() {
		when(vehicleReceivedRepository.findById(1)).thenReturn(Optional.empty());

		Exception ex = assertThrows(ResourceNotFoundException.class,
				() -> vehicleReceivedService.updateVehicleReceived(1, request));
		assertEquals("Vehicle received details not found for this id: 1", ex.getMessage());

		verify(vehicleReceivedRepository, times(1)).findById(1);
		verify(vehicleReceivedRepository, never()).save(any(VehicleReceived.class));
	}

	@Test
	void updateVehicleReceived_technicianNotFound() {
		when(vehicleReceivedRepository.findById(1)).thenReturn(Optional.of(vehicleReceived));
		when(technicianRepository.findById(1)).thenReturn(Optional.empty());

		Exception ex = assertThrows(ResourceNotFoundException.class,
				() -> vehicleReceivedService.updateVehicleReceived(1, request));
		assertEquals("technician not found", ex.getMessage());

		verify(vehicleReceivedRepository, times(1)).findById(1);
		verify(technicianRepository, times(1)).findById(1);
		verify(vehicleReceivedRepository, never()).save(any(VehicleReceived.class));
	}

	@Test
	void updateVehicleReceived_withoutComment() {
		request.setComment(null);

		when(vehicleReceivedRepository.findById(1)).thenReturn(Optional.of(vehicleReceived));
		when(technicianRepository.findById(1)).thenReturn(Optional.of(technician));
		when(vehicleReceivedRepository.save(any(VehicleReceived.class))).thenReturn(vehicleReceived);
		when(vehicleRecievedMapper.entityToResponse(any(VehicleReceived.class))).thenReturn(response);

		VehicleRecievedResponse result = vehicleReceivedService.updateVehicleReceived(1, request);

		assertNotNull(result);
		verify(vehicleReceivedRepository, times(1)).save(any(VehicleReceived.class));
	}

	@Test
	void getBooleanValue_withNull() {
		Boolean result = vehicleReceivedService.getBooleanValue(null);
		assertEquals(Boolean.FALSE, result);
	}

	@Test
	void getBooleanValue_withTrue() {
		Boolean result = vehicleReceivedService.getBooleanValue(true);
		assertEquals(Boolean.TRUE, result);
	}

	@Test
	void getBooleanValue_withFalse() {
		Boolean result = vehicleReceivedService.getBooleanValue(false);
		assertEquals(Boolean.FALSE, result);
	}
}