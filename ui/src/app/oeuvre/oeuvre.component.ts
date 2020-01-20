import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { Service } from "../image-view/image-view.component";
import { ImageService } from "../image.service";
import { OeuvreService } from "../oeuvre.service";
import { PageComponentBase } from "../PageComponentBase";
import { SourceService } from "../source.service";
import {PageElementDisplay,pDisplay} from "../PageElementDisplay";

@Component({
  selector: "app-oeuvre",
  templateUrl: "./oeuvre.component.html",
  styleUrls: ["./oeuvre.component.css"]
})
export class OeuvreComponent extends PageComponentBase implements OnInit {


  constructor(
    private route: ActivatedRoute,
    private oeuvreService: OeuvreService,
    private imageService: ImageService,
    private sourceService: SourceService
  ) {
    super();
    this.oeuvres = [];
  }

  mainService(): Service {
    return this.oeuvreService;
  }

  ngOnInit() {
    this.newItem = this.oeuvreService.currentItem;
    const ss = this.sourceService.source;
    if(ss){
      ss.updated = true;
    }
    this.route.paramMap.subscribe(par => {
      this.currentThemeKey = +par.get("id");

      this.oeuvreService.oauvres(this.currentThemeKey).subscribe(e => {
        e.forEach(ele => {
          if (!ss || ss.id != ele.id ) {
            this.addInTable(ele);
          } else if (ss) {
            this.addInTable(ss);
          }
        });
      });
    });
    this.imageService.lastSelectedImage.subscribe(s => {
      if (ss) {
        ss.image = s;
      } else {
        this.newItem.image = s;
        this.pDisplay.showAdd= true;
      }
    });
    this.incomingDelete.subscribe(e=> this.removeFromView(e))
  }
}
