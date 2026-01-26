package com.example.template.core.data.service

import com.example.template.core.data.dto.ListItemDto
import com.example.template.core.data.dto.ListResponseDto


interface ApiService {
    suspend fun getListItems(page: Int = 1, limit: Int = 10): ListResponseDto<ListItemDto>
    suspend fun getItemById(id: Int): ListItemDto
}
