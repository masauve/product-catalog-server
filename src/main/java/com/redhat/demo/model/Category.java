package com.redhat.demo.model;

import java.sql.Date;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;


@Entity
@Table(name = "categories")
public class Category extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @NotNull
    public Integer id;

    @Column(length = 128)
    @NotNull
    public String name;

    @Column
    public String description;

    @Column
    public Date created;

    @Column
    public LocalDateTime modified;

    public Category() {

    }

    public Category(Integer id, String name, String description, Date created, LocalDateTime modified) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.modified = modified;
    }
}