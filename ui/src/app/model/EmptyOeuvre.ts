import { MenuItem } from '../MenuItem';
import { AbstractPageElement } from './AbstractPageElement';
import { Oeuvre } from './Oeuvre';
export class EmptyOeuvre extends AbstractPageElement {
  type = "empty";
  constructor(public x: number, public y: number, public id: number) { super(); }
  isNonEmpty() {
    return false;
  }
  isMenu() {
    return false;
  }
  asOeuvre(): Oeuvre {
    throw new Error("I can't");
  }
  asMenu(): MenuItem {
    throw new Error("I can't");
  }
  isOeuvre() {
    return false;
  }
}
