import { PageElement, Oeuvre, AbstractPageElement } from "./model/Oeuvre";
import { Id } from './util';

export class MenuItem extends AbstractPageElement {


  type : string;
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
