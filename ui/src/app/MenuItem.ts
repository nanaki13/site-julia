import { PageElement, Oeuvre } from "./model/Oeuvre";
import { Id } from './util';

export class MenuItem implements Id, PageElement {
  x: number;
  y: number;
  src: string;
  alt: string;
  type : string;
  public title: string;
  themeKey: number;
  public id = 0;


  constructor(param?: {
    title?: string;
    themeKey?: number;
    id?: number;
    type?: string;
  }) {
    if (param) {
      this.id = param.id;
      this.title = param.title;
      this.themeKey = param.themeKey;
      this.type = param.type;
    }
  }
  isNonEmpty(): boolean {
    return true;
  }
  isMenu(): boolean {
    return true;
  }
  asOeuvre(): Oeuvre {
    throw new Error("I can't.");
  }
  asMenu(): MenuItem {
    return this;
  }

  isOeuvre() {
    return false;
  }
}
