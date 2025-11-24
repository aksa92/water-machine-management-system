package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_terminal_mapping")
public class DeviceTerminalMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Integer mappingId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "terminal_id", length = 20)
    private String terminalId;

    @Column(name = "terminal_name", length = 100)
    private String terminalName;

    @Enumerated(EnumType.STRING)
    @Column(name = "terminal_status", length = 50)
    private TerminalStatus terminalStatus;

    @Column(name = "install_date")
    private LocalDate installDate;

    public enum TerminalStatus {
        active, inactive
    }
}