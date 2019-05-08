import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MainMenuService, MenuItem } from '../main-menu.service';
import { Observable } from 'rxjs';


@Component({
  selector: 'app-sub-menu',
  templateUrl: './sub-menu.component.html',
  styleUrls: ['./sub-menu.component.css']
})
export class SubMenuComponent implements OnInit {

  newTitle : String
  menu: Observable<MenuItem[]>;
  title: string;
  constructor(private route: ActivatedRoute,private mService : MainMenuService) { }

  ngOnInit() {
   
      this.route.paramMap.subscribe( e => this.loadItem(e))
     
       
   
  }
  loadItem(e: import("@angular/router").ParamMap): void {
    this.title = e.get('title')
    this.menu = this.mService.getSubMenu(this.title)
  }
  
  addMenu(){
    this.mService.addSubMenu(this.title,this.newTitle)
  }

}
