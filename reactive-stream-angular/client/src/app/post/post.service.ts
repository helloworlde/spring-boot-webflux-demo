import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Post} from './post';
import {EventSourcePolyfill} from 'ng-event-source';

@Injectable({
  providedIn: 'root'
})
export class PostService {

  postUrl = environment.baseUrl + '/posts';
  posts: Post[] = [];

  constructor(
    private http: HttpClient
  ) {
  }

  getPostNonBlock(): Observable<Array<Post>> {
    this.posts = [];

    return Observable.create((observer) => {
      const url = `${this.postUrl}/nonblock`;
      const eventSource = new EventSourcePolyfill(url, {
        heartbeatTimeout: 5000,
        connectionTimeout: 5000
      });

      eventSource.onmessage = evt => {
        this.posts.push(JSON.parse(evt.data));
        observer.next(this.posts);
      };

      eventSource.onerror = error => {
        if (eventSource.readyState === 0) {
          console.log('The steam has been closed by the server');
          eventSource.close();
          observer.complete();
        } else {
          observer.error('EventSource error ' + error);
        }
      };
    });
  }

  getPostBlocked(): Observable<Array<Post>> {
    return this.http.get<Array<Post>>(`${this.postUrl}/block`);
  }
}
