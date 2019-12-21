import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { MainMenuService } from "../main-menu.service";
import { MenuItem } from "../MenuItem";
import { Observable } from "rxjs";
import { PageComponentBase } from "../util";

@Component({
  selector: "app-sub-menu",
  templateUrl: "./sub-menu.component.html",
  styleUrls: ["./sub-menu.component.css"]
})
export class SubMenuComponent extends PageComponentBase implements OnInit {
  typeList = ["page", "subMenu"];

  title: string;

  newItem = new MenuItem();
  constructor(
    private route: ActivatedRoute,
    private mService: MainMenuService
  ) {
    super();
  }

  ngOnInit() {
    this.route.paramMap.subscribe(e =>{

      this.oeuvres = [];
      this.loadItem(e)
    });
  }
  loadItem(e: ParamMap): void {
    this.title = e.get("title");
    this.newItem.themeKey = +e.get("id");
    this.mService.getSubMenu(this.newItem.themeKey).subscribe(m => {
      m.forEach(ele => {
        this.addInTable(ele);
      });
    });
  }

  addMenu() {
    this.newItem.x = this.addIncolumn - 1;
    this.mService.addSubMenu(this.newItem).subscribe(e => {

      this.newItem.y = 0;
      this.addInTable(e);
      this.newItem = new MenuItem();

    });
  }
}
