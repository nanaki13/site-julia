// tslint:disable-next-line: quotemark
import { Component } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { AppService } from "./app.service";
import { OeuvreService } from "./oeuvre.service";
import { MainMenuService } from "./main-menu.service";

@Component({
  // tslint:disable-next-line: quotemark
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.css"]
})
export class AppComponent {
  constructor(
    /*private appService: AppService, private http: HttpClient*/
    private os: OeuvreService,
    private ms: MainMenuService
  ) {

  }


  save() {

    this.os.saveLocalContext();
    this. ms.saveLocalContext();
  }
  load() {

    this.os.restoreLocalContext();
    this.ms.restoreLocalContext();
  }
  ngOnInit() {}
}
