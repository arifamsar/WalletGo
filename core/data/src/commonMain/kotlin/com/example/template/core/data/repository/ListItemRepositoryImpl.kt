package com.example.template.core.data.repository

import com.example.template.core.data.mapper.toDomain
import com.example.template.core.data.service.ApiService
import com.example.template.core.domain.model.ListItem
import com.example.template.core.domain.model.PaginatedResult
import com.example.template.core.domain.repository.ListItemRepository

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
