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
  template: `
    <div class="w-80 bg-white shadow-md border-r border-gray-200">
      <div class="h-full">
        <div class="p-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">Conversations</h2>
        </div>
        <div class="overflow-y-auto h-[calc(100%-5rem)]">
          <div
            *ngFor="let conversation of conversations"
            (click)="onConversationSelect(conversation)"
            [class]="getConversationItemClass(conversation)"
          >
            <div class="flex items-center space-x-3">
              <div [class]="getConversationAvatarClass(conversation)">
                {{getConversationIcon(conversation)}}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex justify-between items-baseline">
                  <p class="text-sm font-medium text-gray-900 truncate">{{conversation.name}}</p>
                  <p class="text-xs text-gray-500">{{conversation.lastMessageTime}}</p>
                </div>
                <p class="text-xs text-gray-500 truncate">{{conversation.lastMessage}}</p>
              </div>
              <div
                *ngIf="conversation.unreadCount > 0"
                class="bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center"
              >
                {{conversation.unreadCount}}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
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