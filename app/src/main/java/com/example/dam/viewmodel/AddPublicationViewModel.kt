package com.example.dam.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dam.repository.PublicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AddPublicationViewModel(
    application: Application  // ← AndroidViewModel donne accès au context
) : AndroidViewModel(application) {

    private val TAG = "AddPublicationViewModel"
    private val context: Context = application.applicationContext
    private val repository = PublicationRepository(context)

    // État de l'UI
    private val _uiState = MutableStateFlow<AddPublicationUiState>(AddPublicationUiState.Idle)
    val uiState: StateFlow<AddPublicationUiState> = _uiState.asStateFlow()

    // Données du formulaire
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _selectedTags = MutableStateFlow<List<String>>(emptyList())
    val selectedTags: StateFlow<List<String>> = _selectedTags.asStateFlow()

    private val _mentionedUsers = MutableStateFlow<List<String>>(emptyList())
    val mentionedUsers: StateFlow<List<String>> = _mentionedUsers.asStateFlow()

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    /**
     * Mettre à jour le contenu
     */
    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    /**
     * Ajouter un tag
     */
    fun addTag(tag: String) {
        if (tag.isNotBlank() && !_selectedTags.value.contains(tag)) {
            _selectedTags.value = _selectedTags.value + tag
            Log.d(TAG, "✅ Tag added: $tag")
        }
    }

    /**
     * Supprimer un tag
     */
    fun removeTag(tag: String) {
        _selectedTags.value = _selectedTags.value - tag
        Log.d(TAG, "❌ Tag removed: $tag")
    }

    /**
     * Définir la liste de tags
     */
    fun setTags(tags: List<String>) {
        _selectedTags.value = tags
    }

    /**
     * Ajouter une mention
     */
    fun addMention(userId: String) {
        if (userId.isNotBlank() && !_mentionedUsers.value.contains(userId)) {
            _mentionedUsers.value = _mentionedUsers.value + userId
            Log.d(TAG, "✅ User mentioned: $userId")
        }
    }

    /**
     * Supprimer une mention
     */
    fun removeMention(userId: String) {
        _mentionedUsers.value = _mentionedUsers.value - userId
        Log.d(TAG, "❌ Mention removed: $userId")
    }

    /**
     * Définir la liste de mentions
     */
    fun setMentions(userIds: List<String>) {
        _mentionedUsers.value = userIds
    }

    /**
     * Mettre à jour la location
     */
    fun updateLocation(newLocation: String) {
        _location.value = newLocation
    }

    /**
     * Sélectionner une image
     */
    fun selectImage(uri: Uri?) {
        _selectedImageUri.value = uri
        Log.d(TAG, if (uri != null) "✅ Image selected: $uri" else "❌ Image deselected")
    }

    /**
     * Publier la publication
     */
    fun publishPublication() {
        viewModelScope.launch {
            val contentText = _content.value.trim()

            if (contentText.isBlank()) {
                _uiState.value = AddPublicationUiState.Error("Le contenu ne peut pas être vide")
                return@launch
            }

            _uiState.value = AddPublicationUiState.Loading
            Log.d(TAG, "📤 Publishing publication...")

            try {
                val result = repository.createPublication(
                    content = contentText,
                    imageUri = _selectedImageUri.value,  // ✅ Null si pas d'image
                    tags = _selectedTags.value.takeIf { it.isNotEmpty() },
                    mentions = _mentionedUsers.value.takeIf { it.isNotEmpty() },
                    location = _location.value.takeIf { it.isNotBlank() }
                )

                result.fold(
                    onSuccess = { publication ->
                        Log.d(TAG, "✅ Publication created successfully: ${publication.id}")
                        _uiState.value = AddPublicationUiState.Success(publication.id)
                        resetForm()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ Failed to create publication", error)
                        _uiState.value = AddPublicationUiState.Error(error.message ?: "Erreur lors de la création de la publication")
                    }
                )
            } catch (error: Exception) {
                Log.e(TAG, "❌ Unexpected error", error)
                _uiState.value = AddPublicationUiState.Error(error.message ?: "Erreur inattendue")
            }
        }
    }

    /**
     * Réinitialiser le formulaire
     */
    private fun resetForm() {
        _content.value = ""
        _selectedTags.value = emptyList()
        _mentionedUsers.value = emptyList()
        _location.value = ""
        _selectedImageUri.value = null
    }

    /**
     * Réinitialiser l'état UI
     */
    fun resetUiState() {
        _uiState.value = AddPublicationUiState.Idle
    }
}

/**
 * États possibles de l'UI
 */
sealed class AddPublicationUiState {
    object Idle : AddPublicationUiState()
    object Loading : AddPublicationUiState()
    data class Success(val publicationId: String) : AddPublicationUiState()
    data class Error(val message: String) : AddPublicationUiState()
}