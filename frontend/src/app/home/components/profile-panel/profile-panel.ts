import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '../users-list/users-list';

@Component({
  selector: 'app-profile-panel',
  imports: [CommonModule],
  templateUrl: './profile-panel.html'
})
export class ProfilePanel {
  @Input() user: User | null = null;
  @Input() messageCount: number = 0;
  @Input() lastActivity: string = 'No activity';
  @Output() startChatRequested = new EventEmitter<User>();

  onStartChat() {
    if (this.user) {
      this.startChatRequested.emit(this.user);
    }
  }
}