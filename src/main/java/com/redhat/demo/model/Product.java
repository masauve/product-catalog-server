package com.redhat.demo.model;

import java.sql.Date;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "products")
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer id;

    @Column(length = 128)
    @NotNull
    public String name;

    @Column
    @NotNull
    public Double price;

    @Column
    public String description;

    @Column
    public Date created;

    @Column
    public LocalDateTime modified;

    @OneToOne(targetEntity = Category.class, fetch = FetchType.EAGER)
    @JoinColumn(name="category_id")
    @NotNull
    public Category category;

    public Product() {

    }

    public Product(Integer id, String name, Double price, String description, Category category, Date created, LocalDateTime modified) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.created = created;
        this.modified = modified;
    }
}