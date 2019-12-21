import { Id } from "../util";
import { MenuItem } from '../MenuItem';
export interface PageElement extends Id {
  x: number;
  y: number;
  src: string;
  alt: string;
  title: string;
  type: string;
  themeKey: number;
  isNonEmpty(): boolean;
  isOeuvre(): boolean;
  isMenu(): boolean;
  asOeuvre(): Oeuvre;
  asMenu(): MenuItem;
}

export class Oeuvre implements Id, PageElement {
  id: number;
  themeKey: number;
  title: string;
  date: string;
  dimension: string;
  description: string;
  x: number;
  y: number;
  src: string;
  type = "oeuvre";

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
    return this  ;
  }
  asMenu(): MenuItem {
    throw new Error("I can't");
  }
  isMenu(): boolean {
    return false;
  }
}

export class EmptyOeuvre implements Id, PageElement {
  src = "";
  alt = "X";
  title = "XX";
  type = "empty";
  themeKey: number;
  constructor(public x: number, public y: number, public id: number) {}
  isNonEmpty() {
    return false;
  }
  isMenu() {
    return false;
  }
  asOeuvre() : Oeuvre{
    throw new Error("I can't");
  }
  asMenu() : MenuItem{
    throw new Error("I can't");
  }
  isOeuvre() {
    return false;
  }
}
