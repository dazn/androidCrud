package com.example.androidcrud.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeDestination

@Serializable
data class AddEntryDestination(val entryId: Long? = null)
