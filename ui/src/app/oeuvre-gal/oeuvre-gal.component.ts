import { Component, OnInit } from "@angular/core";
import { Oeuvre } from "../model/Oeuvre";
import { ActivatedRoute, Router } from "@angular/router";
import { PageComponentBase } from "../util";

@Component({
  selector: "app-oeuvre-gal",
  templateUrl: "./oeuvre-gal.component.html",
  styleUrls: ["./oeuvre-gal.component.css"]
})
export class OeuvreGalComponent extends PageComponentBase implements OnInit {
  oeuvre: Oeuvre;
  constructor(private router: Router, private activatedRoute: ActivatedRoute) {
    super();
    //  console.log(this.router.getCurrentNavigation().extras.state);
  }

  ngOnInit() {
    this.oeuvre = history.state;
  }
}
