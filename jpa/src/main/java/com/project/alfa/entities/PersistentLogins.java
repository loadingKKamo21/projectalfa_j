package com.project.alfa.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_persistent_logins")
@Getter
@Setter
public class PersistentLogins {
    
    @Id
    @Column(length = 64)
    private String series;
    
    @Column(nullable = false, length = 64)
    private String username;
    
    @Column(nullable = false, length = 64)
    private String token;
    
    @Column(nullable = false, length = 64)
    private LocalDateTime lastUsed;
    
}
