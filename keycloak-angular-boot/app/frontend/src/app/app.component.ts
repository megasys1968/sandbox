import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.pug'
})
export class AppComponent implements OnInit {
  message: string

  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    this.httpClient.get('/api/world', { responseType: 'text'}).subscribe(res => {
      this.message = res
    })
  }
}
