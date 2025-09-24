import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export type ViewType = 'users' | 'conversations';

@Component({
  selector: 'app-navigation-sidebar',
  imports: [CommonModule],
  templateUrl: './navigation-sidebar.html'
})
export class NavigationSidebar {
  @Input() activeView: ViewType = 'conversations';
  @Output() viewChanged = new EventEmitter<ViewType>();

  onViewChange(view: ViewType) {
    this.viewChanged.emit(view);
  }

  getButtonClass(view: ViewType): string {
    const baseClass = 'w-full text-left px-4 py-3 rounded-lg font-medium transition-colors ';
    const activeClass = 'bg-blue-100 text-blue-700';
    const inactiveClass = 'text-gray-700 hover:bg-gray-100';

    return baseClass + (this.activeView === view ? activeClass : inactiveClass);
  }
}