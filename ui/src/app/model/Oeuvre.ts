import { Id } from "../util";
import { MenuItem } from '../MenuItem';
import { Image, ImageService } from '../image.service';
import { Identifiers } from '@angular/compiler';
export interface PageElement extends Id {
  x: number;
  y: number;

  alt: string;
  title: string;
  type: string;
  themeKey: number;
  image: Image;
  src: string;
  isNonEmpty(): boolean;
  isOeuvre(): boolean;
  isMenu(): boolean;
  asOeuvre(): Oeuvre;
  asMenu(): MenuItem;

}
export abstract class AbstractPageElement implements Id,PageElement{
  id: number;
  x: number;
  y: number;
  get src(): string {

    if(this.title === "Avec Image"){
      console.log(this)
    }

    if(this.image && this.image.link){
      return this.image.link;
    }else{
      return "";
    }
  }
  get alt(): string {
    if(this.image && this.image.link){
      return "";
    }else{
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
    this.id = param.id;
    this.title = param.title;
    this.themeKey = param.themeKey;
    this.type = param.type;
  }
}
}

export class Oeuvre extends AbstractPageElement {


  title: string;
  date: string;
  dimension: string;
  description: string;
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

export class EmptyOeuvre extends AbstractPageElement  {



  type = "empty";

  constructor(public x: number, public y: number, public id: number) {super()}
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
