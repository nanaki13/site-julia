import { Component, OnInit } from '@angular/core';
import { MainMenuService, MenuItem } from '../main-menu.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-main-menu',
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.css']
})
export class MainMenuComponent implements OnInit {
  menu: Observable<MenuItem[]>;

  newTitle : String
  constructor(private mainManuService : MainMenuService) { }

  ngOnInit() {
     this.menu = this.mainManuService.getMenu()
    
  }
  addMenu(){
    this.mainManuService.addMenu(this.newTitle).subscribe( e => {console.log(e);this.ngOnInit()})
  }

}
