package com.builderbears.align.ui.screens.you

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.AppNotification
import com.builderbears.align.data.model.isInboxVisible
import com.builderbears.align.data.service.FriendService
import com.builderbears.align.data.service.InboxService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class InboxViewModel : ViewModel() {
    private val inboxService = InboxService()
    private val friendService = FriendService()
    private val auth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    init {
        startListening()
    }

    private fun startListening() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            inboxService.getNotificationsFlow(userId)
                .catch { e -> Log.e("InboxViewModel", "Listener error", e) }
                .collect { list ->
                    val visible = list.filter { it.isInboxVisible() }
                    _notifications.value = visible
                    _unreadCount.value = visible.count { !it.read }
                }
        }
    }

    fun acceptFriendRequest(notification: AppNotification) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            friendService.acceptRequest(currentUserId, notification.fromUserId)
                .onSuccess {
                    inboxService.markAsRead(currentUserId, notification.id)
                    removeNotification(notification.id)
                }
        }
    }

    fun declineFriendRequest(notification: AppNotification) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            friendService.removeFriend(currentUserId, notification.fromUserId)
                .onSuccess {
                    inboxService.deleteNotification(currentUserId, notification.id)
                    removeNotification(notification.id)
                }
        }
    }

    fun markAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            inboxService.markAsRead(userId, notificationId)
                .onSuccess {
                    _notifications.value = _notifications.value.map {
                        if (it.id == notificationId) it.copy(read = true) else it
                    }
                    _unreadCount.value = _notifications.value.count { !it.read }
                }
        }
    }

    fun clearAll() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            inboxService.clearAll(userId)
            _notifications.value = emptyList()
            _unreadCount.value = 0
        }
    }

    private fun removeNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
        _unreadCount.value = _notifications.value.count { !it.read }
    }
}
