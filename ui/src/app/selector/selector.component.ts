import { Component, OnInit, Input, Output, EventEmitter } from "@angular/core";
import { PageElement } from "../model/PageElement";
import { pDisplay, PageElementDisplay } from "../PageElementDisplay";

@Component({
  selector: "app-selector",
  templateUrl: "./selector.component.html",
  styleUrls: ["./selector.component.css"]
})
export class SelectorComponent implements OnInit {
  @Input()
  elements: PageElement[] = [];
  pDisplay: PageElementDisplay;
  state: boolean[] = [];
  currentSelected: PageElement;

  @Output() selected = new EventEmitter<PageElement>();
  constructor() {
    this.pDisplay = pDisplay;
  }
  change(e: PageElement, v: boolean, i: number) {
    const haveOneSel = this.state.reduce((r, cv) => {
      return (!r && v) || r || v;
    });

    if (haveOneSel && v) {
      for (let i in this.elements) {
        if (this.elements[i] !== e) {
          this.state[i] = false;
        }
      }
    }
    if (v) {
      this.currentSelected = e;
    } else {
      this.currentSelected = undefined;
    }

  }
  ngOnInit() {
    if (this.elements) {
      this.state = new Array[this.elements.length]();
    }
  }
}
