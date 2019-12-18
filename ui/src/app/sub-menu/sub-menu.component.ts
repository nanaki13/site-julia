import { Component, OnInit } from '@angular/core';
import { ActivatedRoute,ParamMap } from '@angular/router';
import { MainMenuService, MenuItem } from '../main-menu.service';
import { Observable } from 'rxjs';


@Component({
  selector: 'app-sub-menu',
  templateUrl: './sub-menu.component.html',
  styleUrls: ['./sub-menu.component.css']
})
export class SubMenuComponent implements OnInit {
  typeList = ["page","subMenu"]
  newTitle : string;
  menu: Observable<MenuItem[]>;
  title: string;
  id: number;
  constructor(private route: ActivatedRoute,private mService : MainMenuService) { }

  ngOnInit() {

      this.route.paramMap.subscribe( e => this.loadItem(e))



  }
  loadItem(e: ParamMap): void {
    this.title = e.get('title')
    this.id = +e.get('id')
    this.menu = this.mService.getSubMenu(this.id)
  }

  addMenu(){
    this.mService.addSubMenu(this.id,this.newTitle).subscribe( e => {console.log(e);this.ngOnInit()})

  }

}
