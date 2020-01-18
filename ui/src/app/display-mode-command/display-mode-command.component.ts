import { Component, OnInit, Input } from "@angular/core";
import { PageElementDisplay, pDisplay } from "../PageElementDisplay";

@Component({
  selector: "app-display-mode-command",
  templateUrl: "./display-mode-command.component.html",
  styleUrls: ["./display-mode-command.component.css"]
})
export class DisplayModeCommandComponent implements OnInit {

  pDisplay : PageElementDisplay;
  @Input()
  textEdit = "edit les éléments";
  @Input()
  textPlacement = "Place les éléments";
  constructor() {
    this.pDisplay = pDisplay;
  }

  ngOnInit() {}
}
