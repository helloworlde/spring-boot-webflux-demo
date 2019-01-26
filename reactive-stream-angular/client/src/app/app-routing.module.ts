import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {PostListComponent} from './post/post-list/post-list.component';

const routes: Routes = [
  {path: '', redirectTo: '/post', pathMatch: 'full'},
  {path: 'post', component: PostListComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
