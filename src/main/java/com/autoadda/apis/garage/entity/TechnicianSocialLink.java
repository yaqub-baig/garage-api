package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.SocialPlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "technician_social_link", indexes = @Index(name = "idx_tech_social_link_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianSocialLink extends TechnicianProfileItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 30)
    private SocialPlatform platform;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "handle")
    private String handle;
}
