import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {PostService} from '../post.service';
import {Post} from '../post';

@Component({
  selector: 'app-post-list',
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.css']
})
export class PostListComponent implements OnInit {

  selectedPost: Post;
  posts: Observable<Post[]>;

  constructor(
    private postService: PostService
  ) {

  }

  ngOnInit(): void {
  }

  getPostNonBlock(): void {
    this.clearCurrentPost();
    this.posts = this.postService.getPostNonBlock();
  }

  getPostBlocked(): void {
    this.clearCurrentPost();
    this.posts = this.postService.getPostBlocked();
  }

  private clearCurrentPost() {
    this.posts = null;
    this.selectedPost = null;
  }


  selectPost(post: Post) {
    this.selectedPost = post;
  }
}
