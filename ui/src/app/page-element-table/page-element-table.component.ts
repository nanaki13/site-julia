import { Component, OnInit, Input } from "@angular/core";
import { PageElement } from "../model/Oeuvre";
import { PageElementDisplay } from "../PageElementDisplay";

@Component({
  selector: "app-page-element-table",
  templateUrl: "./page-element-table.component.html",
  styleUrls: ["./page-element-table.component.css"]
})
export class PageElementTableComponent implements OnInit {
  @Input()
  element: PageElement;
  @Input()
  pDisplay: PageElementDisplay;
  constructor() {}

  ngOnInit() {
      console.log(this.pDisplay.src(this.element));
  }

  src(): string  {return this.pDisplay.src(this.element)}
}
