package com.example.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MonsterRepository : JpaRepository<MonsterEntity, Int>