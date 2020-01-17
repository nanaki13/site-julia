import { Oeuvre } from "./model/Oeuvre";
import { AbstractPageElement } from "./model/AbstractPageElement";
import { PageElement } from "./model/PageElement";
import { Id } from "./Id";

export class MenuItem extends AbstractPageElement {


  type : string = "menu";
  public title: string;
  themeKey: number;
  public id = 0;



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
