import { MenuItem } from "../MenuItem";
import { ImageService } from "../image.service";
import { Identifiers } from "@angular/compiler";
import { AbstractPageElement } from "./AbstractPageElement";
export class Oeuvre extends AbstractPageElement {
  creation: number;
  title: string;
  dimensionX: number;
  dimensionY: number;
  description: string;
  type = "oeuvre";

  constructor(param?: {
    title: string,
    themeKey?: number,
    id?: number,
    type?: string,
    description: string,
    dimensionX: number,
    dimensionY: number,
    creation: number
  }) {
    super(null);
    if (param) {
      Object.assign(this, param);
    }
  }
  get alt(): string {
    return this.title;
  }
  isNonEmpty() {
    return true;
  }
  isOeuvre() {
    return true;
  }
  asOeuvre(): Oeuvre {
    return this;
  }
  asMenu(): MenuItem {
    throw new Error("I can't");
  }
  isMenu(): boolean {
    return false;
  }
}
