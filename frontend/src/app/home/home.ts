import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

// Import components
import { NavigationSidebar, ViewType } from './components/navigation-sidebar/navigation-sidebar';
import { UsersList, User } from './components/users-list/users-list';
import { ConversationsList, Conversation } from './components/conversations-list/conversations-list';
import { ChatInterface, Message } from './components/chat-interface/chat-interface';
import { ProfilePanel } from './components/profile-panel/profile-panel';
import { GroupInfoPanel, GroupMember } from './components/group-info-panel/group-info-panel';

@Component({
  selector: 'app-home',
  imports: [
    CommonModule,
    NavigationSidebar,
    UsersList,
    ConversationsList,
    ChatInterface,
    ProfilePanel,
    GroupInfoPanel
  ],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  activeView: ViewType = 'conversations';
  selectedUser: User | null = null;
  selectedConversation: Conversation | null = null;

  users: User[] = [
    { id: 1, name: 'Alice Johnson', email: 'alice@example.com', online: true },
    { id: 2, name: 'Bob Smith', email: 'bob@example.com', online: false },
    { id: 3, name: 'Charlie Brown', email: 'charlie@example.com', online: true },
    { id: 4, name: 'Diana Prince', email: 'diana@example.com', online: true },
    { id: 5, name: 'Ethan Hunt', email: 'ethan@example.com', online: false },
    { id: 6, name: 'Fiona Green', email: 'fiona@example.com', online: true },
    { id: 7, name: 'George Wilson', email: 'george@example.com', online: false },
    { id: 8, name: 'Hannah Lee', email: 'hannah@example.com', online: true }
  ];

  conversations: Conversation[] = [
    { id: 1, name: 'Alice Johnson', type: 'personal', lastMessage: 'Hey, how are you?', lastMessageTime: '10:30 AM', unreadCount: 2 },
    { id: 2, name: 'Project Team', type: 'group', lastMessage: 'The meeting is at 3 PM', lastMessageTime: '9:15 AM', unreadCount: 0 },
    { id: 3, name: 'Bob Smith', type: 'personal', lastMessage: 'Thanks for the help!', lastMessageTime: 'Yesterday', unreadCount: 0 },
    { id: 4, name: 'Family Group', type: 'group', lastMessage: 'Dinner at 7?', lastMessageTime: 'Yesterday', unreadCount: 1 },
    { id: 5, name: 'Charlie Brown', type: 'personal', lastMessage: 'See you tomorrow', lastMessageTime: '2 days ago', unreadCount: 0 },
    { id: 6, name: 'Work Team', type: 'group', lastMessage: 'Great job everyone!', lastMessageTime: '2 days ago', unreadCount: 3 }
  ];

  private userMessages: { [userId: number]: Message[] } = {
    1: [
      { id: 1, content: 'Hey, how are you?', timestamp: '10:25 AM', isOwn: false },
      { id: 2, content: 'I\'m good, thanks! How about you?', timestamp: '10:26 AM', isOwn: true },
      { id: 3, content: 'Doing great! Want to grab coffee later?', timestamp: '10:30 AM', isOwn: false }
    ],
    3: [
      { id: 1, content: 'Hey Charlie, how\'s the project going?', timestamp: '2:00 PM', isOwn: true },
      { id: 2, content: 'Pretty good! Almost finished with the frontend', timestamp: '2:05 PM', isOwn: false },
      { id: 3, content: 'That\'s awesome! Let me know if you need help', timestamp: '2:06 PM', isOwn: true },
      { id: 4, content: 'Will do, thanks!', timestamp: '2:07 PM', isOwn: false }
    ]
  };

  private conversationMessages: { [conversationId: number]: Message[] } = {
    2: [
      { id: 1, content: 'Hey everyone, don\'t forget about the meeting today', timestamp: '9:00 AM', isOwn: false, senderName: 'Alice Johnson' },
      { id: 2, content: 'What time was it again?', timestamp: '9:05 AM', isOwn: false, senderName: 'Bob Smith' },
      { id: 3, content: 'It\'s at 3 PM in the conference room', timestamp: '9:15 AM', isOwn: true },
      { id: 4, content: 'Perfect, I\'ll be there', timestamp: '9:16 AM', isOwn: false, senderName: 'Charlie Brown' }
    ],
    4: [
      { id: 1, content: 'What should we have for dinner?', timestamp: '6:30 PM', isOwn: false, senderName: 'Mom' },
      { id: 2, content: 'How about pizza?', timestamp: '6:35 PM', isOwn: false, senderName: 'Dad' },
      { id: 3, content: 'Sounds good to me!', timestamp: '6:36 PM', isOwn: true },
      { id: 4, content: 'Dinner at 7?', timestamp: '6:45 PM', isOwn: false, senderName: 'Mom' }
    ]
  };

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  // Event handlers for components
  onViewChanged(view: ViewType) {
    this.activeView = view;
    this.selectedUser = null;
    this.selectedConversation = null;
  }

  onUserSelected(user: User) {
    this.selectedUser = user;
    this.selectedConversation = null;
  }

  onConversationSelected(conversation: Conversation) {
    this.selectedConversation = conversation;
    this.selectedUser = null;

    // Reset unread count when opening conversation
    conversation.unreadCount = 0;
  }

  onMessageSent(messageContent: string) {
    const newMessage: Message = {
      id: Date.now(),
      content: messageContent,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isOwn: true
    };

    if (this.selectedConversation) {
      if (!this.conversationMessages[this.selectedConversation.id]) {
        this.conversationMessages[this.selectedConversation.id] = [];
      }
      this.conversationMessages[this.selectedConversation.id].push(newMessage);

      // Update last message in conversation list
      this.selectedConversation.lastMessage = messageContent;
      this.selectedConversation.lastMessageTime = newMessage.timestamp;
    } else if (this.selectedUser && this.activeView === 'users') {
      if (!this.userMessages[this.selectedUser.id]) {
        this.userMessages[this.selectedUser.id] = [];
      }
      this.userMessages[this.selectedUser.id].push(newMessage);
    }
  }

  onStartChatRequested(user: User) {
    this.startChat(user);
  }

  startChat(user: User) {
    // Check if conversation already exists
    let existingConversation = this.conversations.find(conv =>
      conv.type === 'personal' && conv.name === user.name
    );

    if (!existingConversation) {
      // Create new conversation
      existingConversation = {
        id: Date.now(),
        name: user.name,
        type: 'personal',
        lastMessage: 'Start chatting...',
        lastMessageTime: 'now',
        unreadCount: 0
      };
      this.conversations.unshift(existingConversation);
    }

    // Switch to conversations tab and select the conversation
    this.activeView = 'conversations';
    this.selectedConversation = existingConversation;
    this.selectedUser = null;
  }

  getUserMessageCount(userId: number): number {
    return this.userMessages[userId]?.length || 0;
  }

  getLastActivity(userId: number): string {
    const messages = this.userMessages[userId];
    if (!messages || messages.length === 0) return 'No activity';

    const lastMessage = messages[messages.length - 1];
    return lastMessage.timestamp;
  }

  hasExistingChat(userId: number): boolean {
    return this.userMessages[userId] && this.userMessages[userId].length > 0;
  }

  getCurrentMessages(): Message[] {
    if (this.selectedConversation) {
      return this.conversationMessages[this.selectedConversation.id] || [];
    } else if (this.selectedUser && this.activeView === 'users') {
      return this.userMessages[this.selectedUser.id] || [];
    }
    return [];
  }


  getGroupMembers(conversationId: number): GroupMember[] {
    // Fake data for group members
    const groupMembers: { [key: number]: GroupMember[] } = {
      2: [ // Project Team
        { id: 1, name: 'Alice Johnson', email: 'alice@example.com', online: true, role: 'Team Lead' },
        { id: 3, name: 'Charlie Brown', email: 'charlie@example.com', online: true, role: 'Developer' },
        { id: 4, name: 'Diana Prince', email: 'diana@example.com', online: true, role: 'Designer' },
        { id: 8, name: 'Hannah Lee', email: 'hannah@example.com', online: true, role: 'Tester' }
      ],
      4: [ // Family Group
        { id: 10, name: 'Mom', email: 'mom@family.com', online: false, role: 'Parent' },
        { id: 11, name: 'Dad', email: 'dad@family.com', online: true, role: 'Parent' },
        { id: 12, name: 'Sister', email: 'sister@family.com', online: true, role: 'Member' }
      ],
      6: [ // Work Team
        { id: 1, name: 'Alice Johnson', email: 'alice@example.com', online: true, role: 'Manager' },
        { id: 2, name: 'Bob Smith', email: 'bob@example.com', online: false, role: 'Developer' },
        { id: 5, name: 'Ethan Hunt', email: 'ethan@example.com', online: false, role: 'Analyst' },
        { id: 6, name: 'Fiona Green', email: 'fiona@example.com', online: true, role: 'Developer' },
        { id: 7, name: 'George Wilson', email: 'george@example.com', online: false, role: 'QA' }
      ]
    };

    return groupMembers[conversationId] || [];
  }


  goToProfile() {
    this.router.navigate(['/profile']);
  }

  logout() {
    this.authService.logout().subscribe({
      complete: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        // Even if logout API fails, still redirect to login since local data is cleared
        console.error('Logout API error:', error);
        this.router.navigate(['/login']);
      }
    });
  }
}
