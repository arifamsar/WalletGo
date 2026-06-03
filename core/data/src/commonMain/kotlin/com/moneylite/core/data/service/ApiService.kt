package com.moneylite.core.data.service

import com.moneylite.core.data.dto.ListItemDto
import com.moneylite.core.data.dto.ListResponseDto


interface ApiService {
    suspend fun getListItems(page: Int = 1, limit: Int = 10): ListResponseDto<ListItemDto>
    suspend fun getItemById(id: Int): ListItemDto
}
