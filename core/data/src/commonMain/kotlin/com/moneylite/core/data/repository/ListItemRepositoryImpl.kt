package com.moneylite.core.data.repository

import com.moneylite.core.data.mapper.toDomain
import com.moneylite.core.data.service.ApiService
import com.moneylite.core.domain.model.ListItem
import com.moneylite.core.domain.model.PaginatedResult
import com.moneylite.core.domain.repository.ListItemRepository

class ListItemRepositoryImpl(
    private val apiService: ApiService
) : ListItemRepository {
    
    override suspend fun getListItems(page: Int, limit: Int): PaginatedResult<ListItem> {
        val response = apiService.getListItems(page, limit)
        return response.toDomain { it.toDomain() }
    }
    
    override suspend fun getItemById(id: Int): ListItem {
        val dto = apiService.getItemById(id)
        return dto.toDomain()
    }
}
