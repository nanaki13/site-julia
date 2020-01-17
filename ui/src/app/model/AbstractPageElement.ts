import { Id } from "../Id";
import { Image } from "../Image";
import { MenuItem } from '../MenuItem';
import { PageElement } from './PageElement';
import { Oeuvre } from "./Oeuvre";
export abstract class AbstractPageElement implements Id, PageElement {
  updateStatus: boolean;
  updated: boolean;
  id: number;
  x = 0;
  y = 0;
  get src(): string {
    if (this.title === "Avec Image") {
      console.log(this);
    }
    if (this.image && this.image.link) {
      return this.image.link;
    }
    else {
      return "";
    }
  }
  get alt(): string {
    if (this.image && this.image.link) {
      return "";
    }
    else {
      return "no image";
    }
  }
  title: string;
  type: string;
  themeKey: number;
  image: Image = new Image();
  abstract isNonEmpty(): boolean;
  abstract isOeuvre(): boolean;
  abstract isMenu(): boolean;
  abstract asOeuvre(): Oeuvre;
  abstract asMenu(): MenuItem;
  constructor(param?: {
    title?: string;
    themeKey?: number;
    id?: number;
    type?: string;
  }) {
    if (param) {

      Object.assign(this,param);
    }
  }
}
