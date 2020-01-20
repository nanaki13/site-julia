import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { PageElement } from "../model/PageElement";
import { PageElementDisplay } from "../PageElementDisplay";

@Component({
  selector: "app-create-form",
  templateUrl: "./create-form.component.html",
  styleUrls: ["./create-form.component.css"]
})
export class CreateFormComponent implements OnInit {
  @Input()
  pDisplay: PageElementDisplay;

  addIncolumn = 1;
  @Input()
  newItem: PageElement;

  @Input()
  typeList: string[];
  @Output()
  create = new EventEmitter<PageElement>();
  constructor() {}

  ngOnInit() {}

  showAdd(n: boolean) {
    this.pDisplay.showAdd = n;
  }

  createEntity() {
    this.newItem.x = this.addIncolumn - 1;
    this.create.emit(this.newItem);
  }
}
