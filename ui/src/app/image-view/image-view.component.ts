import { Component, OnInit, EventEmitter } from "@angular/core";
import { ImageService } from "../image.service";
import { Observable, of } from "rxjs";
import { map } from "rxjs/operators";
import { isNumber } from "util";
import { until } from "protractor";
import { ActivatedRoute } from "@angular/router";
import { Location } from "@angular/common";
import { Updatable } from "../Updatable";
import { Image } from "../Image";
import { PageElement } from "../model/PageElement";
@Component({
  selector: "app-image-view",
  templateUrl: "./image-view.component.html",
  styleUrls: ["./image-view.component.css"]
})
export class ImageViewComponent implements OnInit {
  allImages: EventEmitter<Image[]> = new EventEmitter<Image[]>();
  newImageIncomming: EventEmitter<Image> = new EventEmitter<Image>();
  currentImage: Image[] = [];
  comeBack: string = undefined;
  status: boolean = undefined;
  constructor(
    private imageService: ImageService,
    private route: ActivatedRoute,
    private location: Location
  ) {}

  ngOnInit() {
    this.imageService.images().subscribe(e => {
      this.allImages.emit(e);
    });
    this.route.params.subscribe(p => {
      this.comeBack = p.comeBack;
    });
  }

  back() {
    this.location.back();
  }
  updatedImageName(img: Image, event: Event) {
    img.updated = true;
  }

  newImage(img: Image) {
    this.newImageIncomming.emit(img);
    this.currentImage.push(img);
  }
  delete(id: number) {
    this.imageService.delete(id).subscribe(e => this.ngOnInit());
  }
  viewChange(newImages: Image[]) {
    this.currentImage = newImages;
  }
  update(img: Image) {
    this.imageService.update(img).subscribe(e => {
      img.updateStatus = e;
      img.updated = false;
    });
  }

  selected(img: Image) {
    this.imageService.imageSelected(img);
  }

  unselected(img: Image) {}
}
export interface Service {
  update(t: Updatable): Observable<boolean>;
  delete(t: number): Observable<boolean>;
  createEntity(t: Updatable): Observable<PageElement>;
}
export abstract class ComponentUtil {
  incomingDelete = new EventEmitter<number>();
  abstract mainService(): Service;

  update(img: Updatable) {
    this.mainService()
      .update(img)
      .subscribe(e => {
        img.updateStatus = e;
        img.updated = false;
      });
  }

  delete(img: number) {
    this.mainService()
      .delete(img)
      .subscribe(e => {
        if (e) {
          this.incomingDelete.emit(img);
        }
      });
  }
}
