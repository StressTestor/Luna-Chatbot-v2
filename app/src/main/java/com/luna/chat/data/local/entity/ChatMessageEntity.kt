package com.luna.chat.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.luna.chat.domain.entity.ChatMessage
import com.luna.chat.domain.entity.MessageStatus

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["timestamp"]),
        Index(value = ["is_from_user"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey 
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "content")
    val content: String,
    
    @ColumnInfo(name = "is_from_user")
    val isFromUser: Boolean,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    
    @ColumnInfo(name = "status")
    val status: String
) {
    companion object {
        fun fromDomain(chatMessage: ChatMessage, sessionId: String): ChatMessageEntity {
            return ChatMessageEntity(
                id = chatMessage.id,
                content = chatMessage.content,
                isFromUser = chatMessage.isFromUser,
                timestamp = chatMessage.timestamp,
                sessionId = sessionId,
                status = chatMessage.status.name
            )
        }
    }
    
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp,
            status = MessageStatus.valueOf(status)
        )
    }
}