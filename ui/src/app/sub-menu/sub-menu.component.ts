import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { MainMenuService, MenuItem } from "../main-menu.service";
import { Observable } from "rxjs";

@Component({
  selector: "app-sub-menu",
  templateUrl: "./sub-menu.component.html",
  styleUrls: ["./sub-menu.component.css"]
})
export class SubMenuComponent implements OnInit {
  typeList = ["page", "subMenu"];

  menu: Observable<MenuItem[]>;
  title: string;
  id: number;
  newItem = new MenuItem();
  constructor(
    private route: ActivatedRoute,
    private mService: MainMenuService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(e => this.loadItem(e));
  }
  loadItem(e: ParamMap): void {
    this.title = e.get("title");
    this.newItem.parentTheme = +e.get("id");
    this.menu = this.mService.getSubMenu(this.newItem.parentTheme);
  }

  addMenu() {
    this.mService.addSubMenu(this.newItem).subscribe(e => {
      console.log(e);
      this.newItem = new MenuItem();
      this.ngOnInit();
    });
  }
}
