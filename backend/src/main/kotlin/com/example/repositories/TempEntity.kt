package com.example.repositories

import jakarta.persistence.*

@Entity
data class TempEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int? = null,
    @Column(nullable = false) val name: String? = "-",
)