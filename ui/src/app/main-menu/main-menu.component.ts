import { Component, OnInit } from "@angular/core";
import { MainMenuService } from "../main-menu.service";
import { MenuItem } from "../MenuItem";
import { Observable, of } from "rxjs";
import { MessageInternService } from "../message-intern.service";
import { catchError, map } from "rxjs/operators";

@Component({
  selector: "app-main-menu",
  templateUrl: "./main-menu.component.html",
  styleUrls: ["./main-menu.component.css"]
})
export class MainMenuComponent implements OnInit {
  menu: Observable<MenuItem[]>;

  newTitle: string;
  constructor(
    private mainManuService: MainMenuService,
    private ms: MessageInternService
  ) {}

  ngOnInit() {
    this.menu = this.mainManuService.getMenu();
  }
  addMenu() {
    this.mainManuService.addMenu(this.newTitle).subscribe(e => {

      this.ngOnInit();
    });
  }
}
