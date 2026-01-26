package com.example.template.core.domain.repository

import com.example.template.core.domain.model.ListItem
import com.example.template.core.domain.model.PaginatedResult

interface ListItemRepository {
    suspend fun getListItems(page: Int = 1, limit: Int = 10): PaginatedResult<ListItem>
    suspend fun getItemById(id: Int): ListItem
}
