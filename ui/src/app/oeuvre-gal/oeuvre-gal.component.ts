import { Component, OnInit } from "@angular/core";
import { Oeuvre } from "../model/Oeuvre";
import { ActivatedRoute, Router } from "@angular/router";
import { PageComponentBase } from "../PageComponentBase";

@Component({
  selector: "app-oeuvre-gal",
  templateUrl: "./oeuvre-gal.component.html",
  styleUrls: ["./oeuvre-gal.component.css"]
})
export class OeuvreGalComponent  implements OnInit {
  oeuvre: Oeuvre;
  constructor(private router: Router, private activatedRoute: ActivatedRoute) {

    //  console.log(this.router.getCurrentNavigation().extras.state);
  }

  ngOnInit() {
    this.oeuvre = history.state;
  }
}
