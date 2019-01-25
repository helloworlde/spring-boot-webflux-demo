## Client

### 创建应用

```shell
ng new client
```

- 添加 Bootstrap 并引入

```shell
npm install bootstrap
```
在 `style.css` 中添加

```css
@import "~bootstrap";
```

- 添加 EventSourcePolyfill
Angular 新版本中如果直接使用 EventSource 异步加载数据到页面上会有问题，改用 EventSourcePolyfill 代替 EventSource

```shell
npm install ng-event-source
```

### 添加组件

#### Post Model

```typescript
export class Post {

  public id: string;
  public title: string;
  public content: string;
  public createDate: string;

  constructor(id: string, title: string, content: string, createDate: string) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.createDate = createDate;
  }
}
```

#### Post List Component

- 添加组件

```shell
ng generate component post/post-list
```

#### Post Detail Component

- 添加组件

```shell
ng generate component post/post-detail
```

#### Post Service 

- 添加组件

```shell
ng generate service post/post
```

- post-service.ts

```typescript
  getPostNonBlock(): Observable<Array<Post>> {
    this.posts = [];

    return Observable.create((observer) => {
      const url = `${this.postUrl}/stream`;
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
    return this.http.get<Array<Post>>(`${this.postUrl}`);
  }
```


#### 添加路由 
- app-routing.module.ts

```typescript
const routes: Routes = [
  {path: '', redirectTo: '/post', pathMatch: 'full'},
  {path: 'post', component: PostListComponent}
];
```

- environment.ts

```typescript
export const environment = {
  production: false,
  baseUrl: 'http://localhost:8080'
};
```

### 打包

- 修改 angular.json 文件，将编译后的文件添加到 SpringBoot 项目的 `src/main/resources/static/`目录下

```json
"outputPath": "../server/src/main/resources/static/"
```

---

### 问题

- EventSource 数据不显示问题

使用 EventSource 获取数据，页面加载完数据后没有显示；经过一系列对比之后，发现[full-reactive-stack(Angular 4.2.4)](https://github.com/mechero/full-reactive-stack)这个 Demo 在 service 有一个特殊的引入：
```typescript
import * as EventSource from 'eventsource';
```

在当前项目(Angular 7.2.0)里不需要引入就可以使用；这就是问题所在，如果直接使用 `EventSource`，异步加载数据的时候无法显示到页面上，在 [EventSource not working in angular 6 due to module http and https](https://stackoverflow.com/questions/52352532/eventsource-not-working-in-angular-6-due-to-module-http-and-https)找到了答案，需要将`EventSource`替换为 `EventSourcePolyfill`

```typescript
import {EventSourcePolyfill} from 'ng-event-source';

// ...

const eventSource = new EventSourcePolyfill(url, {
        heartbeatTimeout: 5000,
        connectionTimeout: 5000
      });
```


--- 

#### 参考文章 
- [https://github.com/mechero/full-reactive-stack](https://github.com/mechero/full-reactive-stack)
- [EventSource not working in angular 6 due to module http and https](https://stackoverflow.com/questions/52352532/eventsource-not-working-in-angular-6-due-to-module-http-and-https)
