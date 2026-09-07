package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // e.g., "AI & Computing", "Astronomy", "Culinary Science", "Renewable Energy", "Biomedicine"
    val embeddingCsv: String = "", // Comma-separated float values (64-dim local or 768-dim Gemini)
    val embeddingType: String = "LOCAL", // "LOCAL" or "GEMINI"
    val createdAt: Long = System.currentTimeMillis()
)
