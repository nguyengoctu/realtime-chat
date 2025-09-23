import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { UserService, UserResponse, UserUpdateRequest } from '../services/user.service';
import { UploadService } from '../services/upload.service';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  user: UserResponse | null = null;
  loading = true;
  error: string | null = null;
  isEditing = false;
  editForm: UserUpdateRequest = {};
  uploading = false;
  selectedFile: File | null = null;
  constructor(
    private router: Router,
    private authService: AuthService,
    private userService: UserService,
    private uploadService: UploadService
  ) {}

  ngOnInit() {
    this.loadUserProfile();
  }

  loadUserProfile() {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.userService.getUserById(currentUser.id).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.user = response.data;
          } else {
            this.error = response.message || 'Failed to load user profile';
          }
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Failed to load user profile';
          this.loading = false;
          console.error('Error loading user profile:', error);
        }
      });
    } else {
      this.router.navigate(['/login']);
    }
  }

  goToHome() {
    this.router.navigate(['/home']);
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

  startEdit() {
    if (this.user) {
      this.isEditing = true;
      this.editForm = {
        email: this.user.email,
        fullName: this.user.fullName || '',
        avatarUrl: this.user.avatarUrl
      };
    }
  }

  cancelEdit() {
    this.isEditing = false;
    this.editForm = {};
    this.selectedFile = null;
  }

  saveProfile() {
    if (!this.user) return;

    if (this.selectedFile) {
      this.uploading = true;
      this.uploadService.uploadAvatar(this.selectedFile).subscribe({
        next: (uploadResponse) => {
          if (uploadResponse.success && uploadResponse.data) {
            this.editForm.avatarUrl = uploadResponse.data; // Already relative path
          }
          this.updateUserProfile();
        },
        error: (error) => {
          this.uploading = false;
          this.error = 'Failed to upload avatar';
          console.error('Upload error:', error);
        }
      });
    } else {
      this.updateUserProfile();
    }
  }

  private updateUserProfile() {
    if (!this.user) return;

    this.userService.updateUser(this.user.id, this.editForm).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.user = response.data;
          this.isEditing = false;
          this.editForm = {};
          this.selectedFile = null;
        } else {
          this.error = response.message || 'Failed to update profile';
        }
        this.uploading = false;
      },
      error: (error) => {
        this.error = 'Failed to update profile';
        this.uploading = false;
        console.error('Update error:', error);
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.type.startsWith('image/')) {
        this.selectedFile = file;

        // Preview the selected image
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.editForm.avatarUrl = e.target.result;
        };
        reader.readAsDataURL(file);
      } else {
        this.error = 'Please select an image file';
      }
    }
  }
}
