package com.builderbears.align.ui.screens.you

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.AppNotification
import com.builderbears.align.data.service.FriendService
import com.builderbears.align.data.service.InboxService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        loadNotifications()
    }

    fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            inboxService.getNotifications(userId)
                .onSuccess { list ->
                    _notifications.value = list
                    _unreadCount.value = list.count { !it.read }
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

    private fun removeNotification(notificationId: String) {
        _notifications.value = _notifications.value.filter { it.id != notificationId }
        _unreadCount.value = _notifications.value.count { !it.read }
    }
}
