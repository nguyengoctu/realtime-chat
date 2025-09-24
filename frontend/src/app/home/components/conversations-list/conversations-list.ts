import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Conversation {
  id: number;
  name: string;
  type: 'personal' | 'group';
  lastMessage: string;
  lastMessageTime: string;
  unreadCount: number;
}

@Component({
  selector: 'app-conversations-list',
  imports: [CommonModule],
  templateUrl: './conversations-list.html'
})
export class ConversationsList {
  @Input() conversations: Conversation[] = [];
  @Input() selectedConversation: Conversation | null = null;
  @Output() conversationSelected = new EventEmitter<Conversation>();

  onConversationSelect(conversation: Conversation) {
    this.conversationSelected.emit(conversation);
  }

  getConversationItemClass(conversation: Conversation): string {
    const baseClass = 'p-4 border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors ';
    const selectedClass = 'bg-blue-50 border-blue-200';

    return baseClass + (this.selectedConversation?.id === conversation.id ? selectedClass : '');
  }

  getConversationAvatarClass(conversation: Conversation): string {
    const baseClass = 'w-10 h-10 rounded-full flex items-center justify-center text-white font-semibold ';
    const groupClass = 'bg-purple-500';
    const personalClass = 'bg-green-500';

    return baseClass + (conversation.type === 'group' ? groupClass : personalClass);
  }

  getConversationIcon(conversation: Conversation): string {
    return conversation.type === 'group' ? '👥' : '💬';
  }
}