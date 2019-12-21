import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { of } from "rxjs";
import { Oeuvre, PageElement, EmptyOeuvre } from "../model/Oeuvre";
import { OeuvreService } from "../oeuvre.service";
import { environment } from "src/environments/environment";
import { PageComponentBase } from "../util";

@Component({
  selector: "app-oeuvre",
  templateUrl: "./oeuvre.component.html",
  styleUrls: ["./oeuvre.component.css"]
})
export class OeuvreComponent extends PageComponentBase implements OnInit {

  newTitle: string;






  constructor(private route: ActivatedRoute, private mService: OeuvreService) {
    super();
    this.oeuvres = [];
  }
  addOeuvre() {
    this.mService.add(this.newTitle, this.currentThemeKey,this.addIncolumn - 1).subscribe(e => {
      this.addInTable(e);
    });
  }



  ngOnInit() {
    this.route.paramMap.subscribe(par => {
      this.currentThemeKey = +par.get("id");

      this.mService.oauvres( this.currentThemeKey ).subscribe(e => {
        e.forEach(ele => {
          this.addInTable(ele);
        });
      });
    });
  }
}
