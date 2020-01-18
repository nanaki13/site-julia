import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { MainMenuService } from "../main-menu.service";
import { MenuItem } from "../MenuItem";
import { Observable } from "rxjs";
import { PageComponentBase } from "../PageComponentBase";
import { ImageService } from '../image.service';
import { Service } from '../image-view/image-view.component';

@Component({
  selector: "app-sub-menu",
  templateUrl: "./sub-menu.component.html",
  styleUrls: ["./sub-menu.component.css"]
})
export class SubMenuComponent extends PageComponentBase implements OnInit {
  typeList = ["page", "subMenu"];

  title: string;


  constructor(
    private route: ActivatedRoute,
    private mService: MainMenuService,
    private imageService : ImageService
  ) {
    super();

  }
  mainService(): Service {
    return this.mService;
  }
  ngOnInit() {

    this.newItem = this.mService.currentMenuItem;
    this.route.paramMap.subscribe(e =>{

      this.oeuvres = [];
      this.loadItem(e)
    });

    this.imageService.lastSelectedImage.subscribe(s =>{
      this.newItem.image = s

    })
    this.incomingDelete.subscribe(e=> this.removeFromView(e))

  }
  loadItem(e: ParamMap): void {

    this.title = e.get("title");
    this.currentThemeKey = +e.get("id");

    this.mService.getSubMenu( this.currentThemeKey ).subscribe(m => {
      m.forEach(ele => {
        if(!ele.x){
          ele.x = 0 ;
        }
        if(!ele.y){
           ele.y = 0 ;
       }
        this.addInTable(ele);
      });
    });
  }

}
