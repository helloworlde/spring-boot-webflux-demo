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
