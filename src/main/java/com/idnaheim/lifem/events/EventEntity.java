package com.idnaheim.lifem.events;

import com.idnaheim.lifem.utilities.AuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "events")
public class EventEntity extends AuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String description;

    private Instant startDate;

    private Instant endDate;

    private String location;

    private boolean allDay;

    private String category;

}
