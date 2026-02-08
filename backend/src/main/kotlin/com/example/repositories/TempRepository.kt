package com.example.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TempRepository : JpaRepository<TempEntity, Int>
