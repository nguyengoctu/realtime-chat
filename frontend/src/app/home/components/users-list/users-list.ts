import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface User {
  id: number;
  name: string;
  email: string;
  online: boolean;
}

@Component({
  selector: 'app-users-list',
  imports: [CommonModule],
  templateUrl: './users-list.html'
})
export class UsersList {
  @Input() users: User[] = [];
  @Input() selectedUser: User | null = null;
  @Output() userSelected = new EventEmitter<User>();

  onUserSelect(user: User) {
    this.userSelected.emit(user);
  }

  getUserItemClass(user: User): string {
    const baseClass = 'p-4 border-b border-gray-100 hover:bg-gray-50 cursor-pointer transition-colors ';
    const selectedClass = 'bg-blue-50 border-blue-200';

    return baseClass + (this.selectedUser?.id === user.id ? selectedClass : '');
  }

  getOnlineStatusClass(online: boolean): string {
    return `w-3 h-3 rounded-full ${online ? 'bg-green-400' : 'bg-gray-300'}`;
  }
}