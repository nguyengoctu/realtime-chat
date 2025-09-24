import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { User } from '../users-list/users-list';
import { Conversation } from '../conversations-list/conversations-list';

export interface Message {
  id: number;
  content: string;
  timestamp: string;
  isOwn: boolean;
  senderName?: string;
}

@Component({
  selector: 'app-chat-interface',
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-interface.html'
})
export class ChatInterface {
  @Input() selectedUser: User | null = null;
  @Input() selectedConversation: Conversation | null = null;
  @Input() messages: Message[] = [];
  @Output() messageSent = new EventEmitter<string>();
  @Output() startChatRequested = new EventEmitter<User>();

  messageInput: string = '';

  onSendMessage() {
    if (!this.messageInput.trim()) return;

    this.messageSent.emit(this.messageInput);
    this.messageInput = '';
  }

  onStartChat() {
    if (this.selectedUser) {
      this.startChatRequested.emit(this.selectedUser);
    }
  }

  getChatTitle(): string {
    if (this.selectedConversation) {
      return this.selectedConversation.name;
    } else if (this.selectedUser) {
      return this.selectedUser.name;
    }
    return '';
  }

  getChatSubtitle(): string {
    if (this.selectedConversation) {
      return this.selectedConversation.type === 'group' ? 'Group Chat' : 'Personal Chat';
    } else if (this.selectedUser) {
      return this.selectedUser.online ? 'Online' : 'Offline';
    }
    return '';
  }

  getChatHeaderAvatarClass(): string {
    const baseClass = 'w-8 h-8 rounded-full flex items-center justify-center text-white font-semibold ';

    if (this.selectedConversation) {
      return baseClass + (this.selectedConversation.type === 'group' ? 'bg-purple-500' : 'bg-green-500');
    } else if (this.selectedUser) {
      return baseClass + 'bg-blue-500';
    }
    return baseClass + 'bg-gray-500';
  }

  getChatHeaderIcon(): string {
    if (this.selectedConversation) {
      return this.selectedConversation.type === 'group' ? '👥' : '💬';
    } else if (this.selectedUser) {
      return this.selectedUser.name.charAt(0).toUpperCase();
    }
    return '💬';
  }

  getMessagePlaceholder(): string {
    if (this.selectedConversation) {
      return `Message ${this.selectedConversation.name}...`;
    } else if (this.selectedUser) {
      return `Message ${this.selectedUser.name}...`;
    }
    return 'Type a message...';
  }

  getMessageBubbleClass(message: Message): string {
    const baseClass = 'max-w-xs lg:max-w-md px-4 py-2 rounded-lg ';
    return baseClass + (message.isOwn ? 'bg-blue-500 text-white' : 'bg-white text-gray-900 shadow-sm');
  }

  getMessageTimestampClass(message: Message): string {
    return 'text-xs mt-1 ' + (message.isOwn ? 'text-blue-100' : 'text-gray-500');
  }
}