package me.nuguri.resc.entity;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    protected LocalDateTime created = LocalDateTime.now();

    protected LocalDateTime updated;

    protected LocalDateTime deleted;

}
