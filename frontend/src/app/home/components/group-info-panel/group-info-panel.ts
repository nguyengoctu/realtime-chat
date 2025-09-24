import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Conversation } from '../conversations-list/conversations-list';

export interface GroupMember {
  id: number;
  name: string;
  email: string;
  online: boolean;
  role?: string;
}

@Component({
  selector: 'app-group-info-panel',
  imports: [CommonModule],
  templateUrl: './group-info-panel.html'
})
export class GroupInfoPanel {
  @Input() conversation: Conversation | null = null;
  @Input() members: GroupMember[] = [];
  @Input() messageCount: number = 0;
}