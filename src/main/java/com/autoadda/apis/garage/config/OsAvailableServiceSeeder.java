package com.autoadda.apis.garage.config;

import com.autoadda.apis.garage.entity.os.OsAvailableService;
import com.autoadda.apis.garage.repository.os.OsAvailableServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OsAvailableServiceSeeder {

    @Bean
    CommandLineRunner initOsAvailableServices(OsAvailableServiceRepository repository) {
        return args -> {
            List<OsAvailableService> defaultServices = List.of(
                    new OsAvailableService(null, "Tyre & Balancing", "", "https://img.freepik.com/premium-photo/png-three-automobile-tires-mag-wheel-vehicle-spoke-car_53876-752756.jpg?w=2000"),
                    new OsAvailableService(null, "Suspension", "", "https://images.squarespace-cdn.com/content/v1/57a3f72d4402439bb069fed6/1525196667874-HEXAJBHJ48GV5EHCR2GK/car-suspension.png"),
                    new OsAvailableService(null, "Battery", "","https://cdn3d.iconscout.com/3d/premium/thumb/car-battery-3d-illustration-download-in-png-blend-fbx-gltf-file-formats--charging-accumulator-energy-electricity-pack-appliances-illustrations-3535935.png?f=webp"),
                    new OsAvailableService(null, "Recovery", "","https://resources.servicemycar.com/upimages/service-content/Car-recovery-Dubai-20221222040011.png"),
                    new OsAvailableService(null,"Insurance","","https://cdn3d.iconscout.com/3d/premium/thumb/car-insurance-3d-icon-download-in-png-blend-fbx-gltf-file-formats--automobile-vehicle-claim-pack-services-icons-7767112.png" ),
                    new OsAvailableService(null, "Car Inspection", "", "https://cdn3d.iconscout.com/3d/premium/thumb/car-insurance-3d-icon-download-in-png-blend-fbx-gltf-file-formats--auto-coverage-planning-vehicle-protection-security-financial-pack-business-icons-7994011.png?f=webp"),
                    new OsAvailableService(null, "Quality Control", "","https://cdn3d.iconscout.com/3d/premium/thumb/quality-assurance-3d-icon-download-in-png-blend-fbx-gltf-file-formats--check-report-management-digital-skills-pack-future-technology-icons-5873653.png?f=webp"),
                    new OsAvailableService(null,"Engineering Work","","https://cdn3d.iconscout.com/3d/premium/thumb/automotive-parts-3d-icon-download-in-png-blend-fbx-gltf-file-formats--futuristic-line-engineering-factory-pack-industry-icons-10411102.png?f=webp"),
                    new OsAvailableService(null,"Hydraulic Hose","","https://hydrohose.ae/wp-content/uploads/2023/11/Untitled-2380-x-1329-px-1024x572.png"),
                    new OsAvailableService(null,"Part Repairing","","https://png.pngtree.com/png-vector/20240322/ourmid/pngtree-car-maintenance-and-repairing-center-services-png-image_12186512.png"),
                    new OsAvailableService(null,"Accessories","","https://png.pngtree.com/png-clipart/20230814/original/pngtree-car-service-illustration-repair-auto-picture-image_7921951.png"),
                    new OsAvailableService(null,"Upholstery","","https://cdn3d.iconscout.com/3d/premium/thumb/clean-upholstery-3d-icon-download-in-png-blend-fbx-gltf-file-formats--car-seat-safety-wash-pack-business-icons-10402934.png"),
                    new OsAvailableService(null,"Car Wash","","https://cdn3d.iconscout.com/3d/premium/thumb/car-wash-machine-3d-icon-download-in-png-blend-fbx-gltf-file-formats--cleaning-services-detailing-pack-vehicle-icons-8363553.png"),

                    new OsAvailableService(null,"Car Detailing","","https://img.favpng.com/4/10/17/car-wash-vector-graphics-clip-art-cleaning-png-favpng-K1usSNNsUzKtA80RaERLrTcgS.jpg"),
                    new OsAvailableService(null,"Modification","", "https://png.pngtree.com/png-vector/20241120/ourmid/pngtree-colorful-modified-car-with-artistic-graffiti-design-png-image_14499086.png"),
                    new OsAvailableService(null,"Glass Services","","https://vipautoglasstx.com/wp-content/uploads/2020/06/Auto-Glass-Repair-Katy-Tomball-Texas-1002x1024.png.webp"),
                    new OsAvailableService(null,"Oil Change","","https://cdn3d.iconscout.com/3d/premium/thumb/oil-change-3d-icon-download-in-png-blend-fbx-gltf-file-formats--barrel-container-can-car-service-pack-services-icons-10634585.png?f=webp"),

                    new OsAvailableService(null,"Fuel Services","","https://cdn3d.iconscout.com/3d/premium/thumb/petrol-station-3d-icon-download-in-png-blend-fbx-gltf-file-formats--gas-fuel-pump-mechanical-car-repair-tool-pack-service-icons-8317514.png?f=webp"),
                    new OsAvailableService(null,"EV Services","","https://corkautoservices.com/wp-content/uploads/2019/04/Electric-Car-Servicing-Cork-Auto-Services.png"),
                    new OsAvailableService(null,"Tools","","https://cdn3d.iconscout.com/3d/premium/thumb/tool-box-3d-icon-download-in-png-blend-fbx-gltf-file-formats--kit-tools-repair-labor-day-pack-industry-icons-6740546.png"),

                    new OsAvailableService(null, "Delivery", "", "https://5.imimg.com/data5/SELLER/Default/2022/7/WQ/BQ/DF/5673687/home-delivery-service.jpg")



            );

            for (OsAvailableService service : defaultServices) {
                repository.findByName(service.getName())
                        .orElseGet(() -> repository.save(service));
            }
        };
    }
}
